package com.example.androidobligkaraktergivende;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQlite hjelpe klasse som setter opp databasen.
 * 
 * @author Håvard og Jan tore
 * 
 */
public class MySQLiteDatabaseHelper extends SQLiteOpenHelper {

	// Alle Statiske variabler
	// Database Version
	public static final int DATABASE_VERSION = 1;

	// Database navn
	private static final String DATABASE_NAME = "tracking.db";

	// Tabell navn
	public static final String TABLE_USER_POSTIONS = "user_positions";
	public static final String TABLE_USER = "users";

	// posisjons tabellens kolonner
	public static final String U_POS_ID = "id";
	public static final String U_POS_USER_ID = "user_id";
	public static final String U_POS_LATITUDE = "latitude";
	public static final String U_POS_LONGITUDE = "longitude";
	public static final String U_POS_CREATED_AT = "created_at";

	// bruker tabellens kolonner
	public static final String U_ID = "id";
	public static final String U_NAME = "name";
	public static final String U_CREATED_AT = "created_at";
	public static final String U_COLOR = "color";

	// SQL spørring som oppretter tabellene.
	private static final String CREATE_USER_TABLE = "CREATE TABLE "
			+ TABLE_USER + "(" + U_ID + " INTEGER PRIMARY KEY ," + U_NAME
			+ " TEXT," + U_COLOR + " INTEGER," + U_CREATED_AT + " DATETIME );";

	private static final String CREATE_USER_POS_TABLE = "CREATE TABLE "
			+ TABLE_USER_POSTIONS + "(" + U_POS_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + U_POS_USER_ID
			+ " INTEGER," + U_POS_LATITUDE + " REAL," + U_POS_LONGITUDE
			+ " REAL," + U_CREATED_AT + " DATETIME," + " FOREIGN KEY ("
			+ U_POS_USER_ID + ") REFERENCES " + TABLE_USER + " (" + U_ID
			+ "));";

	/**
	 * kontruktør som tar en et context parameter
	 * 
	 * @param context
	 */
	public MySQLiteDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}

	/**
	 * onCreate oppretter tabellene
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL(CREATE_USER_TABLE);
		db.execSQL(CREATE_USER_POS_TABLE);
	}

	/**
	 * Hvis man forandrer databasen slettes tabellene og kjører onCreate
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_POSTIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
		onCreate(db);
	}

}
