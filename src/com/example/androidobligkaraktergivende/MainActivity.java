package com.example.androidobligkaraktergivende;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity {

	private AsyncTask<Void, Void, Void> task;
	private Location currentLocation = null;
	private LocationManager locationManager;
	private DBConnector connection;
	private int interval = 0, distance = 0;
	private static final String PREF_DISTANCE = "pref_key_distance_limit";
	private static final String PREF_INTERVAL = "pref_key_interval_limit";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferences();
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, distance, interval, listener);

		setContentView(R.layout.activity_main);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, new MapFragment()).commit();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayShowHomeEnabled(false);
		}
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	@Override
	protected void onResume() {
		super.onResume();
		getPreferences();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, distance, interval, listener);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent service = new Intent(getApplicationContext(), UpdatepositionService.class);
		stopService(service);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings_id:
			// starter settings aktivitet
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);

			return true;
		case R.id.map_button_id:
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, new MapFragment())
					.commit();
			return true;
		case R.id.people_button_id:
			// Kaller på metode som starter en alarmManager og servicen
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, new PeopleFragment())
					.commit();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	LocationListener listener = new LocationListener() {
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			currentLocation = location;
			connection = new DBConnector(getApplicationContext());
			UserPosition pos = new UserPosition(0, location.getLongitude(), location.getLatitude());
			connection.addUserPosition(pos);
			
			Intent service = new Intent(getApplicationContext(), UpdatepositionService.class);
			service.putExtra("LONGITUDE", pos.getLongitude());
			service.putExtra("LATITUDE", pos.getLatitude());
			
			startService(service);
		}
	};

	private void getPreferences(){
		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		distance = Integer.parseInt(pref.getString(PREF_DISTANCE, "0"));
		interval = Integer.parseInt(pref.getString(PREF_INTERVAL, "0"));
		
	}

}
