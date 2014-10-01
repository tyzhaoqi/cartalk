package com.cartalk;

import java.util.HashMap;
import java.util.List;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cartalk.view.ChartTableLayout;
import com.cartalk.view.ScrollLayout;
import com.cartalk.view.DialChart;

import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import eu.lighthouselabs.obd.commands.SpeedObdCommand;
import eu.lighthouselabs.obd.commands.control.CommandEquivRatioObdCommand;
import eu.lighthouselabs.obd.commands.control.DtcNumberObdCommand;
import eu.lighthouselabs.obd.commands.control.TroubleCodesObdCommand;
import eu.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;
import eu.lighthouselabs.obd.commands.engine.MassAirFlowObdCommand;
import eu.lighthouselabs.obd.commands.engine.ThrottlePositionObdCommand;
import eu.lighthouselabs.obd.commands.fuel.FuelEconomyObdCommand;
import eu.lighthouselabs.obd.commands.fuel.FuelEconomyWithMAFObdCommand;
import eu.lighthouselabs.obd.commands.fuel.FuelLevelObdCommand;
import eu.lighthouselabs.obd.commands.fuel.FuelTrimObdCommand;
import eu.lighthouselabs.obd.commands.pressure.IntakeManifoldPressureObdCommand;
import eu.lighthouselabs.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import eu.lighthouselabs.obd.commands.temperature.EngineCoolantTemperatureObdCommand;
import eu.lighthouselabs.obd.enums.AvailableCommandNames;
import eu.lighthouselabs.obd.enums.FuelTrim;
import eu.lighthouselabs.obd.enums.FuelType;
import com.cartalk.IPostListener;
import com.cartalk.io.HttpUtil;
import com.cartalk.io.ObdCommandJob;
import com.cartalk.io.ObdGatewayService;
import com.cartalk.io.ObdGatewayServiceConnection;
import com.cartalk.io.RealTimeMsg;

public class RealTimeInfoActivity extends Activity {
	private static final String TAG = "RealTimeInfoActivity";

    /*
     * TODO put description
     */
    static final int NO_BLUETOOTH_ID = 0;
    static final int BLUETOOTH_DISABLED = 1;
    static final int NO_GPS_ID = 2;
    static final int START_LIVE_DATA = 3;
    static final int STOP_LIVE_DATA = 4;
    static final int SETTINGS = 5;
    static final int COMMAND_ACTIVITY = 6;
    static final int TABLE_ROW_MARGIN = 7;
    static final int NO_ORIENTATION_SENSOR = 8;
    static final int NO_ACCELEROMETER_SENSOR = 9;
    static final int GET_CHART_TYPE_REQ=0;
    
	private int mScreenWidth = 0;
	private int mScreenHeight = 0;
	private int mOrientation = 0;
    private Handler mHandler=new Handler();

    /**
     * Callback for ObdGatewayService to update UI.
     */
    private IPostListener mListener = null;
    private Intent mServiceIntent = null;
    private ObdGatewayServiceConnection mServiceConnection = null;

    private SensorManager sensorManager = null;
    private Sensor orientSensor = null;
    private Sensor accelerometer = null;
    private SharedPreferences prefs = null;

    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;

    private boolean preRequisites = true;
    private boolean mEditable=false;

    private double speed = 0;
    private double rpm=0;
    private double throttle=0;
    private double boost=0;
    private double maf = 1;
    private double ltft = 0;
    private double equivRatio = 1;
    private double coolant=0;
    private String troublecode;
    private int codeCount=0;
    private double fuelEcon=0;
    private DialChart acceChart=null;
    private DialChart revsChart=null;
    private DialChart throttleChart=null;
    private DialChart speedChart=null;
    private DialChart boostChart=null;
    private DialChart coolantChart=null;
    
    private View acceView=null;
    private View revsView=null;
    private View throttleView=null;
    private View speedView=null;
    private View boostView=null;
    private View coolantView=null;
    
