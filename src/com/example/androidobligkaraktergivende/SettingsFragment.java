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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

/**
 * Innstillings fragment, brukes av android enheter med api nivå fra 11 og
 * oppover. Dokumentasjon er samme som SettingsActivity så hendviser derfor dit.
 * 
 * @author Håvard og Jan Tore
 * 
 */
@SuppressLint("NewApi")
public class SettingsFragment extends PreferenceFragment {

	private DBConnector connection;
	private AsyncTask<Void, Void, Void> unregTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
		preferenceListener();
	}

	private void preferenceListener() {
		Preference clearAll = (Preference) findPreference(CLEAR_ALL);
		Preference clearOwn = (Preference) findPreference(CLEAR_OWN);
		Preference clearRemote = (Preference) findPreference(CLEAR_REMOTE);
		Preference unreg = (Preference) findPreference(PREF_UNREGISTER);
		Preference username = (Preference) findPreference(PREF_USERNAME);
		Preference tracking = (Preference) findPreference(PREF_TRACKING);
		Preference randomColor = (Preference) findPreference(PREF_RANDOM_COLOR);
		Preference yourColor = (Preference) findPreference(PREF_CHANGE_COLOR);

		randomColor
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						// TODO Auto-generated method stub
						Toast.makeText(getActivity(),
								getString(R.string.randomize_color),
								Toast.LENGTH_SHORT).show();
						connection = new DBConnector(getActivity());
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

		yourColor
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						// TODO Auto-generated method stub
						connection = new DBConnector(getActivity());
						User u = connection.getUser(0);
						int y = Integer.parseInt((String) newValue);

						u.setColor(y);
						connection.updateUser(u);
						return true;
					}
				});

		clearAll.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				connection = new DBConnector(getActivity());
				connection.clearAllUserData();
				Toast.makeText(getActivity(),
						getString(R.string.clear_all_label), Toast.LENGTH_SHORT)
						.show();
				return true;
			}
		});
		clearOwn.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				connection = new DBConnector(getActivity());
				connection.clearOwnUserData();
				Toast.makeText(getActivity(),
						getString(R.string.clear_own_label), Toast.LENGTH_SHORT)
						.show();
				return true;
			}
		});
		clearRemote
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						// TODO Auto-generated method stub
						connection = new DBConnector(getActivity());
						connection.clearRemoteUserData();
						Toast.makeText(getActivity(),
								getString(R.string.clear_remote_data),
								Toast.LENGTH_SHORT).show();
						return true;
					}
				});
		unreg.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub

				unregTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						// TODO Auto-generated method stub
						Intent service = new Intent(getActivity(),
								UpdatepositionService.class);

						getActivity().stopService(service);
						GCMRegistrar.unregister(getActivity());
						ApplicationUtil.unregister(getActivity(),
								GCMRegistrar.getRegistrationId(getActivity()));
						Log.d("UNREG", Boolean.toString(GCMRegistrar
								.isRegistered(getActivity())));
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						Intent regIntent = new Intent(getActivity(),
								RegistrationActivity.class);
						regIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						getActivity().startActivity(regIntent);
						Toast.makeText(getActivity(),
								getString(R.string.unregister_application),
								Toast.LENGTH_SHORT).show();
						unregTask = null;
					}
				};
				unregTask.execute(null, null, null);

				return true;
			}
		});
		username.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				// TODO Auto-generated method stub
				connection = new DBConnector(getActivity());
				if (connection.userExists(0))
					connection.updateUser(new User(0, (String) newValue,
							connection.getUser(0).getColor()));

				return true;
			}
		});
		tracking.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				// TODO Auto-generated method stub
				if ((Boolean) newValue)
					Toast.makeText(getActivity(),
							getString(R.string.start_tracking),
							Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getActivity(),
							getString(R.string.stop_tracking),
							Toast.LENGTH_SHORT).show();
				return true;
			}
		});

	}

}
