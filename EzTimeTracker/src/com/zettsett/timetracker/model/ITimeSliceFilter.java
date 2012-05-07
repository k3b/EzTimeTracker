package com.zettsett.timetracker.model;

import java.io.Serializable;

/**
 * Prameters that can be used to filter TimeSlice items
 */
public interface ITimeSliceFilter extends Serializable {

	public long getStartTime();

	public long getEndTime();

	public int getCategoryId();
}