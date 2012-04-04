package com.zettsett.timetracker.model;

import java.io.Serializable;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.zettsett.timetracker.database.TimeSliceCategoryDBAdapter;

public class TimeSliceCategory implements Serializable, Comparable<TimeSliceCategory>{
	private static final long serialVersionUID = 4899523432240132519L;

	public static final int NOT_SAVED = -1;
	public static final TimeSliceCategory NO_CATEGORY = new TimeSliceCategory(NOT_SAVED, "?");

	private int rowId = NOT_SAVED;
	
	private String categoryName;
	
	private String description;

	public TimeSliceCategory()
	{
		this(NOT_SAVED, null);
	}
	
	public TimeSliceCategory(int id, String name) {
		setRowId(id);
		setCategoryName(name);
	}

	public int getRowId() {
		return rowId;
	}

	public void setRowId(int rowId) {
		this.rowId = rowId;
	}

	public String getCategoryName() {
		if(categoryName == null) {
			return "N/A";
		}
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getDescription() {
		if(description == null) {
			return "";
		}
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return categoryName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((categoryName == null) ? 0 : categoryName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeSliceCategory other = (TimeSliceCategory) obj;
		if (categoryName == null) {
			if (other.categoryName != null)
				return false;
		} else if (!categoryName.equals(other.categoryName))
			return false;
		return true;
	}

	public static ArrayAdapter<TimeSliceCategory> getCategoryAdapter(Context context) {
		TimeSliceCategoryDBAdapter timeSliceCategoryDBAdapter = new TimeSliceCategoryDBAdapter(context);
		
		TimeSliceCategory[] durationCategories = timeSliceCategoryDBAdapter
				.fetchAllTimeSliceCategories()
				.toArray(new TimeSliceCategory[0]);
		return new ArrayAdapter<TimeSliceCategory>(
				context, android.R.layout.simple_spinner_item, durationCategories);
		
	}

	@Override
	public int compareTo(TimeSliceCategory anotherTimeSliceCategory) {
		return this.categoryName.compareTo(anotherTimeSliceCategory.categoryName);
	}

	
}
