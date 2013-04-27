package com.example.androidobligkaraktergivende;

/**
 * Posisjonsklasse som brukes som hjelpeklasse ved
 * kontakt med databasen
 * @author Håvard og Jan tore
 *
 */
public class UserPosition {
	
	
	private int id; //id
	private int userId; //bruker id
	private double longitude, latitude; //posisjon
	private String date; //dato
	
	/**
	 * Kontruktør
	 * @param userId
	 * @param latitude
	 * @param longitude
	 */
	public UserPosition(int userId,double latitude, double longitude){
		this.userId = userId;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * henter bruker id
	 * @return bruker id
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * henter lengdegrad
	 * @return lengdegrad
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * setter lengdegrad
	 * @param longitude
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * henter breddegrad
	 * @return breddegrad
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * setter breddegrad
	 * @param latitude
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	
	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id som skal settes
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return dato
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param dato
	 */
	public void setDate(String date) {
		this.date = date;
	}

}
