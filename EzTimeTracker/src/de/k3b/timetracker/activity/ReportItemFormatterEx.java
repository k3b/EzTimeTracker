package de.k3b.timetracker.activity;

import android.content.Context;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;

/**
 * Formats reportItems where header item is of type Long (as date) or
 * TimeSliceCategory<br />
 * and detail item is of type TimeSlice or ReportItemWithStatistics.<br/>
 */
public class ReportItemFormatterEx extends ReportItemFormatter {

	private static final String DETAIL_PREFIX = "\t";
	private final boolean showNotes;
	private String lineTerminator = "\r\n";

	public ReportItemFormatterEx(final Context context,
			final ReportDateGrouping reportDateGrouping, final boolean showNotes) {
		super(context, reportDateGrouping);
		this.showNotes = showNotes;
	}

	@Override
	protected String getValue(final TimeSlice obj) {
		final boolean showNotes = (this.showNotes && (obj.hasNotes()));

		final String notes = (showNotes) ? (this.lineTerminator
				+ ReportItemFormatterEx.DETAIL_PREFIX + ReportItemFormatterEx.DETAIL_PREFIX)
				+ (obj.getNotes())
				: "";
		return ReportItemFormatterEx.DETAIL_PREFIX + super.getValue(obj)
				+ notes + this.lineTerminator;
	}

	@Override
	protected String getValue(final ReportItemWithStatistics obj) {
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
