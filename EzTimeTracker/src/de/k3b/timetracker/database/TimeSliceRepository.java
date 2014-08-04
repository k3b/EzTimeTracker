package de.k3b.timetracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.k3b.android.database.AndroidDatabaseUtil;
import de.k3b.common.database.SqlFilter;
import de.k3b.csv2db.csv.CsvTimeSliceIterator;
import de.k3b.timetracker.Global;
import de.k3b.timetracker.SettingsImpl;
import de.k3b.timetracker.TimeSliceFilterParameter;
import de.k3b.timetracker.model.ITimeSliceFilter;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;

/**
 * handles android specific database persistance for {@link de.k3b.timetracker.model.TimeSlice}.
 */
public class TimeSliceRepository implements ITimeSliceRepository {
    private static final DatabaseInstance DB = DatabaseInstance
            .getCurrentInstance();
    private final TimeSliceCategoryRepsitory categoryRepository;

    public TimeSliceRepository(final Context context, Boolean publicDir, final TimeSliceCategoryRepsitory categoryRepository) {
        TimeSliceRepository.DB.initialize(context, publicDir);
        this.categoryRepository = categoryRepository;
    }

    public static int delete(final ITimeSliceFilter timeSliceFilter) {
        final String context = "deleteForDateRange(" + timeSliceFilter + ") ";

        final SqlFilter sqlFilter = TimeSliceRepository.createFilter(context,
                timeSliceFilter);

        final int result = TimeSliceRepository.DB.getWritableDatabase()
                .delete(TimeSliceSql.TABLE, sqlFilter.sql,
                        sqlFilter.args);
        if (Global.isDebugEnabled()) {
            Log.d(Global.LOG_CONTEXT, context + result + " rows affected");
        }
        return result;
    }

    /**
     * Counts how many TimeSlice items exist that matches the timeSliceFilter
     */
    public static int getCount(final ITimeSliceFilter timeSliceFilter) {
        final String context = "TimeSliceRepository.getCount("
                + timeSliceFilter + ")";

        final SqlFilter sqlFilter = TimeSliceRepository.createFilter(context,
                timeSliceFilter);
        Cursor cur = null;
        int result = -1;
        try {
            final SQLiteDatabase db = TimeSliceRepository.DB
                    .getWritableDatabase();
            cur = db.query(TimeSliceSql.TABLE,
                    new String[]{"COUNT(*)"}, sqlFilter.sql, sqlFilter.args,
                    null, null, null);
            if ((cur != null) && (cur.moveToFirst())) {
                result = cur.getInt(0);
            }
        } catch (final Exception ex) {
            throw new TimeTrackerDBException(context, sqlFilter, ex);
        } finally {
            if (cur != null) {
                cur.close();
            }
            if (Global.isDebugEnabled()) {
                Log.d(Global.LOG_CONTEXT,
                        sqlFilter.getDebugMessage(context + " = " + result));
            }
        }
        return result;
    }

    /**
     * Totals those TimeSlice-Durations that matches the timeSliceFilter
     */
    public static double getTotalDurationInHours(
            final ITimeSliceFilter timeSliceFilter) {
        final String context = "TimeSliceRepository.getTotalDurationInHours("
                + timeSliceFilter + ")";

        final SqlFilter sqlFilter = TimeSliceRepository.createFilter(context,
                timeSliceFilter);
        Cursor cur = null;
        double result = -1.0;
        try {
            cur = TimeSliceRepository.DB.getWritableDatabase().query(
                    TimeSliceSql.TABLE,
                    new String[]{"SUM(" + TimeSliceSql.COL_END_TIME + "-"
                            + TimeSliceSql.COL_START_TIME + ")"},
                    sqlFilter.sql, sqlFilter.args, null, null, null
            );
            if ((cur != null) && (cur.moveToFirst())) {
                result = cur.getLong(0) / (1000.0 * 60 * 60);
            }
        } catch (final Exception ex) {
            throw new TimeTrackerDBException(context, sqlFilter, ex);
        } finally {
            if (cur != null) {
                cur.close();
            }
            if (Global.isDebugEnabled()) {
                Log.d(Global.LOG_CONTEXT,
                        sqlFilter
                                .getDebugMessage("TimeSliceRepository.getTotalDurationInHours("
                                        + timeSliceFilter + ") = " + result)
                );
            }
        }
        return result;

    }

