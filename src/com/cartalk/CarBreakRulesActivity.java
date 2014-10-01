package com.cartalk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cartalk.TroubleCodeActivity.ExpandableListViewaAdapter;
import com.cartalk.io.HttpUtil;

import eu.lighthouselabs.obd.enums.AvailableCommandNames;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

public class CarBreakRulesActivity extends Activity {
    //定义两个List用来控制Group和Child中的String;
    private  List<String>  groupArray;//组列表
    private  List<List<String>> childArray;//子列表
    private  ExpandableListView  expandableListView_one;
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_breakrules);
        expandableListView_one =(ExpandableListView)findViewById(R.id.car_breakrulesListView);
        groupArray =new ArrayList<String>();
        childArray = new ArrayList<List<String>>();
        Intent intent1 = getIntent();
		String carid=intent1.getStringExtra("carid");
		String engineid=intent1.getStringExtra("engineid");
		String chassisid=intent1.getStringExtra("chassisid");
        try{
        	Map<String, String> params=new HashMap<String, String>();
        	params.put("carid", carid);
        	params.put("engineid", engineid);
        	params.put("chassisid", chassisid);
            String result=HttpUtil.http("http://cartalk.sinaapp.com/watchcar/querybreakrules/","POST",params);
        	JSONArray  jsonarray=new JSONArray(result);
        	for(int i=0;i<jsonarray.length();i++){
        		JSONObject obj = jsonarray.getJSONObject(i);
        		String fen = "扣分:\n"+obj.getString("fen");
        		String money = "罚款:\n"+obj.getString("money");
        		String area = "地点:\n"+obj.getString("area");
        		String act = "行为:\n"+obj.getString("act");
        		addInfo("违章"+(i+1),new String[]{fen,money,area,act});
        	}
        }catch(JSONException e){
        	Log.d("json error",e.getMessage());
        }
        expandableListView_one.setAdapter(new ExpandableListViewaAdapter(CarBreakRulesActivity.this));
	}
	class ExpandableListViewaAdapter extends BaseExpandableListAdapter {
        Activity activity;
         public  ExpandableListViewaAdapter(Activity a) 
            { 
                activity = a; 
            } 
       /*-----------------Child */
        public Object getChild(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return childArray.get(groupPosition).get(childPosition);
        }
        public long getChildId(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return childPosition;
        }
        public View getChildView(int groupPosition, int childPosition,
                boolean isLastChild, View convertView, ViewGroup parent) {
            String string =childArray.get(groupPosition).get(childPosition);
            return getGenericView(string);
        }
        public int getChildrenCount(int groupPosition) {
            // TODO Auto-generated method stub
            return childArray.get(groupPosition).size();
        }
       /* ----------------------------Group */
        public Object getGroup(int groupPosition) {
            // TODO Auto-generated method stub
            return getGroup(groupPosition);
        }
        public int getGroupCount() {
            // TODO Auto-generated method stub
            return groupArray.size();
        }
        public long getGroupId(int groupPosition) {
            // TODO Auto-generated method stub
            return groupPosition;
        }
        public View getGroupView(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {
           String   string=groupArray.get(groupPosition);
           return getGenericView(string);
        }
        public boolean hasStableIds() {
        	// TODO Auto-generated method stub
        	return false;
        }
        public boolean isChildSelectable(int groupPosition, int childPosition)
        {
        	// TODO Auto-generated method stub
        	return true;
        }
        private TextView  getGenericView(String string )
        {

           AbsListView.LayoutParams  layoutParams =new AbsListView.LayoutParams(

                 ViewGroup.LayoutParams.MATCH_PARENT,

                 ViewGroup.LayoutParams.WRAP_CONTENT);

            

           TextView  textView =new TextView(activity);

           textView.setLayoutParams(layoutParams);

            

           textView.setGravity(Gravity.CENTER_VERTICAL |Gravity.LEFT);

            

           textView.setPadding(60, 10, 0, 10);

           textView.setText(string);

           return textView;

        }

     }
    private void addInfo(String group,String []child) {
    	groupArray.add(group);
    	List<String>  childItem =new ArrayList<String>();
    	for(int index=0;index<child.length;index++){
    		childItem.add(child[index]);
    	}
    	childArray.add(childItem);
    }
}
