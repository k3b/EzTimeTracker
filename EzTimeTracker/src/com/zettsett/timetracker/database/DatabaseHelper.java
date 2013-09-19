package com.zettsett.timetracker.database;

import com.zettsett.timetracker.model.TimeSliceCategory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import de.k3b.database.DatabaseContext;

/**
 * Encapsulation of the Database create/open/close/upgrade
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION_3_TIMESLICE_WITH_NOTES = 3;
	public static final int DATABASE_VERSION_4_CATEGORY_ACTIVE = 4;

	public static final int DATABASE_VERSION = DATABASE_VERSION_4_CATEGORY_ACTIVE;
	
	public static final String TIME_SLICE_CATEGORY_TABLE = "time_slice_category";
	public static final String TIME_SLICE_TABLE = "time_slice";
	
	DatabaseHelper(final Context context, String databaseName) {
		super(new DatabaseContext(context) , databaseName, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TIME_SLICE_CATEGORY_TABLE
				+ " (_id integer primary key autoincrement, "
				+ "category_name text, description text, start_time date, end_time date)");
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
		if (oldVersion < DATABASE_VERSION_3_TIMESLICE_WITH_NOTES) {
			version3Upgrade(db);
		}
		if (oldVersion < DATABASE_VERSION_4_CATEGORY_ACTIVE) {
			version4Upgrade(db);
		}
	}

	private void version3Upgrade(final SQLiteDatabase db) {
		db.execSQL("ALTER TABLE " + TIME_SLICE_TABLE + " ADD COLUMN notes TEXT");
	}
	
	private void version4Upgrade(final SQLiteDatabase db) {
		db.execSQL("ALTER TABLE " + TIME_SLICE_CATEGORY_TABLE + " ADD COLUMN start_time date");
		db.execSQL("ALTER TABLE " + TIME_SLICE_CATEGORY_TABLE + " ADD COLUMN end_time date");
		db.execSQL("UPDATE " + TIME_SLICE_CATEGORY_TABLE + " set start_time = " + TimeSliceCategory.MIN_VALID_DATE + " where start_time is null");
		db.execSQL("UPDATE " + TIME_SLICE_CATEGORY_TABLE + " set end_time = " + TimeSliceCategory.MAX_VALID_DATE + " where end_time is null");
	}
}
