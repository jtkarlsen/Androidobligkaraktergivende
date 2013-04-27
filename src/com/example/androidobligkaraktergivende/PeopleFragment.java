package com.example.androidobligkaraktergivende;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class PeopleFragment extends ListFragment {
	OnUserSelectedListener mCallback;
	ArrayList<User> users;
    // The container Activity must implement this interface so the frag can deliver messages
    public interface OnUserSelectedListener {
        /** Called by HeadlinesFragment when a list item is selected */
        public void onUserSelected(int id);
    }
	
	private DBConnector con;
	@Override
	  public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    initPeople();
	  }
	
	public void initPeople(){
		users = new ArrayList<User>();
	    con = new DBConnector(getActivity());
	    users = con.getAllUsers();

	    PeopleArrayAdapter adapter = new PeopleArrayAdapter(getActivity(), users);
	    setListAdapter(adapter);

	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnUserSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " må implementere OnUserSelectedListener");
        }
    }
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Notify the parent activity of selected item
        mCallback.onUserSelected(users.get(position).getId());
        
        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);
    }
}
