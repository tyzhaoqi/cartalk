package com.cartalk;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;



public class ChartListActivity extends Activity{
	static final int ADD_DIAL_CHART=0;
	static final int ADD_GRAPH_CHART=1;
	static final int ADD_DIGIT_CHART=2;
	final String[] arrayFruit = new String[] {"加速度", "转速","气门位置","速度","歧管压力","冷却液","空气流速","燃料平衡","空燃比"};
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.charttype); 
		//绑定Layout里面的ListView  
        ListView list = (ListView) findViewById(R.id.charttypeListView);  
          
        //生成动态数组，加入数据  
        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();  
        for(int i=0;i<arrayFruit.length;i++){
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
            	Intent intent=new Intent();
            	intent.putExtra("chart", arg2);
            	setResult(Activity.RESULT_OK, intent);
            	finish();
            }
        });
    }
}
