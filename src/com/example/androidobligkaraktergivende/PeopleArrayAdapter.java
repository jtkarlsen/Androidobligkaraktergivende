package com.example.androidobligkaraktergivende;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PeopleArrayAdapter extends ArrayAdapter<User> {

	private Context context;
	private ArrayList<User> users;
	public PeopleArrayAdapter(Context context, ArrayList<User> users) {
		super(context,R.layout.people_listitem_layout, users);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.users = users;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.people_listitem_layout, parent, false);
	    TextView textView = (TextView) rowView.findViewById(R.id.username);
	    View color = rowView.findViewById(R.id.user_color);
	    GradientDrawable bgShape = (GradientDrawable )color.getBackground();
	    bgShape.setColor(users.get(position).getColor());
	    textView.setText(users.get(position).getName());

	    return rowView;
	}

}
