package de.k3b.timetracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import de.k3b.android.database.DatabaseContext;
import de.k3b.timetracker.Global;
import de.k3b.timetracker.R;

/**
 * Singleton database instance. Database is automatically opend on first access
 */
public class DatabaseInstance {
	private static int lastInstanceNo = 0;
	private int instanceNo = 0;
	private static DatabaseInstance DB;

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private Context ctx;
	private boolean publicDir = true;
	private String dbName = null;

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "DatabaseInstance#" + this.instanceNo + "/"
				+ DatabaseInstance.lastInstanceNo + "('" + this.dbName
				+ "',public=" + this.publicDir + ")";
	}

	public static DatabaseInstance getCurrentInstance() {
		if (DatabaseInstance.DB == null) {
			final DatabaseInstance instance = new DatabaseInstance();
			DatabaseInstance.lastInstanceNo++;
			DatabaseInstance.DB = instance;
			instance.instanceNo = DatabaseInstance.lastInstanceNo;
			if (Global.isDebugEnabled()) {
				Log.d(Global.LOG_CONTEXT, instance.toString() + ".create()");
			}
		}
		return DatabaseInstance.DB;
	}

	public DatabaseInstance initialize(final Context ctx,
			final Boolean publicDir) {
		return this.initialize(ctx, publicDir, null);
	}

	/**
	 * Used by unittests to have more control over the environment
	 */
	public DatabaseInstance initialize(final Context ctx,
			final Boolean publicDir, final String dbName) {
		this.ctx = ctx;

		if (dbName != null) {
			this.dbName = dbName;
		}

		if (this.dbName == null) {
			this.dbName = ctx.getString(R.string.database_name);
		}

		if ((publicDir != null) && (this.publicDir != publicDir.booleanValue())) {
			this.close();
			this.publicDir = publicDir.booleanValue();
		}

		if (Global.isDebugEnabled()) {
			Log.d(Global.LOG_CONTEXT, this.toString() + ".initialized");
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
