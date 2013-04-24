package com.example.androidobligkaraktergivende;

import com.google.android.gcm.GCMRegistrar;

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

@SuppressLint("NewApi")
public class SettingsFragment extends PreferenceFragment {

	private static final String CLEAR_ALL = "pref_key_clear_all_data";
	private static final String CLEAR_REMOTE = "pref_key_clear_remote_data";
	private static final String CLEAR_OWN = "pref_key_clear_own_data";
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
		Preference unreg = (Preference)findPreference("pref_key_unregistrer");
		Preference username = (Preference)findPreference("pref_key_username");
		
		clearAll.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				connection = new DBConnector(getActivity());
				connection.clearAllUserData();
				Toast.makeText(getActivity(), "Cleared all position data...",
						Toast.LENGTH_SHORT).show();
				connection.close();
				return true;
			}
		});
		clearOwn.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				connection = new DBConnector(getActivity());
				connection.clearOwnUserData();
				Toast.makeText(getActivity(), "Cleared all position data...",
						Toast.LENGTH_SHORT).show();
				connection.close();
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
								"Cleared all position data...",
								Toast.LENGTH_SHORT).show();
						connection.close();
						return true;
					}
				});
		unreg
		.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				
				unregTask = new AsyncTask<Void, Void, Void>(){

					@Override
					protected Void doInBackground(Void... params) {
						// TODO Auto-generated method stub
						Intent service = new Intent(getActivity(), UpdatepositionService.class);
						
						getActivity().stopService(service);
						GCMRegistrar.unregister(getActivity());
						ServerUtil.unregister(getActivity(), GCMRegistrar.getRegistrationId(getActivity()));
						Log.d("UNREG", Boolean.toString(GCMRegistrar.isRegistered(getActivity())));
						return null;
					}
					
					@Override
					protected void onPostExecute(Void result) {
						Intent regIntent = new Intent(getActivity(), RegistrationActivity.class);
						regIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						getActivity().startActivity(regIntent);
						Toast.makeText(getActivity(),
								"Cleared all position data...",
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
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// TODO Auto-generated method stub
				connection = new DBConnector(getActivity());
				if(connection.userExists(0))
					connection.updateUser(new User(0, (String)newValue, connection.getUser(0).getColor()));
				
				return false;
			}
		});
		

	}

}
