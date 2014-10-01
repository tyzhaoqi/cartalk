package com.cartalk;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ChartTypeActivity extends Activity{
		static final int GET_DIAL_CHART_REQ=0;
		static final int GET_GRAPH_CHART_REQ=1;
		static final int GET_DIGIT_CHART_REQ=2;
		final String[] arrayFruit = new String[] {"仪表", "图形","数字"};
		int type=0;
		protected void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.charttype); 
			//绑定Layout里面的ListView  
	        ListView list = (ListView) findViewById(R.id.charttypeListView);  
	          
	        //生成动态数组，加入数据  
	        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();  
	        for(int i=0;i<arrayFruit.length;i++)  
	        {
	            HashMap<String, Object> map = new HashMap<String, Object>();
	            map.put("ItemImage", R.drawable.menurealtime);
	            map.put("ItemTitle", arrayFruit[i]);
	            map.put("ItemText", "");
	            listItem.add(map);
	        }
	        SimpleAdapter listItemAdapter = new SimpleAdapter(this,listItem,
	        		R.layout.list_items,
	                new String[] {"ItemImage","ItemTitle", "ItemText"},
	                new int[] {R.id.ItemImage,R.id.ItemTitle,R.id.ItemText}
	        );
	        
	        list.setAdapter(listItemAdapter);
	        list.setOnItemClickListener(new OnItemClickListener() {
	            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
	            	type=arg2;
					Intent intent = new Intent();
					intent.setClass(ChartTypeActivity.this, ChartListActivity.class);
					startActivityForResult(intent,arg2);
	            }
	        });
	    }
		protected void onActivityResult(int requestCode, int resultCode, Intent data){
        	Intent intent=new Intent();
        	intent.putExtra("chart", data.getIntExtra("chart", 0));
        	intent.putExtra("type", type);
        	setResult(Activity.RESULT_OK, intent);
        	finish();
		}
}
