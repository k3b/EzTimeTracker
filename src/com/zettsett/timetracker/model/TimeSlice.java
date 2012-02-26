package com.zettsett.timetracker.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.text.format.DateFormat;

import com.zettsett.timetracker.DateTimeFormatter;

public class TimeSlice implements Serializable {
	private static final long serialVersionUID = 6586305797483181442L;

	private int rowId;

	private long startTime = 0;

	private long endTime = 0;

	private TimeSliceCategory category;

	private String notes;

	private static Calendar calendar = new GregorianCalendar();

	public static final long MILLIS_IN_A_DAY = 24 * 60 * 60 * 1000;

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
		return getDateStr(startTime);
	}

	public String getStartMonthStr() {
		if (startTime == 0) {
			return "";
		} else {
			return DateFormat.format("MMMM yyyy", startTime).toString();
		}
	}

	// TODO make getStartWeekStr() work with non american locale
	public String getStartWeekStr() {
		if (startTime == 0) {
			return "";
		} else {
			long firstDayOfWeekDate = startTime;
			CharSequence dayOfWeek = DateFormat.format("E", startTime);
			if (dayOfWeek.equals("Mon")) {
				firstDayOfWeekDate -= MILLIS_IN_A_DAY;
			} else if (dayOfWeek.equals("Tue")) {
				firstDayOfWeekDate -= MILLIS_IN_A_DAY * 2;
			} else if (dayOfWeek.equals("Wed")) {
				firstDayOfWeekDate -= MILLIS_IN_A_DAY * 3;
			} else if (dayOfWeek.equals("Thu")) {
				firstDayOfWeekDate -= MILLIS_IN_A_DAY * 4;
			} else if (dayOfWeek.equals("Fri")) {
				firstDayOfWeekDate -= MILLIS_IN_A_DAY * 5;
			} else if (dayOfWeek.equals("Sat")) {
				firstDayOfWeekDate -= MILLIS_IN_A_DAY * 6;
			}
			return "Week of " + DateFormat.format("dd MMMM yyyy", firstDayOfWeekDate).toString();
		}
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
		return getTimeString(startTime);
	}

	public String getEndTimeStr() {
		if (startTime == 0) {
			return "";
		} else {
			return getTimeString(endTime);
		}
	}

	public static String getTimeString(long dateTime) {
		if (dateTime == 0) {
			return "";
		} else {
			return DateTimeFormatter.formatTimePerCurrentSettings(dateTime).toString();
		}
	}

	public static String getDateStr(long dateTime) {
		if (dateTime == 0) {
			return "";
		} else {
			return DateFormat.format("E dd.MM.yyyy", dateTime).toString();
		}
	}

	public static String getDateTimeStr(long dateTime) {
		if (dateTime == 0) {
			return "";
		} else {
			return getDateStr(dateTime) + " " + getTimeString(dateTime);
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
		return ((getCategory() != null) ? getCategory().getCategoryName() : "???") + ": " 
				+ getStartTimeStr() + " - " + getEndTimeStr()
				+ " (" + DateTimeFormatter.hrColMin(getDurationInMilliseconds(), true,true) + ")";
	}

	public String getTitle() {
		return getCategory().getCategoryName() + ": " + getStartTimeStr() + " - " + getEndTimeStr();
	}

	public String getNotes() {
		if (notes != null) {
			return notes;
		} else {
			return "";
		}
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	@Override public String toString() {
		return getTitleWithDuration();
	}

}
