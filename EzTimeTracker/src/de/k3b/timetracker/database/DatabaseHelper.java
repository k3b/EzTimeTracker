package de.k3b.timetracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import de.k3b.timetracker.model.TimeSliceCategory;

/**
 * Android specific Encapsulation of the Database create/open/close/upgrade
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION_3_TIMESLICE_WITH_NOTES = 3;
    public static final int DATABASE_VERSION_4_CATEGORY_ACTIVE = 4;
    public static final int DATABASE_VERSION_5_REPORT_VIEW = 5;

    public static final int DATABASE_VERSION = DatabaseHelper.DATABASE_VERSION_5_REPORT_VIEW;

    DatabaseHelper(final Context context, final String databaseName) {
        super(context, databaseName, null, DatabaseHelper.DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(TimeSliceCategorySql.CREATE_TABLE);
        db.execSQL(TimeSliceSql.CREATE_TABLE);

        this.version5Upgrade_REPORT_VIEW(db);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
                          final int newVersion) {
        Log.w(this.getClass().toString(), "Upgrading database from version "
                + oldVersion + " to " + newVersion + ". (Old data is kept.)");
        if (oldVersion < DatabaseHelper.DATABASE_VERSION_3_TIMESLICE_WITH_NOTES) {
            this.version3Upgrade_TIMESLICE_WITH_NOTES(db);
        }
        if (oldVersion < DatabaseHelper.DATABASE_VERSION_4_CATEGORY_ACTIVE) {
            this.version4Upgrade_CATEGORY_ACTIVE(db);
        }
        if (oldVersion < DatabaseHelper.DATABASE_VERSION_5_REPORT_VIEW) {
            this.version5Upgrade_REPORT_VIEW(db);
        }
    }

    private void version3Upgrade_TIMESLICE_WITH_NOTES(final SQLiteDatabase db) {
        // added timeslice.notes
        db.execSQL("ALTER TABLE " + TimeSliceSql.TABLE
                + " ADD COLUMN " + TimeSliceSql.COL_NOTES + " TEXT");
    }

    private void version4Upgrade_CATEGORY_ACTIVE(final SQLiteDatabase db) {
        // added TimeSliceCategory.start/end
        db.execSQL("ALTER TABLE " + TimeSliceCategorySql.TABLE
                + " ADD COLUMN " + TimeSliceCategorySql.COL_START_TIME + " DATE");
        db.execSQL("ALTER TABLE " + TimeSliceCategorySql.TABLE
                + " ADD COLUMN " + TimeSliceCategorySql.COL_END_TIME + " DATE");
        db.execSQL("UPDATE " + TimeSliceCategorySql.TABLE
                + " SET " + TimeSliceCategorySql.COL_START_TIME + " = " + TimeSliceCategory.MIN_VALID_DATE
                + " WHERE " + TimeSliceCategorySql.COL_START_TIME + " IS NULL");
        db.execSQL("UPDATE " + TimeSliceCategorySql.TABLE
                + " SET " + TimeSliceCategorySql.COL_END_TIME + " = " + TimeSliceCategory.MAX_VALID_DATE
                + " WHERE " + TimeSliceCategorySql.COL_END_TIME + " IS NULL");
    }

    private void version5Upgrade_REPORT_VIEW(final SQLiteDatabase db) {
        // redefined time_slice_report
        db.execSQL(TimeSliceSql.CREATE_time_slice_report);
    }
}
