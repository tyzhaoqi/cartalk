package com.cartalk;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.MKPlanNode;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKRoute;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKStep;
import com.baidu.mapapi.MKSuggestionInfo;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.RouteOverlay;

@SuppressLint("NewApi")
public class NavigationActivity extends MapActivity {
    private String mMapKey = "1E5CAD96E619789923BF6E4C475823D91C79A2E7";
    private AutoCompleteTextView destinationEditText = null;
    private Button startNaviButton = null;
    private MapView mapView = null;
    private BMapManager mMapManager = null;
    private MyLocationOverlay myLocationOverlay = null;
    //onResume时注册此listener，onPause时需要Remove,注意此listener不是Android自带的，是百度API中的
    private LocationListener locationListener;
    private MKSearch searchModel;
    private GeoPoint pt;
    private int curNavNode = 0;
    private MKRoute curRoute =null;
    private TextToSpeech mSpeech = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.navigation);
        destinationEditText = (AutoCompleteTextView) this.findViewById(R.id.et_destination);
        startNaviButton = (Button) this.findViewById(R.id.btn_navi);
        mMapManager = new BMapManager(getApplication());
        mMapManager.init(mMapKey, new MyGeneralListener());
        super.initMapActivity(mMapManager);
        
        mapView = (MapView) this.findViewById(R.id.bmapsView);
        //设置启用内置的缩放控件
        mapView.setBuiltInZoomControls(true);  
        //设置在缩放动画过程中也显示overlay,默认为不绘制
//        mapView.setDrawOverlayWhenZooming(true);
        //获取当前位置层
        myLocationOverlay = new MyLocationOverlay(this, mapView);
        //将当前位置的层添加到地图底层中
        mapView.getOverlays().add(myLocationOverlay);
        
        //初始化搜索模块
        searchModel = new MKSearch();
        //设置路线策略为最短距离
        searchModel.setDrivingPolicy(MKSearch.ECAR_DIS_FIRST);
        searchModel.init(mMapManager, new MKSearchListener() {
            //获取驾车路线回调方法
            public void onGetDrivingRouteResult(MKDrivingRouteResult res, int error) {
                // 错误号可参考MKEvent中的定义
                if (error != 0 || res == null) {
                    Toast.makeText(NavigationActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                    return;
                }
                RouteOverlay routeOverlay = new RouteOverlay(NavigationActivity.this, mapView);
                
                // 此处仅展示一个方案作为示例
                MKRoute route = res.getPlan(0).getRoute(0);
                curRoute = route;
                int distanceM = route.getDistance();
                String distanceKm = String.valueOf(distanceM / 1000) +"."+String.valueOf(distanceM % 1000);
                System.out.println("距离:"+distanceKm+"公里---节点数量:"+route.getNumSteps());
                for (int i = 0; i < route.getNumSteps(); i++) {
                    MKStep step = route.getStep(i);
                    System.out.println("节点信息："+step.getContent());
                }
                routeOverlay.setData(route);
                mapView.getOverlays().clear();
                mapView.getOverlays().add(myLocationOverlay);
                mapView.getOverlays().add(routeOverlay);
                mapView.invalidate();
                mapView.getController().animateTo(res.getStart().pt);
            }
            
            //以下两种方式和上面的驾车方案实现方法一样
            public void onGetWalkingRouteResult(MKWalkingRouteResult res, int error) {
                //获取步行路线
            }
            
            public void onGetTransitRouteResult(MKTransitRouteResult arg0, int arg1) {
                //获取公交线路
            }
            
            public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
            	
            }
            public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
            	
            }
            public void onGetSuggestionResult(MKSuggestionResult result, int ret) {
            	if(result==null){
            		return;
            	}
            	String destBefore=destinationEditText.getText().toString();
            	for(int i=0;i<result.getSuggestionNum();i++){
            		MKSuggestionInfo info=result.getSuggestion(i);
            		if(destBefore.equals(info.key+"-"+info.city))
            			return;
            	}
            	List<String> list = new ArrayList<String>();
            	for(int i=0;i<result.getSuggestionNum();i++){
            		MKSuggestionInfo info=result.getSuggestion(i);
            		list.add(info.key+"-"+info.city);
            	}
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String> (NavigationActivity.this,R.layout.dest_items,list);
                destinationEditText.setAdapter(arrayAdapter);
                arrayAdapter.notifyDataSetChanged();
            }
            public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
            	
            }
        });
     // 注册定位事件
        locationListener = new LocationListener(){

            public void onLocationChanged(Location location) {
                if (location != null){
                    //生成GEO类型坐标并在地图上定位到该坐标标示的地点
                     pt = new GeoPoint((int)(location.getLatitude()*1e6),
                            (int)(location.getLongitude()*1e6));
//                  System.out.println("---"+location.getLatitude() +":"+location.getLongitude());
                    mapView.getController().animateTo(pt);
                    navigateToNext(pt);
                }
            }

        };
        startNaviButton.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                String destination = destinationEditText.getText().toString();
                
                //设置起始地（当前位置）
                MKPlanNode startNode = new MKPlanNode();
                startNode.pt = pt;
                //设置目的地
                MKPlanNode endNode = new MKPlanNode();
                String[] vdes=destination.split("-");
                endNode.name = vdes[0];
                
                //展开搜索的城市
                String city = "";
                if(vdes.length>=2){
                	city = vdes[1];
                }
                	
