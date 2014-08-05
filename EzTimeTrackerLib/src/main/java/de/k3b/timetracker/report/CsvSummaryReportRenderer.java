package de.k3b.timetracker.report;

import java.util.List;

import de.k3b.timetracker.model.TimeSliceCategory;

/**
 * creates the csv/text representation of a SummaryReport.
 *
 * @author k3b
 */
public class CsvSummaryReportRenderer {
    private final IReportItemFormatter itemFormatter;
    private final boolean showNotes;
    private final StringBuilder output = new StringBuilder();

    private TimeSliceCategory category;
    private boolean byCategory;
    private String date;

    // itemFormatter = new ReportItemFormatterEx(context, reportDateGrouping, false);
    public CsvSummaryReportRenderer(final IReportItemFormatter itemFormatter, final boolean showNotes) {
        this.itemFormatter = itemFormatter;
        this.showNotes = showNotes;
    }

    /*
     * creates the csv/text representation of a SummaryReport items .<br/>
     * items are sortet and contain either TimeSliceCategory (group by Category) or Long(group by Date)
     * and ReportItemWithStatistics
     */
    public String createReport(final List<Object> items) {
        output.setLength(0);
        CsvDetailReportRenderer.addLine(output, "Start", "DurationInMinutes", "items", "CategoryName", "Notes");

        for (final Object reportItem : items) {
            addLine(reportItem);
        }
        return output.toString();
    }

    private void addLine(final Object item) {
        if (item != null) {
            final Class<? extends Object> objClass = item.getClass();
            if (objClass.isAssignableFrom(TimeSliceCategory.class)) {
                this.category = (TimeSliceCategory) item;
                this.byCategory = true;
            } else if (objClass.isAssignableFrom(Long.class)) {
                this.date = this.itemFormatter.getValueGeneric(item);
                this.byCategory = false;
            } else if (objClass.isAssignableFrom(ReportItemWithStatistics.class)) {
                this.addLine((ReportItemWithStatistics) item);
            }
        }
    }

    private void addLine(final ReportItemWithStatistics item) {
        TimeSliceCategory category = (this.byCategory) ? this.category : (TimeSliceCategory) item.getGroupingKey();
        String start = (!this.byCategory) ? this.date : this.itemFormatter.getValueGeneric(item.getGroupingKey());
        String duration = "" + (item.getDuration() / (1000 * 60));
        String elementCount = "" + item.getItemCount();
        String categoryName = (category != null) ? category.getCategoryName() : null;
        String notes = (this.showNotes) ? item.getNotes() : null;

        CsvDetailReportRenderer.addLine(output, start.replace("\n", "").replace("\r", ""), duration, elementCount, categoryName, notes);
    }
}
