package com.example.androidobligkaraktergivende;

import static com.example.androidobligkaraktergivende.ServerUtil.SENDER_ID;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.graphics.Color;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import java.util.Random;
public class GCMIntentService extends GCMBaseIntentService {
	
	private DBConnector connection;
	public GCMIntentService() {
		
        super(SENDER_ID);
    }
	
	
	@Override
	protected void onError(Context context, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		// TODO Auto-generated method stub
		User user;
		UserPosition userPos;
		int id = Integer.parseInt(intent.getStringExtra("id"));
		String name = intent.getStringExtra("name");
		double lat = Double.parseDouble(intent.getStringExtra("lat"));
		double lon = Double.parseDouble(intent.getStringExtra("long"));
		
		connection = new DBConnector(context);
		
		if(connection.userExists(id)){
			userPos = new UserPosition(id, lon, lat);
			connection.addUserPosition(userPos);
		}
		else
		{
			user = new User(id, name, generateColor());
			userPos = new UserPosition(id, lon, lat);
			connection.addUser(user);
			connection.addUserPosition(userPos);
		}
		connection.close();
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		// TODO Auto-generated method stub    
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean reg = ServerUtil.register(context, regId, preferences.getString("pref_key_username", "username"));
		if(reg){
			Log.d("REGISTRERED", "MONGO");
			Intent i = new Intent(context, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(i);
		}
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		// TODO Auto-generated method stub
		if(GCMRegistrar.isRegisteredOnServer(context)){
			ServerUtil.unregister(context, regId);
		}
	}
	
	private int generateColor(){
		Random rand = new Random();
		int r = rand.nextInt(256);
		int g = rand.nextInt(256);
		int b = rand.nextInt(256);
		return Color.rgb(r, g, b);
		
	}

}
