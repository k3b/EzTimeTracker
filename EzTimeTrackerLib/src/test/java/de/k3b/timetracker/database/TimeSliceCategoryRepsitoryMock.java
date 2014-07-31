package de.k3b.timetracker.database;

import java.util.HashMap;

import de.k3b.timetracker.model.TimeSliceCategory;

/**
 * Simulates a ICategoryRepsitory.
 * Created by k3b on 31.07.2014.
 */
public class TimeSliceCategoryRepsitoryMock implements ICategoryRepsitory {
    private int lastID = 0;
    private HashMap<String, TimeSliceCategory> items = new HashMap<String, TimeSliceCategory>();

    @Override
    public TimeSliceCategory getOrCreateCategory(final String name) {
        TimeSliceCategory found = items.get(name);
        if (found == null) {
            found = new TimeSliceCategory(++lastID, name);
            items.put(name, found);
        }
        return found;
    }
}
