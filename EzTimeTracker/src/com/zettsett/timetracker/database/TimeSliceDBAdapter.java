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
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class TimeSliceDBAdapter {
	private static final DatabaseInstance CURRENT_DB_INSTANCE = DatabaseInstance.getCurrentInstance();
	private static TimeSliceDBAdapter timeSliceDBAdapterSingleton;
	private final TimeSliceCategoryDBAdapter categoryDBAdapter;
	
	public TimeSliceDBAdapter(Context context) {
		CURRENT_DB_INSTANCE.initialize(context);
		categoryDBAdapter = new TimeSliceCategoryDBAdapter(context);	
	}
	
	public static TimeSliceDBAdapter getTimeSliceDBAdapter(Context context) {
		if(timeSliceDBAdapterSingleton == null) {
			timeSliceDBAdapterSingleton = new TimeSliceDBAdapter(context);
		}		
		return timeSliceDBAdapterSingleton;
	}
	
	public long createTimeSlice(final TimeSlice timeSlice) {
		TimeSlice oldTimeSlice = findTimesliceByCategoryAndEndTimeBiggerThan(timeSlice.getCategory(), timeSlice.getStartTime() - Settings.getMinminTrashholdInMilliSecs());
		
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
	
	private TimeSlice findTimesliceByCategoryAndEndTimeBiggerThan(
			TimeSliceCategory category, long minimumEndDate) {
		Cursor cur = null;
		try {
			cur = CURRENT_DB_INSTANCE.getDb().query(true,
					DatabaseHelper.TIME_SLICE_TABLE, columnList(),
					"category_id=? AND end_time>?", new String[] {Long.toString(category.getRowId()), Long.toString(minimumEndDate)}, null, null, null, null);
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

	public boolean delete(final long rowId) {
        return CURRENT_DB_INSTANCE.getDb().delete(DatabaseHelper.TIME_SLICE_TABLE,  "_id=" + rowId, null) > 0;
    }

	public int deleteForDateRange(long startDate, long endDate, long categoryId) {
		StringBuilder filter = new StringBuilder();
		
		add(filter, "start_time>=", startDate, TimeSlice.NO_TIME_VALUE);
		add(filter, "start_time <=", endDate, TimeSlice.NO_TIME_VALUE);
		add(filter, "category_id =", categoryId, TimeSliceCategory.NOT_SAVED);
		return CURRENT_DB_INSTANCE.getDb().delete(DatabaseHelper.TIME_SLICE_TABLE,
				filter.toString(), null);
	}

    private void add(StringBuilder filter, String field, long value, long emptyValue) {
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

    public List<TimeSlice> fetchAllTimeSlices() {
		List<TimeSlice> result = new ArrayList<TimeSlice>(); 
		Cursor cur = null;
		try {
			cur = CURRENT_DB_INSTANCE.getDb().query(
				DatabaseHelper.TIME_SLICE_TABLE, columnList(), null, null,
				null, null, null);
			while (cur.moveToNext()) {
				TimeSlice ts = this.fillTimeSliceFromCursor(cur);
				
				if (ts != null) {
					result.add(ts);
				}
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
				null, null, "start_time");
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
					.getLong(cur.getColumnIndexOrThrow("start_time")));
			ts.setEndTime(cur.getLong(cur.getColumnIndexOrThrow("end_time")));
			ts.setCategory(categoryDBAdapter.fetchByRowID(categoryID));
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
		columns.add("start_time");
		columns.add("end_time");
		columns.add("notes");
		return columns.toArray(new String[0]);
	}

	private ContentValues timeSliceContentValuesList(final TimeSlice timeSlice) {
		ContentValues values = new ContentValues();
		values.put("category_id", timeSlice.getCategory().getRowId());
		values.put("start_time", timeSlice.getStartTime());
		values.put("end_time", timeSlice.getEndTime());
		values.put("notes", timeSlice.getNotes());
		return values;
	}

}
