package com.example.androidobligkaraktergivende;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.location.*;
import android.content.*;
import android.widget.Button;
public class MapFragment extends Fragment {

	private DBConnector connection;
	private LocationManager locationManager;
	private String provider = LocationManager.GPS_PROVIDER;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.map_layout, container, false);
		Button newPos = (Button) view.findViewById(R.id.simulate_move);
		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		final Location last = locationManager.getLastKnownLocation(provider);
		final Location hin = new Location("HIN");
		hin.setLatitude(68.435961);
		hin.setLongitude(17.435303);
		newPos.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

					hin.setLatitude(hin.getLatitude() + 0.000010);
					hin.setLongitude(hin.getLongitude() + 0.000005);
					
					connection = new DBConnector(getActivity());
					UserPosition pos = new UserPosition(0, hin.getLongitude(), hin.getLatitude());
					connection.addUserPosition(pos);
					
					Intent service = new Intent(getActivity(), UpdatepositionService.class);
					service.putExtra("LONGITUDE", pos.getLongitude());
					service.putExtra("LATITUDE", pos.getLatitude());
					
					getActivity().startService(service);
			}
		});
		
        return view;
    }
}
