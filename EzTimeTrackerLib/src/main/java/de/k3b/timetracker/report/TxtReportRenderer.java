package de.k3b.timetracker.report;

import java.util.List;

public class TxtReportRenderer {
    private IReportItemFormatter formatter;

    // ReportItemFormatterEx formatter = new ReportItemFormatterEx(context, reportDateGrouping, showNotes);
    public TxtReportRenderer(IReportItemFormatter formatter) {
        this.formatter = formatter;
    }

    public String createReport(final List<Object> items) {

        final StringBuilder builder = new StringBuilder();
        for (final Object reportItem : items) {
            builder.append(formatter.getValueGeneric(reportItem));
        }
        return builder.toString();
    }

}
