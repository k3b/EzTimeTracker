package com.zettsett.timetracker.model;

import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.database.TimeSliceCategoryRepsitory;

public class TimeSliceCategory implements Serializable, Comparable<TimeSliceCategory>{
	private static final long serialVersionUID = 4899523432240132519L;

	public static final int NOT_SAVED = -1;
	public static final TimeSliceCategory NO_CATEGORY = new TimeSliceCategory(NOT_SAVED, "?");

	public static final long MIN_VALID_DATE = 0;
	public static final long MAX_VALID_DATE = Long.MAX_VALUE;
	
	private int rowId = NOT_SAVED;
	
	private String categoryName;
	
	private String description;

	private long startTime = MIN_VALID_DATE;

	private long endTime = MAX_VALID_DATE;

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

	public long getStartTime() {
		return startTime;
	}

	public TimeSliceCategory setStartTime(long startTime) {
		this.startTime = startTime;
		return this;
	}

	public long getEndTime() {
		return endTime;
	}

	public TimeSliceCategory setEndTime(long endTime) {
		this.endTime = endTime;
		return this;
	}

	public String getStartDateStr() {
		if (startTime == MIN_VALID_DATE) {
			return "";
		} else {
			return DateTimeFormatter.getShortDateStr(startTime);
		}
	}

	public String getEndTimeStr() {
		if (endTime == MAX_VALID_DATE) {
			return "";
		} else {
			return DateTimeFormatter.getShortDateStr(endTime);
		}
	}

	public String getActiveDate() {
		String start = getStartDateStr();
		String end = getEndTimeStr();
		if ((start.length() == 0) && (end.length() == 0)) {
			return "";
		} else {
			return start + "-" + end;
		}
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

	@Override
	public int compareTo(TimeSliceCategory anotherTimeSliceCategory) {
		return this.categoryName.compareTo(anotherTimeSliceCategory.categoryName);
	}

	/**
	 * @param currentDateTime dateTime when isActive should be tested
	 * @return true if currentDateTime is between start and end
	 */
	public boolean isActive(long currentDateTime) {
		if (currentDateTime == TimeSliceCategory.MIN_VALID_DATE) {
			return true;
		}

		return ((currentDateTime >= this.getStartTime()) && (currentDateTime <= this.getEndTime()));
	}

	
}
