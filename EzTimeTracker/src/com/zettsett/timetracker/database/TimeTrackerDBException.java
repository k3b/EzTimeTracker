package com.zettsett.timetracker.database;

import com.zettsett.timetracker.database.TimeSliceSql.SqlFilter;

public class TimeTrackerDBException extends RuntimeException {
	private static final long serialVersionUID = 694973800312220443L;

	public TimeTrackerDBException(final String context,
			final SqlFilter sqlFilter, final Exception ex) {
		super((sqlFilter != null) ? sqlFilter.getDebugMessage(context)
				: context, ex);
	}
}
