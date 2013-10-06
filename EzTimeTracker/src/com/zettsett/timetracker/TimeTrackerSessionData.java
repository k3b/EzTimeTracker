package com.zettsett.timetracker;

import java.io.Serializable;

import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

/**
 * Data for Current Time Recording.
 */
public class TimeTrackerSessionData extends TimeSlice implements Serializable {
	private static final long serialVersionUID = -1223094842173676534L;

	public long updateCount = 0; // for diagnostics purpuses. will be
									// inceremented on every save

	public void beginNewSlice(final TimeSliceCategory category,
			final long startDateTime) {
		if (this != null) {
			this.setEndTime(startDateTime);
		}
		this.setCategory(category);
		this.setStartTime(startDateTime);
		this.setEndTime(TimeSlice.NO_TIME_VALUE);
		this.setNotes("");
	}

	public long getElapsedTimeInMillisecs() {
		if (this.getEndTime() == TimeSlice.NO_TIME_VALUE) {
			return TimeSlice.NO_TIME_VALUE;
		}
		return this.getEndTime() - this.getStartTime();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();

		if (this.updateCount != 0) {
			result.append("#").append(this.updateCount).append(":");
		}

		result.append(super.toString());
		return result.toString();
	}

}
