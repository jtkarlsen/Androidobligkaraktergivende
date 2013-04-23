package com.example.androidobligkaraktergivende;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;

public class PeopleFragment extends ListFragment {


	@Override
	  public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    ArrayList<User> users = new ArrayList<User>();

	    
	    
	    User user = new User(1, "Håvard", Color.BLUE);
	    
	    users.add(user);
	    
	    PeopleArrayAdapter adapter = new PeopleArrayAdapter(getActivity(), users);
	    setListAdapter(adapter);
	    
	    
	  }
}
