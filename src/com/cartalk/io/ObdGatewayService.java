package com.cartalk.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import eu.lighthouselabs.obd.commands.ObdCommand;
import eu.lighthouselabs.obd.commands.fuel.FuelEconomyWithMAFObdCommand;
import eu.lighthouselabs.obd.commands.protocol.EchoOffObdCommand;
import eu.lighthouselabs.obd.commands.protocol.LineFeedOffObdCommand;
import eu.lighthouselabs.obd.commands.protocol.ObdResetCommand;
import eu.lighthouselabs.obd.commands.protocol.SelectProtocolObdCommand;
import eu.lighthouselabs.obd.commands.protocol.TimeoutObdCommand;
import eu.lighthouselabs.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import eu.lighthouselabs.obd.enums.FuelType;
import eu.lighthouselabs.obd.enums.ObdProtocols;
import com.cartalk.IPostListener;
import com.cartalk.IPostMonitor;
import com.cartalk.R;
import com.cartalk.ConfigActivity;
import com.cartalk.MainActivity;
import com.cartalk.io.ObdCommandJob.ObdCommandJobState;

/**
 * This service is primarily responsible for establishing and maintaining a
 * permanent connection between the device where the application runs and a more
 * OBD Bluetooth interface.
 * 
 * Secondarily, it will serve as a repository of ObdCommandJobs and at the same
 * time the application state-machine.
 */
@SuppressLint({ "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi" })
public class ObdGatewayService extends Service {

        private static final String TAG = "ObdGatewayService";
        private static final int thread_num=1;

        private IPostListener _callback = null;
        private final Binder _binder = new LocalBinder();
        private AtomicBoolean _isRunning = new AtomicBoolean(false);
        private NotificationManager _notifManager;

        private BlockingQueue<ObdCommandJob> _queue = new LinkedBlockingQueue<ObdCommandJob>();
        private BlockingQueue<ObdCommandJob> _queueResp = new LinkedBlockingQueue<ObdCommandJob>();
        private AtomicBoolean _isQueueRunning = new AtomicBoolean(false);
        private Long _queueCounter = 0L;

        private BluetoothDevice _dev = null;
        private BluetoothSocket _sock = null;
        private ObdApi m_obdapi = null;
        
        private Thread[] commandWorkders = new Thread[thread_num];
        private Thread updateWorkder;
        private boolean isObdConfiged = false; 
        /*
         * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
         * #createRfcommSocketToServiceRecord(java.util.UUID)
         * 
         * "Hint: If you are connecting to a Bluetooth serial board then try using
         * the well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However if
         * you are connecting to an Android peer then please generate your own
         * unique UUID."
         */
        private static final UUID MY_UUID = UUID
                .fromString("00001101-0000-1000-8000-00805F9B34FB");

        /**
         * As long as the service is bound to another component, say an Activity, it
         * will remain alive.
         */
        @Override
        public IBinder onBind(Intent intent) {
                return _binder;
        }

        @Override
        public void onCreate() {
                _notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                showNotification();
        }

        @Override
        public void onDestroy() {
        	_notifManager.cancel(R.string.service_started);
            stopService();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
                Log.d(TAG, "Received start id " + startId + ": " + intent);

                /*
                 * Register listener Start OBD connection
                 */
                startService();

                /*
                 * We want this service to continue running until it is explicitly
                 * stopped, so return sticky.
                 */
                return START_STICKY;
        }

