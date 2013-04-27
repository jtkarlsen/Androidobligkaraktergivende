package com.example.androidobligkaraktergivende;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * 
 * @author Håvard og Jan Tore Eget ArrayAdapter som viser navn og farge på
 *         personene
 */
public class PeopleArrayAdapter extends ArrayAdapter<User> {

	private Context context; // context
	private ArrayList<User> users; // liste med personer

	/**
	 * Kontruktør
	 * 
	 * @param context
	 * @param users
	 */
	public PeopleArrayAdapter(Context context, ArrayList<User> users) {
		super(context, R.layout.people_listitem_layout, users);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.users = users;
	}

	/**
	 * Setter opp lista med farge og navn på hver person.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.people_listitem_layout,
				parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.username);
		View color = rowView.findViewById(R.id.user_color);
		GradientDrawable bgShape = (GradientDrawable) color.getBackground();
		bgShape.setColor(users.get(position).getColor());
		textView.setText(users.get(position).getName());

		return rowView;
	}

}
