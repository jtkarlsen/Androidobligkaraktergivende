package com.example.androidobligkaraktergivende;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
/**
 * Database kontakt klasse, har metoder for å snakke med databasen
 * @author kakebake
 *
 */
public class DBConnector {

	private SQLiteDatabase db;				//SQLite databsen
	private MySQLiteDatabaseHelper helper;	//Hjelpe klasse
	
	//kolonner i user_positions tabellen
	private String[] userPosColumns = {
			MySQLiteDatabaseHelper.U_POS_ID,
			MySQLiteDatabaseHelper.U_POS_USER_ID,
			MySQLiteDatabaseHelper.U_POS_LATITUDE,
			MySQLiteDatabaseHelper.U_POS_LONGITUDE,
			MySQLiteDatabaseHelper.U_POS_CREATED_AT
	};
	
	//kolonner i users tabellen
	private String[] userColumns = {
		MySQLiteDatabaseHelper.U_ID,
		MySQLiteDatabaseHelper.U_NAME,
		MySQLiteDatabaseHelper.U_COLOR,
		MySQLiteDatabaseHelper.U_CREATED_AT
	};
	
	/**
	 * Kontruktør
	 * @param context
	 */
	public DBConnector(Context context){
		helper = new MySQLiteDatabaseHelper(context);
		
	}
	
	/**
	 * Åpner kontakt med databasen
	 * @throws SQLException
	 */
	public void open() throws SQLException{
		try{
		db = helper.getWritableDatabase();
		}catch(SQLException e){
			db = helper.getReadableDatabase();
		}
	}
	
	/**
	 * Lukker kontakt med databasen
	 * @throws SQLException
	 */
	public void close()throws SQLException{
		
		helper.close();
	}
	
	/**
	 * Legger til posisjonsdata
	 * @param userPos
	 */
	public void addUserPosition(UserPosition userPos){
		open();
		ContentValues values = new ContentValues();
		values.put(MySQLiteDatabaseHelper.U_POS_LATITUDE, userPos.getLatitude());
		values.put(MySQLiteDatabaseHelper.U_POS_LONGITUDE, userPos.getLongitude());
		values.put(MySQLiteDatabaseHelper.U_POS_USER_ID, userPos.getUserId());
		values.put(MySQLiteDatabaseHelper.U_POS_CREATED_AT, userPos.getDate());
		long insert = db.insert(MySQLiteDatabaseHelper.TABLE_USER_POSTIONS, null, values);
		Cursor cursor = db.query(MySQLiteDatabaseHelper.TABLE_USER_POSTIONS, userPosColumns,
				MySQLiteDatabaseHelper.U_ID + " = " + insert, null, null,
				null, null);
		cursor.moveToFirst();
		cursor.close();
		close();
	}
	
