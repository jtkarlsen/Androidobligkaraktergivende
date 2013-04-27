package com.example.androidobligkaraktergivende;

import android.app.IntentService;
import android.content.Intent;

/**
 * Intent service som sender posisjon til server.
 * @author kakebake
 *
 */
public class UpdatepositionService extends IntentService {

	public UpdatepositionService() {
		super("UpdatepositionService");
		
	}

	/**
	 * Oppdaterer server med ny posisjon.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		
		double lat = intent.getDoubleExtra("LATITUDE", 0.0);
		double lon = intent.getDoubleExtra("LONGITUDE", 0.0);
		ApplicationUtil.update(getApplicationContext(), lat, lon);
		
	}
}
