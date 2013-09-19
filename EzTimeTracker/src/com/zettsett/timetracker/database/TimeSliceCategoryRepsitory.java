package com.zettsett.timetracker.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class TimeSliceCategoryRepsitory {

	private static final String COL_ID = "_id";
	private static final String COL_CATEGORY_NAME = "category_name";
	private static final String COL_DESCRIPTION = "description";
	private static final String COL_END_TIME = "end_time";
	private static final String COL_START_TIME = "start_time";

	private static final DatabaseInstance CURRENT_DB_INSTANCE = DatabaseInstance.getCurrentInstance();
	private final Context context;
	
	public TimeSliceCategoryRepsitory(Context context) {
		super();
		this.context = context;
	}

	private ContentValues timeSliceCategoryContentValuesList(
			final TimeSliceCategory category) {
		ContentValues values = new ContentValues();
		values.put(COL_CATEGORY_NAME, category.getCategoryName());
		values.put(COL_DESCRIPTION, category.getDescription());
		
		if (DatabaseHelper.DATABASE_VERSION >= DatabaseHelper.DATABASE_VERSION_4_CATEGORY_ACTIVE) {
			values.put(COL_START_TIME, category.getStartTime());
			values.put(COL_END_TIME, category.getEndTime());
		}
		return values;
	}

    public TimeSliceCategory getOrCreateTimeSlice(String name) {
    	Cursor cur = null;
    	try
    	{
	    	cur = CURRENT_DB_INSTANCE.getDb().query(
					DatabaseHelper.TIME_SLICE_CATEGORY_TABLE, columnList(),
					"category_name = ?" , new String[] {name},
					null, null, null);
			if (cur.moveToNext()) {
				 return this.fillTimeSliceCategoryFromCursor(cur);
			}
    	} finally {
    		if (cur != null)
    			cur.close();
    	}
		
		return this.createTimeSliceCategoryFromName(name);
    }
    
    /**
     * Creates new TimeSliceCategory in database.
     * @return newID generated for dbItem.
     */
	public long createTimeSliceCategory(final TimeSliceCategory category) {

		long newID = CURRENT_DB_INSTANCE.getDb().insert(
				DatabaseHelper.TIME_SLICE_CATEGORY_TABLE, null,
				timeSliceCategoryContentValuesList(category));
		category.setRowId((int)newID);
		return newID;
	}

	private TimeSliceCategory createTimeSliceCategoryFromName(String name) {
		TimeSliceCategory category = new TimeSliceCategory();
		category.setCategoryName(name);
		createTimeSliceCategory(category);
		return category;
	}

	/**
	 * Internal helper that returns all colums supported by dbmodell
	 */
	private String[] columnList() {
		List<String> columns = new ArrayList<String>();
		columns.add(COL_ID);
		columns.add(COL_CATEGORY_NAME);
		columns.add(COL_DESCRIPTION);
		
		if (DatabaseHelper.DATABASE_VERSION >= DatabaseHelper.DATABASE_VERSION_4_CATEGORY_ACTIVE) {
			columns.add(COL_START_TIME);
			columns.add(COL_END_TIME);
		}
		return columns.toArray(new String[0]);
	}

	public List<TimeSliceCategory> fetchAllTimeSliceCategories(long currentDateTime) {
		List<TimeSliceCategory> result = new ArrayList<TimeSliceCategory>();
		Cursor cur = null;
		try
		{
			cur = CURRENT_DB_INSTANCE.getDb().query(
					DatabaseHelper.TIME_SLICE_CATEGORY_TABLE, columnList(), null,
					null, null, null, COL_CATEGORY_NAME); // order by name
			while (cur.moveToNext()) {
				TimeSliceCategory cat = fillTimeSliceCategoryFromCursor(cur);
				result.add(cat);
			}
		} finally {
			if (cur != null)
				cur.close();
		}
		
		// Database does not contain categories yet, create them
		if (result.size() == 0) {
			createInitialDemoCategoriesFromResources();
			result = fetchAllTimeSliceCategories(currentDateTime); // reload the demo items
		}
		return result;
	}

	private TimeSliceCategory fillTimeSliceCategoryFromCursor(Cursor cur) {
		TimeSliceCategory cat = new TimeSliceCategory();
		if(!cur.isAfterLast()) {
			cat.setRowId(cur.getInt(cur.getColumnIndexOrThrow(COL_ID)));
			cat.setCategoryName(cur.getString(cur.getColumnIndex(COL_CATEGORY_NAME)));
			cat.setDescription(cur.getString(cur.getColumnIndex(COL_DESCRIPTION)));
			
			if (DatabaseHelper.DATABASE_VERSION >= DatabaseHelper.DATABASE_VERSION_4_CATEGORY_ACTIVE) {
				cat.setStartTime(getLong(cur,COL_START_TIME, TimeSliceCategory.MIN_VALID_DATE));
				cat.setEndTime(getLong(cur,COL_END_TIME, TimeSliceCategory.MAX_VALID_DATE));
			}			
		}
		return cat;
	}

	/**
	 * get long value from cursor.
	 * @param nullValue returned if column is null
	 * @return nullValue if column is null
	 */
	private long getLong(Cursor cursor, String columnName, long nullValue) {
		int colIndex = cursor.getColumnIndexOrThrow(columnName);
		if (!cursor.isNull(colIndex)) {
			return cursor.getLong(colIndex);
		}
		return nullValue;
	}

	/**
	 * Delete item by rowID.
	 * @param rowId
	 * @return true if item found and deleted.
	 */
	public boolean delete(final long rowId) {
		return CURRENT_DB_INSTANCE.getDb().delete(
				DatabaseHelper.TIME_SLICE_CATEGORY_TABLE, "_id=" + rowId, null) > 0;
	}

	public long update(final TimeSliceCategory timeSliceCategory) {
		return CURRENT_DB_INSTANCE.getDb().update(
				DatabaseHelper.TIME_SLICE_CATEGORY_TABLE,
				this.timeSliceCategoryContentValuesList(timeSliceCategory),
				"_id = " + timeSliceCategory.getRowId(), null);
	}

	public TimeSliceCategory fetchByRowID(final long rowId) throws SQLException {

		Cursor cur = null;
		
		try
		{
			cur = CURRENT_DB_INSTANCE.getDb().query(true,
				DatabaseHelper.TIME_SLICE_CATEGORY_TABLE, columnList(),
				"_id = ?", new String[] {Long.toString(rowId)},  
				null, null, null, null);
			if ((cur != null) && (cur.moveToFirst())) {
				return fillTimeSliceCategoryFromCursor(cur);
			}
		} finally {
			if (cur != null)
				cur.close();
		}
		Log.e(Global.LOG_CONTEXT, "Not found : TimeSliceCategoryDBAdapter.fetchByRowID(rowId = " + rowId + ")");
		return null;
	}

	
	private void createInitialDemoCategoriesFromResources() {
		Resources res = context.getResources();
		String[] catNames = res.getStringArray(R.array.default_categories);
		for(String catName : catNames) {
			createTimeSliceCategoryFromName(catName);
		}
	}

}
