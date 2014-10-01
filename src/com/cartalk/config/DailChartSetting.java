package com.cartalk.config;

public class DailChartSetting {
	public String mTitle;
	public String mDesc;
	public double mMin;
	public double mMax;
	public double mMajortick;
	public double mMinortick;
	public double mMinAngle;
	public double mMaxAngle;
	public DailChartSetting(String title,String desc,double min,double max,double majortick,double minortick,double minAngle,double maxAngle){
		mTitle=title;
		mDesc=desc;
		mMin=min;
		mMax=max;
		mMajortick=majortick;
		mMinortick=minortick;
		mMinAngle=minAngle;
		mMaxAngle=maxAngle;
	}
}
