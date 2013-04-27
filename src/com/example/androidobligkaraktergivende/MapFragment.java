package com.example.androidobligkaraktergivende;

import static com.example.androidobligkaraktergivende.ApplicationUtil.EXTRA_DATE;
import static com.example.androidobligkaraktergivende.ApplicationUtil.EXTRA_LAT;
import static com.example.androidobligkaraktergivende.ApplicationUtil.EXTRA_LONG;
import static com.example.androidobligkaraktergivende.ApplicationUtil.EXTRA_NAME;
import static com.example.androidobligkaraktergivende.ApplicationUtil.EXTRA_USERID;
import static com.example.androidobligkaraktergivende.ApplicationUtil.SEND_USER_POS;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Kartfragmentet som viser kartet til brukeren
 * 
 * @author kakebake
 * 
 */
public class MapFragment extends Fragment {

	private GoogleMap mMap; // Instans av google maps

	private View view; // viewet

	private List<MapUser> users; // Liste med kartbrukere
	private DBConnector db; // Databasekontakt
	private CameraPosition currentPos = null; // nåværende kamera posisjon
	List<LatLng> pos;

	/**
	 * Lager viewet, og setter nåværende posisjon
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState != null) {
			currentPos = new CameraPosition(new LatLng(
					(Double) savedInstanceState.get("lat"),
					(Double) savedInstanceState.get("long")),
					(Float) savedInstanceState.get("zoom"), 0, 0);
		}
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}
		try {
			// setter layout for dette viewet
			view = inflater.inflate(R.layout.map_layout, container, false);
		} catch (InflateException e) {

		}

		return view;

	}

	/**
	 * onResume setter opp kartet hvis det er nødvendig starter receiver som tar
	 * imot posisjonsdata
	 */
	@Override
	public void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		getActivity().registerReceiver(mPosReceiver,
				new IntentFilter(SEND_USER_POS));
	}

	/**
	 * stopper receiver
	 */
	@Override
	public void onStop() {
		super.onStop();
		getActivity().unregisterReceiver(mPosReceiver);
	}

	/**
	 * Setter opp kartet hvis det trengs
	 */
	private void setUpMapIfNeeded() {
		// Sjekker om kartet er null
		if (mMap == null) {
			// henter det ifra xml id
			mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			// sjekker om man fikk tak i kartet
			if (mMap != null) {

				// sjekker om det er første gang du kjører fragmentet eller
				// ikke, altså at om du har en posisjon fra før
				if (currentPos == null) {

					setUpMap(new LatLng(68.435409, 17.434938));
				} else {
					// flytter kartet til nåværende posisjon
					mMap.moveCamera(CameraUpdateFactory
							.newCameraPosition(currentPos));
				}

			}
		} else {
			mMap.clear();
		}
		// Viser sin egen posisjon, med nøyaktighetssirkel
		mMap.setMapType(getMapType());
		mMap.setMyLocationEnabled(true);

		// Henter bruker posisjoner
		getUsers();

		// setter opp markører til hver av brukerene
		setUpMarkers();
	}

	/**
	 * Henter brukerposisjoner fra databasen.
	 */
	private void getUsers() {
		// henter liste med brukere
		db = new DBConnector(getActivity());
		ArrayList<User> usersDB = db.getAllUsers();

		users = new ArrayList<MapUser>();

		// går gjennom brukerene
		for (User mu : usersDB) {
			// henter posisjonsdata basert på brukerens id
			ArrayList<UserPosition> usersposDB = db.getUsersPositions(mu
					.getId());

			List<LatLng> latsnlongs = new ArrayList<LatLng>();
			String stamp = null;

			// går gjennom posisjonene og legger til data i MapUser listen
			for (UserPosition up : usersposDB) {
				LatLng en = new LatLng(up.getLatitude(), up.getLongitude());
				latsnlongs.add(en);
				stamp = up.getDate();
			}
			users.add(new MapUser(mu.getId(), mu.getName(), mu.getColor(),
					stamp, latsnlongs));
		}
	}

	/**
	 * Legger til brukere i listen, slik at han ikke må laste inn alle på nytt
	 * hver gang.
	 * 
	 * @param id
	 * @param stamp
	 */
	private void appendUser(int id, String stamp) {
		db = new DBConnector(getActivity());
		List<LatLng> list = new ArrayList<LatLng>();
		User u = db.getUser(id);
		MapUser user = new MapUser(id, u.getName(), u.getColor(), stamp, list);
		users.add(user);
	}

	/**
	 * flytter kameraet til innsendt posisjon
	 * 
	 * @param pos
	 */
	private void setUpMap(LatLng pos) {
		mMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(new CameraPosition(pos, 14, 0, 0)));
	}

	/**
	 * legger til kameraposisjon i savedInstanceState
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		savedInstanceState.putFloat("zoom", mMap.getCameraPosition().zoom);
		savedInstanceState.putDouble("lat",
				mMap.getCameraPosition().target.latitude);
		savedInstanceState.putDouble("long",
				mMap.getCameraPosition().target.longitude);
	}

	/**
	 * Legger ved en markør til hver person sin siste posisjon
	 */
	private void setUpMarkers() {

		// Går gjennom MapUser listen
		for (MapUser u : users) {

			// Går gjennom posisjonsdata til denne brukeren
			for (LatLng l : u.getPosList()) {
				// Markerer hver posisjon med en sirkel.
				mMap.addCircle(new CircleOptions().center(l)
						.fillColor(u.getColor()).radius(5)
						.strokeColor(Color.TRANSPARENT));
			}

			if (!u.getPosList().isEmpty()) {
				// Tegner en linje mellom alle posisjonene
				u.setMyPolyline(mMap.addPolyline(new PolylineOptions().color(u
						.getColor())));
				u.getMyPolyline().setPoints(u.getPosList());

				// Legger til markør på siste posisjonen
				if (u.getMyMarker() == null) {

					MarkerOptions mo = new MarkerOptions()
							.position(
									u.getPosList().get(
											u.getPosList().size() - 1))
							.title(u.getName()).icon(u.getBitmapDescriptor());
					u.setMyMarker(mMap.addMarker(mo));
				} else {
					u.getMyMarker().setPosition(
							u.getPosList().get(u.getPosList().size() - 1));
				}
			}
		}
	}

	/**
	 * oppdaterer posisjonen til personer på kartet
	 * 
	 * @param id
	 * @param latitude
	 * @param longitude
	 * @param stamp
	 * @param name
	 */
	public void update(int id, double latitude, double longitude, String stamp,
			String name) {
		Log.d("UPDATE", "id: " + id);
		boolean found = false;
		// Går gjennom MapUser listen, finnes brukeren allerede på kartet?
		for (MapUser a : users) {
			// hvis ja er found true.
			if (a.getId() == id)
				found = true;
		}
		// Hvis found ikke er true vil man legge til brukeren
		if (!found)
			appendUser(id, stamp);

		// går så gjennom MapUsers listen og tegner opp nye posisjoner
		for (MapUser u : users) {
			if (u.getId() == id) {
				LatLng ll = new LatLng(latitude, longitude);
				u.getPosList().add(ll);
				u.setStamp(stamp);
				u.setName(name);

				mMap.addCircle(new CircleOptions().center(ll)
						.fillColor(u.getColor()).radius(5)
						.strokeColor(Color.TRANSPARENT));

				if (u.getMyPolyline() == null) {
					u.setMyPolyline(mMap.addPolyline(new PolylineOptions()
							.color(u.getColor())));
				}
				u.getMyPolyline().setPoints(u.getPosList());

				if (u.getMyMarker() == null) {
					MarkerOptions mo = new MarkerOptions().position(ll)
							.title(name).icon(u.getBitmapDescriptor());
					u.setMyMarker(mMap.addMarker(mo));
				} else {
					u.getMyMarker().setTitle(name);
					u.getMyMarker().setPosition(ll);
				}
			}
		}
	}

	/**
	 * BroadcastReceiver som tar imot posisjonsdata.
	 */
	private final BroadcastReceiver mPosReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int id = intent.getIntExtra(EXTRA_USERID, -1);
			double lat = intent.getDoubleExtra(EXTRA_LAT, 0.0);
			double lng = intent.getDoubleExtra(EXTRA_LONG, 0.0);
			String stamp = intent.getStringExtra(EXTRA_DATE);
			String name = intent.getStringExtra(EXTRA_NAME);
			// oppdaterer kartet
			update(id, lat, lng, stamp, name);
		}
	};

	/**
	 * Henter ut valgt kart type fra innstillingene.
	 * 
	 * @return valgt kart id
	 */
	private int getMapType() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		return Integer.parseInt(pref.getString("pref_key_map_type", "1"));
	}

	/**
	 * Metode som blir kalt av MainActivity når man klikker på en person, og man
	 * blir sendt til personens posisjon
	 * 
	 * @param id
	 *            Brukerens id
	 */
	public void goToUserPos(int id) {

		setUpMapIfNeeded();
		db = new DBConnector(getActivity());
		UserPosition pos = db.getLastUserPos(id);
		if (pos != null) {
			LatLng newPos = new LatLng(pos.getLatitude(), pos.getLongitude());
			setUpMap(newPos);
		}
	}

}

