package com.cartalk.view;

import org.achartengine.GraphicalView;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class GraghChart implements IChart {
	/** The main dataset that includes all the series that go into a chart. */
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	/** The main renderer that includes all the renderers customizing a chart. */
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	/** The most recently added series. */
	private XYSeries mCurrentSeries;
	/** The most recently created renderer, customizing the current series. */
	private XYSeriesRenderer mCurrentRenderer;
	private Context mContext=null;
	private int mType=0;
	private int mChart=0;
	private long mStartTick=System.currentTimeMillis()/1000;
	/** The chart view that displays the data. */
	private GraphicalView mChartView;
	ImageView mDelimg=null;
	private double mMin=0;
	private double mMax=0;
	public GraghChart(Context context,int type,int chart){
		mContext=context;
		mType=type;
		mChart=chart;
		// set some properties on the main renderer
	    //mRenderer.setApplyBackgroundColor(true);
	    //mRenderer.setBackgroundColor(Color.argb(100, 250, 0, 0));
	    //mRenderer.setAxisTitleTextSize(16);
	    //mRenderer.setChartTitleTextSize(20);
	    //mRenderer.setLabelsTextSize(15);
	    //mRenderer.setLegendTextSize(15);

	}
	public void setValue(double value) {
		// TODO Auto-generated method stub
		long mCurTick=System.currentTimeMillis()/1000;
		mCurrentSeries.add(mCurTick-mStartTick, value);
		mChartView.repaint();
	}

	public int getChart() {
		// TODO Auto-generated method stub
		return mChart;
	}

	public int getType() {
		// TODO Auto-generated method stub
		return mType;
	}

	public View createView(String title, String desc, double min,
			double max, double majortick, double minortick, double minAngle,
			double maxAngle) {
		// TODO Auto-generated method stub
		
        String seriesTitle = title;
        mMin=min;
        mMax=max;
        // create a new series of data
        XYSeries series = new XYSeries(seriesTitle);
        mDataset.addSeries(series);
        mCurrentSeries = series;
        // create a new renderer for the new series
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        // set some renderer properties
        renderer.setColor(Color.BLUE);
        //renderer.setPointStyle(PointStyle.POINT);
        //renderer.setFillPoints(true);
        //renderer.setDisplayChartValues(true);
        //renderer.setDisplayChartValuesDistance(10);
        mRenderer.addSeriesRenderer(renderer);
        //mRenderer.setShowAxes(true);
        //mRenderer.setChartTitle(seriesTitle);
        //mRenderer.setChartTitleTextSize(15);
		mRenderer.setShowGrid(true);
		mRenderer.setPanEnabled(false);
		mRenderer.setZoomEnabled(false,false);
		mRenderer.setShowLegend(true);
		mRenderer.setShowLabels(true);
		mRenderer.setXTitle(seriesTitle);
		mRenderer.setYLabelsPadding(10);
	    mRenderer.setLabelsTextSize(5);
	    mRenderer.setLegendTextSize(5);
	    mRenderer.setYAxisMin(mMin);
	    mRenderer.setYAxisMax(mMax);
	    mRenderer.setMargins(new int[] { 10, 10, 10, 10 });
	    mRenderer.setMarginsColor(Color.argb(0, 1, 1, 1));
	    
        mCurrentRenderer = renderer;
        mChartView = ChartFactory.getLineChartView(mContext, mDataset, mRenderer);
        mChartView.setBackgroundResource(com.cartalk.R.drawable.defaultsquare);
        //mChartView.repaint();
        mDelimg=new ImageView(mContext);
	    mDelimg.setImageResource(android.R.drawable.ic_delete);
	    mDelimg.setVisibility(View.INVISIBLE);
	    RelativeLayout wrapper=new RelativeLayout(mContext);
	    wrapper.addView(mChartView);
	    wrapper.addView(mDelimg);
		return wrapper;
	}
	public void enableEditMode(boolean enable){
		if(mDelimg==null)
			return;
		if(enable==true){
			mDelimg.setVisibility(View.VISIBLE);
		}else{
			mDelimg.setVisibility(View.INVISIBLE);
		}
	}
	public View getView() {
		// TODO Auto-generated method stub
		return null;
	}
	public View getCtlView() {
		// TODO Auto-generated method stub
		return null;
	}
}
