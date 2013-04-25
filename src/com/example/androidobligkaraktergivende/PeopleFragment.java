package com.example.androidobligkaraktergivende;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;

public class PeopleFragment extends ListFragment {
	
	
	private DBConnector con;
	@Override
	  public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    initPeople();
	  }
	
	public void initPeople(){
		ArrayList<User> users = new ArrayList<User>();
	    con = new DBConnector(getActivity());
	    users = con.getAllUsers();

	    PeopleArrayAdapter adapter = new PeopleArrayAdapter(getActivity(), users);
	    setListAdapter(adapter);

	}
}
