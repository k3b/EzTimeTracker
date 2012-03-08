package com.zettsett.timetracker.database;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import de.k3b.database.DatabaseContext;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 3;
	public static final String TIME_SLICE_CATEGORY_TABLE = "time_slice_category";
	public static final String TIME_SLICE_TABLE = "time_slice";

	DatabaseHelper(final Context context, String databaseName) {
		super(new DatabaseContext(context), databaseName, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TIME_SLICE_CATEGORY_TABLE
				+ " (_id integer primary key autoincrement, "
				+ "category_name text, description text)");
		db.execSQL("CREATE TABLE " + TIME_SLICE_TABLE + " (_id integer primary key autoincrement, "
						+ "category_id integer, start_time date, end_time date)");
		version3Upgrade(db);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
			final int newVersion) {
		Log.w(this.getClass().toString(),
				"Upgrading database from version " + oldVersion + " to "
				+ newVersion + ". (Old data is kept.)");
		if (oldVersion < 3) {
			version3Upgrade(db);
		}
	}

	private void version3Upgrade(final SQLiteDatabase db) {
		db.execSQL("ALTER TABLE " + TIME_SLICE_TABLE + " ADD COLUMN notes TEXT");
	}

	// howto open db from local filesystem. not used
	private SQLiteDatabase OpenExternalDatabase()
	{
		SQLiteDatabase db = null;
		String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        // mExternalStorageAvailable = mExternalStorageWriteable = true;

			File sdcard = Environment.getExternalStorageDirectory();	
			String dbfile = sdcard.getAbsolutePath() + File.separator+ "external_sd" + File.separator + "Database.db" ;
			db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
	    } 

		return db;
	}
}
