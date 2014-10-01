package com.cartalk.view;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.cartalk.ChartTypeActivity;
import com.cartalk.RealTimeInfoActivity;
import com.cartalk.config.DailChartSetting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableRow;

@SuppressLint({ "NewApi", "NewApi", "NewApi" })
public class ChartTableLayout extends TableLayout {
	public static Vector<ChartTableLayout> allLayouts=new Vector<ChartTableLayout>();
	private Context mContext=null;
	private Timer timer=null;
	private TimerTask task=null;
	float mLastX=0;
	long mLastDownTime=0;
	private boolean press=false;
	private SharedPreferences mPreference=null;
	private Vector<Pair<Integer,Integer>> mChartCfg=new Vector<Pair<Integer,Integer>>();
	private Vector<IChart> mCharts=new Vector<IChart>();
	private Hashtable<Integer,DailChartSetting> dailChartSetting=new Hashtable<Integer,DailChartSetting>();
	private int mId=0;
	private int mViewCount=0;
	private TableRow curTableRow;
	private static final int LONG_PRESS_DURATION=2000;
	private static final int MAX_VIEW=6;
	private int mScreenWidth = 0;
	private int mScreenHeight = 0;
	private int mViewPerRow = 2;
	private int mViewPerCol = 3;
	private boolean mEditMode=false;
	public static final int GET_CHART_TYPE_REQ=0;
	public ChartTableLayout(Context context,AttributeSet attrs) {
		super(context,attrs);
		// TODO Auto-generated constructor stub
		mContext=context;
		dailChartSetting.put(0,new DailChartSetting("加速度","",-10,10,2,1,520,200));
		dailChartSetting.put(1,new DailChartSetting("转速","",0,7,1,0.5,360,40));
		dailChartSetting.put(2,new DailChartSetting("气门位置","",0,100,20,10,360,60));
		dailChartSetting.put(3,new DailChartSetting("速度","",0,300,20,10,360,40));
		dailChartSetting.put(4,new DailChartSetting("增压","",-20,20,4,2,520,200));
		dailChartSetting.put(5,new DailChartSetting("冷却液","",0,120,20,10,360,40));
		dailChartSetting.put(6,new DailChartSetting("空气流量","",0,700,100,50,360,40));
		dailChartSetting.put(7,new DailChartSetting("燃料平衡","",0,700,100,50,360,40));
		dailChartSetting.put(8,new DailChartSetting("空燃比","",0,30,5,2.5,360,90));
		
		mId=allLayouts.size();
		allLayouts.add(this);
	}
	public ChartTableLayout(Context context) {
		super(context);
		mContext=context;
	}
	public static void RemoveAll(){
		allLayouts.removeAllElements();
	}
	public boolean onTouchEventDisp(MotionEvent event) {
		//detector.onTouchEvent(event);
		super.onTouchEvent(event);
		float curX = event.getX();
		float curY = event.getY();
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if(timer==null){
				timer=new Timer();
				task=new TimerTask(){
					public void run(){
					    Intent intent = new Intent();
					    intent.setClass(mContext, ChartTypeActivity.class);
					    ((Activity)mContext).startActivityForResult(intent,GET_CHART_TYPE_REQ);
					}
				};
				timer.schedule(task, LONG_PRESS_DURATION);
			}
			if(mEditMode){
				int i=0;
				Rect outRect=new Rect();
				getWindowVisibleDisplayFrame(outRect);
				for(i=0;i<mCharts.size();i++){
					View v=mCharts.get(i).getCtlView();
					int[] loc=new int[2];
					v.getLocationOnScreen(loc);
					int left=loc[0];
					int top = loc[1]-outRect.top;
					int right = v.getWidth()+left;
					int bottom = v.getHeight()+top;
					if(curX>left && curX<right && curY>top && curY<bottom){
						break;
					}
				}
				if(mCharts.size()>0 && i<mCharts.size()){
					mCharts.remove(i);
					mChartCfg.remove(i);
					String prefstring=makeSettingStr();
					String prefername="realtime"+mId;
					SharedPreferences.Editor editor = mPreference.edit(); 
					editor.putString(prefername, prefstring);
					editor.commit();
					removeAllView();
					loadPreferences(mScreenWidth,mScreenHeight);
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if(timer!=null){
				timer.cancel();
				timer=null;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if(timer!=null){
				timer.cancel();
				timer=null;
			}
            break;
        default:
        	break;
		}
		return false;
	}
	public boolean loadPreferences(int width,int height){
		mScreenWidth=width;
		mScreenHeight=height;
		String prefername="realtime"+mId;
		getPreferences("realtimepreferences",prefername);
		return true;
	}
	private boolean getPreferences(String preferenceName, String key){
		mPreference=mContext.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
		if(!mPreference.contains(key)){
			SharedPreferences.Editor editor = mPreference.edit(); 
			editor.putString(key, "0=0 0=1 0=2 0=3 0=4 0=5");
			editor.commit();
		}
		String preferstring=mPreference.getString(key, "");
		String[] prefervec=preferstring.split(" ");
		for(int i=0;i<prefervec.length;i++){
			String[] items=prefervec[i].split("=");
			if(items.length!=2){
				mChartCfg.clear();
				return false;
			}
			int type=Integer.parseInt(items[0]);
			int chart=Integer.parseInt(items[1]);
			addView(type,chart);
		}
		return true;
	}
	@SuppressLint({ "NewApi", "NewApi", "NewApi", "NewApi" })
	public void addView(int type,int chart){
		if(mChartCfg.size()>=MAX_VIEW){
			return;
		}
		IChart ichart = null;
		View v=null;
		DailChartSetting s=null;
		switch(type){
		case 0:
			ichart=new DialChart(mContext,type,chart);
			s=dailChartSetting.get(chart);
			v=ichart.createView(s.mTitle, s.mDesc, s.mMin, s.mMax, s.mMajortick, s.mMinortick, s.mMinAngle, s.mMaxAngle);
			break;
		case 1:
			ichart=new GraghChart(mContext,type,chart);
			s=dailChartSetting.get(chart);
			v=ichart.createView(s.mTitle, s.mDesc, s.mMin, s.mMax, s.mMajortick, s.mMinortick, s.mMinAngle, s.mMaxAngle);
			break;
		default:
			break;
		}
		if(ichart==null || v==null){
			return;
		}
		mChartCfg.add(new Pair<Integer, Integer>(type,chart));
		mCharts.add(ichart);
		String prefstring=makeSettingStr();
		String prefername="realtime"+mId;
		SharedPreferences.Editor editor = mPreference.edit(); 
		editor.putString(prefername, prefstring);
		editor.commit();
		if(mScreenWidth>mScreenHeight){
			mViewPerRow=3;
			mViewPerCol=2;
		}else
		{
			mViewPerRow=2;
			mViewPerCol=3;
		}
		int r=Math.min(mScreenWidth/mViewPerRow,mScreenHeight/mViewPerCol);
		if(mViewCount%mViewPerRow==0){
			curTableRow = new TableRow(mContext);
			super.addView(curTableRow);
		}
		mViewCount++;
		curTableRow.addView(v,r,r);
	}
	public void updateView(Hashtable<Integer,Vector<Double> > mapData){
		for(int i=0;i<mCharts.size();i++){
			int chart=mCharts.get(i).getChart();
			if(mapData.get(chart)!=null){
				mCharts.get(i).setValue(mapData.get(chart).lastElement());
			}
		}
	}
	public void removeAllView(){
		mViewCount=0;
		mCharts.clear();
		mChartCfg.clear();
		removeAllViews();
	}
	public String makeSettingStr(){
		String prefstring="";
		for(int i=0;i<mChartCfg.size();i++){
			Pair<Integer,Integer> p=mChartCfg.get(i);
			prefstring+=""+p.first+"="+p.second+" ";
		}
		prefstring.trim();
		return prefstring;
	}
	public void SetEditMode(boolean enable){
		mEditMode=enable;
		for(int i=0;i<mCharts.size();i++){
			mCharts.get(i).enableEditMode(enable);
		}
	}
}
