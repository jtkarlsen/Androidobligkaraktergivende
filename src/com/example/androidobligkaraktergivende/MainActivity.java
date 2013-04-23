package com.example.androidobligkaraktergivende;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;


@SuppressLint("NewApi") 
public class MainActivity extends FragmentActivity{
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		getSupportFragmentManager().beginTransaction()
    	.replace(R.id.fragment_container, new MapFragment())
    	.commit();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
		{
			getActionBar().setDisplayShowHomeEnabled(false);
		}
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
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

}
