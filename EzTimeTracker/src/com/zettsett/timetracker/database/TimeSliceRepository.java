package com.zettsett.timetracker.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.Settings;
import com.zettsett.timetracker.activity.TimeSliceFilterParameter;
import com.zettsett.timetracker.database.TimeSliceSql.SqlFilter;
import com.zettsett.timetracker.model.ITimeSliceFilter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class TimeSliceRepository {
	private static final DatabaseInstance CURRENT_DB_INSTANCE = DatabaseInstance
			.getCurrentInstance();
	private static TimeSliceRepository timeSliceRepositorySingleton;
	private final TimeSliceCategoryRepsitory categoryRepository;

	public TimeSliceRepository(final Context context) {
		TimeSliceRepository.CURRENT_DB_INSTANCE.initialize(context);
		this.categoryRepository = new TimeSliceCategoryRepsitory(context);
	}

	public static TimeSliceRepository getTimeSliceDBAdapter(
			final Context context) {
		if (TimeSliceRepository.timeSliceRepositorySingleton == null) {
			TimeSliceRepository.timeSliceRepositorySingleton = new TimeSliceRepository(
					context);
		}
		return TimeSliceRepository.timeSliceRepositorySingleton;
	}

	public long createTimeSlice(final TimeSlice timeSlice) {
		final long startTime = timeSlice.getStartTime();
		final TimeSlice oldTimeSlice = this
				.findTimesliceByCategoryAndEndTimeInterval(
						"createTimeSlice-Find possible duplicate",
						timeSlice.getCategory(),
						startTime - Settings.getMinminTrashholdInMilliSecs(),
						startTime);

		if (oldTimeSlice != null) {
			oldTimeSlice.setEndTime(timeSlice.getEndTime());
			oldTimeSlice.setNotes(oldTimeSlice.getNotes() + " "
					+ timeSlice.getNotes());

			if (Global.isDebugEnabled()) {
				Log.d(Global.LOG_CONTEXT, "db-updating exising timeslice '"
						+ oldTimeSlice + "' from '" + timeSlice + "'.");
			}
			TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().update(
					DatabaseHelper.TIME_SLICE_TABLE,
					this.timeSliceContentValuesList(oldTimeSlice), "_id=?",
					new String[] { Long.toString(oldTimeSlice.getRowId()) });
			return oldTimeSlice.getRowId();
		} else {
			if (Global.isDebugEnabled()) {
				Log.d(Global.LOG_CONTEXT, "db-inserting new timeslice '"
						+ timeSlice + "'.");
			}
			return TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().insert(
					DatabaseHelper.TIME_SLICE_TABLE, null,
					this.timeSliceContentValuesList(timeSlice));
		}
	}

	public long updateTimeSlice(final TimeSlice timeSlice) {
		final int result = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb()
				.update(DatabaseHelper.TIME_SLICE_TABLE,
						this.timeSliceContentValuesList(timeSlice),
						"_id = " + timeSlice.getRowId(), null);
		if (Global.isDebugEnabled()) {
			Log.d(Global.LOG_CONTEXT, "updateTimeSlice(" + timeSlice + ") "
					+ result + " rows affected");
		}

		return result;
	}

	public boolean delete(final long rowId) {
		final int result = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb()
				.delete(DatabaseHelper.TIME_SLICE_TABLE, "_id=" + rowId, null);
		if (Global.isDebugEnabled()) {
			Log.d(Global.LOG_CONTEXT, "delete(rowId=" + rowId + ") " + result
					+ " rows affected");
		}

		return result > 0;
	}

	public static int deleteForDateRange(final ITimeSliceFilter timeSliceFilter) {
		final String context = "deleteForDateRange(" + timeSliceFilter + ") ";

		final SqlFilter sqlFilter = TimeSliceRepository.createFilter(context,
				timeSliceFilter);

		final int result = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb()
				.delete(DatabaseHelper.TIME_SLICE_TABLE, sqlFilter.sql,
						sqlFilter.args);
		if (Global.isDebugEnabled()) {
			Log.d(Global.LOG_CONTEXT, context + result + " rows affected");
		}
		return result;
	}

	public TimeSlice fetchByRowID(final long rowId) throws SQLException {
		Cursor cur = null;
		try {
			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(true,
					DatabaseHelper.TIME_SLICE_TABLE, this.columnList(),
					"_id=?", new String[] { Long.toString(rowId) }, null, null,
					null, null);
			if ((cur != null) && (cur.moveToFirst())) {
				return this.fillTimeSliceFromCursor(cur);
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
						+ ")");
		return null;
	}

	public List<TimeSlice> fetchTimeSlices(
			final ITimeSliceFilter timeSliceFilter) {
		return this.fetchTimeSlicesInternal(
				"TimeSliceRepository.fetchTimeSlices(" + timeSliceFilter + ")",
				timeSliceFilter);
	}

	private List<TimeSlice> fetchTimeSlicesInternal(final String debugMessage,
			final ITimeSliceFilter timeSliceFilter) {

		final SqlFilter sqlFilter = TimeSliceRepository.createFilter(
				debugMessage, timeSliceFilter);
		final List<TimeSlice> result = new ArrayList<TimeSlice>();
		Cursor cur = null;
		try {
			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(
					DatabaseHelper.TIME_SLICE_TABLE, this.columnList(),
					sqlFilter.sql, sqlFilter.args, null, null,
					TimeSliceSql.COL_START_TIME);
			while (cur.moveToNext()) {
				final TimeSlice ts = this.fillTimeSliceFromCursor(cur);
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

	private TimeSlice fetchTimeSlice(final String debugMessage,
			final ITimeSliceFilter timeSliceFilter) {
		final List<TimeSlice> result = this.fetchTimeSlicesInternal(
				debugMessage, timeSliceFilter);
		if (result.size() == 0) {
			return result.get(0);
		}
		return null;
	}

	public boolean categoryHasTimeSlices(final TimeSliceCategory category)
			throws SQLException {
		Cursor cur = null;
		boolean result = false;
		try {
			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(true,
					DatabaseHelper.TIME_SLICE_TABLE, this.columnList(),
					TimeSliceSql.COL_CATEGORY_ID + "=?",
					new String[] { Long.toString(category.getRowId()) }, null,
					null, null, null);
			if (cur.moveToNext()) {
				result = true;
			}
			return result;
		} catch (final Exception ex) {
			throw new TimeTrackerDBException(
					"TimeSliceRepository.categoryHasTimeSlices(" + category
							+ ") ", null, ex);
		} finally {
			if (cur != null) {
				cur.close();
			}
			if (Global.isDebugEnabled()) {
				Log.d(Global.LOG_CONTEXT, "categoryHasTimeSlices(" + category
						+ ") " + result + " items found");
			}
		}

	}

	private TimeSlice findTimesliceByCategoryAndEndTimeInterval(
			final String debugMessage, final TimeSliceCategory category,
			final long minimumEndDate, final long maximumEndDate) {
		final TimeSliceFilterParameter timeSliceFilter = new TimeSliceFilterParameter()
				.setParameter(minimumEndDate, maximumEndDate,
						category.getRowId());
		return this.fetchTimeSlice(debugMessage, timeSliceFilter);
	}

	/**
	 * Counts how many TimeSlice items exist that matches the timeSliceFilter
	 * 
	 * @return
	 */
	public static int getCount(final ITimeSliceFilter timeSliceFilter) {
		final String context = "TimeSliceRepository.getCount("
				+ timeSliceFilter + ")";

		final SqlFilter sqlFilter = TimeSliceRepository.createFilter(context,
				timeSliceFilter);
		Cursor cur = null;
		int result = -1;
		try {
			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(
					DatabaseHelper.TIME_SLICE_TABLE,
					new String[] { "COUNT(*)" }, sqlFilter.sql, sqlFilter.args,
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
	 * 
	 * @return
	 */
	public static double getTotalDurationInHours(
			final ITimeSliceFilter timeSliceFilter) {
		final String context = "TimeSliceRepository.getTotalDurationInHours("
				+ timeSliceFilter + ")";

		final SqlFilter sqlFilter = TimeSliceRepository.createFilter(context,
				timeSliceFilter);
		Cursor cur = null;
		double result = 0.0;
		try {
			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(
					DatabaseHelper.TIME_SLICE_TABLE,
					new String[] { "SUM(" + TimeSliceSql.COL_END_TIME + "-"
							+ TimeSliceSql.COL_START_TIME + ")" },
					sqlFilter.sql, sqlFilter.args, null, null, null);
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
										+ timeSliceFilter + ") = " + result));
			}
		}
		Log.e(Global.LOG_CONTEXT,
				sqlFilter
						.getDebugMessage("Not found : TimeSliceRepository.getTotalDurationInHours()"));
		return 0.0;

	}

	private TimeSlice fillTimeSliceFromCursor(final Cursor cur) {
		final int categoryID = cur.getInt(cur
				.getColumnIndexOrThrow(TimeSliceSql.COL_CATEGORY_ID));

		if (categoryID != TimeSliceCategory.NOT_SAVED) {
			final TimeSlice ts = new TimeSlice();
			ts.setRowId(cur.getInt(cur.getColumnIndexOrThrow("_id")));
			ts.setStartTime(cur.getLong(cur
					.getColumnIndexOrThrow(TimeSliceSql.COL_START_TIME)));
			ts.setEndTime(cur.getLong(cur
					.getColumnIndexOrThrow(TimeSliceSql.COL_END_TIME)));
			ts.setCategory(this.categoryRepository.fetchByRowID(categoryID));
			ts.setNotes(cur.getString(cur
					.getColumnIndexOrThrow(TimeSliceSql.COL_NOTES)));
			return ts;
		}

		Log.w(Global.LOG_CONTEXT, "Ignoring timeslice with categoryID="
				+ TimeSliceCategory.NOT_SAVED);
		return null;
	}

	private String[] columnList() {
		final List<String> columns = new ArrayList<String>();
		columns.add("_id");
		columns.add(TimeSliceSql.COL_CATEGORY_ID);
		columns.add(TimeSliceSql.COL_START_TIME);
		columns.add(TimeSliceSql.COL_END_TIME);
		columns.add(TimeSliceSql.COL_NOTES);
		return columns.toArray(new String[0]);
	}

	private ContentValues timeSliceContentValuesList(final TimeSlice timeSlice) {
		final ContentValues values = new ContentValues();
		values.put(TimeSliceSql.COL_CATEGORY_ID, timeSlice.getCategoryId());
		values.put(TimeSliceSql.COL_START_TIME, timeSlice.getStartTime());
		values.put(TimeSliceSql.COL_END_TIME, timeSlice.getEndTime());
		values.put(TimeSliceSql.COL_NOTES, timeSlice.getNotes());
		return values;
	}

	private static SqlFilter createFilter(final String debugContext,
			final ITimeSliceFilter timeSliceFilter) {
		final SqlFilter sqlFilter = TimeSliceSql.createFilter(timeSliceFilter);

		final String context = "TimeSliceRepository.createFilter("
				+ debugContext + "," + timeSliceFilter + ")";
		Log.d(Global.LOG_CONTEXT,
				(sqlFilter != null) ? sqlFilter.getDebugMessage(context)
						: context);

		return sqlFilter;
	}
}
