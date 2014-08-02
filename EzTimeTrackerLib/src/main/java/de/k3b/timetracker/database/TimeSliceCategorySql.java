package de.k3b.timetracker.database;

/**
 * TimeSliceCategory dependent sql with no dependencies to android.<br/>
 * Used by {@link de.k3b.timetracker.database.ICategoryRepsitory} to build sql.<br/>
 * <p/>
 * Scope=package to allow unittesting.<br/>
 */
class TimeSliceCategorySql {
    public static final String TIME_SLICE_CATEGORY_TABLE = "time_slice_category";
    static final String COL_PK = "_id";
    static final String COL_CATEGORY_NAME = "category_name";
    static final String COL_START_TIME = "start_time";
    static final String COL_END_TIME = "end_time";
    static final String COL_DESCRIPTION = "description";
    static final String CREATE_TABLE = "CREATE TABLE " + TIME_SLICE_CATEGORY_TABLE
            + "(" + COL_PK + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_CATEGORY_NAME + " TEXT, "
            + COL_DESCRIPTION + " TEXT,"
            + COL_START_TIME + " DATE,"
            + COL_END_TIME + " DATE" + // v4
            ")";
}
