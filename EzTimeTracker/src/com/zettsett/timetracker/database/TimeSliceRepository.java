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
import com.zettsett.timetracker.model.ITimeSliceFilter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class TimeSliceRepository {
	private static final String COL_END_TIME = "end_time";
	private static final String COL_START_TIME = "start_time";
	private static final DatabaseInstance CURRENT_DB_INSTANCE = DatabaseInstance.getCurrentInstance();
	private static TimeSliceRepository timeSliceRepositorySingleton;
	private final TimeSliceCategoryRepsitory categoryRepository;
	
	public TimeSliceRepository(Context context) {
		CURRENT_DB_INSTANCE.initialize(context);
		categoryRepository = new TimeSliceCategoryRepsitory(context);	
	}
	
	public static TimeSliceRepository getTimeSliceDBAdapter(Context context) {
		if(timeSliceRepositorySingleton == null) {
			timeSliceRepositorySingleton = new TimeSliceRepository(context);
		}		
		return timeSliceRepositorySingleton;
	}
	
	public long createTimeSlice(final TimeSlice timeSlice) {
		long startTime = timeSlice.getStartTime();
		TimeSlice oldTimeSlice = findTimesliceByCategoryAndEndTimeInterval(
				timeSlice.getCategory(), 
				startTime - Settings.getMinminTrashholdInMilliSecs(), startTime);
		
		if (oldTimeSlice != null)
		{
			oldTimeSlice.setEndTime(timeSlice.getEndTime());
			oldTimeSlice.setNotes(oldTimeSlice.getNotes() + " " + timeSlice.getNotes());
			
			Log.d(Global.LOG_CONTEXT, "db-updating exising timeslice '"+ oldTimeSlice +"' from '" + timeSlice +"'.");
			CURRENT_DB_INSTANCE.getDb().update(
					DatabaseHelper.TIME_SLICE_TABLE, timeSliceContentValuesList(oldTimeSlice), "_id=?",
					new String[] {Long.toString(oldTimeSlice.getRowId())});
			return oldTimeSlice.getRowId();
		} else {
			Log.d(Global.LOG_CONTEXT, "db-inserting new timeslice '"+ timeSlice +"'.");
			return CURRENT_DB_INSTANCE.getDb().insert(DatabaseHelper.TIME_SLICE_TABLE,
					null, timeSliceContentValuesList(timeSlice));
		}
	}
	
	private TimeSlice findTimesliceByCategoryAndEndTimeInterval(
			TimeSliceCategory category, long minimumEndDate, long maximumEndDate) {
		Cursor cur = null;
		try {
			cur = CURRENT_DB_INSTANCE.getDb().query(true,
					DatabaseHelper.TIME_SLICE_TABLE, columnList(),
					"category_id=? AND end_time>=?  AND end_time<=?", 
					new String[] {Long.toString(category.getRowId()), Long.toString(minimumEndDate), Long.toString(maximumEndDate)}, null, null, null, null);
			if ((cur != null) && (cur.moveToFirst())) {
				return fillTimeSliceFromCursor(cur);
			}
    	} finally {
    		if (cur != null)
    			cur.close();
    	}
		Log.d(Global.LOG_CONTEXT, "Not found : TimeSliceDBAdapter.findTimesliceByCategoryAndEndTimeBiggerThan(...)");
		return null;
	}

	public long updateTimeSlice(final TimeSlice timeSlice) {
		return CURRENT_DB_INSTANCE.getDb().update(DatabaseHelper.TIME_SLICE_TABLE,
				timeSliceContentValuesList(timeSlice),"_id = " + timeSlice.getRowId(), null);
	}

	/**
	 * Counts how many TimeSlice items exist that matches the filter
	 * @return
	 */
	public static int getCount(ITimeSliceFilter filterParam, boolean ignoreDates) {
		String filter = createFilter(filterParam, ignoreDates);
		Cursor cur = null;
		try {
			cur = CURRENT_DB_INSTANCE.getDb().query(DatabaseHelper.TIME_SLICE_TABLE, new String[] {"COUNT(*)"}, filter, null, null, null, null);
			if ((cur != null) && (cur.moveToFirst())) {
				int count = cur.getInt(0);
				Log.d(Global.LOG_CONTEXT, "TimeSliceDBAdapter.getCount(" + filter + ") = " + count);
				return count;
			}
    	} finally {
    		if (cur != null)
    			cur.close();
    	}
		Log.e(Global.LOG_CONTEXT, "Not found : TimeSliceDBAdapter.getCount(" + filter + ")");
		return -1;
		
	}

	/**
	 * Totals those TimeSlice-Durations that matches the filter
	 * @return
	 */
	public static double getTotalDurationInHours(ITimeSliceFilter filterParam, boolean ignoreDates) {
		String filter = createFilter(filterParam, ignoreDates);
		Cursor cur = null;
		try {
			cur = CURRENT_DB_INSTANCE.getDb().query(DatabaseHelper.TIME_SLICE_TABLE, new String[] {
					"SUM(" + COL_END_TIME + "-" + COL_START_TIME + ")"}, filter, null, null, null, null);
			if ((cur != null) && (cur.moveToFirst())) {
				double duration = cur.getLong(0) / (1000.0*60*60);
				Log.d(Global.LOG_CONTEXT, "TimeSliceDBAdapter.getTotalDurationInHours(" + filter + ") = " + duration);
				return duration;
			}
    	} finally {
    		if (cur != null)
    			cur.close();
    	}
		Log.e(Global.LOG_CONTEXT, "Not found : TimeSliceDBAdapter.getTotalDurationInHours(" + filter + ")");
		return 0.0;
		
	}

	public boolean delete(final long rowId) {
        return CURRENT_DB_INSTANCE.getDb().delete(DatabaseHelper.TIME_SLICE_TABLE,  "_id=" + rowId, null) > 0;
    }

	public static int deleteForDateRange(ITimeSliceFilter filterParam, boolean ignoreDates) {
		String filter = createFilter(filterParam, ignoreDates);
		return CURRENT_DB_INSTANCE.getDb().delete(DatabaseHelper.TIME_SLICE_TABLE,
				filter, null);
	}

	public static String createFilter(ITimeSliceFilter filter, boolean ignoreDates) {
		return createFilter(filter.getStartTime(), filter.getEndTime(), filter.getCategoryId(), ignoreDates);
	}
	
	public static String createFilter(long startDate, long endDate,
			long categoryId, boolean ignoreDates) {
		StringBuilder filter = new StringBuilder();
		
		if (!ignoreDates) {
			add(filter, "start_time>=", startDate, TimeSlice.NO_TIME_VALUE);
			add(filter, "start_time <=", endDate, TimeSlice.NO_TIME_VALUE);
		}
		add(filter, "category_id =", categoryId, TimeSliceCategory.NOT_SAVED);
		if (filter.length() == 0)
		{
			return null;
		} else {
			return filter.toString();
		}
	}

    private static void add(StringBuilder filter, String field, long value, long emptyValue) {
    	if (value != emptyValue)
    	{
    		if (filter.length() > 0)
    			filter.append(" and ");
    		filter.append(field).append(value);
    	}
	}

	public TimeSlice fetchByRowID(final long rowId) throws SQLException {
		Cursor cur = null;
		try {
			cur = CURRENT_DB_INSTANCE.getDb().query(true,
					DatabaseHelper.TIME_SLICE_TABLE, columnList(),
					"_id=?", new String[] {Long.toString(rowId)}, null, null, null, null);
			if ((cur != null) && (cur.moveToFirst())) {
				return fillTimeSliceFromCursor(cur);
			}
    	} finally {
    		if (cur != null)
    			cur.close();
    	}
		Log.e(Global.LOG_CONTEXT, "Not found : TimeSliceDBAdapter.fetchByRowID(rowId = " + rowId + ")");
		return null;
	}

    public boolean categoryHasTimeSlices(final TimeSliceCategory category) throws SQLException {
		Cursor cur = null;
		try {
			cur = CURRENT_DB_INSTANCE.getDb().query(true,
				DatabaseHelper.TIME_SLICE_TABLE, columnList(),
				"category_id=?", new String[] {Long.toString(category.getRowId())}, null, null, null, null);
			if (cur.moveToNext()) {
				return true;
			}
			return false;
    	} finally {
    		if (cur != null)
    			cur.close();
    	}

	}

    public List<TimeSlice> fetchTimeSlices(ITimeSliceFilter filterParam, boolean ignoreDates) {
		String filter = createFilter(filterParam, ignoreDates);
		List<TimeSlice> result = new ArrayList<TimeSlice>(); 
		Cursor cur = null;
		try {
			cur = CURRENT_DB_INSTANCE.getDb().query(
				DatabaseHelper.TIME_SLICE_TABLE, columnList(), 
				filter 
				, null,
				null, null, COL_START_TIME);
			while (cur.moveToNext()) {
				TimeSlice ts = this.fillTimeSliceFromCursor(cur);
				result.add(ts);
			}
    	} finally {
    		if (cur != null)
    			cur.close();
    	}

		return result;
	}

	public List<TimeSlice> fetchTimeSlicesByDateRange(long startDate, long endDate) {
		List<TimeSlice> result = new ArrayList<TimeSlice>(); 
		Cursor cur = null;
		try {
			cur = CURRENT_DB_INSTANCE.getDb().query(
				DatabaseHelper.TIME_SLICE_TABLE, columnList(), 
				"start_time >= ? and start_time <= ?" 
				, new String[] {Long.toString(startDate),Long.toString(endDate)},
				null, null, COL_START_TIME);
			while (cur.moveToNext()) {
				TimeSlice ts = this.fillTimeSliceFromCursor(cur);
				result.add(ts);
			}
    	} finally {
    		if (cur != null)
    			cur.close();
    	}

		return result;
	}

	private TimeSlice fillTimeSliceFromCursor(Cursor cur) {
		int categoryID = cur.getInt(cur
				.getColumnIndexOrThrow("category_id"));
		
		if (categoryID != 0) {
			TimeSlice ts = new TimeSlice();
			ts.setRowId(cur.getInt(cur.getColumnIndexOrThrow("_id")));
			ts.setStartTime(cur
					.getLong(cur.getColumnIndexOrThrow(COL_START_TIME)));
			ts.setEndTime(cur.getLong(cur.getColumnIndexOrThrow(COL_END_TIME)));
			ts.setCategory(categoryRepository.fetchByRowID(categoryID));
			ts.setNotes(cur.getString(cur.getColumnIndexOrThrow("notes")));
			return ts;
		}
		
		Log.w(Global.LOG_CONTEXT, "Ignoring timeslice with categoryID=0");
		return null;
	}

	private String[] columnList() {
		List<String> columns = new ArrayList<String>();
		columns.add("_id");
		columns.add("category_id");
		columns.add(COL_START_TIME);
		columns.add(COL_END_TIME);
		columns.add("notes");
		return columns.toArray(new String[0]);
	}

	private ContentValues timeSliceContentValuesList(final TimeSlice timeSlice) {
		ContentValues values = new ContentValues();
		values.put("category_id", timeSlice.getCategoryId());
		values.put(COL_START_TIME, timeSlice.getStartTime());
		values.put(COL_END_TIME, timeSlice.getEndTime());
		values.put("notes", timeSlice.getNotes());
		return values;
	}

}
