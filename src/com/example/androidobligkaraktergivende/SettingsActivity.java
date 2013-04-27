package com.example.androidobligkaraktergivende;

import static com.example.androidobligkaraktergivende.ApplicationUtil.generateColor;
import static com.example.androidobligkaraktergivende.PreferenceUtil.CLEAR_ALL;
import static com.example.androidobligkaraktergivende.PreferenceUtil.CLEAR_OWN;
import static com.example.androidobligkaraktergivende.PreferenceUtil.CLEAR_REMOTE;
import static com.example.androidobligkaraktergivende.PreferenceUtil.PREF_CHANGE_COLOR;
import static com.example.androidobligkaraktergivende.PreferenceUtil.PREF_RANDOM_COLOR;
import static com.example.androidobligkaraktergivende.PreferenceUtil.PREF_TRACKING;
import static com.example.androidobligkaraktergivende.PreferenceUtil.PREF_UNREGISTER;
import static com.example.androidobligkaraktergivende.PreferenceUtil.PREF_USERNAME;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

/**
 * Innstillings aktivitet, bruker depricated metode pga tilpassing til version
 * 2.2
 * 
 * @author kakebake
 * 
 */
@SuppressLint("NewApi")
public class SettingsActivity extends PreferenceActivity {

	private DBConnector connection; // Database kontakt
	private AsyncTask<Void, Void, Void> unregTask; // asyncTask

	/**
	 * onCreate som enten henter innstillinger fra xml eller laster inn
	 * SettingsFragment. dette er basert på api nivå.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayShowHomeEnabled(false);
		}
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource(R.xml.settings);
			preferenceListener();
		} else {
			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new SettingsFragment())
					.commit();
		}

	}

	/**
	 * Metode som listner på forskjellige innstillingene.
	 */
	@SuppressWarnings("deprecation")
	private void preferenceListener() {
		Preference clearAll = (Preference) findPreference(CLEAR_ALL);
		Preference clearOwn = (Preference) findPreference(CLEAR_OWN);
		Preference clearRemote = (Preference) findPreference(CLEAR_REMOTE);
		Preference unreg = (Preference) findPreference(PREF_UNREGISTER);
		Preference username = (Preference) findPreference(PREF_USERNAME);
		Preference tracking = (Preference) findPreference(PREF_TRACKING);
		Preference randomColor = (Preference) findPreference(PREF_RANDOM_COLOR);
		Preference yourColor = (Preference) findPreference(PREF_CHANGE_COLOR);

		final Context context = this;

		// Oppdaterer databasen med nye farger etter å ha klikket på random
		// farge knappen.
		randomColor
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						Toast.makeText(context,
								getString(R.string.randomize_color),
								Toast.LENGTH_SHORT).show();
						connection = new DBConnector(context);
						ArrayList<User> users = connection.getAllUsers();
						for (User u : users) {
							if (u.getId() != 0) {
								u.setColor(generateColor());
								connection.updateUser(u);
							}
						}

						return true;
					}
				});

		/**
		 * Oppdaterer databasen med den fargen du velger fra lista.
		 */
		yourColor
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {

						connection = new DBConnector(context);
						User u = connection.getUser(0);
						int y = Integer.parseInt((String) newValue);

						u.setColor(y);
						connection.updateUser(u);
						return true;
					}
				});

		/**
		 * Sletter all posisjonsdata og andre brukere fra databasen
		 */
		clearAll.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {

				connection = new DBConnector(context);
				connection.clearAllUserData();
				Toast.makeText(context, getString(R.string.clear_all_label),
						Toast.LENGTH_SHORT).show();
				return true;
			}
		});

		/**
		 * Sletter egne posisjonene
		 */
		clearOwn.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {

				connection = new DBConnector(context);
				connection.clearOwnUserData();
				Toast.makeText(context, getString(R.string.clear_own_label),
						Toast.LENGTH_SHORT).show();
				return true;
			}
		});

		/**
		 * Sletter all posisjonsdata bortsett fra dine.
		 */
		clearRemote
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						connection = new DBConnector(context);
						connection.clearRemoteUserData();
						Toast.makeText(context,
								getString(R.string.clear_remote_data),
								Toast.LENGTH_SHORT).show();
						return true;
					}
				});

		/**
		 * Avregistrerer deg fra tjener.
		 */
		unreg.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {

				// Starter en asynctask
				unregTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {

						// Avregistrerer seg
						Intent service = new Intent(context,
								UpdatepositionService.class);

						context.stopService(service);
						GCMRegistrar.unregister(context);
						ApplicationUtil.unregister(context,
								GCMRegistrar.getRegistrationId(context));
						Log.d("UNREG", Boolean.toString(GCMRegistrar
								.isRegistered(context)));
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						// Etter at man er avregistrert sendes man ut av kartet
						// og tilbake til registrerings aktiviteten
						Intent regIntent = new Intent(context,
								RegistrationActivity.class);
						regIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						context.startActivity(regIntent);
						Toast.makeText(context,
								getString(R.string.unregister_application),
								Toast.LENGTH_SHORT).show();
						unregTask = null;
					}
				};
				unregTask.execute(null, null, null);

				return true;
			}
		});
		
		/**
		 * Oppdaterer databasen med nytt brukernavn
		 */
		username.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				
				connection = new DBConnector(context);
				if (connection.userExists(0))
					connection.updateUser(new User(0, (String) newValue,
							connection.getUser(0).getColor()));

				return true;
			}
		});
		
		/**
		 * Slår av og på sporing
		 */
		tracking.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				
				if ((Boolean) newValue)
					Toast.makeText(context, getString(R.string.start_tracking),
							Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(context, getString(R.string.stop_tracking),
							Toast.LENGTH_SHORT).show();
				return true;
			}
		});

	}

}
