package com.zettsett.timetracker;

import java.io.Serializable;

import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class TimeTrackerData implements Serializable {
	private static final long serialVersionUID = -1223094842172676534L;

	private long punchInBase;
	private long elapsed = 0;
	private TimeSlice currentTimeSlice = new TimeSlice();
	private boolean punchedOut = true;

	public void beginNewSlice(TimeSliceCategory category) {
		if(currentTimeSlice != null) {
			endCurrentTimeSlice();
		}
		currentTimeSlice = new TimeSlice();
		currentTimeSlice.setCategory(category);
		currentTimeSlice.setStartTime(System.currentTimeMillis());
		punchedOut = false;
	}
 
	public void endCurrentTimeSlice() {
		currentTimeSlice.setEndTime(System.currentTimeMillis());
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

	public long getElapsed() {
		return elapsed;
	}

	public void setElapsed(long elapsed) {
		this.elapsed = elapsed;
	}

	public long getPunchInBase() {
		return punchInBase;
	}

	public void setPunchInBase(long punchInBase) {
		this.punchInBase = punchInBase;
	}

	public boolean isPunchedOut() {
		return punchedOut;
	}

	public void setPunchedOut(boolean punchedOut) {
		this.punchedOut = punchedOut;
	}

}