//              System.out.println("----"+city+"---"+destination+"---"+pt);
                searchModel.drivingSearch(null, startNode,city, endNode);
                //步行路线
//              searchModel.walkingSearch(city, startNode, city, endNode);
                //公交路线
//              searchModel.transitSearch(city, startNode, endNode);
            }
        });
        destinationEditText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				String destination = destinationEditText.getText().toString();
				searchModel.suggestionSearch(destination);
			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
			}
        	
        });
        mSpeech = new TextToSpeech(this, new OnInitListener() {
        	public void onInit(int status) {
        		// TODO Auto-generated method stub
        		if (status == TextToSpeech.SUCCESS) {
        			mSpeech.setEngineByPackageName("com.iflytek.tts");
        			int result = mSpeech.setLanguage(Locale.CHINA);
        		    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        		        Log.e("lanageTag", "not use");
        		    }
        		}
            }
        });
        
    }
    
    @Override
    protected void onResume() {
        mMapManager.getLocationManager().requestLocationUpdates(locationListener);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableCompass(); // 打开指南针
        mMapManager.start();
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        mMapManager.getLocationManager().removeUpdates(locationListener);
        myLocationOverlay.disableMyLocation();//显示当前位置
        myLocationOverlay.disableCompass(); // 关闭指南针
        mMapManager.stop();
        super.onPause();
    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }
    
    private void navigateToNext(GeoPoint curPt){
    	if(curRoute==null)
    		return;
    	MKStep step = curRoute.getStep(curNavNode);
    	GeoPoint rnode=step.getPoint();
    	float[] results =new float[3];
    	Location.distanceBetween(rnode.getLatitudeE6()/1E6, rnode.getLongitudeE6()/1E6, curPt.getLatitudeE6()/1E6, curPt.getLongitudeE6()/1E6, results);
    	if(results[0]<100){
    		int ret=mSpeech.speak(step.getContent(), TextToSpeech.QUEUE_FLUSH, null);
    		curNavNode++;
    	}
    }
    
    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    class MyGeneralListener implements MKGeneralListener {
            public void onGetNetworkState(int iError) {
                Log.d("MyGeneralListener", "onGetNetworkState error is "+ iError);
                Toast.makeText(NavigationActivity.this, "您的网络出错啦！",
                        Toast.LENGTH_LONG).show();
            }

            public void onGetPermissionState(int iError) {
                Log.d("MyGeneralListener", "onGetPermissionState error is "+ iError);
                if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
                    // 授权Key错误：
                    Toast.makeText(NavigationActivity.this, 
                            "请在BMapApiDemoApp.java文件输入正确的授权Key！",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
} 