    private static SqlFilter createFilter(final String debugContext,
                                          final ITimeSliceFilter timeSliceFilter) {
        final SqlFilter sqlFilter = TimeSliceSql.createFilter(timeSliceFilter);

        final String context = "TimeSliceRepository.createFilter("
                + debugContext + "," + timeSliceFilter + ")";
        if (Global.isDebugEnabled()) {
            Log.d(Global.LOG_CONTEXT,
                    (sqlFilter != null) ? sqlFilter.getDebugMessage(context)
                            : context
            );
        }
        return sqlFilter;
    }

    /**
     * creates a new timeSlice, <br/>
     * if distance to last timeslice > Settings.minPunchInTrashhold.<br/>
     * Else append to previous.
     *
     * @return rowid if successfull or -1 if error or negative value if timeslice was appended.
     */
    @Override
    public long create(final TimeSlice timeSlice) {
        final long newStartTime = timeSlice.getStartTime();
        final TimeSlice oldTimeSlice = this
                .fetchOldestByCategoryAndEndTimeInterval(
                        "create-Find with same category to append to",
                        timeSlice.getCategory(),
                        newStartTime
                                - SettingsImpl.getMinPunchInTreshholdInMilliSecs(),
                        newStartTime
                );

        if (oldTimeSlice != null) {
            timeSlice.setNotes(
                    oldTimeSlice.getNotes() + " " + timeSlice.getNotes())
                    .setRowId(oldTimeSlice.getRowId());
            final long oldStartTime = oldTimeSlice.getStartTime();
            if (oldStartTime < newStartTime) {
                timeSlice.setStartTime(oldStartTime);
            }
            if (Global.isDebugEnabled()) {
                Log.d(Global.LOG_CONTEXT, "create(): merging old timeslice '"
                        + oldTimeSlice + "' to '" + timeSlice + "'.");
            }
            return (this.update(timeSlice) > 0) ? -timeSlice.getRowId() : -1;
        } else {
            if (Global.isDebugEnabled()) {
                Log.d(Global.LOG_CONTEXT,
                        "create(): db-inserting new timeslice '" + timeSlice
                                + "'."
                );
            }
            return TimeSliceRepository.DB.getWritableDatabase().insert(
                    TimeSliceSql.TABLE, null,
                    this.asContentValues(timeSlice));
        }
    }

    /**
     * update existing timeslice.
     *
     * @return number of affected rows.
     */
    public long update(final TimeSlice timeSlice) {
        final int result = TimeSliceRepository.DB.getWritableDatabase()
                .update(TimeSliceSql.TABLE,
                        this.asContentValues(timeSlice),
                        "_id = " + timeSlice.getRowId(), null);
        if (Global.isDebugEnabled()) {
            Log.d(Global.LOG_CONTEXT, "updateTimeSlice(" + timeSlice + ") "
                    + result + " rows affected");
        }

        return result;
    }

