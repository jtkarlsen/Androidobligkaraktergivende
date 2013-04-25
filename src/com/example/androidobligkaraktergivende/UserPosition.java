package com.example.androidobligkaraktergivende;


public class UserPosition {
	
	
	private int id;
	private int userId;
	private double longitude, latitude;
	private String date;
	
	public UserPosition(int userId,double latitude, double longitude){
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

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}

}
