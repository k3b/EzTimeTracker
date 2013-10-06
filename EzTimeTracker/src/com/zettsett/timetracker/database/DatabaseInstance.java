package com.zettsett.timetracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.zetter.androidTime.R;

/**
 * Singleton database instance. Database is automatically opend on first access
 */
public class DatabaseInstance {
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private Context mCtx;
	private static DatabaseInstance currentInstance;

	public static DatabaseInstance getCurrentInstance() {
		if (DatabaseInstance.currentInstance == null) {
			final DatabaseInstance instance = new DatabaseInstance();
			DatabaseInstance.currentInstance = instance;
		}
		return DatabaseInstance.currentInstance;
	}

	public DatabaseInstance initialize(final Context ctx) {
		DatabaseInstance.currentInstance.mCtx = ctx;
		return this;
	}

	public DatabaseInstance close() {
		try {
			if (DatabaseInstance.currentInstance.mDb != null) {
				DatabaseInstance.currentInstance.mDb.close();
				DatabaseInstance.currentInstance.mDbHelper.close();
			}
		} finally {
			DatabaseInstance.currentInstance.mDb = null;
			DatabaseInstance.currentInstance.mDbHelper = null;
		}
		return this;
	}

	public SQLiteDatabase getDb() {
		if ((DatabaseInstance.currentInstance.mDb == null)
				|| !DatabaseInstance.currentInstance.mDb.isOpen()) {
			this.mDbHelper = new DatabaseHelper(this.mCtx,
					this.mCtx.getString(R.string.database_name));
			this.mDb = this.mDbHelper.getWritableDatabase();
		}
		return DatabaseInstance.currentInstance.mDb;
	}
}