    public TimeSlice fetchByRowID(final long rowId) {
        Cursor cur = null;
        try {
            cur = TimeSliceRepository.DB.getWritableDatabase().query(true,
                    TimeSliceSql.TABLE, TimeSliceSql.allColumnNames(),
                    "_id=?", new String[]{Long.toString(rowId)}, null, null,
                    null, null);
            if ((cur != null) && (cur.moveToFirst())) {
                return this.fillTimeSliceFromCursor(cur, null);
            }
        } catch (final Exception ex) {
            throw new TimeTrackerDBException(
                    "TimeSliceRepository.fetchByRowID(rowId = " + rowId + ")",
                    null, ex);
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        Log.e(Global.LOG_CONTEXT,
                "Not found : TimeSliceRepository.fetchByRowID(rowId = " + rowId
                        + ")"
        );
        return null;
    }

    public List<TimeSlice> fetchList(final ITimeSliceFilter timeSliceFilter) {
        final String debugMessage = "TimeSliceRepository.fetchTimeSlices("
                + timeSliceFilter + ")";
        final SqlFilter sqlFilter = TimeSliceRepository.createFilter(
                debugMessage, timeSliceFilter);

        return this.fetchListInternal(debugMessage, sqlFilter);
    }

    private List<TimeSlice> fetchListInternal(final String debugMessage,
                                              final SqlFilter sqlFilter) {

        final List<TimeSlice> result = new ArrayList<TimeSlice>();
        Cursor cur = null;
        try {
            cur = TimeSliceRepository.DB.getWritableDatabase().query(
                    TimeSliceSql.TABLE, TimeSliceSql.allColumnNames(),
                    sqlFilter.sql, sqlFilter.args, null, null,
                    TimeSliceSql.COL_START_TIME);
            HashMap<String, String> values = new HashMap<String, String>();
            while (cur.moveToNext()) {
                final TimeSlice ts = this.fillTimeSliceFromCursor(cur, values);
                result.add(ts);
            }
            if (Global.isDebugEnabled()) {
                Log.d(Global.LOG_CONTEXT, debugMessage + ": " + result.size()
                        + " rows affected");
            }
            return result;
        } catch (final Exception ex) {
            throw new TimeTrackerDBException(debugMessage, sqlFilter, ex);
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
    }

    private TimeSlice fetchOldest(final String debugMessage,
                                  final SqlFilter sqlFilter) {
        final List<TimeSlice> result = this.fetchListInternal(debugMessage,
                sqlFilter);
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    private TimeSlice fetchOldestByCategoryAndEndTimeInterval(
            final String debugMessage, final TimeSliceCategory category,
            final long minimumEndDate, final long maximumEndDate) {
        final TimeSliceFilterParameter timeSliceFilter = new TimeSliceFilterParameter()
                .setParameter(minimumEndDate, maximumEndDate,
                        category.getRowId());
        final SqlFilter sqlFilter = TimeSliceRepository
                .createFilter(debugMessage, timeSliceFilter);
        final SqlFilter sqlEndFilter = new SqlFilter(sqlFilter.sql.replace(
                TimeSliceSql.COL_START_TIME, TimeSliceSql.COL_END_TIME),
                sqlFilter.args
        );
        return this.fetchOldest(debugMessage, sqlEndFilter);
    }

    public int getCount(final TimeSliceCategory category) throws SQLException {
        final ITimeSliceFilter timeSliceFilter = new TimeSliceFilterParameter()
                .setCategoryId(category.getRowId());
        return TimeSliceRepository.getCount(timeSliceFilter);
    }

    private TimeSlice fillTimeSliceFromCursor(final Cursor cur, HashMap<String, String> values) {
        final TimeSlice item = new TimeSlice();
        if (!cur.isAfterLast()) {
            if (values == null) values = new HashMap<String, String>();
            AndroidDatabaseUtil.cursorRowToContentValues(cur, values);

            TimeSliceSql.fromMap(item, values, this.categoryRepository);
        }
        return item;
    }

    private ContentValues asContentValues(final TimeSlice timeSlice) {
        return AndroidDatabaseUtil.toContentValues(TimeSliceSql.asMap(timeSlice));
    }

    void createInitialDemoDataFromResources() {
        InputStream resourceStream = null;
        Reader reader = null;
        CsvTimeSliceIterator iter = null;

        try {
            resourceStream = CsvTimeSliceIterator.class.getResourceAsStream("/DemoData.csv");
            reader = new InputStreamReader(resourceStream);
            try {
                iter = new CsvTimeSliceIterator(reader, this.categoryRepository);
                while (iter.hasNext()) {
                    this.update(iter.next());
                }
            } finally {
                if (reader != null) reader.close();
                if (resourceStream != null) resourceStream.close();
                if (iter != null) iter.close();
            }
        } catch (IOException ignore) {
            Log.e(Global.LOG_CONTEXT, "error createInitialDemoDataFromResources(). '/' = " + CsvTimeSliceIterator.class.getResource("/").getPath(), ignore);
        }
    }
}
