package de.k3b.timetracker.database;

import de.k3b.timetracker.model.TimeSlice;

/**
 * Simulates a ITimeSliceRepository for tests
 * Created by k3b on 31.07.2014.
 */
public class TimeSliceRepositoryMock implements ITimeSliceRepository {
    private int count = 0;
    private boolean appendMode = false;

    @Override
    public long create(final TimeSlice timeSlice) {
        if (this.appendMode) {
            this.appendMode = false;
            return -this.count;
        } else {
            this.count++;
            return this.count;
        }
    }

    /**
     * return number of items in database
     */
    public int getCount() {
        return count;
    }

    /**
     * the next create will be in append mode
     */
    public void setAppendMode() {
        this.appendMode = true;
    }
}
