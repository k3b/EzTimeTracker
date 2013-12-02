package com.zettsett.timetracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zettsett.timetracker.model.TimeSliceCategory;

import de.k3b.android.database.DatabaseContext;

/**
 * Encapsulation of the Database create/open/close/upgrade
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION_3_TIMESLICE_WITH_NOTES = 3;
	public static final int DATABASE_VERSION_4_CATEGORY_ACTIVE = 4;
	public static final int DATABASE_VERSION_5_REPORT_VIEW = 5;

	public static final int DATABASE_VERSION = DatabaseHelper.DATABASE_VERSION_5_REPORT_VIEW;

	public static final String TIME_SLICE_CATEGORY_TABLE = "time_slice_category";
	public static final String TIME_SLICE_TABLE = "time_slice";

	DatabaseHelper(final Context context, final String databaseName) {
		super(new DatabaseContext(context), databaseName, null,
				DatabaseHelper.DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + DatabaseHelper.TIME_SLICE_CATEGORY_TABLE
				+ "(" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "category_name TEXT, description TEXT"
				+ ",start_time DATE,end_time DATE" + // v4
				")");
		db.execSQL("CREATE TABLE " + DatabaseHelper.TIME_SLICE_TABLE + "("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "category_id INTEGER REFERENCES "
				+ DatabaseHelper.TIME_SLICE_CATEGORY_TABLE + "(_id), "
				+ "start_time DATE, end_time DATE, " + // v3
				"notes TEXT)");

		this.version5Upgrade(db);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
			final int newVersion) {
		Log.w(this.getClass().toString(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion + ". (Old data is kept.)");
		if (oldVersion < DatabaseHelper.DATABASE_VERSION_3_TIMESLICE_WITH_NOTES) {
			this.version3Upgrade(db);
		}
		if (oldVersion < DatabaseHelper.DATABASE_VERSION_4_CATEGORY_ACTIVE) {
			this.version4Upgrade(db);
		}
		if (oldVersion < DatabaseHelper.DATABASE_VERSION_5_REPORT_VIEW) {
			this.version5Upgrade(db);
		}
	}

	private void version3Upgrade(final SQLiteDatabase db) {
		db.execSQL("ALTER TABLE " + DatabaseHelper.TIME_SLICE_TABLE
				+ " ADD COLUMN notes TEXT");
	}

	private void version4Upgrade(final SQLiteDatabase db) {
		db.execSQL("ALTER TABLE " + DatabaseHelper.TIME_SLICE_CATEGORY_TABLE
				+ " ADD COLUMN start_time DATE");
		db.execSQL("ALTER TABLE " + DatabaseHelper.TIME_SLICE_CATEGORY_TABLE
				+ " ADD COLUMN end_time DATE");
		db.execSQL("UPDATE " + DatabaseHelper.TIME_SLICE_CATEGORY_TABLE
				+ " SET start_time = " + TimeSliceCategory.MIN_VALID_DATE
				+ " WHERE start_time IS NULL");
		db.execSQL("UPDATE " + DatabaseHelper.TIME_SLICE_CATEGORY_TABLE
				+ " SET end_time = " + TimeSliceCategory.MAX_VALID_DATE
				+ " WHERE end_time IS NULL");
	}

	private void version5Upgrade(final SQLiteDatabase db) {
		db.execSQL("CREATE VIEW time_slice_report AS "
				+ "SELECT "
				+ "ca.category_name, "
				+ "datetime(ts.start_time /1000, 'unixepoch', 'localtime') AS start, "
				+ "datetime(ts.end_time /1000, 'unixepoch', 'localtime') AS end, "
				+ "(ts.end_time - ts.start_time)/3600.0/1000.0 AS hours, "
				+ "notes, " + "ts._id, " + "category_id " + "FROM "
				+ DatabaseHelper.TIME_SLICE_TABLE + " AS ts " + "LEFT JOIN "
				+ DatabaseHelper.TIME_SLICE_CATEGORY_TABLE
				+ " AS ca ON ts.category_id = ca._id "
				+ "ORDER BY ts.start_time DESC ");
	}
}
