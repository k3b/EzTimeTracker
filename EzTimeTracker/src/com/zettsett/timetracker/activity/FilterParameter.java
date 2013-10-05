package com.zettsett.timetracker.activity;

import java.io.Serializable;
import java.util.Locale;

import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.model.ITimeSliceFilter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

import de.k3b.util.DateTimeUtil;

public class FilterParameter implements Serializable, ITimeSliceFilter {
	private static final long serialVersionUID = 6586305797483181492L;

	private boolean ignoreDates = false;
	private long startTime = TimeSlice.NO_TIME_VALUE;
	private long endTime = TimeSlice.NO_TIME_VALUE;
	private int categoryId = TimeSliceCategory.NOT_SAVED;
	private boolean notesNotNull = false;

	private String notes;

	public FilterParameter setParameter(final long startTime,
			final long endTime, final int categoryId) {
		this.setStartTime(startTime);
		this.setEndTime(endTime);
		this.setCategoryId(categoryId);

		return this;
	}

	public FilterParameter setParameter(final ITimeSliceFilter source) {
		if (source != null) {
			return this.setParameter(source.getStartTime(),
					source.getEndTime(), source.getCategoryId());
		}
		return this;
	}

	public FilterParameter setParameter(final FilterParameter source) {
		if (source != null) {
			return this.setParameter((ITimeSliceFilter) source).setIgnoreDates(
					source.isIgnoreDates());
		}
		return this;
	}

	public FilterParameter setIgnoreDates(final boolean mIgnoreDates) {
		this.ignoreDates = mIgnoreDates;
		return this;
	}

	public boolean isIgnoreDates() {
		return this.ignoreDates;
	}

	public FilterParameter setStartTime(final long startTime) {
		this.startTime = startTime;
		return this;
	}

	@Override
	public long getStartTime() {
		return this.startTime;
	}

	public FilterParameter setEndTime(final long endTime) {
		this.endTime = endTime;
		return this;
	}

	@Override
	public long getEndTime() {
		return this.endTime;
	}

	public FilterParameter setCategoryId(final int categoryId) {
		this.categoryId = categoryId;
		return this;
	}

	@Override
	public int getCategoryId() {
		return this.categoryId;
	}

	public FilterParameter setNotesNotNull(final boolean checked) {
		this.notesNotNull = checked;
		return this;
	}

	public boolean isNotesNotNull() {
		return this.notesNotNull;
	}

	public FilterParameter setNotes(final String notes) {
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
		final String categoryName = (categoryId != TimeSliceCategory.NO_CATEGORY
				.getRowId()) ? String.format(Locale.US, "Category=%1$d",
				categoryId) : null;

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

}
