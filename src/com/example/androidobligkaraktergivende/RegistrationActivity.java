package com.example.androidobligkaraktergivende;

import static com.example.androidobligkaraktergivende.ApplicationUtil.EXTRA_MESSAGE;
import static com.example.androidobligkaraktergivende.ApplicationUtil.EXTRA_STATUS;
import static com.example.androidobligkaraktergivende.ApplicationUtil.SENDER_ID;
import static com.example.androidobligkaraktergivende.ApplicationUtil.SEND_STATUS;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

/**
 * Registrerings aktivitet. Starter når man ikke har registrert seg hos tjener
 * 
 * @author Håvard og Jan Tore
 * 
 */
public class RegistrationActivity extends Activity {

	private AsyncTask<Void, Void, Void> registerTask; // asyncTask
	private TextView errorView; // Textview som viser errormeldinger
	ProgressDialog dia; // innlastings dialog

	/**
	 * onCreate, sjekker om man er registrert hos tjener. Vil da starte kartet.
	 * starter også en button listener
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dia = new ProgressDialog(this);
		// Sjekker om man har rett manifest og enhet til å bruke GCM
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);

		// Er man registrert starter kartet
		if (GCMRegistrar.isRegisteredOnServer(this)) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
		setContentView(R.layout.activity_register);
		final Context con = this;

		Button regButton = (Button) findViewById(R.id.register_button);
		regButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				/**
				 * Er brukernavn ikke null og man har internett vil man starte å
				 * kontakte tjener.
				 */
				errorView = (TextView) findViewById(R.id.error_view);
				EditText username = (EditText) findViewById(R.id.username_field);
				Log.d("GCM", username.getText().toString());
				if (!username.getText().toString().equals("")) {

					// Lagres brukernavn i innstillingene
					SharedPreferences pref = PreferenceManager
							.getDefaultSharedPreferences(getApplicationContext());
					pref.edit()
							.putString("pref_key_username",
									username.getText().toString()).commit();

					if (isNetworkOnline()) {

						dia.setMessage("Connecting to server...");
						dia.show();
						// registrerer hos tjener
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

	/**
	 * registrerer status Receiver.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mStatusReceiver, new IntentFilter(SEND_STATUS));
	}

	/**
	 * uregistrerer status Receiver
	 */
	@Override
	protected void onStop() {
		super.onStop();
		try {
			unregisterReceiver(mStatusReceiver);
		} catch (IllegalArgumentException ill) {
			throw ill;
		}
	}

	/**
	 * stopper dialogen
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dia.isShowing()) {
			dia.dismiss();
		}
	}

	/**
	 * Sjekker om man er registrert hos GCM, om ikke vil man sende inn
	 * registrerings id og navn til tjener og registrerer seg hos GCM.
	 * 
	 * @param regName
	 */
	private void register(final String regName) {
		final String regId = GCMRegistrar.getRegistrationId(this);
		Log.d("GCM", "register(final String regName)");
		// er regId "" kjører man registrering
		if (regId.equals("")) {
			GCMRegistrar.register(this, SENDER_ID);
		} else {
			// er man registrert starter kartet
			if (GCMRegistrar.isRegistered(this)) {
				Intent intent = new Intent(this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			} else {

				// starter async task som registrerer seg hos tjener
				final Context context = this;
				registerTask = new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {

						boolean registered = ApplicationUtil.register(context,
								regId, regName);

						if (!registered) {
							GCMRegistrar.unregister(context);
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

	/**
	 * Sjekker om man har internett tilkobling. Bruker CONNECTION_SERVICE til å
	 * hente nettverk state.
	 * 
	 * @return om man har nettverk eller ikke.
	 */
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

	/**
	 * Kringkastingsmottaker som tar imot status melding når man prøver å
	 * kontakte tjener.
	 */
	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String status = intent.getStringExtra(EXTRA_MESSAGE);
			Toast.makeText(context, status, Toast.LENGTH_LONG).show();
			if (!intent.getBooleanExtra(EXTRA_STATUS, false)) {
				GCMRegistrar.unregister(getApplicationContext());
				GCMRegistrar.setRegisteredOnServer(getApplicationContext(),
						false);
			}
		}
	};

}