/**
 * Klasse laget for å samle data man trenger til å tegne opp kartet.
 * 
 * @author Håvard og Jan Tore
 * 
 */
class MapUser {
	private int id; // Bruker id
	private Marker myMarker; // markør
	private Polyline myPolyline; // linja som går gjennom posisjonene
	private List<LatLng> posList; // liste med posisjoner
	private int color; // farge
	private float[] hsvColor = { 0, 0, 0 }; // farge i hsv format
	private String stamp; // tidsstempel
	private String name; // navn
	private BitmapDescriptor bitmapDescriptor; // til å forandre farge på

	/**
	 * Kontruktør
	 * 
	 * @param _id
	 *            bruker id
	 * @param _name
	 *            bruker navn
	 * @param _color
	 *            farge
	 * @param _stamp
	 *            tidsstempel
	 * @param _posList
	 *            posisjonsliste
	 */
	public MapUser(int _id, String _name, int _color, String _stamp,
			List<LatLng> _posList) {
		id = _id;
		setName(_name);
		setColor(_color);
		setStamp(_stamp);
		setPosList(_posList);
		setBitmapDescriptor(BitmapDescriptorFactory
				.defaultMarker(getHsvColor()[0]));
	}

	/**
	 * get metode til posisjonslista
	 * 
	 * @return - Liste med posisjoner
	 */
	public List<LatLng> getPosList() {
		return posList;
	}

