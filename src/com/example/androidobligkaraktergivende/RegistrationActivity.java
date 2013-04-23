package com.example.androidobligkaraktergivende;

import static com.example.androidobligkaraktergivende.ServerUtil.SENDER_ID;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gcm.GCMRegistrar;

public class RegistrationActivity extends Activity {

	private AsyncTask<Void, Void, Void> registerTask;
	private static final String USERNAME_ACTION = "com.example.androidobligkaraktergivende.USERNAME";
	private static final String USERNAME_MESSAGE = "username";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 GCMRegistrar.unregister(this);
		setContentView(R.layout.activity_register);
		Button regButton = (Button) findViewById(R.id.register_button);
		regButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText username = (EditText)findViewById(R.id.username_field);
				Log.d("GCM", username.getText().toString());
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				pref.edit().putString("pref_key_username", username.getText().toString()).commit();
				Log.i("PREF",pref.getString("pref_key_username", "username"));
				register(username.getText().toString());
			}
		});
	}

	private void register(final String regName) {
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, SENDER_ID);
			Log.d("GCM", "gcm reg");
		} else {
			if (GCMRegistrar.isRegistered(this)) {
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
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
							startActivity(intent);
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

}
