package com.zettsett.timetracker.model;

/**
 * Prameters that can be used to filter TimeSlice items
 */
public interface ITimeSliceFilter {

	public long getStartTime();

	public long getEndTime();

	public int getCategoryId();
}