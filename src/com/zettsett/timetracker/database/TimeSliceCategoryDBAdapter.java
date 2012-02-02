package com.zettsett.timetracker.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class TimeSliceCategoryDBAdapter {

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

	public long createTimeSliceCategory(final TimeSliceCategory category) {

		return DatabaseInstance.getDb().insert(
				DatabaseHelper.TIME_SLICE_CATEGORY_TABLE, null,
				timeSliceCategoryContentValuesList(category));
	}

	private void createTimeSliceCategoryFromName(String name) {
		TimeSliceCategory category = new TimeSliceCategory();
		category.setCategoryName(name);
		createTimeSliceCategory(category);
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
		Cursor cur = DatabaseInstance.getDb().query(
				DatabaseHelper.TIME_SLICE_CATEGORY_TABLE, columnList(), null,
				null, null, null, "category_name");
		while (cur.moveToNext()) {
			TimeSliceCategory cat = fillTimeSliceCategoryFromCursor(cur);
			result.add(cat);
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
		return DatabaseInstance.getDb().delete(
				DatabaseHelper.TIME_SLICE_CATEGORY_TABLE, "_id=" + rowId, null) > 0;
	}

	public long update(final TimeSliceCategory timeSliceCategory) {
		return DatabaseInstance.getDb().update(
				DatabaseHelper.TIME_SLICE_CATEGORY_TABLE,
				this.timeSliceCategoryContentValuesList(timeSliceCategory),
				"_id = " + timeSliceCategory.getRowId(), null);
	}

	public TimeSliceCategory fetchByRowID(final long rowId) throws SQLException {

		Cursor cur = DatabaseInstance.getDb().query(true,
				DatabaseHelper.TIME_SLICE_CATEGORY_TABLE, columnList(),
				"_id=" + rowId, null, null, null, null, null);
		if (cur != null) {
			cur.moveToFirst();
		}
		return fillTimeSliceCategoryFromCursor(cur);

	}

	
	private void initialize() {
		Resources res = context.getResources();
		String[] catNames = res.getStringArray(R.array.default_categories);
		for(String catName : catNames) {
			createTimeSliceCategoryFromName(catName);
		}
	}

}
