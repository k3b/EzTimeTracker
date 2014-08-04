package de.k3b.timetracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import de.k3b.android.database.DatabaseContext;
import de.k3b.timetracker.database.DatabaseHelper;
import de.k3b.timetracker.database.TimeSliceCategoryRepsitory;
import de.k3b.timetracker.database.TimeSliceRepository;
import de.k3b.util.SessionDataPersistance;

/**
 * Poor mans-s ioc container.
 * Created by k3b on 31.07.2014.
 */
public class Factory {
    private static Factory ourInstance = null;

    /**
     * if changed factory items have to be recreated.
     */
    private final Boolean publicDir;
    private TimeSliceCategoryRepsitory categoryRepsitory = null;
    private TimeSliceRepository timeSliceRepository = null;
    private TimeTrackerManager timeTrackerManager = null;
    private TimeTrackerSessionData sessionData = null;

    private SQLiteDatabase mDb = null;
    private DatabaseHelper mDbHelper = null;
    private Uri databaseUri = null;

    private Factory(final Boolean publicDir) {
        this.publicDir = publicDir;
    }

    public static Factory getInstance() {
        final boolean publicDatabase = SettingsImpl.isPublicDatabase();
        if ((ourInstance != null) && (ourInstance.publicDir != publicDatabase)) {
            if (Global.isDebugEnabled()) {
                Log.d(Global.LOG_CONTEXT, "close Factory");
            }

            ourInstance.close();
            ourInstance = null;
        }
        if (ourInstance == null) {
            if (Global.isDebugEnabled()) {
                Log.d(Global.LOG_CONTEXT, "create Factory");
            }
            ourInstance = new Factory(publicDatabase);
        }
        return ourInstance;
    }

    public SQLiteDatabase getDatabase(final Context context) {
        if (mDb == null) {
            if (Global.isDebugEnabled()) {
                Log.d(Global.LOG_CONTEXT, "Factory.getDatabase() creating");
            }
            final Context dbContext = (this.publicDir) ? new DatabaseContext(context) : context;
            final String dbName = context.getString(R.string.database_name);
            this.mDbHelper = new DatabaseHelper(dbContext, dbName);
            this.databaseUri = (this.publicDir) ? Uri.fromFile(dbContext.getDatabasePath(dbName)) : null;

            this.mDb = this.mDbHelper.getWritableDatabase();
            if (Global.isDebugEnabled()) {
                Log.d(Global.LOG_CONTEXT, "Factory.getDatabase() created");
            }
        }
        return mDb;
    }

    public Uri getDatabaseUri() {
        return (this.publicDir) ? this.databaseUri : null;
    }

    private void close() {
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
            categoryRepsitory = null;
            timeSliceRepository = null;
            timeTrackerManager = null;
            sessionData = null;
        }
    }

    public TimeSliceCategoryRepsitory createTimeSliceCategoryRepsitory(final Context context) {
        if (categoryRepsitory == null) {
            if (Global.isDebugEnabled()) {
                Log.d(Global.LOG_CONTEXT, "createTimeSliceCategoryRepsitory");
            }
            categoryRepsitory = new TimeSliceCategoryRepsitory(context, getDatabase(context));
        }
        return categoryRepsitory;
    }

    public TimeSliceRepository createTimeSliceRepository(final Context context) {
        if (timeSliceRepository == null) {
            if (Global.isDebugEnabled()) {
                Log.d(Global.LOG_CONTEXT, "createTimeSliceRepository");
            }

            final TimeSliceCategoryRepsitory timeSliceCategoryRepsitory = createTimeSliceCategoryRepsitory(context);
            timeSliceRepository = new TimeSliceRepository(context, getDatabase(context), timeSliceCategoryRepsitory);
            loadDemoDataIfNew(context);
        }
        return timeSliceRepository;
    }

    public void loadDemoDataIfNew(Context context) {
        if (DatabaseHelper.mustCreateDemoData) {
            DatabaseHelper.loadDemoData(createTimeSliceCategoryRepsitory(context), createTimeSliceRepository(context));
        }
    }


    public TimeTrackerManager createTimeTrackerManager(final Context context) {
        if (timeTrackerManager == null) {
            if (Global.isDebugEnabled()) {
                Log.d(Global.LOG_CONTEXT, "createTimeTrackerManager");
            }
            if (sessionData == null) {
                sessionData = new TimeTrackerSessionData();
            }
            timeTrackerManager = new TimeTrackerManager(new SessionDataPersistance<TimeTrackerSessionData>(context),
                    createTimeSliceRepository(context),
                    createTimeSliceCategoryRepsitory(context),
                    sessionData, SettingsImpl.getLogger(), SettingsImpl.getInstance());
        }
        return timeTrackerManager;
    }
}
