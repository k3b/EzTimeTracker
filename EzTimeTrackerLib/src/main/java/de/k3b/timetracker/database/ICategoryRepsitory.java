package de.k3b.timetracker.database;

import de.k3b.timetracker.model.TimeSliceCategory;

public interface ICategoryRepsitory {

    TimeSliceCategory getOrCreateCategory(String name);

    long createTimeSliceCategory(TimeSliceCategory category);

    TimeSliceCategory fetchByRowID(long rowId);
}