	/**
	 * set metode til posisjonslista
	 * 
	 * @param posList
	 *            - Liste med posisjoner
	 */
	public void setPosList(List<LatLng> posList) {
		this.posList = posList;
	}

	/**
	 * henter farge
	 * 
	 * @return farge
	 */
	public int getColor() {
		return color;
	}

	/**
	 * setter farge, hsv farge og bitmapdescriptor
	 * 
	 * @param color
	 *            - farge
	 */
	public void setColor(int color) {
		this.color = color;
		Color.colorToHSV(getColor(), getHsvColor());
		setBitmapDescriptor(BitmapDescriptorFactory
				.defaultMarker(getHsvColor()[0]));
	}

	/**
	 * henter linja mellom posisjonene
	 * 
	 * @return posisjons linja
	 */
	public Polyline getMyPolyline() {
		return myPolyline;
	}

	/**
	 * setter posisjonslina
	 * 
	 * @param myPolyline
	 */
	public void setMyPolyline(Polyline myPolyline) {
		this.myPolyline = myPolyline;
	}

	/**
	 * henter markør
	 * 
	 * @return markør
	 */
	public Marker getMyMarker() {
		return myMarker;
	}

	/**
	 * setter markør
	 * 
	 * @param myMarker
	 */
	public void setMyMarker(Marker myMarker) {
		this.myMarker = myMarker;
	}

	/**
	 * setter tidsstempel
	 * 
	 * @param stamp
	 */
	public void setStamp(String stamp) {
		this.stamp = stamp;
	}

	/**
	 * henter navn på brukeren
	 * 
	 * @return navn
	 */
	public String getName() {
		return name;
	}

	/**
	 * setter navn
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * henter hsv fargen
	 * 
	 * @return hsv farge
	 */
	public float[] getHsvColor() {
		return hsvColor;
	}

	/**
	 * henter id
	 * 
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Henter bitmapDescriptor
	 * 
	 * @return
	 */
	public BitmapDescriptor getBitmapDescriptor() {
		return bitmapDescriptor;
	}

	/**
	 * Setter bitmapdescriptor
	 * 
	 * @param bitmapDescriptor
	 */
	public void setBitmapDescriptor(BitmapDescriptor bitmapDescriptor) {
		this.bitmapDescriptor = bitmapDescriptor;
	}
}