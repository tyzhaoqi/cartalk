package com.cartalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class CarDetailActivity extends Activity {
    private Spinner province=null;
    private Spinner order=null;
    private String carid="";
    private String engineid="";
    private String chassisid="";
    private String strProvince=null;
    private String strOrder=null;
    private EditText CarIdEdit = null;
    private EditText EngineIdEdit = null;
    private EditText ChassisIdEdit = null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_detail);
        this.province=(Spinner) super.findViewById(R.id.province);
        this.order=(Spinner) super.findViewById(R.id.order);
        this.CarIdEdit = (EditText) super.findViewById(R.id.carid);
        this.EngineIdEdit = (EditText) super.findViewById(R.id.engineid);
        this.ChassisIdEdit = (EditText) super.findViewById(R.id.chassisid);
        this.province.setOnItemSelectedListener(new OnItemSelectedListenerImp());
        this.order.setOnItemSelectedListener(new OnOrderItemSelectedListenerImp());
        Button bt_ok = (Button) findViewById(R.id.bt_car_detail_ok);
        bt_ok.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				carid = strProvince+strOrder+CarIdEdit.getText().toString();
				engineid = EngineIdEdit.getText().toString();
				chassisid = ChassisIdEdit.getText().toString();
            	Intent intent=new Intent();
            	intent.putExtra("carid", carid);
            	intent.putExtra("engineid", engineid);
            	intent.putExtra("chassisid", chassisid);
            	setResult(Activity.RESULT_OK, intent);
            	finish();
			}
        	
        });
    }

    private class OnItemSelectedListenerImp implements OnItemSelectedListener{

        public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
        	strProvince=parent.getItemAtPosition(position).toString();
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }
    
    private class OnOrderItemSelectedListenerImp implements OnItemSelectedListener{

        public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
        	strOrder = parent.getItemAtPosition(position).toString(); 
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

}