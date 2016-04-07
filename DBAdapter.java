// ------------------------------------ DBADapter.java ---------------------------------------------

// TODO: Change the package to match your project.
package com.swift.swamdclient;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;


// TO USE:
// Change the package (at top) to match your project.
// Search for "TODO", and make the appropriate changes.
public class DBAdapter {

	/////////////////////////////////////////////////////////////////////
	//	Constants & Data
	/////////////////////////////////////////////////////////////////////
	// For logging:
	private static final String TAG = "DBAdapter";
	
	// DB Fields
	public static final String KEY_ROWID = "_id";
	public static final int COL_ROWID = 0;
	/*
	 * CHANGE 1:
	 */
	// TODO: Setup your fields here:
	public static final String KEY_NAME = "name";
	public static final String KEY_STUDENTNUM = "studentnum";
	public static final String KEY_FAVCOLOUR = "favcolour";
	
	// TODO: Setup your field numbers here (0 = KEY_ROWID, 1=...)
	public static final int COL_NAME = 1;
	public static final int COL_STUDENTNUM = 2;
	public static final int COL_FAVCOLOUR = 3;
	// Field numbers for second table
	public static final int COL_DIGEST = 1;
	public static final int COL_IDMAINTABLE = 2;
	public static final int COL_MOBILEID = 3;
	public static final int COL_FLAG = 4;


	// The Fields For our Second Table
	public static final String KEY_DIGEST = "mdvalue";
	public static final String KEY_IDMAINTABLE = "idMain";
	public static final String KEY_MOBILEID = "mobileID";
	public static final String KEY_FLAG = "flag";



