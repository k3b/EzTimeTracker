package com.zettsett.timetracker.database;

import java.util.ArrayList;
import java.util.List;

import com.zettsett.timetracker.activity.TimeSliceFilterParameter;
import com.zettsett.timetracker.model.ITimeSliceFilter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

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

	private static final String[] STRING_ARRAY = new String[0];

	/**
	 * helper class because java function cannot return 2 values
	 */
	static class SqlFilter {
		SqlFilter(final String sql, final String... args) {
			this.sql = sql;
			this.args = args;
		}

		/**
		 * genereted sql-where
		 */
		final String sql;

		/**
		 * "?" placeholder values needed for prepared sql statements
		 */
		final String[] args;

		/**
		 * formats sql for debugging purposes
		 */
		String getDebugMessage(final String debugContext) {
			final StringBuffer result = new StringBuffer().append(debugContext)
					.append(": ").append(this.sql);
			if (this.args != null) {
				result.append(" [");
				for (final String argument : this.args) {
					result.append("'").append(argument).append("', ");
				}
				result.append("]");
			}
			return result.toString();
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return this.getDebugMessage("TimeSliceSql.SqlFilter");
		}
	};

	/**
	 * generates sql-where from genericTimeSliceFilter
	 */
	static SqlFilter createFilter(final ITimeSliceFilter genericTimeSliceFilter) {
		final StringBuilder sql = new StringBuilder();
		final List<String> filterArgs = new ArrayList<String>();

		if (genericTimeSliceFilter != null) {
			final TimeSliceFilterParameter timeSliceFilter = (genericTimeSliceFilter instanceof TimeSliceFilterParameter) ? (TimeSliceFilterParameter) genericTimeSliceFilter
					: null;
			final boolean ignoreDates = (timeSliceFilter == null) ? false
					: timeSliceFilter.isIgnoreDates();

			TimeSliceSql.createFilter(sql, filterArgs,
					genericTimeSliceFilter.getStartTime(),
					genericTimeSliceFilter.getEndTime(),
					genericTimeSliceFilter.getCategoryId(), ignoreDates);

			if (timeSliceFilter != null) {
				if (timeSliceFilter.isNotesNotNull()) {
					TimeSliceSql.addAND(sql).append(TimeSliceSql.COL_NOTES)
							.append(" IS NOT NULL AND ")
							.append(TimeSliceSql.COL_NOTES).append(" <> '' ");
				} else {
					final String notes = timeSliceFilter.getNotes();
					if ((notes != null) && (notes.length() > 0)) {
						TimeSliceSql.add(sql, filterArgs,
								TimeSliceSql.COL_NOTES + " LIKE ?", "%" + notes
										+ "%", "");
					}
				}
			} // if filterParameter
		} // if not null
		if (sql.length() == 0) {
			return null;
		} else {
			return new SqlFilter(sql.toString(),
					(filterArgs.size() == 0) ? null
							: filterArgs.toArray(TimeSliceSql.STRING_ARRAY));
		}

		// debugContext
	}

	private static void createFilter(final StringBuilder result,
			final List<String> filterArgs, final long startDate,
			final long endDate, final long categoryId, final boolean ignoreDates) {

		if (!ignoreDates) {
			TimeSliceSql.add(result, filterArgs, TimeSliceSql.COL_START_TIME
					+ ">= ?", "" + startDate, "" + TimeSlice.NO_TIME_VALUE);
			TimeSliceSql.add(result, filterArgs, TimeSliceSql.COL_START_TIME
					+ "<= ?", "" + endDate, "" + TimeSlice.NO_TIME_VALUE);
		}
		TimeSliceSql.add(result, filterArgs, TimeSliceSql.COL_CATEGORY_ID
				+ " = ?", "" + categoryId, "" + TimeSliceCategory.NOT_SAVED);
	}

	private static void add(final StringBuilder result,
			final List<String> filterArgs, final String sqlExpressiont,
			final String value, final String emptyValue) {
		if (emptyValue.compareTo(value) != 0) {
			TimeSliceSql.addAND(result);
			result.append(sqlExpressiont);
			filterArgs.add(value);
		}
	}

	private static StringBuilder addAND(final StringBuilder result) {
		if (result.length() > 0) {
			result.append(" AND ");
		}
		return result;
	}
}
