package com.example.androidobligkaraktergivende;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRegistered(Context context, String regId) {
		// TODO Auto-generated method stub    
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean reg = ServerUtil.register(context, regId, preferences.getString("pref_key_username", "username"));
		if(reg){
			Log.d("REGISTRERED", "MONGO");
			Intent i = new Intent(context, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(i);
		}
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub

	}

}
