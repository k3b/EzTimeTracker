package de.k3b.timetracker.model;

import java.io.Serializable;

import de.k3b.common.ItemWithRowId;
import de.k3b.timetracker.DateTimeFormatter;

public class TimeSliceCategory extends ItemWithRowId implements Serializable,
		Comparable<TimeSliceCategory> {
	private static final long serialVersionUID = 4899523432240132519L;

	public static final int NOT_SAVED = -1;
	public static final TimeSliceCategory NO_CATEGORY = new TimeSliceCategory(
			TimeSliceCategory.NOT_SAVED, "?");

	public static final long MIN_VALID_DATE = 0;
	public static final long MAX_VALID_DATE = Long.MAX_VALUE;

	private static long currentDateTime = TimeSliceCategory.MIN_VALID_DATE;

	private String categoryName;

	private String description;

	private long startTime = TimeSliceCategory.MIN_VALID_DATE;

	private long endTime = TimeSliceCategory.MAX_VALID_DATE;

	public TimeSliceCategory() {
		this(TimeSliceCategory.NOT_SAVED, null);
	}

	public TimeSliceCategory(final int id, final String name) {
		this.setRowId(id);
		this.setCategoryName(name);
	}

	public String getCategoryName() {
		if (this.categoryName == null) {
			return "N/A";
		}
		return this.categoryName;
	}

	public void setCategoryName(final String categoryName) {
		this.categoryName = categoryName;
	}

	public String getDescription() {
		if (this.description == null) {
			return "";
		}
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public static void setCurrentDateTime(final long value) {
		TimeSliceCategory.currentDateTime = value;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public TimeSliceCategory setStartTime(final long startTime) {
		this.startTime = startTime;
		return this;
	}

	public long getEndTime() {
		return this.endTime;
	}

	public TimeSliceCategory setEndTime(final long endTime) {
		this.endTime = endTime;
		return this;
	}

	public String getStartDateStr() {
		if (this.startTime == TimeSliceCategory.MIN_VALID_DATE) {
			return "";
		} else {
			return DateTimeFormatter.getInstance().getShortDateStr(
					this.startTime);
		}
	}

	public String getEndTimeStr() {
		if (this.endTime == TimeSliceCategory.MAX_VALID_DATE) {
			return "";
		} else {
			return DateTimeFormatter.getInstance()
					.getShortDateStr(this.endTime);
		}
	}

	public String getActiveDate() {
		final String start = this.getStartDateStr();
		final String end = this.getEndTimeStr();
		if ((start.length() == 0) && (end.length() == 0)) {
			return "";
		} else {
			return start + "-" + end;
		}
	}

	@Override
	public String toString() {
		if (this.isActive(TimeSliceCategory.currentDateTime)) {
			return this.categoryName;
		} else {
			return "[-- " + this.categoryName + " --]";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.categoryName == null) ? 0 : this.categoryName
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final TimeSliceCategory other = (TimeSliceCategory) obj;
		if (this.categoryName == null) {
			if (other.categoryName != null) {
				return false;
			}
		} else if (!this.categoryName.equals(other.categoryName)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(final TimeSliceCategory anotherTimeSliceCategory) {
		return this.categoryName
				.compareTo(anotherTimeSliceCategory.categoryName);
	}

	/**
	 * @param currentDateTime
	 *            dateTime when isActive should be tested
	 * @return true if currentDateTime is between start and end
	 */
	public boolean isActive(final long currentDateTime) {
		if ((currentDateTime == TimeSliceCategory.MIN_VALID_DATE)
				|| (this == TimeSliceCategory.NO_CATEGORY)) {
			return true;
		}

		return ((currentDateTime >= this.getStartTime()) && (currentDateTime <= this
				.getEndTime()));
	}

}
