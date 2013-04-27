package com.example.androidobligkaraktergivende;


import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

/**
 * Statiske variabler og metoder brukt av applikasjonen
 * @author Håvard og Jan Tore
 *
 */
public class ApplicationUtil {
	
	//Tjener url
    public static final String SERVER_URL = "http://158.39.26.242:8088/AndroidServer";
    
    //API nøkkel
    public static final String SENDER_ID = "192271238030";


    
    // Intent brukt til å sende posisjonen til kartet
    public static final String SEND_USER_POS =
            "com.example.androidobligkaraktergivende.SEND_USER_POS";

    // Intenten sine extra variabler
    public static final String EXTRA_LAT = "latitude";
    public static final String EXTRA_LONG = "longitude";
    public static final String EXTRA_USERID = "userId";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_DATE = "date";
    
    //Intent brukt til å sende status melding til brukeren
    public static final String SEND_STATUS = "com.example.androidobligkaraktergivende.SEND_STATUS";
    
    //Status og beskjed
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_MESSAGE = "message";
    
    //Antall tilatte forsøk på å koble seg til tjener
    private static final int MAX_ATTEMPTS = 5;

    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();
    
    /**
     * Registrerer klienten hos tjener
     *
     * @return om registrering var vellykket eller ikke
     */
    static boolean register(final Context context, final String regId, final String regName) {
        DBConnector con = new DBConnector(context);
        
        String serverUrl = SERVER_URL + "/register";
        Log.d("GCM", "static boolean register");
        //Gjør klar parameter som skal sendes til tjeneren
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("regName", regName);
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        
        //prøver å kontakte tjener
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            try {
            	//lagrer brukeren i lokal database
            	if(con.userExists(0)){
                	con.updateUser(new User(0, regName, Color.CYAN));
                }
                else
                {
                	con.addUser(new User(0, regName, Color.CYAN));
                }
                post(context, serverUrl, params);
                GCMRegistrar.setRegisteredOnServer(context, true);
                
                //lagrer brukeren i lokal database
                
                return true;
                
            }
            catch (IOException e) {
                //Catcher IOException hvis man ikke fikk kontakt med tjener
            	Log.d("IOEX", "IO");
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {

                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {

                    Thread.currentThread().interrupt();
                    return false;
                }
                // øker backoff 
                backoff *= 2;
            }
        }
        return false;
    }

    /**
     * avregistrerer klient fra tjener.
     */
    static void unregister(final Context context, final String regId) {

        String serverUrl = SERVER_URL + "/unregister";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        try {
            post(context,serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, false);
        } catch (IOException e) {
            

        }
    }
    /**
     * Metode som blir kjørt når server kaster deg ut.
     * @param context
     */
    static void kickedOut(final Context context){

    	//Klargjør en notification
    	NotificationManager manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = createNotification();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
				Intent service = new Intent(context, UpdatepositionService.class);

				//Stopper oppdatering
				context.stopService(service);
				GCMRegistrar.setRegisteredOnServer(context, false);
				GCMRegistrar.unregister(context);
				Log.d("UNREG", Boolean.toString(GCMRegistrar.isRegistered(context)));

				//Sender notification
				Intent intent = new Intent(context, RegistrationActivity.class);
				notification.setLatestEventInfo(context,"aMap Warning!",
						context.getText(R.string.kicked_out), PendingIntent
								.getActivity(context, 1, intent, 0));
				manager.notify(1,
						notification);
				
				//Starter Registrerings aktiviteten
				Intent regIntent = new Intent(context, RegistrationActivity.class);
				regIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				regIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(regIntent);
				
				
    }
    
    /**
     * Sender posisjonsdata til tjener.
     * @param context
     * @param lat
     * @param lon
     */
    static void update(final Context context, final double lat, double lon){
    	String serverUrl = SERVER_URL + "/update";
    	DBConnector connection = new DBConnector(context);
    	
    	//Henter seg selv fra databasen (Vil alltid være brukerid 0)
    	User user = connection.getUser(0);
    	
    	//sender med variabler, navn siden man kan bytte navn.
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", GCMRegistrar.getRegistrationId(context));
        params.put("regName", user.getName());
        params.put("regLat", Double.toString(lat));
        params.put("regLong", Double.toString(lon));
        Log.d("UPDATE", "update");
        try {
            post(context, serverUrl, params);

        } catch (IOException e) {
        	e.printStackTrace();

        }
    }

    /**
     * Sender POST forespørsel til tjener
     *
     * @param adressen
     * @param info som sendes med
     *
     * @throws IOException propagated from POST.
     */
    private static void post(final Context context, String endpoint, Map<String, String> params)
            throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        
        // lager urlen
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        
        //gjør det om til ett byte array
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
        	//Åpner tilkobling
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // Kjører Post
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // sjekker respons melding fra server
            int status = conn.getResponseCode();
            if (status != 200) {
            	if(status == 503) {
            		//Om server er full kommer man hit
            		sendStatus(context, "Server is full!", false);
            		return;
            	}
            	
              throw new IOException("Post failed with error code " + status);
            }
            else
            {
            	sendStatus(context, "Registered on server!", true);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
      }
    
    /**
     * Kringkaster en status melding som fanges opp og vises på skjerm
     * @param context
     * @param message
     * @param status
     */
    public static void sendStatus(Context context, String message, boolean status){
    	Intent intent = new Intent(SEND_STATUS);
    	intent.putExtra(EXTRA_MESSAGE, message);
    	intent.putExtra(EXTRA_STATUS, status);
    	context.sendBroadcast(intent);
    }
    
    /**
     * Kringkaster brukeren sine oppdaterte data til kartet.
     * @param context
     * @param userPos
     * @param name
     */
    public static void sendUserPos(Context context, UserPosition userPos, String name) {
        Intent intent = new Intent(SEND_USER_POS);
        intent.putExtra(EXTRA_USERID, userPos.getUserId());
        intent.putExtra(EXTRA_LAT, userPos.getLatitude());
        intent.putExtra(EXTRA_LONG, userPos.getLongitude());
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_DATE, userPos.getDate());
        Log.d("DEBUG", "sendUserPos"+ name);
        context.sendBroadcast(intent);
    }

    /**
     * Genererer en tilfeldig farge. 
     * Brukes til å indentifisere brukere på kartet.
     * @return en fargekode
     */
    public static int generateColor(){
		Random rand = new Random();
		int r = rand.nextInt(256);
		int g = rand.nextInt(256);
		int b = rand.nextInt(256);
		return Color.rgb(r, g, b);
		
	}
    
    /**
     * Lager en notification
     * @return
     */
    private static Notification createNotification() {
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_map;
		notification.when = System.currentTimeMillis();
		return notification;
	}
}
