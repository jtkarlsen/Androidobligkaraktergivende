package com.example.androidobligkaraktergivende;

import static com.example.androidobligkaraktergivende.ServerUtil.EXTRA_DATE;
import static com.example.androidobligkaraktergivende.ServerUtil.EXTRA_LAT;
import static com.example.androidobligkaraktergivende.ServerUtil.EXTRA_LONG;
import static com.example.androidobligkaraktergivende.ServerUtil.EXTRA_NAME;
import static com.example.androidobligkaraktergivende.ServerUtil.EXTRA_USERID;
import static com.example.androidobligkaraktergivende.ServerUtil.SEND_USER_POS;
import static com.example.androidobligkaraktergivende.ServerUtil.sendUserPos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapFragment extends Fragment {

	/**
	 * Note that this may be null if the Google Play services APK is not
	 * available.
	 */
	private GoogleMap mMap;
	private LocationManager lm;

	private View view;
	private Marker myMarker;
	private List<MapUser> users;
	private DBConnector db;

	List<LatLng> pos;
	private boolean aktiv;
	private LocationManager locationManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		


		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}
		try {
			// Inflate the layout for this fragment
			view = inflater.inflate(R.layout.map_layout, container, false);
		} catch (InflateException e) {
			/* map is already there, just return view as it is */
			Log.d("INFLATE", e.getMessage());
		}
		Button newPos = (Button) view.findViewById(R.id.simulate_move);
		locationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		final Location hin = new Location("HIN");
		hin.setLatitude(68.435961);
		hin.setLongitude(17.435303);
		newPos.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				hin.setLatitude(hin.getLatitude() + 0.002000);
				hin.setLongitude(hin.getLongitude() + 0.000100);

				db = new DBConnector(getActivity());
				UserPosition pos = new UserPosition(0, hin.getLatitude(), hin
						.getLongitude());
				pos.setDate(new Date().toString());
				db.addUserPosition(pos);

				Intent service = new Intent(getActivity(),
						UpdatepositionService.class);
				service.putExtra("LONGITUDE", pos.getLongitude());
				service.putExtra("LATITUDE", pos.getLatitude());
				SharedPreferences pref = PreferenceManager
						.getDefaultSharedPreferences(getActivity());

				sendUserPos(getActivity(), pos,
						pref.getString("pref_key_username", "username"));
				getActivity().startService(service);
			}
		});
		return view;

	}

	@Override
	public void onResume() {
		super.onResume();
		// In case Google Play services has since become available.
		setUpMapIfNeeded();
		getActivity().registerReceiver(mHandleMessageReceiver,
				new IntentFilter(SEND_USER_POS));
	}
	@Override
	public void onStop(){
		super.onStop();
		getActivity().unregisterReceiver(mHandleMessageReceiver);
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
				// Enabling MyLocation Layer of Google Map

				// mMap.setMapType(mMap.MAP_TYPE_HYBRID);
				// Setting event handler for location change
				// mMap.setOnMyLocationChangeListener(this);
			}
		} else {
			mMap.clear();
		}
		mMap.setMyLocationEnabled(true);
		getUsers();
		setUpMarkers();
	}

	private void getUsers() {
		// TODO: Liste ut brukere fra databasen
		db = new DBConnector(getActivity());
		ArrayList<User> usersDB = db.getAllUsers();

		users = new ArrayList<MapUser>();
		for (User mu : usersDB) {
			// TODO: hente ut brukerposisjoner for brukeren
			ArrayList<UserPosition> usersposDB = db.getUsersPositions(mu
					.getId());

			List<LatLng> latsnlongs = new ArrayList<LatLng>();
			String stamp = null;
			for (UserPosition up : usersposDB) {
				LatLng en = new LatLng(up.getLatitude(), up.getLongitude());
				latsnlongs.add(en);
				stamp = up.getDate();
			}
			users.add(new MapUser(mu.getId(), mu.getName(), mu.getColor(),
					stamp, latsnlongs));
		}
	}

	private void appendUser(int id, String stamp){
		db = new DBConnector(getActivity());
		List<LatLng> list = new ArrayList<LatLng>();
		User u = db.getUser(id);
		MapUser user = new MapUser(id, u.getName(), u.getColor(), stamp, list);
		users.add(user);
	}
	
	private void setUpMap() {
		
			// Showing the current location in Google Map
			mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(68.435,
					17.434)));

			// lm.getLastKnownLocation(lm.getBestProvider(null, true));

			// Zoom in the Google Map
			mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
	}

	private void setUpMarkers() {
		for (MapUser u : users) {
			for (LatLng l : u.getPosList()) {
				mMap.addCircle(new CircleOptions().center(l)
						.fillColor(u.getColor()).radius(5)
						.strokeColor(Color.TRANSPARENT));
			}
			if(!u.getPosList().isEmpty()){
			u.setMyPolyline(mMap.addPolyline(new PolylineOptions().color(u
					.getColor())));
			u.getMyPolyline().setPoints(u.getPosList());
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

	public void update(int id, double latitude, double longitude, String stamp,
			String name) {
		Log.d("UPDATE", "id: " + id);
		boolean found = false;
		for (MapUser a : users)
		{
			if (a.getId() == id)
				found = true;
		}
		if (!found)
			appendUser(id, stamp);
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

	/*
	 * @Override public void onMyLocationChange(Location location) {
	 * 
	 * // Getting latitude of the current location double latitude =
	 * location.getLatitude();
	 * 
	 * // Getting longitude of the current location double longitude =
	 * location.getLongitude();
	 * 
	 * // Henter nøyaktighetern double accuracy = location.getAccuracy();
	 * 
	 * // Creating a LatLng object for the current location LatLng latLng = new
	 * LatLng(latitude, longitude); float[] hsvColor = {0, 0, 0};
	 * Color.colorToHSV(Color.MAGENTA, hsvColor);
	 * 
	 * mMap.addCircle(new
	 * CircleOptions().center(latLng).fillColor(Color.CYAN).radius
	 * (5).strokeColor(Color.TRANSPARENT)); pos = new ArrayList<LatLng>();
	 * pos.add(latLng); //pl.setPoints(pos); if (myMarker == null) {
	 * BitmapDescriptor bitmapDescriptor =
	 * BitmapDescriptorFactory.defaultMarker(hsvColor[0]); MarkerOptions mo =
	 * new
	 * MarkerOptions().position(latLng).title("Jan Tore").icon(bitmapDescriptor
	 * ); myMarker = mMap.addMarker(mo); } else { myMarker.setPosition(latLng);
	 * } }
	 */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int id = intent.getIntExtra(EXTRA_USERID, -1);
			double lat = intent.getDoubleExtra(EXTRA_LAT, 0.0);
			double lng = intent.getDoubleExtra(EXTRA_LONG, 0.0);
			String stamp = intent.getStringExtra(EXTRA_DATE);
			String name = intent.getStringExtra(EXTRA_NAME);
			update(id, lat, lng, stamp, name);
			Log.d("DEBUG", "reciever" + name + id);
		}
	};
}

class MapUser {
	private int id;
	private Marker myMarker;
	private Polyline myPolyline;
	private List<LatLng> posList;
	private int color;
	private float[] hsvColor = { 0, 0, 0 };
	private String stamp;
	private String name;
	private BitmapDescriptor bitmapDescriptor;

	public MapUser(int _id, String _name, int _color, String _stamp,
			List<LatLng> _posList) {
		setId(_id);
		setName(_name);
		setColor(_color);
		setStamp(_stamp);
		setPosList(_posList);
		setBitmapDescriptor(BitmapDescriptorFactory
				.defaultMarker(getHsvColor()[0]));
	}

	public List<LatLng> getPosList() {
		return posList;
	}

	public void setPosList(List<LatLng> posList) {
		this.posList = posList;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
		Color.colorToHSV(getColor(), getHsvColor());
		setBitmapDescriptor(BitmapDescriptorFactory
				.defaultMarker(getHsvColor()[0]));
	}

	public Polyline getMyPolyline() {
		return myPolyline;
	}

	public void setMyPolyline(Polyline myPolyline) {
		this.myPolyline = myPolyline;
	}

	public Marker getMyMarker() {
		return myMarker;
	}

	public void setMyMarker(Marker myMarker) {
		this.myMarker = myMarker;
	}

	public String getStamp() {
		return stamp;
	}

	public void setStamp(String stamp) {
		this.stamp = stamp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float[] getHsvColor() {
		return hsvColor;
	}

	public void setHsvColor(float[] hsvColor) {
		this.hsvColor = hsvColor;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public BitmapDescriptor getBitmapDescriptor() {
		return bitmapDescriptor;
	}

	public void setBitmapDescriptor(BitmapDescriptor bitmapDescriptor) {
		this.bitmapDescriptor = bitmapDescriptor;
	}
}