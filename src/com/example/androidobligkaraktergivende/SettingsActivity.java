package com.example.androidobligkaraktergivende;


import android.annotation.SuppressLint;
import android.app.ApplicationErrorReport.AnrInfo;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

@SuppressLint("NewApi") 
public class SettingsActivity extends PreferenceActivity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
		{
			getActionBar().setDisplayShowHomeEnabled(false);
		}
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB){
			addPreferencesFromResource(R.xml.settings);
		}
		else
		{
			getFragmentManager().beginTransaction()
        	.replace(android.R.id.content, new SettingsFragment())
        	.commit();
		}
	}

	
}
