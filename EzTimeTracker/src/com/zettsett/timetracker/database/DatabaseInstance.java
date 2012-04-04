package com.zettsett.timetracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.zetter.androidTime.R;

/**
 * Singleton database instance.
 * Database is automatically opend on first access
 */
public class DatabaseInstance {
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private Context mCtx;
	private static DatabaseInstance currentInstance;
			
	public static DatabaseInstance getCurrentInstance() {
		if(currentInstance == null) {
			DatabaseInstance instance = new DatabaseInstance();
			currentInstance = instance; 
		}
		return currentInstance;
	}

	public DatabaseInstance initialize(Context ctx) {
		currentInstance.mCtx = ctx;
		return this;
	}
	
	public DatabaseInstance close() {
		try {
			if (currentInstance.mDb != null) {
				currentInstance.mDb.close();
				currentInstance.mDbHelper.close();
			}
		} finally {
			currentInstance.mDb = null;
			currentInstance.mDbHelper = null;
		}
		return this;
	}

	public SQLiteDatabase getDb() {
		if(currentInstance.mDb == null || !currentInstance.mDb.isOpen()) {
			mDbHelper = new DatabaseHelper(mCtx, mCtx.getString(R.string.database_name));
			mDb = mDbHelper.getWritableDatabase();
		}
		return currentInstance.mDb;
	}
}
