package de.k3b.timetracker.activity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.k3b.timetracker.DateTimeFormatter;
import de.k3b.timetracker.model.ITimeSliceFilter;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;
import de.k3b.util.DateTimeUtil;

public class TimeSliceFilterParameter implements Serializable, ITimeSliceFilter {
	private static final long serialVersionUID = 6586305797483181492L;

	private boolean ignoreDates = false;
	private long startTime = TimeSlice.NO_TIME_VALUE;
	private long endTime = TimeSlice.NO_TIME_VALUE;
	private int categoryId = TimeSliceCategory.NOT_SAVED;
	private boolean notesNotNull = false;

	private String notes;

	public TimeSliceFilterParameter setParameter(final long startTime,
			final long endTime, final int categoryId) {
		this.setStartTime(startTime);
		this.setEndTime(endTime);
		this.setCategoryId(categoryId);

		return this;
	}

	public TimeSliceFilterParameter setParameter(final ITimeSliceFilter source) {
		if (source != null) {
			return this.setParameter(source.getStartTime(),
					source.getEndTime(), source.getCategoryId());
		}
		return this;
	}

	public TimeSliceFilterParameter setParameter(
			final TimeSliceFilterParameter source) {
		if (source != null) {
			return this.setParameter((ITimeSliceFilter) source)
					.setIgnoreDates(source.isIgnoreDates())
					.setNotes(source.getNotes())
					.setNotesNotNull(source.isNotesNotNull());
		}
		return this;
	}

	public TimeSliceFilterParameter setIgnoreDates(final boolean mIgnoreDates) {
		this.ignoreDates = mIgnoreDates;
		return this;
	}

	public boolean isIgnoreDates() {
		return this.ignoreDates;
	}

	public TimeSliceFilterParameter setStartTime(final long startTime) {
		this.startTime = startTime;
		return this;
	}

	@Override
	public long getStartTime() {
		return this.startTime;
	}

	public TimeSliceFilterParameter setEndTime(final long endTime) {
		this.endTime = endTime;
		return this;
	}

	@Override
	public long getEndTime() {
		return this.endTime;
	}

	public TimeSliceFilterParameter setCategoryId(final int categoryId) {
		this.categoryId = categoryId;
		return this;
	}

	@Override
	public int getCategoryId() {
		return this.categoryId;
	}

	public TimeSliceFilterParameter setNotesNotNull(final boolean checked) {
		this.notesNotNull = checked;
		return this;
	}

	public boolean isNotesNotNull() {
		return this.notesNotNull;
	}

	public TimeSliceFilterParameter setNotes(final String notes) {
		this.notes = (notes != null) ? notes.trim() : "";
		return this;
	}

	public String getNotes() {
		if (this.notes != null) {
			return this.notes;
		} else {
			return "";
		}
	}

	@Override
	public String toString() {
		final int categoryId = this.getCategoryId();
		final String categoryName = TimeSliceCategory.isValid(categoryId) ? String.format(Locale.US, "Category=%1$d",
				categoryId) : null;

		return this.toString(categoryName);
	}

	public String toString(final TimeSliceCategory selectedCategory) {
		final String categoryName = (!TimeSliceCategory.isValid(selectedCategory)) ? null
				: selectedCategory.getCategoryName();
		return this.toString(categoryName);
	}

	public String toString(final String categoryName) {
		final StringBuffer result = new StringBuffer();

		if (!this.ignoreDates) {
			final DateTimeUtil formatter = DateTimeFormatter.getInstance();
			result.append(String.format(Locale.US, "%1$s-%2$s",
					formatter.getShortDateStr(this.getStartTime()),
					formatter.getShortDateStr(this.getEndTime())));
		}

		if (categoryName != null) {
			result.append(";" + categoryName);
		}

		if (this.notesNotNull) {
			result.append(":Notes");
		} else if ((this.notes != null) && (this.notes.length() > 0)) {
			result.append(":Notes='" + this.notes + "'");
		}

		return result.toString();
	}

	/**
	 * creates filter if null. Fixes start/end to meaningfull defaults if empty.
	 */
	public static TimeSliceFilterParameter filterWithDefaultsIfNeccessary(
			final TimeSliceFilterParameter filter) {
		final TimeSliceFilterParameter currentRangeFilter = (filter != null) ? filter
				: new TimeSliceFilterParameter();

		final Date currDate = new Date();

		if (currentRangeFilter.getStartTime() == TimeSlice.NO_TIME_VALUE) {
			// start = now-2months
			final Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			calendar.add(Calendar.MONTH, -2);
			final long startTime = calendar.getTimeInMillis();
			currentRangeFilter.setStartTime(startTime);
		}

		if (currentRangeFilter.getEndTime() == TimeSlice.NO_TIME_VALUE) {
			// end = now+1week
			final Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			calendar.add(Calendar.WEEK_OF_YEAR, 1);
			final long endTime = calendar.getTimeInMillis();
			currentRangeFilter.setEndTime(endTime);
		}
		return currentRangeFilter;
	}

}
