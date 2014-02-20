package com.zettsett.timetracker.activity;

public class ReportItemWithDuration {
	private static final String DELIMITER = "\n";

	private final Object subKey;
	private long duration;
	private String notes;

	public ReportItemWithDuration(final Object subKey, final long duration,
			final String notes) {
		this.subKey = subKey;
		this.setDuration(duration);
		this.setNotes(notes);
	}

	public void setDuration(final long duration) {
		this.duration = duration;
	}

	public long getDuration() {
		return this.duration;
	}

	public void incrementDuration(final long diffValue) {
		if (diffValue > 0) {
			this.duration += diffValue;
		}
	}

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	public String getNotes() {
		return this.notes;
	}

	public boolean hasNotes() {
		return (this.notes != null) && (this.notes.length() > 0);
	}

	public void appendNotes(final String notes) {
		if ((notes != null) && (notes.length() > 0)) {
			this.notes = (this.notes == null) ? notes : (this.notes
					+ ReportItemWithDuration.DELIMITER + notes);
		}
	}

	public Object getSubKey() {
		return this.subKey;
	}
}