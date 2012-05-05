package com.zettsett.timetracker.activity;

import java.io.Serializable;

import com.zettsett.timetracker.model.*;

public class FilterParameter  implements Serializable, ITimeSliceFilter {
	private static final long serialVersionUID = 6586305797483181492L;

	private long startTime = TimeSlice.NO_TIME_VALUE;
	private long endTime = TimeSlice.NO_TIME_VALUE;
	private int categoryId = TimeSliceCategory.NOT_SAVED;
	private boolean mIgnoreDates = false;

	public FilterParameter setParameter(long startTime, long endTime, int categoryId) {
		this.setStartTime(startTime);
		this.setEndTime(endTime);
		this.setCategoryId(categoryId);
		
		return this;
	}
	
	public FilterParameter setParameter(ITimeSliceFilter timeSlice)
	{
		if (timeSlice != null) 
		{
			return setParameter(timeSlice.getStartTime(), timeSlice.getEndTime(), timeSlice.getCategoryId());
		}
		return this;
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

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	
	public boolean isIgnoreDates() {
		return mIgnoreDates;
	}

	public void setIgnoreDates(boolean mIgnoreDates) {
		this.mIgnoreDates = mIgnoreDates;
	}

}
