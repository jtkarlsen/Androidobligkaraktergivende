package com.example.androidobligkaraktergivende;

import static com.example.androidobligkaraktergivende.ApplicationUtil.sendUserPos;
import static com.example.androidobligkaraktergivende.PreferenceUtil.PREF_DISTANCE;
import static com.example.androidobligkaraktergivende.PreferenceUtil.PREF_INTERVAL;
import static com.example.androidobligkaraktergivende.PreferenceUtil.PREF_TRACKING;

import java.util.Date;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;

import com.example.androidobligkaraktergivende.PeopleFragment.OnUserSelectedListener;

/**
 * Hovedaktivitet. Tar seg av håndtering av de andre fragmentene. Han har en
 * onLocationChangedListener som henter nye posisjoner.
 * 
 * @author Håvard og Jan Tore
 * 
 */
@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements
		OnUserSelectedListener {

	private LocationManager locationManager; // loationmanager (henter posisjon)
	private DBConnector connection; // database kontakt
	private int interval = 0, distance = 0; // grenseverdier
	private MapFragment map; // Instans av kartet
	private boolean tracking = false; // sjekk om man skal starte sporing
	private String bestProvider; // beste lokasjons provider
	private PeopleFragment peopleFragment; // Instans av person fragmentet
	private EventFragment eventFragment; // Instans av logg fragmentet
	private static int currentFragment; // gjeldene fragment
	private boolean landscape; // er man i landscape eller portrait

	/**
	 * onCreate. Setter opp fragmentene.
	 */
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
			eventFragment = (EventFragment) getSupportFragmentManager()
					.findFragmentByTag("event");

		} else {
			currentFragment = 1;
			map = new MapFragment();
			peopleFragment = new PeopleFragment();
			eventFragment = new EventFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, map, "map")
					.add(R.id.fragment_container, peopleFragment, "people")
					.add(R.id.fragment_container, eventFragment, "event")
					.commit();
		}
		selectFragment();

	}

	/**
	 * Laster inn actionbaren.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayShowHomeEnabled(false);
		}
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	/**
	 * onResume, her lastes innstillingene inn, og starter lokasjons
	 * oppdatering.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		getPreferences();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			landscape = true;
		} else
			landscape = false;

		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		setCriteria();
		if (tracking) {

			locationManager.requestLocationUpdates(bestProvider, interval,
					distance, listener);
		} else {
			locationManager.removeUpdates(listener);
		}
	}

	/**
	 * onStop, stopper servicen som sender posisjon til tjener. stopper
	 * lokasjons listener
	 */
	@Override
	protected void onStop() {
		super.onStop();
		Intent service = new Intent(getApplicationContext(),
				UpdatepositionService.class);
		stopService(service);
		locationManager.removeUpdates(listener);
	}

	/**
	 * onDestroy, stopper servicen om det ikke er gjort.
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent service = new Intent(getApplicationContext(),
				UpdatepositionService.class);
		stopService(service);
	}

	/**
	 * Håndterer actionbar menyvalg
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings_id:
			// starter settings aktivitet
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);

			return true;
		case R.id.map_button_id:
			// Åpner kartet
			currentFragment = 1;
			selectFragment();
			return true;
		case R.id.people_button_id:
			// Åpner listen av personer
			currentFragment = 2;
			peopleFragment.initPeople();
			selectFragment();

			return true;
		case R.id.log_button_id:
			// Åpner loggen
			currentFragment = 3;
			eventFragment.initEvents();
			selectFragment();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Setter opp kriterier for å få den beste location provideren.
	 */
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

	/**
	 * Henter innstillingene
	 */
	private void getPreferences() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		// Avstand mellom hver lokasjons oppdatering
		distance = Integer.parseInt(pref.getString(PREF_DISTANCE, "0"));

		// Tidsintervall mellom hver lokasjons oppdatering
		interval = Integer.parseInt(pref.getString(PREF_INTERVAL, "0"));

		// Er sporing startet eller ikke
		tracking = pref.getBoolean(PREF_TRACKING, false);
	}

	/**
	 * Gjemmer og viser fragmenter etter hvilket man velger
	 */
	private void selectFragment() {
		switch (currentFragment) {
		case 1:
			getSupportFragmentManager().beginTransaction().hide(peopleFragment)
					.hide(eventFragment)

					.show(map).commit();

			break;
		case 2:
			/**
			 * Om man er i landscape har man muligheten til å få en liste med
			 * personer på siden av kartet, og det legges til en animasjon.
			 */
			if (landscape) {
				if (getSupportFragmentManager().findFragmentByTag("people")
						.isHidden()) {
					/**Henter størrelsen på skjermen og setter størrelsen på persons 
					 * fragmentet i forhold til dette
					*/
					currentFragment = 1;
					Display display = getWindowManager().getDefaultDisplay();
					Point size = new Point();
					display.getSize(size);
					int width = size.x;
					ListView view = (ListView) findViewById(android.R.id.list);
					LayoutParams params = (LayoutParams) view.getLayoutParams();
					params.width = width - 700;
					
					//Gjør bakgrunnen litt transparent
					view.setBackgroundColor(Color.argb(150, 255, 255, 255));
					view.setLayoutParams(params);
					getSupportFragmentManager()
							.beginTransaction()
							.setCustomAnimations(android.R.anim.slide_in_left,
									android.R.anim.slide_out_right).show(map)
							.hide(eventFragment).show(peopleFragment).commit();

				} else {
					getSupportFragmentManager().beginTransaction()
							.setCustomAnimations(0, android.R.anim.fade_out)
							.show(map).hide(eventFragment).hide(peopleFragment)
							.commit();
					currentFragment = 1;
				}
			} else {
				getSupportFragmentManager()
						.beginTransaction()
						.setCustomAnimations(android.R.anim.slide_in_left,
								android.R.anim.slide_out_right).hide(map)
						.hide(eventFragment).show(peopleFragment).commit();
			}
			break;
		case 3:
			getSupportFragmentManager().beginTransaction().hide(peopleFragment)
					.hide(map).show(eventFragment).commit();
			break;
		default:
			break;

		}
	}

	/**
	 * LocationListener, kalles når posisjonen oppdaterer seg utenfor satte
	 * grenseverdier.
	 */
	LocationListener listener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onLocationChanged(Location location) {

			//nytt instans av databasen, og lagrer den nye posisjonen
			connection = new DBConnector(getApplicationContext());
			UserPosition pos = new UserPosition(0, location.getLatitude(),
					location.getLongitude());
			pos.setDate(new Date().toString());
			connection.addUserPosition(pos);
			String name = connection.getUser(0).getName();
			
			//starter servicen som sender posisjon til tjener
			Intent service = new Intent(getApplicationContext(),
					UpdatepositionService.class);
			service.putExtra("LONGITUDE", pos.getLongitude());
			service.putExtra("LATITUDE", pos.getLatitude());

			startService(service);
			
			//Sender en kringkastning som kartet fanger opp
			sendUserPos(getApplicationContext(), pos, name);
		}
	};

	/**
	 * Implementert metode fra PeopleFragment
	 * når noen trykker på en person vil man gå til dens posisjon.
	 */
	public void onUserSelected(int id) {
		

		
		MapFragment mapFrag = (MapFragment) getSupportFragmentManager()
				.findFragmentByTag("map");

		//Sjekker om karter er synlig
		if (mapFrag.isVisible()) {
			//går da direkte til posisjonen
			mapFrag.goToUserPos(id);

		} else {
			// Hvis ikke åpnes kartet før man går til posisjonen
			currentFragment = 1;
			getSupportFragmentManager()
					.beginTransaction()
					.setCustomAnimations(android.R.anim.slide_in_left,
							android.R.anim.slide_out_right).show(map)
					.hide(eventFragment).hide(peopleFragment).commit();
			map.goToUserPos(id);
		}
	}

}
