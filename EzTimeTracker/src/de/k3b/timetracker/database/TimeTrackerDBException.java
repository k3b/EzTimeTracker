package de.k3b.timetracker.database;

import de.k3b.android.database.SqlFilter;

public class TimeTrackerDBException extends RuntimeException {
	private static final long serialVersionUID = 694973800312220443L;

	public TimeTrackerDBException(final String context,
			final SqlFilter sqlFilter, final Exception ex) {
		super((sqlFilter != null) ? sqlFilter.getDebugMessage(context)
				: context, ex);
	}
}