	public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_NAME, KEY_STUDENTNUM, KEY_FAVCOLOUR};
	
	// DB info: it's name, and the table we are using (just one).
	public static final String DATABASE_NAME = "MyDb";
	public static final String DATABASE_TABLE = "mainTable";
	public static final String DATABASE_TABLE_DIGEST = "messageDigest";
	// Track DB version if a new version of your app changes the format.
	public static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE_SQL =
			"create table " + DATABASE_TABLE 
			+ " (" + KEY_ROWID + " integer primary key autoincrement, "
			
			/*
			 * CHANGE 2:
			 */
			// TODO: Place your fields here!
			// + KEY_{...} + " {type} not null"
			//	- Key is the column name you created above.
			//	- {type} is one of: text, integer, real, blob
			//		(http://www.sqlite.org/datatype3.html)
			//  - "not null" means it is a required field (must be given a value).
			// NOTE: All must be comma separated (end of line!) Last one must have NO comma!!
			+ KEY_NAME + " text not null, "
			+ KEY_STUDENTNUM + " integer not null, "
			+ KEY_FAVCOLOUR + " string not null"
			
			// Rest  of creation:
			+ ");";

	private static final String DATABASE_CREATE_SQL_DIGEST =
			"create table " + DATABASE_TABLE_DIGEST
					+ " (" + KEY_ROWID + " integer primary key autoincrement, "

			/*
			 * CHANGE 2:
			 */
					// TODO: Place your fields here!
					// + KEY_{...} + " {type} not null"
					//	- Key is the column name you created above.
					//	- {type} is one of: text, integer, real, blob
					//		(http://www.sqlite.org/datatype3.html)
					//  - "not null" means it is a required field (must be given a value).
					// NOTE: All must be comma separated (end of line!) Last one must have NO comma!!
					+ KEY_DIGEST + " text not null, "
					+ KEY_IDMAINTABLE + " int not null,"
					+ KEY_MOBILEID + " text default null,"
					+ KEY_FLAG + " int not null"
					// Rest  of creation:
					+ ");";


	// Context of application who uses us.
	private final Context context;
	
	private DatabaseHelper myDBHelper;
	private SQLiteDatabase db;

	/////////////////////////////////////////////////////////////////////
	//	Public methods:
	/////////////////////////////////////////////////////////////////////
	
	public DBAdapter(Context ctx) {
		this.context = ctx;
		myDBHelper = new DatabaseHelper(context);
	}
	
	// Open the database connection.
	public DBAdapter open() {
		db = myDBHelper.getWritableDatabase();
		return this;
	}
	
	// Close the database connection.
	public void close() {
		myDBHelper.close();
	}
	
	// Add a new set of values to the database.
	public long insertRow(String name, int studentNum, String favColour) {
		/*
		 * CHANGE 3:
		 */		
		// TODO: Update data in the row with new fields.
		// TODO: Also change the function's arguments to be what you need!
		// Create row's data:
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_STUDENTNUM, studentNum);
		initialValues.put(KEY_FAVCOLOUR, favColour);
		
		// Insert it into the database.
		return db.insert(DATABASE_TABLE, null, initialValues);


	}

	public long insertRow(String digest, long idMain, String mobileID, int flag) {
		/*
		 * CHANGE 3:
		 */
		// TODO: Update data in the row with new fields.
		// TODO: Also change the function's arguments to be what you need!
		// Create row's data:
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DIGEST, digest);
		initialValues.put(KEY_IDMAINTABLE, idMain);
		initialValues.put(KEY_MOBILEID, mobileID);
		initialValues.put(KEY_FLAG, flag);

		// Insert it into the database.
		return db.insert(DATABASE_TABLE_DIGEST, null, initialValues);
	}


	// Delete a row from the database, by rowId (primary key)
	public boolean deleteRow(long rowId) {
		String where = KEY_ROWID + "=" + rowId;
		return db.delete(DATABASE_TABLE, where, null) != 0;
	}


	public int deleteAllMDRows(){

		//SECOND PART
		//Deleting MDValues requires special attention...HOW do u tell the server the data to delete?
		// To notify of this change, we will send a request with deviceID and special character '*'
		// when the user clicks on 'SyncRecords Button'.
		return db.delete(DATABASE_TABLE_DIGEST, null, null);

	}

	
	public void deleteAll() {
		Cursor c = getAllRows();
		long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
		if (c.moveToFirst()) {
			do {
				deleteRow(c.getLong((int) rowId));				
			} while (c.moveToNext());
		}
		c.close();

	}


	
	// Return all data in the database.
	public Cursor getAllRows() {
		String where = null;
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Get a specific row (by rowId)
	public Cursor getRow(long rowId) {
		String where = KEY_ROWID + "=" + rowId;
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
						where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	
	// Change an existing row to be equal to new data.
	public boolean updateRow(long rowId, String name, int studentNum, String favColour) {
		String where = KEY_ROWID + "=" + rowId;

		/*
		 * CHANGE 4:
		 */
		// TODO: Update data in the row with new fields.
		// TODO: Also change the function's arguments to be what you need!
		// Create row's data:
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_NAME, name);
		newValues.put(KEY_STUDENTNUM, studentNum);
		newValues.put(KEY_FAVCOLOUR, favColour);
		
		// Insert it into the database.
		return db.update(DATABASE_TABLE, newValues, where, null) != 0;
	}

	/**
	 * Getting user data from database, where flag is 1
	 * A flag value of 1 indicates that the row requires synchronization!
	 * */
	public Cursor getRowsForSynchronization() {

		String selectQuery = "SELECT " + KEY_NAME + ","
				+ KEY_STUDENTNUM + ","
				+ KEY_FAVCOLOUR + ","
				+ KEY_DIGEST + ","
				+ KEY_IDMAINTABLE + ","
				+ KEY_FLAG
				+ " FROM " + DATABASE_TABLE
				+ " INNER JOIN " + DATABASE_TABLE_DIGEST
				+ " ON " + KEY_IDMAINTABLE + "=" + DATABASE_TABLE + "." + KEY_ROWID
				+ " WHERE " + DATABASE_TABLE_DIGEST + "." + KEY_FLAG + "= 1";


		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor != null) {
			cursor.moveToFirst();
		}else {

			Log.e("Cursor Empty TAG:", "_____NOTHING________NOTHING________");
		}

		return cursor;

	}


	public boolean updateRow( String digest, long idMain, String mobileID, int flag ){
		String where = KEY_FLAG + "=" + flag;

		ContentValues newValues = new ContentValues();
		newValues.put(KEY_DIGEST, digest);
		newValues.put(KEY_IDMAINTABLE, idMain);
		newValues.put(KEY_MOBILEID, mobileID);
		newValues.put(KEY_FLAG, 0);

		// Insert it into the database.
		return db.update(DATABASE_TABLE_DIGEST, newValues, where, null) != 0;

	}

	public boolean updateDigest(String digest, long idmain){
		String where = KEY_IDMAINTABLE + "=" + idmain;

		ContentValues newValues = new ContentValues();
		newValues.put(KEY_DIGEST, digest);
		newValues.put(KEY_IDMAINTABLE, idmain);

		return db.update(DATABASE_TABLE_DIGEST, newValues, where, null) != 0;

	}




	/////////////////////////////////////////////////////////////////////
	//	Private Helper Classes:
	/////////////////////////////////////////////////////////////////////
	/**
	 * Private class which handles database creation and upgrading.
	 * Used to handle low-level database access.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			Log.d("MyApp", "onCreate invoked");
			_db.execSQL(DATABASE_CREATE_SQL);
			_db.execSQL(DATABASE_CREATE_SQL_DIGEST);
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			Log.d("MyApp", "onUpgrade invoked");
			Log.w(TAG, "Upgrading application's database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data!");

			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_DIGEST);

			// Recreate new database:
			onCreate(_db);
		}

	}
}
