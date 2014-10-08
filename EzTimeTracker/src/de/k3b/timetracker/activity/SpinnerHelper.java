package de.k3b.timetracker.activity;

import android.widget.Spinner;

import de.k3b.common.ItemWithRowId;

/**
 * Helper that interprets a spinner as containing ItemWithRowId items
 */
public class SpinnerHelper {

	public static void selectSpinner(final Spinner spinner,
			final ItemWithRowId currentCategory) {
		final int currentCategoryID = SpinnerHelper
				.getCategoryId(currentCategory, -1);
		SpinnerHelper.selectSpinner(spinner, currentCategoryID);
	}

	public static void selectSpinner(final Spinner spinner,
			final int currentCategoryID) {
		for (int position = 0; position < spinner.getCount(); position++) {

			if (((ItemWithRowId) spinner.getItemAtPosition(position))
					.getRowId() == currentCategoryID) {
				spinner.setSelection(position);
				break;
			}
		}
	}

	public static int getCategoryId(final ItemWithRowId category, int notFoundValue) {
		return (category != null) ? category.getRowId()
				: notFoundValue;
	}
}
