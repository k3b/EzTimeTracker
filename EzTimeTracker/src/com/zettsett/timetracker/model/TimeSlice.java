package com.zettsett.timetracker.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.zettsett.timetracker.DateTimeFormatter;

public class TimeSlice implements Serializable {
	private static final long serialVersionUID = 6586305797483181442L;

	private int rowId;

	private long startTime = 0;

	private long endTime = 0;

	private TimeSliceCategory category;

	private String notes;

	private static Calendar calendar = new GregorianCalendar();

	public static final int IS_NEW_TIMESLICE = -1;

	public int getRowId() {
		return rowId;
	}

	public void setRowId(int rowId) {
		this.rowId = rowId;
	}

	public long getDurationInMilliseconds() {
		return endTime - startTime;
	}

	public String getStartDateStr() {
		return DateTimeFormatter.getLongDateStr(startTime);
	}

	public String getStartMonthStr() {
		return DateTimeFormatter.getMonthStr(startTime);
	}

	// TODO make getStartWeekStr() work with non american locale
	public String getStartWeekStr() {
		long startTime = this.startTime;
		return DateTimeFormatter.getWeekStr(startTime);
	}

	public int getStartTimeComponent(int componentId) {
		calendar.setTimeInMillis(startTime);
		return calendar.get(componentId);
	}

	public void setStartTimeComponent(int componentId, int value) {
		calendar.setTimeInMillis(startTime);
		calendar.set(componentId, value);
		startTime = calendar.getTimeInMillis();
	}

	public int getEndTimeComponent(int componentId) {
		calendar.setTimeInMillis(endTime);
		return calendar.get(componentId);
	}

	public void setEndTimeComponent(int componentId, int value) {
		calendar.setTimeInMillis(endTime);
		calendar.set(componentId, value);
		endTime = calendar.getTimeInMillis();
	}

	public String getStartTimeStr() {
		return DateTimeFormatter.getTimeString(startTime);
	}

	public String getEndTimeStr() {
		if (startTime == 0) {
			return "";
		} else {
			return DateTimeFormatter.getTimeString(endTime);
		}
	}

	public TimeSliceCategory getCategory() {
		return category;
	}

	public void setCategory(TimeSliceCategory category) {
		this.category = category;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public String getTitleWithDuration() {
		return getCategoryName() + ": " 
				+ getStartTimeStr() + " - " + getEndTimeStr()
				+ " (" + DateTimeFormatter.hrColMin(getDurationInMilliseconds(), true,true) + ")";
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

	public String getNotes() {
		if (notes != null) {
			return notes;
		} else {
			return "";
		}
	}

	public void setNotes(String notes) {
		this.notes = (notes != null) ? notes.trim() : "";
	}

	public boolean isPunchedIn() {
		return (this.getStartTime() != 0) && (this.getEndTime() == 0);
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