	private ChartTableLayout realtimeLayouts[];
	private TableLayout curClickLayout;
	private ScrollLayout realtimeslayout;
	private Hashtable<Integer,Vector<Double> > mapData=new Hashtable<Integer,Vector<Double>>();
	private BlockingQueue<RealTimeMsg> msgQueue=new LinkedBlockingQueue<RealTimeMsg>();
	private Thread sendThread;
    private final SensorEventListener orientListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
            	switch(event.sensor.getType()){
            	case Sensor.TYPE_MAGNETIC_FIELD:
                    float x = event.values[0];
                    String dir = "";
                    if (x >= 337.5 || x < 22.5) {
                            dir = "N";
                    } else if (x >= 22.5 && x < 67.5) {
                            dir = "NE";
                    } else if (x >= 67.5 && x < 112.5) {
                            dir = "E";
                    } else if (x >= 112.5 && x < 157.5) {
                            dir = "SE";
                    } else if (x >= 157.5 && x < 202.5) {
                            dir = "S";
                    } else if (x >= 202.5 && x < 247.5) {
                            dir = "SW";
                    } else if (x >= 247.5 && x < 292.5) {
                            dir = "W";
                    } else if (x >= 292.5 && x < 337.5) {
                            dir = "NW";
                    }
                    break;
            	case Sensor.TYPE_LINEAR_ACCELERATION:
            		double ax = event.values[0];
            		
            		if(mapData.get(0)==null){
                		mapData.put(0, new Vector<Double>());
                	}
                	mapData.get(0).add(ax);
                	for(int i=0;i<realtimeLayouts.length;i++){
                    	realtimeLayouts[i].updateView(mapData);
                    }
            		break;
            	}
                    //TextView compass = (TextView) findViewById(R.id.compass_text);
                    //updateTextView(compass, dir);
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // TODO Auto-generated method stub
            }
    };


	@SuppressLint({ "NewApi", "NewApi", "NewApi" })
	public void initDevice() {
            mListener = new IPostListener() {
                    public void stateUpdate(ObdCommandJob job) {
                    	try{
                            String cmdName = job.getCommand().getName();
                            String cmdResultRaw = job.getCommand().getFormattedResult();
                            if(cmdResultRaw=="")
                            	return;
                            String[] cmdResultParse=cmdResultRaw.split("\r\r\n");
                            String cmdRaw = cmdResultParse.length>1 ? cmdResultParse[0] : "";
                            String cmdResult=cmdResultParse.length>1 ? cmdResultParse[1] : cmdResultParse[0];
                            if(cmdResultParse.length>1){
                            	cmdResult = cmdResultParse[1];
                            }
                            if(cmdResult=="")
                            	return;
                            double dret=0;
                            Pattern pattern = Pattern.compile("^[\\+]*[\\-]*[\\d]+[\\.]*[\\d]*");
                            Matcher matcher = pattern.matcher(cmdResult);
                            if(matcher.find()){
                            	dret=Double.parseDouble(matcher.group(0));
                            }
                            Log.d(TAG, FuelTrim.LONG_TERM_BANK_1.getBank() + " equals " + cmdName + "?");
                            if (AvailableCommandNames.ENGINE_RPM.getValue().equals(cmdName)) {
                            	
                            	rpm=((EngineRPMObdCommand)job.getCommand()).getRPM();
                            	if(mapData.get(1)==null){
                            		mapData.put(1, new Vector<Double>());
                            	}
                            	mapData.get(1).add(rpm);
                            } else if (AvailableCommandNames.SPEED.getValue().equals(cmdName)) {
                                    speed = ((SpeedObdCommand) job.getCommand()).getMetricSpeed();
                                	if(mapData.get(3)==null){
                                		mapData.put(3, new Vector<Double>());
                                	}
                                	mapData.get(3).add(speed);
                            } else if (AvailableCommandNames.MAF.getValue().equals(cmdName)) {
                                    maf = ((MassAirFlowObdCommand) job.getCommand()).getMAF();
                                    if(mapData.get(6)==null){
                                		mapData.put(6, new Vector<Double>());
                                	}
                                    mapData.get(6).add(maf);
                            } else if (FuelTrim.LONG_TERM_BANK_1.getBank().equals(cmdName)) {
                                    ltft = ((FuelTrimObdCommand) job.getCommand()).getValue();
                                    if(mapData.get(7)==null){
                                		mapData.put(7, new Vector<Double>());
                                	}
                                    mapData.get(7).add(ltft);
                            } else if (AvailableCommandNames.EQUIV_RATIO.getValue().equals(cmdName)) {
                                    equivRatio = ((CommandEquivRatioObdCommand) job.getCommand()).getRatio();
                                    if(mapData.get(8)==null){
                                		mapData.put(8, new Vector<Double>());
                                	}
                                    mapData.get(8).add(equivRatio);
                                    
                            } else if (AvailableCommandNames.THROTTLE_POS.getValue().equals(cmdName)){
                            	throttle =  dret;
                            	if(mapData.get(2)==null){
                            		mapData.put(2, new Vector<Double>());
                            	}
                            	mapData.get(2).add(throttle);
                            } else if (AvailableCommandNames.INTAKE_MANIFOLD_PRESSURE.getValue().equals(cmdName)){
                            	boost=dret;
                            	if(mapData.get(4)==null){
                            		mapData.put(4, new Vector<Double>());
                            	}
                            	mapData.get(4).add(boost);
                            } else if (AvailableCommandNames.ENGINE_COOLANT_TEMP.getValue().equals(cmdName)){
                            	coolant=dret;
                            	if(mapData.get(5)==null){
                            		mapData.put(5, new Vector<Double>());
                            	}
                            	mapData.get(5).add(coolant);
                            } else if (AvailableCommandNames.TROUBLE_CODES.getValue().equals(cmdName)){
                            	troublecode=cmdResult;
                            } else if (AvailableCommandNames.DTC_NUMBER.getValue().equals(cmdName)){
                            	codeCount = ((DtcNumberObdCommand) job.getCommand()).getTotalAvailableCodes();
                            } else if (AvailableCommandNames.FUEL_ECONOMY.getValue().equals(cmdName)){
                            	fuelEcon = ((FuelEconomyObdCommand) job.getCommand()).getLitersPer100Km();
                            }
                            else {
                                   
                            }
                            for(int i=0;i<realtimeLayouts.length;i++){
                            	realtimeLayouts[i].updateView(mapData);
                            }
                            RealTimeMsg cmdmsg=new RealTimeMsg();
                            cmdmsg.cmdName=cmdName;
                            cmdmsg.cmdResult=cmdResult;
                            //sendMsgToQueue(cmdmsg);
                    	}catch(Exception e){
                    		Log.d(TAG, "Invalid result "+e.getMessage());
                    	}
                    }

					public void stateUpdate(int command, float value) {
						// TODO Auto-generated method stub
						if(mapData.get(command)==null){
                    		mapData.put(command, new Vector<Double>());
                    	}
                    	mapData.get(command).add((double)value);
					}
            };

            /*
             * Validate GPS service.
             */
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.getProvider(LocationManager.GPS_PROVIDER) == null) {
                    /*
                     * TODO for testing purposes we'll not make GPS a pre-requisite.
                     */
                    // preRequisites = false;
                    showDialog(NO_GPS_ID);
            }

            /*
             * Validate Bluetooth service.
             */
            // Bluetooth device exists?
            final BluetoothAdapter mBtAdapter = BluetoothAdapter
                            .getDefaultAdapter();
            if (mBtAdapter == null) {
                    preRequisites = false;
                    showDialog(NO_BLUETOOTH_ID);
            } else {
                    // Bluetooth device is enabled?
                    if (!mBtAdapter.isEnabled()) {
                            preRequisites = false;
                            showDialog(BLUETOOTH_DISABLED);
                    }
            }

            /*
             * Get Orientation sensor.
             */
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> sens = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
            if (sens.size() <= 0) {
                    showDialog(NO_ORIENTATION_SENSOR);
            } else {
                    orientSensor = sens.get(0);
            }
            
            sens = sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
            if(sens.size() <= 0){
            	showDialog(NO_ACCELEROMETER_SENSOR);
            }else{
            	accelerometer = sens.get(0);
            }
            

            // validate app pre-requisites
            if (preRequisites) {
                    /*
                     * Prepare service and its connection
                     */
                    mServiceIntent = new Intent(this, ObdGatewayService.class);
                    mServiceConnection = new ObdGatewayServiceConnection();
                    mServiceConnection.setServiceListener(mListener);

                    // bind service
                    Log.d(TAG, "Binding service..");
                    bindService(mServiceIntent, mServiceConnection,
                                    Context.BIND_AUTO_CREATE);
            }
    }

    @Override
    protected void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacksAndMessages(null);
            if (mServiceIntent!=null){
                stopService(mServiceIntent);
            }
            releaseWakeLockIfHeld();
            ChartTableLayout.RemoveAll();
            mServiceIntent = null;
            mServiceConnection = null;
            mListener = null;
            mHandler = null;

    }

    @Override
    protected void onPause() {
            super.onPause();
            Log.d(TAG, "Pausing..");
            releaseWakeLockIfHeld();
    }

    /**
     * If lock is held, release. Lock will be held when the service is running.
     */
    private void releaseWakeLockIfHeld() {
            if (wakeLock.isHeld()) {
                    wakeLock.release();
            }
    }

    protected void onResume() {
            super.onResume();

            Log.d(TAG, "Resuming..");

            sensorManager.registerListener(orientListener, orientSensor,
                            SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(orientListener, accelerometer,
                    SensorManager.SENSOR_DELAY_UI);
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                            "ObdReader");
    }

    private void updateConfig() {
            Intent configIntent = new Intent(this, ConfigActivity.class);
            startActivity(configIntent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
            //menu.add(0, START_LIVE_DATA, 0, "Start Live Data");
            //menu.add(0, COMMAND_ACTIVITY, 0, "Run Command");
            //menu.add(0, STOP_LIVE_DATA, 0, "Stop");
            //menu.add(0, STOP_LIVE_DATA, 0, "add");
            //menu.add(0, STOP_LIVE_DATA, 0, "del");
            //menu.add(0, SETTINGS, 0, "Settings");
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = new MenuInflater(getApplicationContext());
        inflater.inflate(R.menu.realtimemenu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
            case R.id.menu_start:
                startLiveData();
                return true;
            case R.id.menu_stop:
                stopLiveData();
                return true;
            case R.id.menu_setting:
            	updateConfig();
                return true;
                    // case COMMAND_ACTIVITY:
                    // staticCommand();
                    // return true;
            case R.id.menu_add:
            	addChart();
            	return true;
            case R.id.menu_del:
            	setEditMode(true);
            	return true;
            }
            return false;
    }

    // private void staticCommand() {
    // Intent commandIntent = new Intent(this, ObdReaderCommandActivity.class);
    // startActivity(commandIntent);
    // }

    private void startLiveData() {
            Log.d(TAG, "Starting live data..");

            if (!mServiceConnection.isRunning()) {
                    Log.d(TAG, "Service is not running. Going to start it..");
                    startService(mServiceIntent);
            }
            // start command execution
           // mHandler.post(mQueueCommands);

            //mHandler.post(mQueueCommands);

            // screen won't turn off until wakeLock.release()
            wakeLock.acquire();
    }

    private void stopLiveData() {
            Log.d(TAG, "Stopping live data..");

            if (mServiceConnection.isRunning())
                    stopService(mServiceIntent);
            // remove runnable
            //mHandler.removeCallbacks(mQueueCommands);

            releaseWakeLockIfHeld();
    }
    
    private void addChart(){
    	Log.d(TAG, "Adding new chart..");
    	Intent intent = new Intent();
	    intent.setClass(this, ChartTypeActivity.class);
	    startActivityForResult(intent,GET_CHART_TYPE_REQ);
    }
    protected Dialog onCreateDialog(int id) {
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            switch (id) {
            case NO_BLUETOOTH_ID:
                    build.setMessage("Sorry, your device doesn't support Bluetooth.");
                    return build.create();
            case BLUETOOTH_DISABLED:
                    build.setMessage("You have Bluetooth disabled. Please enable it!");
                    return build.create();
            case NO_GPS_ID:
                    build.setMessage("Sorry, your device doesn't support GPS.");
                    return build.create();
            case NO_ORIENTATION_SENSOR:
                    build.setMessage("Orientation sensor missing?");
                    return build.create();
            case NO_ACCELEROMETER_SENSOR:
                build.setMessage("Accelerometer sensor missing?");
                return build.create();
            }
            return null;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
            MenuItem startItem = menu.findItem(R.id.menu_start);
            MenuItem stopItem = menu.findItem(R.id.menu_stop);
            MenuItem settingsItem = menu.findItem(R.id.menu_setting);
            MenuItem addItem =menu.findItem(R.id.menu_add);
            MenuItem delItem =menu.findItem(R.id.menu_del);
            //MenuItem commandItem = menu.findItem(COMMAND_ACTIVITY);

            // validate if preRequisites are satisfied.
            if (preRequisites) {
                    if (mServiceConnection.isRunning()) {
                            startItem.setEnabled(false);
                            stopItem.setEnabled(true);
                            settingsItem.setEnabled(false);
                            addItem.setEnabled(false);
                            delItem.setEnabled(false);
                           // commandItem.setEnabled(false);
                    } else {
                            stopItem.setEnabled(false);
                            startItem.setEnabled(true);
                            settingsItem.setEnabled(true);
                            addItem.setEnabled(true);
                            delItem.setEnabled(true);
                           // commandItem.setEnabled(false);
                    }
            } else {
                    startItem.setEnabled(false);
                    stopItem.setEnabled(false);
                    settingsItem.setEnabled(false);
                   // commandItem.setEnabled(false);
            }

            return true;
    }

    private void addTableRow(String key, String val) {
/*            TableLayout tl = (TableLayout) findViewById(R.id.data_table);
            TableRow tr = new TableRow(this);
            MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN,
                            TABLE_ROW_MARGIN);
            tr.setLayoutParams(params);
            tr.setBackgroundColor(Color.BLACK);
            TextView name = new TextView(this);
            name.setGravity(Gravity.RIGHT);
            name.setText(key + ": ");
            TextView value = new TextView(this);
            value.setGravity(Gravity.LEFT);
            value.setText(val);
            tr.addView(name);
            tr.addView(value);
            tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT));

            /*
             * TODO remove this hack
             * 
             * let's define a limit number of rows
             */
            /*if (tl.getChildCount() > 10)
                    tl.removeViewAt(0);*/
    }

    /**
     * 
     */
   
    /**
     * 
     */
    private void queueCommands() {
            final ObdCommandJob airTemp = new ObdCommandJob(
                            new AmbientAirTemperatureObdCommand());
            final ObdCommandJob speed = new ObdCommandJob(new SpeedObdCommand());
            final ObdCommandJob fuelEcon = new ObdCommandJob(
                            new FuelEconomyObdCommand());
            final ObdCommandJob rpm = new ObdCommandJob(new EngineRPMObdCommand());
            final ObdCommandJob maf = new ObdCommandJob(new MassAirFlowObdCommand());
            final ObdCommandJob fuelLevel = new ObdCommandJob(
                            new FuelLevelObdCommand());
            final ObdCommandJob ltft1 = new ObdCommandJob(new FuelTrimObdCommand(
                            FuelTrim.LONG_TERM_BANK_1));
            final ObdCommandJob ltft2 = new ObdCommandJob(new FuelTrimObdCommand(
                            FuelTrim.LONG_TERM_BANK_2));
            final ObdCommandJob stft1 = new ObdCommandJob(new FuelTrimObdCommand(
                            FuelTrim.SHORT_TERM_BANK_1));
            final ObdCommandJob stft2 = new ObdCommandJob(new FuelTrimObdCommand(
                            FuelTrim.SHORT_TERM_BANK_2));
            final ObdCommandJob equiv = new ObdCommandJob(new CommandEquivRatioObdCommand());
            final ObdCommandJob throttle= new ObdCommandJob(new ThrottlePositionObdCommand());
            final ObdCommandJob boost= new ObdCommandJob(new IntakeManifoldPressureObdCommand());
            final ObdCommandJob coolant= new ObdCommandJob(new EngineCoolantTemperatureObdCommand());
            final ObdCommandJob dtcNumber= new ObdCommandJob(new DtcNumberObdCommand());
            
            
            mServiceConnection.addJobToQueueNotRun(airTemp);
            mServiceConnection.addJobToQueueNotRun(speed);
            mServiceConnection.addJobToQueueNotRun(fuelEcon);
            mServiceConnection.addJobToQueueNotRun(rpm);
            mServiceConnection.addJobToQueueNotRun(maf);
            mServiceConnection.addJobToQueueNotRun(fuelLevel);
            mServiceConnection.addJobToQueueNotRun(equiv);
            mServiceConnection.addJobToQueueNotRun(ltft1);
            // mServiceConnection.addJobToQueue(ltft2);
            // mServiceConnection.addJobToQueue(stft1);
            // mServiceConnection.addJobToQueue(stft2);
            mServiceConnection.addJobToQueueNotRun(throttle);
            mServiceConnection.addJobToQueueNotRun(boost);
            mServiceConnection.addJobToQueueNotRun(coolant);
            mServiceConnection.addJobToQueueNotRun(dtcNumber);
            if(codeCount>0){
            	final ObdCommandJob trouble= new ObdCommandJob(new TroubleCodesObdCommand(codeCount));
            	mServiceConnection.addJobToQueueNotRun(trouble);
            }
            mServiceConnection.addJobToQueueNotRun(dtcNumber);
    }
    
    public void createViews(){
		DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
		mScreenWidth = metric.widthPixels;
		mScreenHeight = metric.heightPixels;
		mOrientation = getRequestedOrientation();
		
    	realtimeslayout=(ScrollLayout)this.findViewById(R.id.realtimes);
    	realtimeLayouts=new ChartTableLayout[5];
    	realtimeLayouts[0]=(ChartTableLayout) this.findViewById(R.id.realtime1);
    	realtimeLayouts[1]=(ChartTableLayout) this.findViewById(R.id.realtime2);
    	realtimeLayouts[2]=(ChartTableLayout) this.findViewById(R.id.realtime3);
    	realtimeLayouts[3]=(ChartTableLayout) this.findViewById(R.id.realtime4);
    	realtimeLayouts[4]=(ChartTableLayout) this.findViewById(R.id.realtime5);
    	
    	for(int i=0;i<realtimeLayouts.length;i++){
    		realtimeLayouts[i].loadPreferences(mScreenWidth,mScreenHeight);
    	}

    }
    
    public void setEditMode(boolean enable){
    	mEditable=enable;
    	for(int i=0;i<realtimeLayouts.length;i++){
    		realtimeLayouts[i].SetEditMode(enable);
    	}
    }
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK){
		    int type=data.getIntExtra("type", 0);
		    int chart=data.getIntExtra("chart", 0);
		    realtimeLayouts[realtimeslayout.getCurScreen()].addView(type,chart);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK){
			if(mEditable){
				setEditMode(false);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private boolean sendMsgToQueue(RealTimeMsg msg){
		return msgQueue.offer(msg);
	}
	
	public RealTimeMsg getMsgFromQueue(){
		return msgQueue.poll();
	}
	private void startSendThread(){
		sendThread=new Thread(){
			public void run(){
				while(true){
					RealTimeMsg msg =getMsgFromQueue();
					if(msg==null){
						continue;
					}
					Map<String, String> params=new HashMap<String, String>();
					params.put("username", "zhaoqi");
					params.put("cmdname", msg.cmdName);
					params.put("cmdresult", msg.cmdResult);
					HttpUtil.http("http://cartalk.sinaapp.com/vehicledata/1/add/","POST",params);
				}
			}
		};
		sendThread.start();
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.realtimeinfo);
		createViews();
		initDevice();
		startSendThread();
	}
}
