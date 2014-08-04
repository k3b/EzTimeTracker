package de.k3b.timetracker.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.k3b.common.database.NumberUtil;
import de.k3b.common.database.SqlFilter;
import de.k3b.common.database.SqlFilterBuilder;
import de.k3b.timetracker.TimeSliceFilterParameter;
import de.k3b.timetracker.model.ITimeSliceFilter;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;

/**
 * TimeSlice dependent sql with no dependencies to android.<br/>
 * Used by {@link de.k3b.timetracker.database.ITimeSliceRepository} to build sql.<br/>
 * Scope=package to allow unittesting.<br/>
 */
class TimeSliceSql {
    public static final String TABLE = "time_slice";
    public static final String COL_PK = "_id";
    static final String COL_CATEGORY_ID = "category_id";
    static final String COL_NOTES = "notes";
    static final String COL_END_TIME = "end_time";
    static final String COL_START_TIME = "start_time";
    static final String CREATE_time_slice_report = "CREATE VIEW time_slice_report AS "
            + "SELECT "
            + "ca." + TimeSliceCategorySql.COL_CATEGORY_NAME + ", "
            + "datetime(ts." + COL_START_TIME + " /1000, 'unixepoch', 'localtime') AS start, "
            + "datetime(ts." + COL_END_TIME + " /1000, 'unixepoch', 'localtime') AS end, "
            + "(ts." + COL_END_TIME + " - ts." + COL_START_TIME + ")/3600.0/1000.0 AS hours, "
            + COL_NOTES + ", "
            + "ts." + COL_PK + ", "
            + COL_CATEGORY_ID
            + " FROM " + TABLE + " AS ts "
            + " LEFT JOIN " + TimeSliceCategorySql.TABLE + " AS ca" +
            " ON ts." + COL_CATEGORY_ID + " = ca." +
            TimeSliceCategorySql.COL_PK
            + " ORDER BY ts." + COL_START_TIME + " DESC ";

    static final String CREATE_TABLE = "CREATE TABLE " + TABLE + "("
            + COL_PK + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_CATEGORY_ID + " INTEGER REFERENCES " + TimeSliceCategorySql.TABLE + "(_id), "
            + COL_START_TIME + " DATE, " +
            COL_END_TIME + " DATE, " + // v3
            COL_NOTES + " TEXT)";

    private TimeSliceSql() {
    }

    /**
     * generates sql-where from genericTimeSliceFilter
     */
    static SqlFilter createFilter(final ITimeSliceFilter genericTimeSliceFilter) {
        final SqlFilterBuilder builder = new SqlFilterBuilder();
        if (genericTimeSliceFilter != null) {
            final TimeSliceFilterParameter timeSliceFilter = (genericTimeSliceFilter instanceof TimeSliceFilterParameter) ? (TimeSliceFilterParameter) genericTimeSliceFilter
                    : null;

            builder.add(TimeSliceSql.COL_CATEGORY_ID + " = ?", ""
                    + genericTimeSliceFilter.getCategoryId(), ""
                    + TimeSliceCategory.NOT_SAVED);

            final boolean ignoreDates = (timeSliceFilter == null) ? false
                    : timeSliceFilter.isIgnoreDates();

            TimeSliceSql.createDateFilter(builder,
                    genericTimeSliceFilter.getStartTime(),
                    genericTimeSliceFilter.getEndTime(), ignoreDates);

            if (timeSliceFilter != null) {
                if (timeSliceFilter.isNotesNotNull()) {
                    builder.addConst(TimeSliceSql.COL_NOTES, "IS NOT NULL")
                            .addConst(TimeSliceSql.COL_NOTES, "<> ''");
                } else {
                    final String notes = timeSliceFilter.getNotes();
                    if ((notes != null) && (notes.length() > 0)) {
                        builder.add(TimeSliceSql.COL_NOTES + " LIKE ?", "%"
                                + notes + "%", "");
                    }
                }
            } // if filterParameter
        } // if not null

        return builder.toFilter();
        // debugContext
    }

    private static void createDateFilter(final SqlFilterBuilder builder,
                                         final long startDate, final long endDate, final boolean ignoreDates) {

        if (!ignoreDates) {
            builder.add(TimeSliceSql.COL_START_TIME + ">= ?", "" + startDate,
                    "" + TimeSlice.NO_TIME_VALUE);
            builder.add(TimeSliceSql.COL_START_TIME + "<= ?", "" + endDate, ""
                    + TimeSlice.NO_TIME_VALUE);
        }
    }

    static String[] allColumnNames() {
        final List<String> columns = new ArrayList<String>();
        columns.add("_id");
        columns.add(COL_CATEGORY_ID);
        columns.add(COL_START_TIME);
        columns.add(COL_END_TIME);
        columns.add(COL_NOTES);
        return columns.toArray(new String[columns.size()]);
    }

    public static Map<String, String> asMap(final TimeSlice timeSlice) {

        final Map<String, String> values = new HashMap<String, String>();
        values.put(TimeSliceSql.COL_CATEGORY_ID, Integer.toString(timeSlice.getCategoryId()));
        values.put(TimeSliceSql.COL_START_TIME, Long.toString(timeSlice.getStartTime()));
        values.put(TimeSliceSql.COL_END_TIME, Long.toString(timeSlice.getEndTime()));
        values.put(TimeSliceSql.COL_NOTES, timeSlice.getNotes());
        final int rowId = timeSlice.getRowId();

        if (rowId != TimeSlice.IS_NEW_TIMESLICE) {
            values.put(TimeSliceSql.COL_PK,
                    Long.toString(rowId));
        }
        return values;

    }

    public static void fromMap(final TimeSlice dest, final Map<String, String> src, ICategoryRepsitory categoryRepository) {
        dest.setRowId(NumberUtil.getInt(TABLE, src, COL_PK, TimeSlice.IS_NEW_TIMESLICE));
        dest.setStartTime(NumberUtil.getLong(TABLE, src, TimeSliceSql.COL_START_TIME, TimeSliceCategory.MIN_VALID_DATE));
        dest.setEndTime(NumberUtil.getLong(TABLE, src, TimeSliceSql.COL_END_TIME, TimeSliceCategory.MAX_VALID_DATE));
        dest.setNotes(src.get(TimeSliceSql.COL_NOTES));

        if (categoryRepository != null) {
            final int rowID = NumberUtil.getInt(TABLE, src, COL_CATEGORY_ID, TimeSliceCategory.IS_NEW_TIMESLICE);
            dest.setCategory(categoryRepository.fetchByRowID(rowID));
        }
    }
}
