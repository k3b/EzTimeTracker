package com.zettsett.timetracker.database;

import com.zettsett.timetracker.activity.TimeSliceFilterParameter;
import com.zettsett.timetracker.model.ITimeSliceFilter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

class TimeSliceSql {
	static final String COL_CATEGORY_ID = "category_id";
	static final String COL_NOTES = "notes";
	static final String COL_END_TIME = "end_time";
	static final String COL_START_TIME = "start_time";

	static String createFilter(final ITimeSliceFilter filter) {
		final StringBuilder result = new StringBuilder();

		if (filter != null) {
			final TimeSliceFilterParameter timeSliceFilterParameter = (filter instanceof TimeSliceFilterParameter) ? (TimeSliceFilterParameter) filter
					: null;
			final boolean ignoreDates = (timeSliceFilterParameter == null) ? false
					: timeSliceFilterParameter.isIgnoreDates();

			TimeSliceSql.createFilter(result, filter.getStartTime(),
					filter.getEndTime(), filter.getCategoryId(), ignoreDates);

			if (timeSliceFilterParameter != null) {
				if (timeSliceFilterParameter.isNotesNotNull()) {
					TimeSliceSql.addAND(result).append(TimeSliceSql.COL_NOTES)
							.append(" IS NOT NULL ");
				} else {
					final String notes = timeSliceFilterParameter.getNotes();
					if ((notes != null) && (notes.length() > 0)) {
						TimeSliceSql.addAND(result)
								.append(TimeSliceSql.COL_NOTES)
								.append(" like '%").append(notes).append("%'");
					}
				}
			} // if filterParameter
		} // if not null
		if (result.length() == 0) {
			return null;
		} else {
			return result.toString();
		}
	}

	private static void createFilter(final StringBuilder result,
			final long startDate, final long endDate, final long categoryId,
			final boolean ignoreDates) {

		if (!ignoreDates) {
			TimeSliceSql.add(result, TimeSliceSql.COL_START_TIME + ">=",
					startDate, TimeSlice.NO_TIME_VALUE);
			TimeSliceSql.add(result, TimeSliceSql.COL_START_TIME + "<=",
					endDate, TimeSlice.NO_TIME_VALUE);
		}
		TimeSliceSql.add(result, TimeSliceSql.COL_CATEGORY_ID + " =",
				categoryId, TimeSliceCategory.NOT_SAVED);
	}

	private static void add(final StringBuilder result, final String field,
			final long value, final long emptyValue) {
		if (value != emptyValue) {
			TimeSliceSql.addAND(result).append(value);
		}
	}

	private static StringBuilder addAND(final StringBuilder result) {
		if (result.length() > 0) {
			result.append(" AND ");
		}
		return result;
	}
}
