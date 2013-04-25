package com.example.androidobligkaraktergivende;

import static com.example.androidobligkaraktergivende.PreferenceUtil.PREF_DISTANCE;
import static com.example.androidobligkaraktergivende.PreferenceUtil.PREF_INTERVAL;
import static com.example.androidobligkaraktergivende.PreferenceUtil.PREF_TRACKING;
import static com.example.androidobligkaraktergivende.ServerUtil.sendUserPos;
import java.util.Date;

import com.google.android.gms.maps.SupportMapFragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity {

	private AsyncTask<Void, Void, Void> task;
	private Location currentLocation = null;
	private LocationManager locationManager;
	private DBConnector connection;
	private int interval = 0, distance = 0;
	private MapFragment map;
	private boolean tracking = false;
	private String bestProvider;
	private PeopleFragment peopleFragment;
	private static int currentFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferences();

		setContentView(R.layout.activity_main);
		if (savedInstanceState != null) {
			map = (MapFragment) getSupportFragmentManager().findFragmentByTag(
					"map");
			peopleFragment = (PeopleFragment) getSupportFragmentManager()
					.findFragmentByTag("people");
			

		} else {
			currentFragment = 1;
			map = new MapFragment();
			peopleFragment = new PeopleFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, map, "map")
					.add(R.id.fragment_container, peopleFragment, "people").commit();
		}
		selectFragment();
		
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
		

		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		setCriteria();
		if (tracking) {

			locationManager.requestLocationUpdates(bestProvider, interval, distance,
					listener);
		}
		else
		{
			locationManager.removeUpdates(listener);
		}
	}
	

	@Override
	protected void onStop(){
		super.onStop();
		Intent service = new Intent(getApplicationContext(),
				UpdatepositionService.class);
		stopService(service);
		locationManager.removeUpdates(listener);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent service = new Intent(getApplicationContext(),
				UpdatepositionService.class);
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
			currentFragment = 1;
			selectFragment();
			return true;
		case R.id.people_button_id:
			// Kaller på metode som starter en alarmManager og servicen
			// peopleFragment = new PeopleFragment();
			currentFragment = 2;
			peopleFragment.initPeople();
			selectFragment();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void setCriteria() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(true);
		bestProvider = locationManager.getBestProvider(criteria, true);
	}

	private void getPreferences() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		distance = Integer.parseInt(pref.getString(PREF_DISTANCE, "0"));
		interval = Integer.parseInt(pref.getString(PREF_INTERVAL, "0"));
		tracking = pref.getBoolean(PREF_TRACKING, false);
		Log.d("PREF", "Pref ok" + interval);
	}
	
	private void selectFragment(){
		switch (currentFragment) {
		case 1:
			getSupportFragmentManager().beginTransaction().hide(peopleFragment)
			.show(map)
			.commit();
			
			break;
		case 2:
			getSupportFragmentManager().beginTransaction().hide(map)
			.show(peopleFragment)
			.commit();
			break;
		default:
			break;

		}
	}
	
	LocationListener listener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status,
				Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			Log.d("LOC", "loc ok");
			currentLocation = location;
			connection = new DBConnector(getApplicationContext());
			UserPosition pos = new UserPosition(0, location.getLatitude(),
					location.getLongitude());
			pos.setDate(new Date().toString());
			connection.addUserPosition(pos);
			String name = connection.getUser(0).getName();
			Intent service = new Intent(getApplicationContext(),
					UpdatepositionService.class);
			service.putExtra("LONGITUDE", pos.getLongitude());
			service.putExtra("LATITUDE", pos.getLatitude());

			startService(service);
			Toast.makeText(getApplicationContext(),
					"lat:" + location.getLatitude(), Toast.LENGTH_SHORT)
					.show();
			sendUserPos(getApplicationContext(), pos, name);
		}
	};

}
