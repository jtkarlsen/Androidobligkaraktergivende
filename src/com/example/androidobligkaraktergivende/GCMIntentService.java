package com.example.androidobligkaraktergivende;

import static com.example.androidobligkaraktergivende.ApplicationUtil.SENDER_ID;
import static com.example.androidobligkaraktergivende.ApplicationUtil.generateColor;
import static com.example.androidobligkaraktergivende.ApplicationUtil.sendUserPos;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * GCM IntentService, tar imot intents fra GCM server.
 * @author kakebake
 *
 */
public class GCMIntentService extends GCMBaseIntentService {

	private DBConnector connection;	//database kontakt
	
	/**
	 * Kontruktør, som sender api nøkkel til super
	 */
	public GCMIntentService() {

		super(SENDER_ID);

	}

	/**
	 * Ikke implementert
	 */
	@Override
	protected void onError(Context context, String arg1) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Tar imot posisjoner fra tjener, tar også imot en string
	 * som sier at man er blitt kastet ut av serveren.
	 */
	@Override
	protected void onMessage(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		//Sjekker om serveren sier at man er kastet ut eller om det er en normal posisjons oppdatering
		if (intent.getStringExtra("unregister") != null && intent.getStringExtra("unregister").equals("true")) {

			//Avregistrerer fra GCM.
			ApplicationUtil.kickedOut(context);
			
		} else {
			User user;
			UserPosition userPos;
			int id = Integer.parseInt(intent.getStringExtra("id"));
			String name = intent.getStringExtra("name");
			double lat = Double.parseDouble(intent.getStringExtra("lat"));
			double lon = Double.parseDouble(intent.getStringExtra("long"));
			String date = intent.getStringExtra("updated");
			connection = new DBConnector(context);
			
			//Sjekker om brukeren finnes, oppdaterer om den gjør det. Elles opprettes en ny en.
			if (connection.userExists(id)) {
				userPos = new UserPosition(id, lat, lon);
				userPos.setDate(date);
				connection.addUserPosition(userPos);
				connection.updateUser(new User(id, name, connection.getUser(id)
						.getColor()));
			} else {
				user = new User(id, name, generateColor());
				userPos = new UserPosition(id, lat, lon);
				connection.addUser(user);
				connection.addUserPosition(userPos);
			}
			//Sender data til kartet
			sendUserPos(context, userPos, name);
		}
	}
	
	/**
	 * Tar imot at man registrerer seg til GCM, vil da registrere
	 * seg til tjeneren.
	 */
	@Override
	protected void onRegistered(Context context, String regId) {
		// TODO Auto-generated method stub
		Log.d("GCM", "onRegistered!");
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		//registrerer, med bool resultat
		boolean reg = ApplicationUtil.register(context, regId,
				preferences.getString("pref_key_username", "username"));
		
		//Om man blir registrert starter man MainActivity
		if (reg) {
			Intent i = new Intent(context, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(i);
		}
	}

	/**
	 * Når man avregistrerer seg manualt fra klient
	 * vil denne bli kalt og man kjører ApplicationUtil.unregister,
	 * som vil gjøre at man blir avregistrert fra tjener
	 */
	@Override
	protected void onUnregistered(Context context, String regId) {
		// TODO Auto-generated method stub
		if (GCMRegistrar.isRegisteredOnServer(context)) {
			ApplicationUtil.unregister(context, regId);
		}
	}

}
