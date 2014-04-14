package de.k3b.timetracker.activity;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;
import de.k3b.timetracker.TimeTrackerManager;
import de.k3b.timetracker.database.TimeSliceCategoryRepsitory;
import de.k3b.timetracker.model.TimeSliceCategory;

public class CategoryListAdapterSimple {
	public static ArrayAdapter<TimeSliceCategory> createAdapter(
			final Context context, final TimeSliceCategory firstElement,
			final long currentDateTime, final String debugContext) {
		final TimeSliceCategoryRepsitory repository = new TimeSliceCategoryRepsitory(
				context);

		final List<TimeSliceCategory> categories = repository
				.fetchAllTimeSliceCategories(currentDateTime, debugContext
						+ "-CategoryListAdapterSimple");
		if (firstElement != null) {
			categories.add(0, firstElement);
		}

		TimeSliceCategory.setCurrentDateTime(TimeTrackerManager
				.currentTimeMillis());

		final TimeSliceCategory[] durationCategories = categories
				.toArray(new TimeSliceCategory[0]);
		return new ArrayAdapter<TimeSliceCategory>(context,
				android.R.layout.simple_spinner_item, durationCategories);

	}
}
