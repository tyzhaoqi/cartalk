package com.cartalk.view;

import java.util.Timer;
import java.util.TimerTask;

import com.cartalk.ChartTypeActivity;
import com.cartalk.RealTimeInfoActivity;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 
 * @author zq
 * 
 */
public class ScrollLayout extends ViewGroup {
	Scroller scroller;
	VelocityTracker velocity;
	int mCurScreen;
	float mLastX;
	int mLastPressTime;
	Context mContext;
//	Timer timer=null;
//	TimerTask task=null;
	public static final int SNAP_VELOCITY = 600;
	public static final int LONG_PRESS_DURATION=2000;
	static final int GET_CHART_TYPE_REQ=0;
	
	public ScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ScrollLayout(Context context) {
		super(context);
		init(context);
	}

	public void init(Context context) {
		scroller = new Scroller(context);
		mCurScreen = 0;
		mContext=context;
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int childCount = getChildCount();
		int width = MeasureSpec.getSize(widthMeasureSpec);
		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);
			childView.measure(widthMeasureSpec, heightMeasureSpec);
		}

		
		scrollTo(mCurScreen * width, 0);
	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		//if(changed){
			int childCount = getChildCount();
			int childLeft = 0;
			for(int i = 0; i < childCount; i ++){
				View childView = getChildAt(i);
				int width = childView.getMeasuredWidth();
				childView.layout(childLeft, 0, childLeft + width, childView.getMeasuredHeight());
				
				childLeft += width;
			}
		}
	//}
	
	@Override
	public void computeScroll() {
		if(scroller.computeScrollOffset()){
			scrollTo(scroller.getCurrX(), scroller.getCurrY());
			postInvalidate();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float curX = event.getX();
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if(velocity == null ){
				velocity = VelocityTracker.obtain();
				velocity.addMovement(event);
			}
			if(!scroller.isFinished()){
				scroller.abortAnimation();
			}
			mLastX = curX;
/*			if(timer==null){
				timer=new Timer();
				TimerTask task=new TimerTask(){
					public void run(){
					    Intent intent = new Intent();
					    intent.setClass(mContext, ChartTypeActivity.class);
					    mContext.startActivity(intent);
					}
				};
				timer.schedule(task, LONG_PRESS_DURATION);
			}
*/
			break;

		case MotionEvent.ACTION_MOVE:
			int distance_x = (int)(mLastX - curX);
			if(IsCanMove(distance_x)){
				if(velocity != null ){
					velocity.addMovement(event);
				}
				mLastX = curX;
/*				if(timer!=null)
				{
					timer.cancel();
					timer=null;
				}
*/
				scrollBy(distance_x, 0);
			}
			break;
		case MotionEvent.ACTION_UP:
			int velocityX = 0;
			mLastPressTime=0;
            if (velocity != null)
            {
            	velocity.addMovement(event); 
            	velocity.computeCurrentVelocity(1000);  
            	velocityX = (int) velocity.getXVelocity();
            }
                    
                
            if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {       
                // Fling enough to move left       
                snapToScreen(mCurScreen - 1);       
            } else if (velocityX < -SNAP_VELOCITY       
                    && mCurScreen < getChildCount() - 1) {       
                // Fling enough to move right       
                snapToScreen(mCurScreen + 1);       
            } else {       
                snapToDestination();       
            }      
            
           
            
            if (velocity != null) {       
            	velocity.recycle();       
            	velocity = null;       
            }       
/*            if(timer!=null)
            {
            	timer.cancel();
            	timer=null;
            }
*/
            break;   
		}
		onTouchEventDisp(event);
		return true;
	}
	
	public void onTouchEventDisp(MotionEvent event){
		
	}
	 public void snapToDestination() {
	        final int screenWidth = getWidth();    

	        final int destScreen = (getScrollX()+ screenWidth/2)/screenWidth;    
	        snapToScreen(destScreen);    
	 }  
	
	 public void snapToScreen(int whichScreen) {    
	
	        // get the valid layout page    
	        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount()-1));    
	        if (getScrollX() != (whichScreen*getWidth())) {    
	                
	            final int delta = whichScreen*getWidth()-getScrollX();    
	        
	            scroller.startScroll(getScrollX(), 0,     
	                    delta, 0, Math.abs(delta)*2);

	            
	            mCurScreen = whichScreen;    
	            invalidate();       // Redraw the layout    
	            
	        }
	    }    


	public boolean IsCanMove(int distance_x){
		if(distance_x < 0 && getScrollX() < 0){
			return false;
		}
		
		if(getScrollX() > (getChildCount() - 1) * getWidth() && distance_x > 0){
			return false;
		}
		return true;
	}
	public int getCurScreen(){
		return mCurScreen;
	}
}
