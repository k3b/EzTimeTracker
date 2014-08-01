package de.k3b.timetracker.report;

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

    public ReportItemFormatterEx(final DurationFormatter durationFormatter,
                                 final ReportDateGrouping reportDateGrouping, final boolean showNotes) {
        super(durationFormatter, reportDateGrouping);
        this.showNotes = showNotes;
    }

    @Override
    protected String getValue(final TimeSlice obj) {
        final boolean showNotes = (this.showNotes && (obj.hasNotes()));

        StringBuilder newNotes = new StringBuilder(ReportItemFormatterEx.DETAIL_PREFIX)
                .append(super.getValue(obj)).append(this.lineTerminator);

        //	+ newNotes + this.lineTerminator;
        if (showNotes) {
            for (String note : obj.getNotes().split("[\n\r]")) {
                newNotes.append(ReportItemFormatterEx.DETAIL_PREFIX).append(ReportItemFormatterEx.DETAIL_PREFIX).append(note).append(this.lineTerminator);
            }
        }
        return newNotes.toString();
    }

    @Override
    protected String getValue(final ReportItemWithStatistics obj) {
        StringBuilder result = new StringBuilder()
                .append(ReportItemFormatterEx.DETAIL_PREFIX)
                .append(super.getValue(obj).replace(this.lineTerminator, ""))
                .append(this.lineTerminator);
        boolean showNotes = this.showNotes && obj.hasNotes();
        if (showNotes) {
            for (String line : obj.getNotes().split("[\n\r]+")) {
                if ((line != null) && (line.length() > 0)) {
                    result
                            .append(ReportItemFormatterEx.DETAIL_PREFIX)
                            .append(ReportItemFormatterEx.DETAIL_PREFIX)
                            .append(line.trim())
                            .append(this.lineTerminator);
                }
            }
        }
        return result.toString();
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
