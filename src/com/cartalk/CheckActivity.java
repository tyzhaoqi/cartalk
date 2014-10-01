package com.cartalk;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Text;


import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CheckActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.checkcode);
	    ImageButton bt_warning = (ImageButton)this.findViewById(R.id.bt_warning);
	    bt_warning.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
		        Intent intent = new Intent();
		        intent.setClass(CheckActivity.this, TroubleCodeActivity.class); 
		        startActivity(intent);
			}
	    	
	    });
	    
	    ImageButton bt_fuelecon = (ImageButton)this.findViewById(R.id.bt_fuelecon);
	    bt_fuelecon.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
		        Intent intent = new Intent();
		        intent.setClass(CheckActivity.this, FuelEconActivity.class); 
		        startActivity(intent);
			}
	    	
	    });
	}
}

