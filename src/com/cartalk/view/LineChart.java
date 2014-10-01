package com.cartalk.view;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.DialRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.DialRenderer.Type;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;

public class LineChart {
	private GraphicalView mChartView=null;
	private Context mContext=null;
	private CategorySeries category=new CategorySeries("Weight indic");
	private XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	private XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
	private XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	public LineChart(Context context){
		mContext=context;
	}
	public GraphicalView createView(String title,String desc,double min,double max,double majortick,double minortick,double minAngle,double maxAngle){
	    dataset.addSeries(category.toXYSeries());
	    xyRenderer.setColor(Color.BLUE);
	    renderer.addSeriesRenderer(xyRenderer);
	    double[] range = { 0, 10, 1, 200 };
		renderer.setRange(range);
		renderer.setXLabels(10);
		renderer.setYLabels(10);
		renderer.setShowGrid(true);
		renderer.setShowAxes(true);
	    mChartView=ChartFactory.getLineChartView(mContext, dataset, renderer);
	    mChartView.setBackgroundResource(com.cartalk.R.drawable.defaultsquare);
		return mChartView;
	}
	public void setValue(double value){
		category.add("cur", value);
		mChartView.postInvalidate();
	}
}