	/**
	 * Henter all posisjonsdata
	 * @return
	 */
	public ArrayList<UserPosition> getAllUserPositions(){
		open();
		ArrayList<UserPosition> userPos = new ArrayList<UserPosition>();

		//kjører en spørring på databasen
	    Cursor cursor = db.query(MySQLiteDatabaseHelper.TABLE_USER_POSTIONS,
	    		userPosColumns, null, null, null, null, null);

	    //Flytter cursor til start og kjører igjennom alle radene og legger det til klassen.
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	UserPosition pos = cursorToUserPosition(cursor);
	    	userPos.add(pos);
	      cursor.moveToNext();
	    }
	    // lukker cursor
	    cursor.close();
	    close();
	    return userPos;
	}
	
	/**
	 * Henter posisjonsdata basert på brukeid
	 * @param userId
	 * @return
	 */
	public ArrayList<UserPosition> getUsersPositions(int userId){
		open();
		ArrayList<UserPosition> userPos = new ArrayList<UserPosition>();
		String where = "user_id = ?";
		String args[] = {Integer.toString(userId)};
		//kjører en spørring på databasen
	    Cursor cursor = db.query(MySQLiteDatabaseHelper.TABLE_USER_POSTIONS,
	    		userPosColumns, where, args, null, null, null);

	    //Flytter cursor til start og kjører igjennom alle radene og legger det til klassen.
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	UserPosition pos = cursorToUserPosition(cursor);
	    	userPos.add(pos);
	      cursor.moveToNext();
	    }
	    // lukker cursor
	    close();
	    cursor.close();
	    
	    return userPos;
	}
	
	/**
	 * Legger til en ny bruker i databasen
	 * @param user
	 */
	public void addUser(User user){
		open();
		ContentValues values = new ContentValues();
		values.put(MySQLiteDatabaseHelper.U_ID, user.getId());
		values.put(MySQLiteDatabaseHelper.U_NAME, user.getName());
		values.put(MySQLiteDatabaseHelper.U_COLOR, user.getColor());
		values.put(MySQLiteDatabaseHelper.U_CREATED_AT, (new Date()).toString());
		long insert = db.insert(MySQLiteDatabaseHelper.TABLE_USER, null, values);
		Cursor cursor = db.query(MySQLiteDatabaseHelper.TABLE_USER, userColumns,
				MySQLiteDatabaseHelper.U_ID + " = " + insert, null, null,
				null, null);
		cursor.moveToFirst();
		cursor.close();
		close();
	}
	
	/**
	 * Henter alle brukere fra databasen
	 * @return
	 */
	public ArrayList<User> getAllUsers(){
		open();
		ArrayList<User> users = new ArrayList<User>();

		//kjører en spørring på databasen
	    Cursor cursor = db.query(MySQLiteDatabaseHelper.TABLE_USER,
	    		userColumns, null, null, null, null, null);

	    //Flytter cursor til start og kjører igjennom alle radene og legger det til klassen.
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	User user = cursorToUser(cursor);
	    	users.add(user);
	      cursor.moveToNext();
	    }
	    // lukker cursor
	    cursor.close();
	    close();
	    return users;
	}
	
	/**
	 * Henter siste brukerposisjon.
	 * @param userId
	 * @return
	 */
	public UserPosition getLastUserPos(int userId){
		open();
		UserPosition userPos = null;
		String[] selectionValue = {Integer.toString(userId)};
		Cursor cursor = db.rawQuery("Select * from user_positions where user_id = ? order by id desc limit 1", selectionValue);

		if(cursor.moveToFirst()){
			userPos = cursorToUserPosition(cursor);
	    
		}
		// lukker cursor
	    cursor.close();
	    close();
	    return userPos;
	}
	
	/**
	 * Henter ut en bruker fra databasen
	 * @param userId
	 * @return
	 */
	public User getUser(int userId){
		open();
		User user = null;
		String selection = "id=?";
		String[] selectionValue = {Integer.toString(userId)};
		Cursor cursor = db.query(MySQLiteDatabaseHelper.TABLE_USER, userColumns, 
				selection, selectionValue, null, null, null);
		if(cursor.moveToFirst()){
			user = cursorToUser(cursor);
	    
		}
		// lukker cursor
	    cursor.close();
	    close();
	    return user;
		
	}
	
	/**
	 * Sjekker at en bruker eksisterer
	 * @param userId
	 * @return
	 */
	public boolean userExists(int userId){
		int count = -1;
		open();
		Cursor cursor = db.query(MySQLiteDatabaseHelper.TABLE_USER, new String[]{"id"},
				"id=?", new String[]{Integer.toString(userId)}, null,null,null);
		if (cursor.moveToFirst()){
			count = cursor.getInt(0);
		}
		cursor.close();
		close();
		return count >= 0;
		
		
	}
	
	public void updateUser(User user){
		open();
		String filter = "id=" + user.getId();
		ContentValues args = new ContentValues();
		args.put("name", user.getName());
		args.put("color", user.getColor());
		db.update(MySQLiteDatabaseHelper.TABLE_USER, args, filter, null);
		close();
	}
	
	public void clearAllUserData(){
		open();
		String where = "id != ?";
		String whereArgs[] = {"0"};
		
		db.delete(MySQLiteDatabaseHelper.TABLE_USER_POSTIONS, null, null);
		
		db.delete(MySQLiteDatabaseHelper.TABLE_USER, where, whereArgs);
		close();
	}
	
	public void clearOwnUserData(){
		open();
		String where = "user_id = ?";
		String args[] = {"0"};
		db.delete(MySQLiteDatabaseHelper.TABLE_USER_POSTIONS, where, args);
		
		close();
	}
	
	public void clearRemoteUserData(){
		open();
		String where = "user_id != ?";
		String userWhere = "id != ?";
		String args[] = {"0"};
		db.delete(MySQLiteDatabaseHelper.TABLE_USER_POSTIONS, where, args);
		db.delete(MySQLiteDatabaseHelper.TABLE_USER, userWhere, args);
		close();
	}
	
	
	private UserPosition cursorToUserPosition(Cursor cursor) { 
		UserPosition userPos = new UserPosition(cursor.getInt(1), cursor.getDouble(2), cursor.getDouble(3));
		userPos.setDate(cursor.getString(4));
	    return userPos;
	  }
	
	private User cursorToUser(Cursor cursor){
		User user = new User(cursor.getInt(0), cursor.getString(1), cursor.getInt(2));
		return user;
	}
	
	
	
}
