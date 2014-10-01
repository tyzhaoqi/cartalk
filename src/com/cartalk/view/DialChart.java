package com.cartalk.view;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DialRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.DialRenderer.Type;

import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ViewAnimator;

public class DialChart implements IChart{
	private GraphicalView mChartView=null;
	private RelativeLayout mWrapper=null;
	private ImageView mDelimg=null;
	private Context mContext=null;
	private CategorySeries category=null;
	private DialRenderer renderer=null;
	private int mType=0;
	private int mChart=0;
	public DialChart(Context context,int type,int chart){
		mContext=context;
		mType=type;
		mChart=chart;
	}
	public int getType(){return mType;}
	public int getChart(){return mChart;}
	public View createView(String title,String desc,double min,double max,double majortick,double minortick,double minAngle,double maxAngle){
		category = new CategorySeries("Weight indic");
	    category.add("cur",0);
	    
	    renderer = new DialRenderer();
	    renderer.setMajorTicksSpacing(majortick);
	    renderer.setMinorTicksSpacing(minortick);
	    renderer.setShowLegend(false);
	    renderer.setMargins(new int[] {0,0,0,0});
	    renderer.setAngleMin(minAngle);
	    renderer.setAngleMax(maxAngle);
	    SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	    r.setColor(Color.WHITE);
	    renderer.addSeriesRenderer(r);
	    renderer.setLabelsTextSize(8);
	    renderer.setLabelsColor(Color.WHITE);
	    renderer.setVisualTypes(new DialRenderer.Type[] {Type.ARROW});
	    renderer.setMinValue(min);
	    renderer.setMaxValue(max);
	    renderer.setInScroll(true);
	    renderer.setPanEnabled(false);
	    renderer.setZoomEnabled(false);
	    renderer.setChartTitle(title);
	    renderer.setShowLabels(true);
	    mChartView=ChartFactory.getDialChartView(mContext, category, renderer);
	    mChartView.setBackgroundResource(com.cartalk.R.drawable.defaultdial);
	    mDelimg=new ImageView(mContext);
	    mDelimg.setImageResource(android.R.drawable.ic_delete);
	    mDelimg.setVisibility(View.INVISIBLE);
	    mWrapper=new RelativeLayout(mContext);
	    mWrapper.addView(mChartView);
	    mWrapper.addView(mDelimg);
		return mWrapper;
	}
	public void setValue(double value){
		category.set(0, "cur", value);
		//mChartView.invalidate();
		mChartView.postInvalidate();
	}
	public void enableEditMode(boolean enable){
		if(mDelimg==null)
			return;
		if(enable==true){
			mDelimg.setVisibility(View.VISIBLE);
			RotateAnimation anim=new RotateAnimation(0f, 360f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			anim.setInterpolator(new LinearInterpolator());
			//anim.setRepeatCount(1);
			anim.setDuration(700);
			mDelimg.startAnimation(anim);
		}else{
			mDelimg.setVisibility(View.INVISIBLE);
		}
	}
	public View getView(){
		return mWrapper;
	}
	public View getCtlView(){
		return mDelimg;
	}
}
