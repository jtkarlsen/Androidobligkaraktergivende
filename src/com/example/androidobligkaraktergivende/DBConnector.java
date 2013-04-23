package com.example.androidobligkaraktergivende;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBConnector {

	private SQLiteDatabase db;
	private MySQLiteDatabaseHelper helper;
	
	private String[] userPosColumns = {
			MySQLiteDatabaseHelper.U_POS_ID,
			MySQLiteDatabaseHelper.U_POS_USER_ID,
			MySQLiteDatabaseHelper.U_POS_LATITUDE,
			MySQLiteDatabaseHelper.U_POS_LONGITUDE,
			MySQLiteDatabaseHelper.U_POS_CREATED_AT
	};
	private String[] userColumns = {
		MySQLiteDatabaseHelper.U_ID,
		MySQLiteDatabaseHelper.U_NAME,
		MySQLiteDatabaseHelper.U_COLOR,
		MySQLiteDatabaseHelper.U_CREATED_AT
	};
	
	
	public DBConnector(Context context){
		helper = new MySQLiteDatabaseHelper(context);
	}
	
	public void open() throws SQLException{
		db = helper.getWritableDatabase();
	}
	
	public void close()throws SQLException{
		helper.close();
	}
	
	public void addUserPosition(UserPosition userPos){
		ContentValues values = new ContentValues();
		values.put(MySQLiteDatabaseHelper.U_POS_LATITUDE, userPos.getLatitude());
		values.put(MySQLiteDatabaseHelper.U_POS_LONGITUDE, userPos.getLongitude());
		values.put(MySQLiteDatabaseHelper.U_POS_USER_ID, userPos.getUserId());
		values.put(MySQLiteDatabaseHelper.U_POS_CREATED_AT, (new Date()).toString());
		long insert = db.insert(MySQLiteDatabaseHelper.TABLE_USER_POSTIONS, null, values);
		Cursor cursor = db.query(MySQLiteDatabaseHelper.TABLE_USER_POSTIONS, userPosColumns,
				MySQLiteDatabaseHelper.U_ID + " = " + insert, null, null,
				null, null);
		cursor.moveToFirst();
		cursor.close();
	}
	
	public ArrayList<UserPosition> getAllUserPositions(){
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
	    return userPos;
	}
	
	public void addUser(User user){
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
	}
	
	public ArrayList<User> getAllUsers(){
		ArrayList<User> users = new ArrayList<User>();

		//kjører en spørring på databasen
	    Cursor cursor = db.query(MySQLiteDatabaseHelper.TABLE_USER_POSTIONS,
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
	    return users;
	}
	
	
	private UserPosition cursorToUserPosition(Cursor cursor) { 
		UserPosition userPos = new UserPosition(cursor.getInt(1), cursor.getDouble(2), cursor.getDouble(3));
	    return userPos;
	  }
	
	private User cursorToUser(Cursor cursor){
		User user = new User(cursor.getInt(0), cursor.getString(1), cursor.getInt(2));
		return user;
	}
	
	
	
}
