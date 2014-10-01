package com.cartalk;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cartalk.io.HttpUtil;

import eu.lighthouselabs.obd.enums.AvailableCommandNames;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class FuelEconActivity extends Activity {
	/** The main dataset that includes all the series that go into a chart. */
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	/** The main renderer that includes all the renderers customizing a chart. */
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	/** The most recently added series. */
	private XYSeries mCurrentSeries;
	/** The most recently created renderer, customizing the current series. */
	private XYSeriesRenderer mCurrentRenderer;
	/** The chart view that displays the data. */
	private GraphicalView mChartView;
	
	int[] fuel_leve_imgs={R.id.fuel_level_img0,R.id.fuel_level_img1,R.id.fuel_level_img2,
			              R.id.fuel_level_img3,R.id.fuel_level_img4};
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.fueleconview);
	    //mRenderer.setApplyBackgroundColor(true);
	    //mRenderer.setBackgroundColor(Color.argb(0, 0, 50, 50));
	    mRenderer.setAxisTitleTextSize(16);
	    mRenderer.setChartTitleTextSize(20);
	    mRenderer.setLabelsTextSize(15);
	    mRenderer.setLegendTextSize(15);
	    mRenderer.setMargins(new int[] { 20, 20, 10, 10 });
	    mRenderer.setZoomButtonsVisible(false);
	    mRenderer.setPointSize(5);
	    mRenderer.setShowGrid(true);
	    mRenderer.setYTitle("L/100 KM");
	    mRenderer.setXTitle("month");
	}
	@Override
	protected void onResume() {
		super.onResume();
	    if (mChartView == null) {
	      LinearLayout layout = (LinearLayout) findViewById(R.id.fueleconchart);
	      mChartView = ChartFactory.getTimeChartView(this, mDataset, mRenderer,"M ÔÂ yy");
	      // enable the chart click events
	      mRenderer.setClickEnabled(true);
	      mRenderer.setSelectableBuffer(10);
	      mChartView.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	          // handle the click event on the chart
	          SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
	          if (seriesSelection == null) {
	            //Toast.makeText(XYChartBuilder.this, "No chart element", Toast.LENGTH_SHORT).show();
	          } else {
	            // display information of the clicked point
	            /*Toast.makeText(
	                XYChartBuilder.this,
	                "Chart element in series index " + seriesSelection.getSeriesIndex()
	                    + " data point index " + seriesSelection.getPointIndex() + " was clicked"
	                    + " closest point value X=" + seriesSelection.getXValue() + ", Y="
	                    + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();*/
	          }
	        }
	      });
	      layout.addView(mChartView);
	      boolean enabled = mDataset.getSeriesCount() > 0;
		    String seriesTitle = "Series " + (mDataset.getSeriesCount() + 1);
	        // create a new series of data
		    TimeSeries series = new TimeSeries(seriesTitle);
		    Calendar time=Calendar.getInstance();
		    Map<String, String> params=new HashMap<String, String>();
	    	params.put("username", "zhaoqi");
	    	params.put("cmdname", AvailableCommandNames.FUEL_ECONOMY.getValue());
	        String result=HttpUtil.http("http://cartalk.sinaapp.com/vehicledata/1/select/","POST",params);
	        try{
	        	TreeMap<Date,Vector<Double>> fuelmap = new TreeMap<Date,Vector<Double>>();
	        	JSONArray  jsonarray=new JSONArray(result);
	        	for(int i=0;i<jsonarray.length();i++){
	        		JSONObject obj = jsonarray.getJSONObject(i);
	        		String cmdresult = obj.getString("cmdresult");
	        		String strUpdateTime = obj.getString("updatetime");
	        		double fuelecon = Double.parseDouble(cmdresult.split(" ")[0]);
	        		SimpleDateFormat simple = new SimpleDateFormat();
	        		simple.applyPattern("yyyy-MM-dd HH:mm:ss");
	        		Date updatetime = simple.parse(strUpdateTime);
	        		simple.applyPattern("yyyy-MM");
	        		String strDateInMonth = simple.format(updatetime);
	        		updatetime = simple.parse(strUpdateTime);
	        		if(fuelmap.get(updatetime)==null){
	        			Vector<Double> tmpvec = new Vector<Double>();
	        			tmpvec.add(fuelecon);
	        			tmpvec.add(fuelecon);
	        			fuelmap.put(updatetime, tmpvec);
	        		}else{
	        			Vector<Double> tmpvec = fuelmap.get(updatetime);
	        			tmpvec.add(fuelecon);
	        			if(tmpvec.size()>0){
	        				double sum = 0;
	        				for(int j = 1; j<tmpvec.size(); j++){
	        				    sum += tmpvec.get(j);
	        			    }
	        			    double avg = sum/(tmpvec.size()-1);
	        			    tmpvec.set(0, avg);
	        			}
	        		}
	        	}
	        	double sum = 0;
	        	double totalavg = 0;
	        	int count = 0;
	        	Iterator<?> iterator = fuelmap.entrySet().iterator();
	        	while(iterator.hasNext()){
	        		Entry<?, ?> entry = (Entry<?, ?>) iterator.next();
	        		Date updatetime = (Date)entry.getKey();
	        		Vector<Double> tmpvec = (Vector<Double>)entry.getValue();
	        		series.add(updatetime, tmpvec.get(0));
	        		sum += tmpvec.get(0);
	        		count++;
	        	}
	        	if(count>0){
	        		totalavg = sum/count;
	        	}
	        	int fuellevel = (int) (5 - (totalavg-7.5));
	        	TableLayout fuellevellayout = (TableLayout) findViewById(R.id.fuellevel);
	        	int i =0;
	        	for(i=0;i<fuellevel && i<fuel_leve_imgs.length;i++){
	        		ImageView image = (ImageView)findViewById(fuel_leve_imgs[i]);
	        		image.setVisibility(ImageView.VISIBLE);
	        	}
	        	
	        	for(;i<fuel_leve_imgs.length;i++){
	        		ImageView image = (ImageView)findViewById(fuel_leve_imgs[i]);
	        		image.setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));
	        		image.setVisibility(ImageView.VISIBLE);
	        	}
	        	fuellevellayout.invalidate();
	        }catch(JSONException e){
	        	Log.d("json error",e.getMessage());
	        } catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        mDataset.addSeries(series);
	        mCurrentSeries = series;
	        // create a new renderer for the new series
	        XYSeriesRenderer renderer = new XYSeriesRenderer();
	        mRenderer.addSeriesRenderer(renderer);
	        // set some renderer properties
	        renderer.setPointStyle(PointStyle.CIRCLE);
	        renderer.setFillPoints(true);
	        renderer.setDisplayChartValues(true);
	        renderer.setDisplayChartValuesDistance(10);
	        mCurrentRenderer = renderer;
	        mChartView.repaint();
	    } else {
	      mChartView.repaint();
	    }
	  }

}
