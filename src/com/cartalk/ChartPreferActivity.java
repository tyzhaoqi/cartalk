package com.cartalk;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class ChartPreferActivity extends PreferenceActivity implements
                      OnPreferenceChangeListener,OnPreferenceClickListener {
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.chartpreferences);
        PreferenceScreen cmdScr = (PreferenceScreen) getPreferenceScreen()
                .findPreference("");
        
	}
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		Intent configIntent = new Intent(this, ChartListActivity.class);
        startActivity(configIntent);
		return false;
	}
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		return false;
	}
}
