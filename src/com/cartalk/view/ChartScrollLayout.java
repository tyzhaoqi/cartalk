package com.cartalk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ChartScrollLayout extends ScrollLayout {

	public ChartScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
		// TODO Auto-generated constructor stub
	}
	public ChartScrollLayout(Context context, AttributeSet attrs) {
		super(context,attrs);
		// TODO Auto-generated constructor stub
	}
	public ChartScrollLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public void onTouchEventDisp(MotionEvent event){
		ChartTableLayout.allLayouts.get(mCurScreen).onTouchEventDisp(event);
	}
}
