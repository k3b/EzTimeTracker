package de.k3b.timetracker.database;

import de.k3b.timetracker.model.TimeSlice;

/**
 * Created by EVE on 31.07.2014.
 */
public interface ITimeSliceRepository {
    /**
     * creates a new timeSlice, <br/>
     * if distance to last timeslice > Settings.minPunchInTrashhold.<br/>
     * Else append to previous.
     *
     * @param timeSlice
     * @return rowid if successfull or -1 if error or negative value if timeslice was appended.
     */
    long create(TimeSlice timeSlice);
}
