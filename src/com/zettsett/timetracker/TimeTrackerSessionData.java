package com.zettsett.timetracker;

import java.io.Serializable;

import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

/**
 * Data for Current Time Recording.
 */
public class TimeTrackerSessionData implements Serializable {
	private static final long serialVersionUID = -1223094842173676534L;

	private long punchInTimeStartInMillisecs;
	private long elapsedTimeInMillisecs = 0;
	private TimeSlice currentTimeSlice = new TimeSlice();
	private boolean punchedOut = true;
	public long updateCount = 0; // for diagnostics purpuses. will be inceremented on every save

	public void beginNewSlice(TimeSliceCategory category, long startDateTime) {
		if(currentTimeSlice != null) {
			endCurrentTimeSlice(startDateTime);
		}
		currentTimeSlice = new TimeSlice();
		currentTimeSlice.setCategory(category);
		currentTimeSlice.setStartTime(startDateTime);
		setPunchedOut(false);
	}
 
	public void endCurrentTimeSlice(long endDateTime) {
		currentTimeSlice.setEndTime(endDateTime);
		setPunchedOut(true);
	}
	
	public TimeSlice getCurrentTimeSlice() {
		return currentTimeSlice;
	}

	public TimeSliceCategory getTimeSliceCategory() {
		return currentTimeSlice.getCategory();
	}

	public void setTimeSliceCategory(TimeSliceCategory category) {
		currentTimeSlice.setCategory(category);
	}

	public long getElapsedTimeInMillisecs() {
		return elapsedTimeInMillisecs;
	}

	public void setElapsedTimeInMillisecs(long elapsed) {
		this.elapsedTimeInMillisecs = elapsed;
	}

	public long getPunchInTimeStartInMillisecs() {
		return punchInTimeStartInMillisecs;
	}

	public void setPunchInTimeStartInMillisecs(long punchInBase) {
		this.punchInTimeStartInMillisecs = punchInBase;
		this.setPunchedOut(false);
	}

	public boolean isPunchedOut() {
		return punchedOut;
	}

	private void setPunchedOut(boolean punchedOut) {
		this.punchedOut = punchedOut;
	}

	public void setCurrentNotes(String notes) {
		getCurrentTimeSlice().setNotes(notes);
	}

	@Override public String toString() {
		StringBuilder result = new StringBuilder();
		
		if (this.updateCount != 0)
			result.append("#").append(this.updateCount).append(":");
		
		TimeSlice slice = this.getCurrentTimeSlice();
		if (slice != null)
			result.append(slice.toString()).append(" ");
		
		if (this.isPunchedOut())
			result.append("punched out ");
		else
			result.append("punched in ");
			
		result.append(TimeSlice.getDateTimeStr(this.getPunchInTimeStartInMillisecs())).append("+").append(this.getElapsedTimeInMillisecs());
		return result.toString();
	}

}
