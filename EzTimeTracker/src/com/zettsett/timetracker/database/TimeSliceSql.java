package com.zettsett.timetracker.database;

import com.zettsett.timetracker.activity.TimeSliceFilterParameter;
import com.zettsett.timetracker.model.ITimeSliceFilter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

import de.k3b.database.SqlFilter;
import de.k3b.database.SqlFilterBuilder;

/**
 * TimeSlice dependent sql with no dependencies to android.<br/>
 * Used by TimeSliceRepository to build sql.<br/>
 * Scope=package to allow unittesting.<br/>
 * 
 */
class TimeSliceSql {
	static final String COL_CATEGORY_ID = "category_id";
	static final String COL_NOTES = "notes";
	static final String COL_END_TIME = "end_time";
	static final String COL_START_TIME = "start_time";

	/**
	 * generates sql-where from genericTimeSliceFilter
	 */
	static SqlFilter createFilter(final ITimeSliceFilter genericTimeSliceFilter) {
		final SqlFilterBuilder builder = new SqlFilterBuilder();
		if (genericTimeSliceFilter != null) {
			final TimeSliceFilterParameter timeSliceFilter = (genericTimeSliceFilter instanceof TimeSliceFilterParameter) ? (TimeSliceFilterParameter) genericTimeSliceFilter
					: null;

			builder.add(TimeSliceSql.COL_CATEGORY_ID + " = ?", ""
					+ genericTimeSliceFilter.getCategoryId(), ""
					+ TimeSliceCategory.NOT_SAVED);

			final boolean ignoreDates = (timeSliceFilter == null) ? false
					: timeSliceFilter.isIgnoreDates();

			TimeSliceSql.createDateFilter(builder,
					genericTimeSliceFilter.getStartTime(),
					genericTimeSliceFilter.getEndTime(), ignoreDates);

			if (timeSliceFilter != null) {
				if (timeSliceFilter.isNotesNotNull()) {
					builder.addConst(TimeSliceSql.COL_NOTES, "IS NOT NULL")
							.addConst(TimeSliceSql.COL_NOTES, "<> ''");
				} else {
					final String notes = timeSliceFilter.getNotes();
					if ((notes != null) && (notes.length() > 0)) {
						builder.add(TimeSliceSql.COL_NOTES + " LIKE ?", "%"
								+ notes + "%", "");
					}
				}
			} // if filterParameter
		} // if not null

		return builder.toFilter();
		// debugContext
	}

	private static void createDateFilter(final SqlFilterBuilder builder,
			final long startDate, final long endDate, final boolean ignoreDates) {

		if (!ignoreDates) {
			builder.add(TimeSliceSql.COL_START_TIME + ">= ?", "" + startDate,
					"" + TimeSlice.NO_TIME_VALUE);
			builder.add(TimeSliceSql.COL_START_TIME + "<= ?", "" + endDate, ""
					+ TimeSlice.NO_TIME_VALUE);
		}
	}
}
