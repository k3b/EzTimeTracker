package com.zettsett.timetracker.activity;

import android.content.Context;

import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

/**
 * Formats reportItems where header item is of type Long (as date) or
 * TimeSliceCategory<br />
 * and detail item is of type TimeSlice or ReportItemWithDuration.<br/>
 */
public class ReportItemFormatterEx extends ReportItemFormatter {

	private static final String DETAIL_PREFIX = "\t";
	private boolean showNotes;
	private String lineTerminator = "\r\n";

	public ReportItemFormatterEx(final Context context,
			final ReportDateGrouping reportDateGrouping) {
		super(context, reportDateGrouping);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getValue(final TimeSlice obj) {
		final boolean showNotes = (this.showNotes && (obj.getNotes() != null) && (obj
				.getNotes().length() > 0));

		final String notes = (showNotes) ? (obj.getNotes()
				+ this.lineTerminator + ReportItemFormatterEx.DETAIL_PREFIX + ReportItemFormatterEx.DETAIL_PREFIX)
				: "";
		return ReportItemFormatterEx.DETAIL_PREFIX + super.getValue(obj)
				+ notes + this.lineTerminator;
	}

	@Override
	protected String getValue(final ReportItemWithDuration obj) {
		return ReportItemFormatterEx.DETAIL_PREFIX + super.getValue(obj)
				+ this.lineTerminator;
	}

	@Override
	protected String getValue(final long obj) {
		return super.getValue(obj) + this.lineTerminator;
	}

	@Override
	protected String getValue(final TimeSliceCategory obj) {
		return super.getValue(obj) + this.lineTerminator;
	}

	public ReportItemFormatterEx setLineTerminator(final String lineTerminator) {
		this.lineTerminator = lineTerminator;
		return this;
	}

}
