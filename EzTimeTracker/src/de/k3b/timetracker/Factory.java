package de.k3b.timetracker;

import android.content.Context;

import de.k3b.timetracker.database.TimeSliceCategoryRepsitory;
import de.k3b.timetracker.database.TimeSliceRepository;
import de.k3b.util.SessionDataPersistance;

/**
 * Poor mans-s ioc container.
 * Created by k3b on 31.07.2014.
 */
public class Factory {
    private static Factory ourInstance = new Factory();

    private Factory() {
    }

    public static Factory getInstance() {
        return ourInstance;
    }

    public TimeTrackerManager createTimeTrackerManager(final Context context, final Boolean publicDir) {
        // poor man's dependency injection
        return new TimeTrackerManager(new SessionDataPersistance<TimeTrackerSessionData>(context),
                new TimeSliceRepository(context, publicDir),
                new TimeSliceCategoryRepsitory(context),
                new TimeTrackerSessionData());
    }
}
