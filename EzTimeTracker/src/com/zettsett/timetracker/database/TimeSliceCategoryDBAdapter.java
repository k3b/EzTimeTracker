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

public class TimeSliceCategoryDBAdapter {

	private static final DatabaseInstance CURRENT_DB_INSTANCE = DatabaseInstance.getCurrentInstance();
	private final Context context;
	
	public TimeSliceCategoryDBAdapter(Context context) {
		super();
		this.context = context;
	}

	private ContentValues timeSliceCategoryContentValuesList(
			final TimeSliceCategory category) {
		ContentValues values = new ContentValues();
		values.put("category_name", category.getCategoryName());
		values.put("description", category.getDescription());

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

	private String[] columnList() {
		List<String> columns = new ArrayList<String>();
		columns.add("_id");
		columns.add("category_name");
		columns.add("description");
		return columns.toArray(new String[0]);
	}

	public List<TimeSliceCategory> fetchAllTimeSliceCategories() {
		List<TimeSliceCategory> result = new ArrayList<TimeSliceCategory>();
		Cursor cur = null;
		try
		{
			cur = CURRENT_DB_INSTANCE.getDb().query(
					DatabaseHelper.TIME_SLICE_CATEGORY_TABLE, columnList(), null,
					null, null, null, "category_name");
			while (cur.moveToNext()) {
				TimeSliceCategory cat = fillTimeSliceCategoryFromCursor(cur);
				result.add(cat);
			}
		} finally {
			if (cur != null)
				cur.close();
		}
		if (result.size() == 0) {
			initialize();
			result = fetchAllTimeSliceCategories();
		}
		return result;
	}

	private TimeSliceCategory fillTimeSliceCategoryFromCursor(Cursor cur) {
		TimeSliceCategory cat = new TimeSliceCategory();
		if(!cur.isAfterLast()) {
			cat.setRowId(cur.getInt(cur.getColumnIndexOrThrow("_id")));
			cat.setCategoryName(cur.getString(cur.getColumnIndex("category_name")));
			cat.setDescription(cur.getString(cur.getColumnIndex("description")));
		}
		return cat;
	}

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

	
	private void initialize() {
		Resources res = context.getResources();
		String[] catNames = res.getStringArray(R.array.default_categories);
		for(String catName : catNames) {
			createTimeSliceCategoryFromName(catName);
		}
	}

}
