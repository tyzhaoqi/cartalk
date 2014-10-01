package com.cartalk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cartalk.io.HttpUtil;
import com.cartalk.io.RealTimeMsg;

import eu.lighthouselabs.obd.enums.AvailableCommandNames;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class TroubleCodeActivity extends Activity {
    //定义两个List用来控制Group和Child中的String;
    private  List<String>  groupArray;//组列表
    private  List<List<String>> childArray;//子列表
    private  ExpandableListView  expandableListView_one;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);  //设置为无标题
        setContentView(R.layout.toublecodeview);
        expandableListView_one =(ExpandableListView)findViewById(R.id.troublecodeListView);
        groupArray =new ArrayList<String>();
        childArray = new ArrayList<List<String>>();
        initdata();
        expandableListView_one.setAdapter(new ExpandableListViewaAdapter(TroubleCodeActivity.this));
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
    private void initdata() {

    	Map<String, String> params=new HashMap<String, String>();
    	params.put("username", "zhaoqi");
    	params.put("cmdname", AvailableCommandNames.TROUBLE_CODES.getValue());
        String result=HttpUtil.http("http://cartalk.sinaapp.com/vehicledata/1/select/","POST",params);
        try{
        	JSONArray  jsonarray=new JSONArray(result);
        	for(int i=0;i<jsonarray.length();i++){
        		JSONObject obj = jsonarray.getJSONObject(i);
        		String troublecode = obj.getString("troublecode");
        		String desc = "描述:\n"+obj.getString("desc");
        		String detail = "详情:\n"+obj.getString("detail").replaceAll("<br>", "\n");
        		String symptoms = "症状:\n"+obj.getString("symptoms").replaceAll("<br>", "\n");
        		String causes = "原因:\n"+obj.getString("causes").replaceAll("<br>", "\n");
        		String solution = "解决方案:\n"+obj.getString("solution").replaceAll("<br>", "\n");
        		addInfo(troublecode, new String[]{desc,detail,symptoms,causes,solution});
        	}
        }catch(JSONException e){
        	Log.d("json error",e.getMessage());
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
