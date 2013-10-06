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
import com.zettsett.timetracker.activity.FilterParameter;
import com.zettsett.timetracker.model.ITimeSliceFilter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class TimeSliceRepository {
	private static final String COL_CATEGORY_ID = "category_id";
	private static final String COL_NOTES = "notes";
	private static final String COL_END_TIME = "end_time";
	private static final String COL_START_TIME = "start_time";

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
						timeSlice.getCategory(),
						startTime - Settings.getMinminTrashholdInMilliSecs(),
						startTime);

		if (oldTimeSlice != null) {
			oldTimeSlice.setEndTime(timeSlice.getEndTime());
			oldTimeSlice.setNotes(oldTimeSlice.getNotes() + " "
					+ timeSlice.getNotes());

			Log.d(Global.LOG_CONTEXT, "db-updating exising timeslice '"
					+ oldTimeSlice + "' from '" + timeSlice + "'.");
			TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().update(
					DatabaseHelper.TIME_SLICE_TABLE,
					this.timeSliceContentValuesList(oldTimeSlice), "_id=?",
					new String[] { Long.toString(oldTimeSlice.getRowId()) });
			return oldTimeSlice.getRowId();
		} else {
			Log.d(Global.LOG_CONTEXT, "db-inserting new timeslice '"
					+ timeSlice + "'.");
			return TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().insert(
					DatabaseHelper.TIME_SLICE_TABLE, null,
					this.timeSliceContentValuesList(timeSlice));
		}
	}

	private TimeSlice findTimesliceByCategoryAndEndTimeInterval(
			final TimeSliceCategory category, final long minimumEndDate,
			final long maximumEndDate) {
		Cursor cur = null;
		try {
			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(
					true,
					DatabaseHelper.TIME_SLICE_TABLE,
					this.columnList(),
					TimeSliceRepository.COL_CATEGORY_ID + "=? AND "
							+ TimeSliceRepository.COL_END_TIME + ">=?  AND "
							+ TimeSliceRepository.COL_END_TIME + "<=?",
					new String[] { Long.toString(category.getRowId()),
							Long.toString(minimumEndDate),
							Long.toString(maximumEndDate) }, null, null, null,
					null);
			if ((cur != null) && (cur.moveToFirst())) {
				return this.fillTimeSliceFromCursor(cur);
			}
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
		Log.d(Global.LOG_CONTEXT,
				"Not found : TimeSliceDBAdapter.findTimesliceByCategoryAndEndTimeBiggerThan(...)");
		return null;
	}

	public long updateTimeSlice(final TimeSlice timeSlice) {
		return TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().update(
				DatabaseHelper.TIME_SLICE_TABLE,
				this.timeSliceContentValuesList(timeSlice),
				"_id = " + timeSlice.getRowId(), null);
	}

	/**
	 * Counts how many TimeSlice items exist that matches the filter
	 * 
	 * @return
	 */
	public static int getCount(final ITimeSliceFilter filterParam) {
		final String filter = TimeSliceRepository.createFilter(filterParam);
		Cursor cur = null;
		try {
			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb()
					.query(DatabaseHelper.TIME_SLICE_TABLE,
							new String[] { "COUNT(*)" }, filter, null, null,
							null, null);
			if ((cur != null) && (cur.moveToFirst())) {
				final int count = cur.getInt(0);
				Log.d(Global.LOG_CONTEXT, "TimeSliceDBAdapter.getCount("
						+ filter + ") = " + count);
				return count;
			}
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
		Log.e(Global.LOG_CONTEXT, "Not found : TimeSliceDBAdapter.getCount("
				+ filter + ")");
		return -1;

	}

	/**
	 * Totals those TimeSlice-Durations that matches the filter
	 * 
	 * @return
	 */
	public static double getTotalDurationInHours(
			final ITimeSliceFilter filterParam) {
		final String filter = TimeSliceRepository.createFilter(filterParam);
		Cursor cur = null;
		try {
			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(
					DatabaseHelper.TIME_SLICE_TABLE,
					new String[] { "SUM(" + TimeSliceRepository.COL_END_TIME
							+ "-" + TimeSliceRepository.COL_START_TIME + ")" },
					filter, null, null, null, null);
			if ((cur != null) && (cur.moveToFirst())) {
				final double duration = cur.getLong(0) / (1000.0 * 60 * 60);
				Log.d(Global.LOG_CONTEXT,
						"TimeSliceDBAdapter.getTotalDurationInHours(" + filter
								+ ") = " + duration);
				return duration;
			}
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
		Log.e(Global.LOG_CONTEXT,
				"Not found : TimeSliceDBAdapter.getTotalDurationInHours("
						+ filter + ")");
		return 0.0;

	}

	public boolean delete(final long rowId) {
		return TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().delete(
				DatabaseHelper.TIME_SLICE_TABLE, "_id=" + rowId, null) > 0;
	}

	public static int deleteForDateRange(final ITimeSliceFilter filterParam) {
		final String filter = TimeSliceRepository.createFilter(filterParam);
		return TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().delete(
				DatabaseHelper.TIME_SLICE_TABLE, filter, null);
	}

	public static String createFilter(final ITimeSliceFilter filter) {
		final StringBuilder result = new StringBuilder();

		if (filter != null) {
			final FilterParameter filterParameter = (filter instanceof FilterParameter) ? (FilterParameter) filter
					: null;
			final boolean ignoreDates = (filterParameter == null) ? false
					: filterParameter.isIgnoreDates();

			TimeSliceRepository.createFilter(result, filter.getStartTime(),
					filter.getEndTime(), filter.getCategoryId(), ignoreDates);

			if (filterParameter != null) {
				if (filterParameter.isNotesNotNull()) {
					TimeSliceRepository.addAND(result)
							.append(TimeSliceRepository.COL_NOTES)
							.append(" IS NOT NULL ");
				} else {
					final String notes = filterParameter.getNotes();
					if ((notes != null) && (notes.length() > 0)) {
						TimeSliceRepository.addAND(result)
								.append(TimeSliceRepository.COL_NOTES)
								.append(" like '%").append(notes).append("%'");
					}
				}
			} // if filterParameter
		} // if not null
		if (result.length() == 0) {
			return null;
		} else {
			return result.toString();
		}
	}

	private static void createFilter(final StringBuilder result,
			final long startDate, final long endDate, final long categoryId,
			final boolean ignoreDates) {

		if (!ignoreDates) {
			TimeSliceRepository.add(result, TimeSliceRepository.COL_START_TIME
					+ ">=", startDate, TimeSlice.NO_TIME_VALUE);
			TimeSliceRepository.add(result, TimeSliceRepository.COL_START_TIME
					+ "<=", endDate, TimeSlice.NO_TIME_VALUE);
		}
		TimeSliceRepository.add(result, TimeSliceRepository.COL_CATEGORY_ID
				+ " =", categoryId, TimeSliceCategory.NOT_SAVED);
	}

	private static void add(final StringBuilder result, final String field,
			final long value, final long emptyValue) {
		if (value != emptyValue) {
			TimeSliceRepository.addAND(result).append(value);
		}
	}

	private static StringBuilder addAND(final StringBuilder result) {
		if (result.length() > 0) {
			result.append(" AND ");
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
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
		Log.e(Global.LOG_CONTEXT,
				"Not found : TimeSliceDBAdapter.fetchByRowID(rowId = " + rowId
						+ ")");
		return null;
	}

	public boolean categoryHasTimeSlices(final TimeSliceCategory category)
			throws SQLException {
		Cursor cur = null;
		try {
			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(true,
					DatabaseHelper.TIME_SLICE_TABLE, this.columnList(),
					TimeSliceRepository.COL_CATEGORY_ID + "=?",
					new String[] { Long.toString(category.getRowId()) }, null,
					null, null, null);
			if (cur.moveToNext()) {
				return true;
			}
			return false;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}

	}

	public List<TimeSlice> fetchTimeSlices(final ITimeSliceFilter filterParam) {
		final String filter = TimeSliceRepository.createFilter(filterParam);
		final List<TimeSlice> result = new ArrayList<TimeSlice>();
		Cursor cur = null;
		try {
			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(
					DatabaseHelper.TIME_SLICE_TABLE, this.columnList(), filter,
					null, null, null, TimeSliceRepository.COL_START_TIME);
			while (cur.moveToNext()) {
				final TimeSlice ts = this.fillTimeSliceFromCursor(cur);
				result.add(ts);
			}
		} finally {
			if (cur != null) {
				cur.close();
			}
		}

		return result;
	}

	public List<TimeSlice> fetchTimeSlicesByDateRange(final long startDate,
			final long endDate) {
		final List<TimeSlice> result = new ArrayList<TimeSlice>();
		Cursor cur = null;
		try {
			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(
					DatabaseHelper.TIME_SLICE_TABLE,
					this.columnList(),
					TimeSliceRepository.COL_START_TIME + " >= ? and "
							+ TimeSliceRepository.COL_START_TIME + " <= ?",
					new String[] { Long.toString(startDate),
							Long.toString(endDate) }, null, null,
					TimeSliceRepository.COL_START_TIME);
			while (cur.moveToNext()) {
				final TimeSlice ts = this.fillTimeSliceFromCursor(cur);
				result.add(ts);
			}
		} finally {
			if (cur != null) {
				cur.close();
			}
		}

		return result;
	}

	private TimeSlice fillTimeSliceFromCursor(final Cursor cur) {
		final int categoryID = cur.getInt(cur
				.getColumnIndexOrThrow(TimeSliceRepository.COL_CATEGORY_ID));

		if (categoryID != 0) {
			final TimeSlice ts = new TimeSlice();
			ts.setRowId(cur.getInt(cur.getColumnIndexOrThrow("_id")));
			ts.setStartTime(cur.getLong(cur
					.getColumnIndexOrThrow(TimeSliceRepository.COL_START_TIME)));
			ts.setEndTime(cur.getLong(cur
					.getColumnIndexOrThrow(TimeSliceRepository.COL_END_TIME)));
			ts.setCategory(this.categoryRepository.fetchByRowID(categoryID));
			ts.setNotes(cur.getString(cur
					.getColumnIndexOrThrow(TimeSliceRepository.COL_NOTES)));
			return ts;
		}

		Log.w(Global.LOG_CONTEXT, "Ignoring timeslice with categoryID=0");
		return null;
	}

	private String[] columnList() {
		final List<String> columns = new ArrayList<String>();
		columns.add("_id");
		columns.add(TimeSliceRepository.COL_CATEGORY_ID);
		columns.add(TimeSliceRepository.COL_START_TIME);
		columns.add(TimeSliceRepository.COL_END_TIME);
		columns.add(TimeSliceRepository.COL_NOTES);
		return columns.toArray(new String[0]);
	}

	private ContentValues timeSliceContentValuesList(final TimeSlice timeSlice) {
		final ContentValues values = new ContentValues();
		values.put(TimeSliceRepository.COL_CATEGORY_ID,
				timeSlice.getCategoryId());
		values.put(TimeSliceRepository.COL_START_TIME, timeSlice.getStartTime());
		values.put(TimeSliceRepository.COL_END_TIME, timeSlice.getEndTime());
		values.put(TimeSliceRepository.COL_NOTES, timeSlice.getNotes());
		return values;
	}

}
