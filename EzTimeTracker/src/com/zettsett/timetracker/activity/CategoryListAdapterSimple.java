package com.zettsett.timetracker.activity;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.zettsett.timetracker.TimeTrackerManager;
import com.zettsett.timetracker.database.TimeSliceCategoryRepsitory;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class CategoryListAdapterSimple {
	public static ArrayAdapter<TimeSliceCategory> createAdapter(Context context, 
			TimeSliceCategory firstElement, long currentDateTime, String debugContext) {
		TimeSliceCategoryRepsitory repository = new TimeSliceCategoryRepsitory(context);
		
		List<TimeSliceCategory> categories = repository
				.fetchAllTimeSliceCategories(currentDateTime, debugContext + "-CategoryListAdapterSimple");
		if(firstElement != null)
		{
			categories.add(0, firstElement);
		}

		TimeSliceCategory.setCurrentDateTime(TimeTrackerManager.currentTimeMillis());

		TimeSliceCategory[] durationCategories = categories
				.toArray(new TimeSliceCategory[0]);
		return new ArrayAdapter<TimeSliceCategory>(
				context, android.R.layout.simple_spinner_item, durationCategories);
		
	}
}
