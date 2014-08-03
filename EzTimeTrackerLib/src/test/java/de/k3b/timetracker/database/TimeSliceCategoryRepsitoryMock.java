package de.k3b.timetracker.database;

import android.util.SparseArray;

import java.util.HashMap;

import de.k3b.timetracker.model.TimeSliceCategory;

/**
 * Simulates a ICategoryRepsitory.
 * Created by k3b on 31.07.2014.
 */
public class TimeSliceCategoryRepsitoryMock implements ICategoryRepsitory {
    private int lastID = 0;
    private HashMap<String, TimeSliceCategory> names = new HashMap<String, TimeSliceCategory>();

    private SparseArray<TimeSliceCategory> keys = new SparseArray<TimeSliceCategory>();

    public TimeSliceCategoryRepsitoryMock(TimeSliceCategory... initialItems) {
        for (TimeSliceCategory item : initialItems)
            createTimeSliceCategory(item);
    }

    @Override
    public long createTimeSliceCategory(final TimeSliceCategory category) {
        names.put(category.getCategoryName(), category);
        keys.put(category.getRowId(), category);
        return category.getRowId();
    }

    @Override
    public TimeSliceCategory getOrCreateCategory(final String name) {
        TimeSliceCategory found = names.get(name);
        if (found == null) {
            found = new TimeSliceCategory(++lastID, name);
            createTimeSliceCategory(found);
        }
        return found;
    }

    @Override
    public TimeSliceCategory fetchByRowID(final long rowId) {
        TimeSliceCategory found = keys.get((int) rowId);
        if (found == null) throw new TimeTrackerDBException(
                "TimeSliceRepository.fetchByRowID(_id = " + rowId + ")",
                null, null);

        return found;
    }
}