        private void startService() {
                Log.d(TAG, "Starting service..");

                /*
                 * Retrieve preferences
                 */
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(this);

                /*
                 * Let's get the remote Bluetooth device
                 */
                String remoteDevice = prefs.getString(
                        ConfigActivity.BLUETOOTH_LIST_KEY, null);
                if (remoteDevice == null || "".equals(remoteDevice)) {
                        Toast.makeText(this, "No Bluetooth device selected",
                                Toast.LENGTH_LONG).show();

                        // log error
                        Log.e(TAG, "No Bluetooth device has been selected.");

                        // TODO kill this service gracefully
                        stopService();
                }

                final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                _dev = btAdapter.getRemoteDevice(remoteDevice);

                /*
                 * TODO put this as deprecated Determine if upload is enabled
                 */
                // boolean uploadEnabled = prefs.getBoolean(
                // ConfigActivity.UPLOAD_DATA_KEY, false);
                // String uploadUrl = null;
                // if (uploadEnabled) {
                // uploadUrl = prefs.getString(ConfigActivity.UPLOAD_URL_KEY,
                // null);
                // }

                /*
                 * Get GPS
                 */
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean gps = prefs.getBoolean(ConfigActivity.ENABLE_GPS_KEY, false);

                /*
                 * TODO clean
                 * 
                 * Get more preferences
                 */
                int period = ConfigActivity.getUpdatePeriod(prefs);
                double ve = ConfigActivity.getVolumetricEfficieny(prefs);
                double ed = ConfigActivity.getEngineDisplacement(prefs);
                boolean imperialUnits = prefs.getBoolean(
                        ConfigActivity.IMPERIAL_UNITS_KEY, false);
                ArrayList<ObdCommand> cmds = ConfigActivity.getObdCommands(prefs);

                /*
                 * Establish Bluetooth connection
                 * 
                 * Because discovery is a heavyweight procedure for the Bluetooth
                 * adapter, this method should always be called before attempting to
                 * connect to a remote device with connect(). Discovery is not managed
                 * by the Activity, but is run as a system service, so an application
                 * should always call cancel discovery even if it did not directly
                 * request a discovery, just to be sure. If Bluetooth state is not
                 * STATE_ON, this API will return false.
                 * 
                 * see
                 * http://developer.android.com/reference/android/bluetooth/BluetoothAdapter
                 * .html#cancelDiscovery()
                 */
                Log.d(TAG, "Stopping Bluetooth discovery.");
                btAdapter.cancelDiscovery();

                Toast.makeText(this, "Starting OBD connection..", Toast.LENGTH_SHORT).show();

                try {
                        startObdConnection();
                } catch (Exception e) {
                        Log.e(TAG, "There was an error while establishing connection. -> "
                                + e.getMessage());
                        
                        // in case of failure, stop this service.
                        stopService();
                }
        }

        /**
         * Start and configure the connection to the OBD interface.
         * 
         * @throws IOException
         */
        private void startObdConnection() throws IOException {
                Log.d(TAG, "Starting OBD connection..");

                // Instantiate a BluetoothSocket for the remote device and connect it.
                try{
                    _sock = _dev.createRfcommSocketToServiceRecord(MY_UUID);
                    _sock.connect();
                    Thread.sleep(5000);
                }catch(IOException e){
                	_sock = null;
                	throw e;
                }
                catch(Exception e){
                	return;
                }
                Toast.makeText(this, "OBD connected.", Toast.LENGTH_SHORT).show();
                m_obdapi = new ObdApi(_sock.getInputStream(),_sock.getOutputStream());
        		

        		//m_obdapi.Supported();
        		//m_obdapi.Headers(1);
        		//m_obdapi.GetProtocol();
        		//m_obdapi.Supported();
        		//m_obdapi.Headers(0);
        		//m_obdapi.Supported();
        		
                // Let's configure the connection.
                Log.d(TAG, "Queing jobs for connection configuration..");
                queueJob(new ObdCommandJob(new ObdResetCommand()));
                queueJob(new ObdCommandJob(new EchoOffObdCommand()));

                /*
                 * Will send second-time based on tests.
                 * 
                 * TODO this can be done w/o having to queue jobs by just issuing
                 * command.run(), command.getResult() and validate the result.
                 */
                queueJob(new ObdCommandJob(new EchoOffObdCommand()));
                queueJob(new ObdCommandJob(new LineFeedOffObdCommand()));
                //queueJob(new ObdCommandJob(new TimeoutObdCommand(10)));
                queueJob(new ObdCommandJob(new AdaptiveTimingObdCommand(2)));

                // For now set protocol to AUTO
                queueJob(new ObdCommandJob(new SelectProtocolObdCommand(
                        ObdProtocols.ISO_14230_4_KWP)));
                
                // Job for returning dummy data
                queueJob(new ObdCommandJob(new AmbientAirTemperatureObdCommand()));

                Log.d(TAG, "Initialization jobs queued.");
                

                // Service is running..
                _isRunning.set(true);

                // Set queue execution counter
                _queueCounter = 0L;
                for(int i=0;i<thread_num;i++){
                	commandWorkders[i]=new Thread(mCommandRunnable);
                }
                updateWorkder=new Thread(mUpdaterRunnable);
                
                for(int i=0;i<thread_num;i++){                
                	commandWorkders[i].start();
                }
                //updateWorkder.start();
        }
        
