package com.cartalk.view;

import org.achartengine.GraphicalView;

import android.view.View;

public interface IChart {
	public void setValue(double value);
	public void enableEditMode(boolean enable);
	public View getView();
	public View getCtlView();
	public int getChart();
	public int getType();
	public View createView(String title,String desc,double min,double max,double majortick,double minortick,double minAngle,double maxAngle);
}
