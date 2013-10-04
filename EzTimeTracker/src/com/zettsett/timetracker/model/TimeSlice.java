package com.zettsett.timetracker.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.zettsett.timetracker.DateTimeFormatter;

public class TimeSlice implements Serializable, ITimeSliceFilter {
	private static final long serialVersionUID = 6586305797483181442L;
	
	public static final long NO_TIME_VALUE = 0;
	public static final int IS_NEW_TIMESLICE = -1;

	private int rowId = IS_NEW_TIMESLICE;

	private long startTime = NO_TIME_VALUE;

	private long endTime = NO_TIME_VALUE;

	private TimeSliceCategory category;

	private String notes;

	private static Calendar calendar = new GregorianCalendar();

	public int getRowId() {
		return rowId;
	}

	public TimeSlice setRowId(int rowId) {
		this.rowId = rowId;
		return this;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	public TimeSlice setStartTime(long startTime) {
		this.startTime = startTime;
		return this;
	}

	public String getStartDateStr() {
		return DateTimeFormatter.getInstance().getLongDateStr(startTime);
	}

	public int getStartTimeComponent(int componentId) {
		calendar.setTimeInMillis(startTime);
		return calendar.get(componentId);
	}

	public String getStartTimeStr() {
		return DateTimeFormatter.getInstance().getTimeString(startTime);
	}

	public String getEndTimeStr() {
		if (startTime == NO_TIME_VALUE) {
			return "";
		} else {
			return DateTimeFormatter.getInstance().getTimeString(endTime);
		}
	}

	@Override
	public long getEndTime() {
		return endTime;
	}

	public TimeSlice setEndTime(long endTime) {
		this.endTime = endTime;
		return this;
	}

	public long getDurationInMilliseconds() {
		return endTime - startTime;
	}

	@Override
	public int getCategoryId() {
		TimeSliceCategory category = getCategory();
		return (category != null) ? category.getRowId() : TimeSliceCategory.NOT_SAVED;
	}

	public TimeSliceCategory getCategory() {
		return category;
	}

	public TimeSlice setCategory(TimeSliceCategory category) {
		this.category = category;
		return this;
	}

	public String getCategoryName() {
		return (getCategory() != null) ? getCategory().getCategoryName() : "???";
	}

	public Object getCategoryDescription() {
		return (getCategory() != null) ? getCategory().getDescription() : "???";
	}

	public String getTitle() {
		return getCategoryName() + ": " + getStartTimeStr() + " - " + getEndTimeStr();
	}

	public String getTitleWithDuration() {
		return getCategoryName() + ": " 
				+ getStartTimeStr() + " - " + getEndTimeStr()
				+ " (" + DateTimeFormatter.getInstance().hrColMin(getDurationInMilliseconds(), true,true) + ")";
	}

	public String getNotes() {
		if (notes != null) {
			return notes;
		} else {
			return "";
		}
	}

	public TimeSlice setNotes(String notes) {
		this.notes = (notes != null) ? notes.trim() : "";
		return this;
	}

	public boolean isPunchedIn() {
		return (this.getStartTime() != NO_TIME_VALUE) && (this.getEndTime() == NO_TIME_VALUE);
	}

	public void load(TimeSlice source)
	{
		if (source != null)
		{
			this.setRowId(source.getRowId());
			this.setCategory(source.getCategory());
			this.setStartTime(source.getStartTime());
			this.setEndTime(source.getEndTime());
			this.setNotes(source.getNotes());
		}
	}

	@Override public String toString() {
		return getTitleWithDuration();
	}
}
