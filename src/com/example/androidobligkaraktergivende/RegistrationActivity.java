package com.example.androidobligkaraktergivende;

import static com.example.androidobligkaraktergivende.ServerUtil.SENDER_ID;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;

public class RegistrationActivity extends Activity {

	private AsyncTask<Void, Void, Void> registerTask;
	private TextView errorView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (GCMRegistrar.isRegisteredOnServer(this)) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}

		setContentView(R.layout.activity_register);
		Button regButton = (Button) findViewById(R.id.register_button);
		regButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				errorView = (TextView) findViewById(R.id.error_view);
				EditText username = (EditText) findViewById(R.id.username_field);
				Log.d("GCM", username.getText().toString());
				if (!username.getText().toString().equals("")) {
					SharedPreferences pref = PreferenceManager
							.getDefaultSharedPreferences(getApplicationContext());
					pref.edit()
							.putString("pref_key_username",
									username.getText().toString()).commit();
					Log.i("PREF",
							pref.getString("pref_key_username", "username"));
					if (isNetworkOnline()) {
						register(username.getText().toString());
					} else {
						errorView.setText(getString(R.string.network_error));
					}

				} else {
					errorView.setText(getString(R.string.empty_username_error));
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	private void register(final String regName) {
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, SENDER_ID);
			Log.d("GCM", "gcm reg");
		} else {
			if (GCMRegistrar.isRegistered(this)) {
				Intent intent = new Intent(this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				Log.d("GCM", "allerede registrert");
			} else {

				final Context context = this;
				Log.d("GCM", "Starter reg");
				registerTask = new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {

						boolean registered = ServerUtil.register(context,
								regId, regName);

						if (!registered) {
							GCMRegistrar.unregister(context);
							Log.d("GCM", "ikke registrert");
						} else {
							Intent intent = new Intent(context,
									MainActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						registerTask = null;
					}
				};
				registerTask.execute(null, null, null);

			}
		}
	}

	public boolean isNetworkOnline() {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null
					&& netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if (netInfo != null
						&& netInfo.getState() == NetworkInfo.State.CONNECTED)
					status = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return status;

	}

}
