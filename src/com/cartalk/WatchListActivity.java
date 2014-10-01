package com.cartalk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cartalk.io.HttpUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class WatchListActivity extends Activity{
	static final int ADD_DIAL_CHART=0;
	static final int ADD_GRAPH_CHART=1;
	static final int ADD_DIGIT_CHART=2;
	ListView watchlist = null;
	String[] caridList=null;
	String[] engineidList=null;
	String[] chassisidList=null;
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.watchlist); 
		//绑定Layout里面的ListView  
		watchlist = (ListView) findViewById(R.id.watchListView);
        Button button_add = (Button)findViewById(R.id.bt_addcar); 
        
        button_add.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
		        intent.setClass(WatchListActivity.this, CarDetailActivity.class); 
		        startActivityForResult(intent, 1);
			}
        	
        });
        refreshWatchlist();
    }
	
	private void refreshWatchlist(){
		 //生成动态数组，加入数据  
		Map<String, String> params=new HashMap<String, String>();
		params.put("username", "zhaoqi");
		String result = HttpUtil.http("http://cartalk.sinaapp.com/watchcar/1/select/","POST",params);
		try{
	        JSONArray  jsonarray=new JSONArray(result);
	        if(jsonarray.length()>0){
	        	caridList = new String[jsonarray.length()];
	        	engineidList = new String[jsonarray.length()];
	        	chassisidList = new String[jsonarray.length()];
	            for(int i=0;i<jsonarray.length();i++){
	        	    JSONObject obj = jsonarray.getJSONObject(i);
	        	    caridList[i] = obj.getString("carid");
	        	    engineidList[i] = obj.getString("engineid");
	        	    chassisidList[i] = obj.getString("chassisid");
	            }
	        }
	    }catch(JSONException e){
	        	Log.d("json error",e.getMessage());
	    }
        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();  
        for(int i=0;i<caridList.length;i++){
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("WatchItemImage", R.drawable.caritem);
            map.put("WatchItemTitle", caridList[i]);
            map.put("WatchItemText", engineidList[i]);
            listItem.add(map);
        }
        SimpleAdapter listItemAdapter = new SimpleAdapter(this,listItem,
        		R.layout.watchlistitem,
                new String[] {"WatchItemImage","WatchItemTitle", "WatchItemText"},
                new int[] {R.id.WatchItemImage,R.id.WatchItemTitle,R.id.WatchItemText}
        );
        
        watchlist.setAdapter(listItemAdapter);
        watchlist.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
    			String carid=caridList[arg2];
    			String engineid=engineidList[arg2];
    			String chassisid=chassisidList[arg2];
            	try {
            		carid = URLEncoder.encode(carid,"UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	Intent intent=new Intent();
            	intent.putExtra("carid", carid);
            	intent.putExtra("engineid", engineid);
            	intent.putExtra("chassisid", chassisid);
            	intent.setClass(WatchListActivity.this, CarBreakRulesActivity.class);
                startActivity(intent);
            	
            }
        });
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
		case RESULT_OK:
			Bundle b=data.getExtras();
			String carid=b.getString("carid");
			String engineid=b.getString("engineid");
			String chassisid=b.getString("chassisid");
			Map<String, String> params=new HashMap<String, String>();
			params.put("username", "zhaoqi");
			params.put("carid", carid);
			params.put("engineid", engineid);
			params.put("chassisid", chassisid);
			HttpUtil.http("http://cartalk.sinaapp.com/watchcar/1/add/","POST",params);
		    break;
		default:
		     break;
		}
		refreshWatchlist();
	}
}
