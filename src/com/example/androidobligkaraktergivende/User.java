package com.example.androidobligkaraktergivende;

import android.graphics.Color;

public class User {

	private int id;
	private String name;
	private int color;
	public User(int id, String name, int color){
		this.color = color;
		this.id = id;
		this.name = name;
	}
	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	
	
}
