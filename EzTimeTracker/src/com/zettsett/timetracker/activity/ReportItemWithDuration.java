package com.zettsett.timetracker.activity;

public class ReportItemWithDuration {
	public final Object subKey;
	public final long duration;

	public ReportItemWithDuration(final Object subKey, final long duration) {
		this.subKey = subKey;
		this.duration = duration;
	}
}