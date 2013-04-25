package com.example.androidobligkaraktergivende;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class UpdatepositionService extends IntentService {

	public UpdatepositionService() {
		super("UpdatepositionService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		double lat = intent.getDoubleExtra("LATITUDE", 0.0);
		double lon = intent.getDoubleExtra("LONGITUDE", 0.0);
		Log.d("ASYNC", "lat: " + lat + " long: " + lon);
		ServerUtil.update(getApplicationContext(), lat, lon);
		
	}
}
