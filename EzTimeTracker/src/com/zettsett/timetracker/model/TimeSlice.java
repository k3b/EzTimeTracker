package com.zettsett.timetracker.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.zettsett.timetracker.DateTimeFormatter;

import de.k3b.common.IItemWithRowId;
import de.k3b.common.ItemWithRowId;

public class TimeSlice extends ItemWithRowId implements Serializable,
		ITimeSliceFilter, IItemWithRowId {
	private static final long serialVersionUID = 6586305797483181442L;

	public static final TimeSlice EMPTY = new TimeSlice(
			TimeSlice.IS_NEW_TIMESLICE);

	public static final long NO_TIME_VALUE = 0;

	private long startTime = TimeSlice.NO_TIME_VALUE;

	private long endTime = TimeSlice.NO_TIME_VALUE;

	private TimeSliceCategory category;

	private String notes;

	private static Calendar calendar = new GregorianCalendar();

	public TimeSlice() {

	}

	public TimeSlice(final int rowId) {
		this.setRowId(rowId);

	}

	@Override
	public long getStartTime() {
		return this.startTime;
	}

	public TimeSlice setStartTime(final long startTime) {
		this.startTime = startTime;
		return this;
	}

	public String getStartDateStr() {
		return DateTimeFormatter.getInstance().getLongDateStr(this.startTime);
	}

	public int getStartTimeComponent(final int componentId) {
		TimeSlice.calendar.setTimeInMillis(this.startTime);
		return TimeSlice.calendar.get(componentId);
	}

	public String getStartTimeStr() {
		return DateTimeFormatter.getInstance().getTimeString(this.startTime);
	}

	public String getEndTimeStr() {
		if (this.startTime == TimeSlice.NO_TIME_VALUE) {
			return "";
		} else {
			return DateTimeFormatter.getInstance().getTimeString(this.endTime);
		}
	}

	@Override
	public long getEndTime() {
		return this.endTime;
	}

	public TimeSlice setEndTime(final long endTime) {
		this.endTime = endTime;
		return this;
	}

	public long getDurationInMilliseconds() {
		return this.endTime - this.startTime;
	}

	@Override
	public int getCategoryId() {
		final TimeSliceCategory category = this.getCategory();
		return (category != null) ? category.getRowId()
				: TimeSliceCategory.NOT_SAVED;
	}

	public TimeSliceCategory getCategory() {
		return this.category;
	}

	public TimeSlice setCategory(final TimeSliceCategory category) {
		this.category = category;
		return this;
	}

	public String getCategoryName() {
		return (this.getCategory() != null) ? this.getCategory()
				.getCategoryName() : "???";
	}

	public Object getCategoryDescription() {
		return (this.getCategory() != null) ? this.getCategory()
				.getDescription() : "???";
	}

	public String getTitle() {
		return this.getCategoryName() + ": " + this.getStartTimeStr() + " - "
				+ this.getEndTimeStr();
	}

	public String getTitleWithDuration() {
		return this.getCategoryName()
				+ ": "
				+ this.getStartTimeStr()
				+ " - "
				+ this.getEndTimeStr()
				+ " ("
				+ DateTimeFormatter.getInstance().hrColMin(
						this.getDurationInMilliseconds(), true, true) + ")";
	}

	public String getNotes() {
		if (this.notes != null) {
			return this.notes;
		} else {
			return "";
		}
	}

	public TimeSlice setNotes(final String notes) {
		this.notes = (notes != null) ? notes.trim() : "";
		return this;
	}

	public boolean hasNotes() {
		return (this.notes != null) && (this.notes.length() > 0);
	}

	public boolean isPunchedIn() {
		return (this.getStartTime() != TimeSlice.NO_TIME_VALUE)
				&& (this.getEndTime() == TimeSlice.NO_TIME_VALUE);
	}

	public void load(final TimeSlice source) {
		if (source != null) {
			this.setRowId(source.getRowId());
			this.setCategory(source.getCategory());
			this.setStartTime(source.getStartTime());
			this.setEndTime(source.getEndTime());
			this.setNotes(source.getNotes());
		}
	}

	@Override
	public String toString() {
		return this.getTitleWithDuration() + ":" + this.getNotes();
	}

}
