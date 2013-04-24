package com.example.androidobligkaraktergivende;

import android.graphics.Color;

public class UserPosition {
	
	
	private int id;
	private int userId;
	private double longitude, latitude;
	
	
	public UserPosition(int userId, double longitude, double latitude){
		this.userId = userId;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public int getUserId() {
		return userId;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
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

}
