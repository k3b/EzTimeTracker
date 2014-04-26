package de.k3b.timetracker.database;

import de.k3b.timetracker.model.TimeSliceCategory;

public interface ICategoryRepsitory {

	public abstract TimeSliceCategory getOrCreateTimeSlice(String name);

}