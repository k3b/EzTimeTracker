package de.k3b.timetracker.activity;

import android.widget.Spinner;
import de.k3b.timetracker.model.TimeSliceCategory;

public class CategorySpinner {

	public static void selectSpinner(final Spinner spinner,
			final TimeSliceCategory currentCategory) {
		final int currentCategoryID = CategorySpinner
				.getCategoryId(currentCategory);
		CategorySpinner.selectSpinner(spinner, currentCategoryID);
	}

	public static void selectSpinner(final Spinner spinner,
			final int currentCategoryID) {
		for (int position = 0; position < spinner.getCount(); position++) {

			if (((TimeSliceCategory) spinner.getItemAtPosition(position))
					.getRowId() == currentCategoryID) {
				spinner.setSelection(position);
				break;
			}
		}
	}

	public static int getCategoryId(final TimeSliceCategory category) {
		return (category != null) ? category.getRowId()
				: TimeSliceCategory.NOT_SAVED;
	}
}
