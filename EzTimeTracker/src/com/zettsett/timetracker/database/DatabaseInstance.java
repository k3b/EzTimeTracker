package com.zettsett.timetracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.Global;

import de.k3b.android.database.DatabaseContext;

/**
 * Singleton database instance. Database is automatically opend on first access
 */
public class DatabaseInstance {
	private static int lastInstanceNo = 0;
	private int instanceNo = 0;
	private static DatabaseInstance currentInstance;

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private Context ctx;
	private Boolean publicDir = null;
	private String dbName = null;

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "DatabaseInstance#" + this.instanceNo + "/"
				+ DatabaseInstance.lastInstanceNo + "('" + this.dbName
				+ "',public=" + this.publicDir + ")";
	}

	public static DatabaseInstance getCurrentInstance() {
		if (DatabaseInstance.currentInstance == null) {
			final DatabaseInstance instance = new DatabaseInstance();
			DatabaseInstance.lastInstanceNo++;
			DatabaseInstance.currentInstance = instance;
			instance.instanceNo = DatabaseInstance.lastInstanceNo;
			if (Global.isDebugEnabled()) {
				Log.d(Global.LOG_CONTEXT, instance.toString() + ".create()");
			}
		}
		return DatabaseInstance.currentInstance;
	}

	public DatabaseInstance initialize(final Context ctx) {
		this.ctx = ctx;
		if (this.dbName == null) {
			this.dbName = ctx.getString(R.string.database_name);
		}

		if (this.publicDir == null) {
			this.publicDir = true;
		}

		if (Global.isDebugEnabled()) {
			Log.d(Global.LOG_CONTEXT, this.toString() + ".initialized from ctx");
		}
		return this;
	}

	/**
	 * Used by unittests to have more control over the environment
	 */
	public DatabaseInstance initialize(final Context ctx,
			final Boolean publicDir, final String dbName) {
		this.ctx = ctx;
		this.publicDir = publicDir;
		this.dbName = dbName;
		if (Global.isDebugEnabled()) {
			Log.d(Global.LOG_CONTEXT, this.toString() + ".initialized full");
		}
		return this;
	}

	public DatabaseInstance close() {
		try {
			if (this.mDb != null) {
				if (Global.isDebugEnabled()) {
					Log.d(Global.LOG_CONTEXT, this.toString() + ".closing");
				}
				this.mDb.close();
				this.mDbHelper.close();
			}
		} finally {
			this.mDb = null;
			this.mDbHelper = null;
		}
		return this;
	}

	public SQLiteDatabase getWritableDatabase() {
		if ((this.mDb == null) || !this.mDb.isOpen()) {
			if (Global.isDebugEnabled()) {
				Log.d(Global.LOG_CONTEXT, this.toString() + ".creating helper");
			}
			this.mDbHelper = new DatabaseHelper(
					(this.publicDir) ? new DatabaseContext(this.ctx) : this.ctx,
					this.dbName);
			this.mDb = this.mDbHelper.getWritableDatabase();
		}
		return this.mDb;
	}
}
