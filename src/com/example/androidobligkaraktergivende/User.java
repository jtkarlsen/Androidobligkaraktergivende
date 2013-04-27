package com.example.androidobligkaraktergivende;

/**
 * Bruker klasse, brukes som hjelpeklasse med
 * kontakt til databasen.
 * @author Håvard og Jan Tore
 *
 */
public class User {

	private int id; // id
	private String name; // navn
	private int color; //farge
	
	/**
	 * Kontruktør
	 * @param id
	 * @param name
	 * @param color
	 */
	public User(int id, String name, int color){
		this.color = color;
		this.id = id;
		this.name = name;
	}
	
	/**
	 * @return farge
	 */
	public int getColor() {
		return color;
	}

	/**
	 * @param farge
	 */
	public void setColor(int color) {
		this.color = color;
	}

	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return navn
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param navn
	 */
	public void setName(String name) {
		this.name = name;
	}

	
	
}