        private boolean configObd(){
        	//Toast.makeText(this, "Starting OBD configeration..", Toast.LENGTH_SHORT).show();
        	
    		while(!m_obdapi.ResetObd()){
    			try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		/*while(!m_obdapi.EchoOff()){
    			try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}*/
    		while(!m_obdapi.LineFeedOff()){
    			try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		while(!m_obdapi.SpaceOff()){
    			try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		while(!m_obdapi.Headers(0)){
    			try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		while(!m_obdapi.AdaptiveTiming(1)){
    			try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}        		
    		while(!m_obdapi.SelectProtocol(0)){
    			try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
/*    		while(!m_obdapi.Supported()){
    			try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}*/
    		//Toast.makeText(this, "finish OBD configeration..", Toast.LENGTH_SHORT).show();
    		isObdConfiged = true;
    		return true;
        }
        private class CommandRunnable implements Runnable{

        	private boolean stoprun=false;
        	private boolean running = false;
        	public void Stop(){
        		stoprun = true;
        	}
        	public boolean isRunning(){
        		return running;
        	}
			public void run() {
				// TODO Auto-generated method stub
	           	while(!stoprun){
            		running = true;
            		if(!isObdConfiged){
            			configObd();
            		}

            		float rpm = m_obdapi.RPM();
            		_callback.stateUpdate(1,rpm/1000);
            
            		float speed = m_obdapi.Speed();
            		_callback.stateUpdate(3,speed);
            		
            		float throttle = m_obdapi.ThrottlePosition();
            		_callback.stateUpdate(2,throttle);
            		
            		float coolant = m_obdapi.EngineCoolantTemperature();
            		_callback.stateUpdate(5,coolant);
            		
            		float intake = m_obdapi.IntakeManifoldPressure();
            		_callback.stateUpdate(4,intake);
            		
            		float mass_air_flow = m_obdapi.MassAirFlow();
            		_callback.stateUpdate(6, mass_air_flow);
            		
            		float equiv_ratio = m_obdapi.EquivRatio();
            		_callback.stateUpdate(8, equiv_ratio);

            	    /*ObdCommandJob job = null;
                    try {
                            job = _queue.take();
                            // log job
                            Log.d(TAG, "Taking job[" + job.getId() + "] from queue..");

                            if (job.getState().equals(ObdCommandJobState.NEW)) {
                                    Log.d(TAG, "Job state is NEW. Run it..");

                                    job.setState(ObdCommandJobState.RUNNING);
                                    job.getCommand().run(_sock.getInputStream(),_sock.getOutputStream());
                            } else {
                                    // log not new job
                                    Log.e(TAG, "Job state was not new, so it shouldn't be in queue. BUG ALERT!");
                                }
                        }catch (Exception e) {
                            job.setState(ObdCommandJobState.EXECUTION_ERROR);
                            Log.e(TAG, "Failed to run command. -> " + e.getMessage());
                        }
                        if (job != null) {
                            Log.d(TAG, "Job is finished.");
                            job.setState(ObdCommandJobState.FINISHED);
                            _queueResp.add(job);
                            //_callback.stateUpdate(job);
                        }*/
            		
                }
            	running = false;
			}
        	
        }
        private CommandRunnable mCommandRunnable =new CommandRunnable();
        
        private class UpdateRunnable implements Runnable{
        	private boolean stoprun=false;
        	private boolean running = false;
        	public void Stop(){
        		stoprun = true;
        	}
        	public boolean isRunning(){
        		return running;
        	}
			public void run() {
				// TODO Auto-generated method stub
				while(!stoprun){
            		running = true;
            	    ObdCommandJob job = null;
                    try {
                            job = _queueResp.take();
                            _callback.stateUpdate(job);
                    }catch (Exception e) {
                        Log.e(TAG, "Update chart fail. -> " + e.getMessage());
                    }
                }
            	running = false;
			}
        }
        private UpdateRunnable mUpdaterRunnable = new UpdateRunnable();
        /**
         * Runs the queue until the service is stopped
         */
        @SuppressLint({ "NewApi", "NewApi" })
		private void _executeQueue() {
                Log.d(TAG, "Executing queue..");

                _isQueueRunning.set(true);

                while (!_queue.isEmpty()) {
                        ObdCommandJob job = null;
                        try {
                                job = _queue.take();

                                // log job
                                Log.d(TAG, "Taking job[" + job.getId() + "] from queue..");

                                if (job.getState().equals(ObdCommandJobState.NEW)) {
                                        Log.d(TAG, "Job state is NEW. Run it..");

                                        job.setState(ObdCommandJobState.RUNNING);
                                        job.getCommand().run(_sock.getInputStream(),
                                                _sock.getOutputStream());
                                } else {
                                        // log not new job
                                        Log.e(TAG,
                                                "Job state was not new, so it shouldn't be in queue. BUG ALERT!");
                                }
                        } catch (Exception e) {
                                job.setState(ObdCommandJobState.EXECUTION_ERROR);
                                Log.e(TAG, "Failed to run command. -> " + e.getMessage());
                        }

                        if (job != null) {
                                Log.d(TAG, "Job is finished.");
                                job.setState(ObdCommandJobState.FINISHED);
                                _callback.stateUpdate(job);
                        }
                }

                _isQueueRunning.set(false);
        }

        /**
         * This method will add a job to the queue while setting its ID to the
         * internal queue counter.
         * 
         * @param job
         * @return
         */
        public Long queueJob(ObdCommandJob job) {
                _queueCounter++;
                Log.d(TAG, "Adding job[" + _queueCounter + "] to queue..");

                job.setId(_queueCounter);
                try {
                        _queue.put(job);
                } catch (InterruptedException e) {
                        job.setState(ObdCommandJobState.QUEUE_ERROR);
                        // log error
                        Log.e(TAG, "Failed to queue job.");
                }

                Log.d(TAG, "Job queued successfully.");
                return _queueCounter;
        }

        /**
         * Stop OBD connection and queue processing.
         */
		public void stopService() {
                Log.d(TAG, "Stopping service..");

                for(int i=0;i<thread_num;i++){
                	if(commandWorkders[i]==null)
                		continue;
                	mCommandRunnable.Stop();
                	try {
						commandWorkders[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	commandWorkders[i]=null;
                }
            	if(updateWorkder!=null)
            	{
            		mUpdaterRunnable.Stop();
            		try {
						updateWorkder.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		updateWorkder = null;
            	}
                clearNotification();
                _queue.removeAll(_queue); // TODO is this safe?
                _isQueueRunning.set(false);
                _callback = null;
                _isRunning.set(false);

                // close socket
                try {
                	if(_sock!=null){
                        _sock.close();
                	}
                } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                }

                // kill service
                stopSelf();
        }

        /**
         * Show a notification while this service is running.
         */
        private void showNotification() {
                // Set the icon, scrolling text and timestamp
                Notification notification = new Notification(R.drawable.notify,
                        getText(R.string.service_started), System.currentTimeMillis());

                // Launch our activity if the user selects this notification
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class), 0);

                // Set the info for the views that show in the notification panel.
                notification.setLatestEventInfo(this,
                        getText(R.string.notification_label),
                        getText(R.string.service_started), contentIntent);

                // Send the notification.
                _notifManager.notify(R.string.service_started, notification);
        }

        /**
         * Clear notification.
         */
        private void clearNotification() {
                _notifManager.cancel(R.string.service_started);
        }

        /**
         * TODO put description
         */
        public class LocalBinder extends Binder implements IPostMonitor {
                public void setListener(IPostListener callback) {
                        _callback = callback;
                }

                public boolean isRunning() {
                        return _isRunning.get();
                }

                public void executeQueue() {
                        _executeQueue();
                }

                public void addJobToQueue(ObdCommandJob job) {
                        Log.d(TAG, "Adding job [" + job.getCommand().getName() + "] to queue.");
                        _queue.add(job);

                        if (!_isQueueRunning.get())
                                _executeQueue();
                }
                
                public void addJobToQueueNotRun(ObdCommandJob job) {
                    Log.d(TAG, "Adding job [" + job.getCommand().getName() + "] to queue.");
                    _queue.add(job);
                }
        }

}


