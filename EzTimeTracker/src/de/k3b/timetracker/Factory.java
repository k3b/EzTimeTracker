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

    public TimeSliceCategoryRepsitory createTimeSliceCategoryRepsitory(final Context context) {
        final Boolean publicDir = SettingsImpl.isPublicDatabase();
        return new TimeSliceCategoryRepsitory(context, publicDir);
    }

    public TimeSliceRepository createTimeSliceRepository(final Context context, TimeSliceCategoryRepsitory timeSliceCategoryRepsitory) {
        if (timeSliceCategoryRepsitory == null)
            timeSliceCategoryRepsitory = createTimeSliceCategoryRepsitory(context);
        final Boolean publicDir = SettingsImpl.isPublicDatabase();
        return new TimeSliceRepository(context, publicDir, timeSliceCategoryRepsitory);
    }

    public TimeTrackerManager createTimeTrackerManager(final Context context, TimeSliceCategoryRepsitory timeSliceCategoryRepsitory, TimeSliceRepository timeSliceRepsitory) {
        // poor man's dependency injection
        if (timeSliceCategoryRepsitory == null)
            timeSliceCategoryRepsitory = createTimeSliceCategoryRepsitory(context);
        if (timeSliceRepsitory == null)
            timeSliceRepsitory = createTimeSliceRepository(context, timeSliceCategoryRepsitory);
        return new TimeTrackerManager(new SessionDataPersistance<TimeTrackerSessionData>(context),
                timeSliceRepsitory,
                timeSliceCategoryRepsitory,
                new TimeTrackerSessionData(), SettingsImpl.getLogger(), SettingsImpl.getInstance());
    }
}
