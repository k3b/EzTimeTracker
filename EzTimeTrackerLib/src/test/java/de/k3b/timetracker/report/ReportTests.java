package de.k3b.timetracker.report;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;
import de.k3b.util.DateTimeUtil;

/**
 * Created by EVE on 01.08.2014.
 */
public class ReportTests {
    private final DateTimeUtil dateTimeUtil = new DateTimeUtil(0);
    private final TimeSliceCategory cat1 = new TimeSliceCategory(1, "category#1");
    private final TimeSliceCategory cat2 = new TimeSliceCategory(2, "category#2");

    @Test
    public void shouldCreateTextWeeklySummaryReportByDate() {
        final ReportDateGrouping dateGrouping = ReportDateGrouping.WEEKLY;
        final SummaryReportCalculator.ReportModes reportMode = SummaryReportCalculator.ReportModes.BY_DATE_AND_CATEGORY;
        final boolean showNotes = true;

        createTextSummaryReport(dateGrouping, reportMode, showNotes);
    }

    @Test
    public void shouldCreateTextMonthlySummaryReportByCategory() {
        final ReportDateGrouping dateGrouping = ReportDateGrouping.MONTHLY;
        final SummaryReportCalculator.ReportModes reportMode = SummaryReportCalculator.ReportModes.BY_CATEGORY_AND_DATE;
        final boolean showNotes = true;

        createTextSummaryReport(dateGrouping, reportMode, showNotes);
    }

    private void createTextSummaryReport(final ReportDateGrouping dateGrouping, final SummaryReportCalculator.ReportModes reportMode, final boolean showNotes) {
        final List<Object> data = createSummaryReport(reportMode, dateGrouping, showNotes);

        ReportItemFormatterEx formatter = new ReportItemFormatterEx(new DurationFormatterSimple(), dateGrouping, showNotes);

        // String result = new CsvSummaryReportRenderer(formatter, this.showNotes).createReport(data);
        String result = new TxtReportRenderer(formatter).createReport(data);

        System.out.println("TextSummary " + dateGrouping + "," + reportMode + "," + showNotes);
        System.out.println(result);
    }

    @Test
    public void shouldCreateCsvWeeklySummaryReportByDate() {
        final ReportDateGrouping dateGrouping = ReportDateGrouping.WEEKLY;
        final SummaryReportCalculator.ReportModes reportMode = SummaryReportCalculator.ReportModes.BY_DATE_AND_CATEGORY;
        final boolean showNotes = true;

        createCsvSummaryReport(dateGrouping, reportMode, showNotes);
    }

    @Test
    public void shouldCreateCsvYearlySummaryReportByCategory() {
        final ReportDateGrouping dateGrouping = ReportDateGrouping.YEARLY;
        final SummaryReportCalculator.ReportModes reportMode = SummaryReportCalculator.ReportModes.BY_CATEGORY_AND_DATE;
        final boolean showNotes = false;

        createCsvSummaryReport(dateGrouping, reportMode, showNotes);
    }

    private void createCsvSummaryReport(final ReportDateGrouping dateGrouping, final SummaryReportCalculator.ReportModes reportMode, final boolean showNotes) {
        final List<Object> data = createSummaryReport(reportMode, dateGrouping, showNotes);

        ReportItemFormatterEx formatter = new ReportItemFormatterEx(new DurationFormatterSimple(), dateGrouping, showNotes);

        String result = new CsvSummaryReportRenderer(formatter, showNotes).createReport(data);
        // String result = new TxtReportRenderer(formatter).createReport(data);

        System.out.println("CsvSummary " + dateGrouping + "," + reportMode + "," + showNotes);
        System.out.println(result);
    }

    private List<Object> createSummaryReport(final SummaryReportCalculator.ReportModes reportMode, final ReportDateGrouping dateGrouping, final boolean showNotes) {
        List<TimeSlice> slices = createSlices(null, dateTimeUtil.getCalendar(2001, 1 - 1, 1, 15, 30, 1, 2), 7, cat1);
        slices = createSlices(slices, dateTimeUtil.getCalendar(2001, 1 - 1, 13, 13, 33, 31, 32), 6, cat2);

        return SummaryReportCalculator
                .createStatistics(slices, reportMode,
                        dateGrouping, showNotes);
    }

    @Test
    public void shouldCreateTextDetailReport() {
        final boolean showNotes = true;
        final List<Object> detailReport = createDetailReport();

        String result = new TxtReportRenderer(new ReportItemFormatterEx(new DurationFormatterSimple(), ReportDateGrouping.DAILY, showNotes))
                .createReport(detailReport);

        System.out.println(result);

    }

    @Test
    public void shouldCreateCsvDetailReport() {
        final boolean showNotes = true;
        final List<Object> detailReport = createDetailReport();

        String result = new CsvDetailReportRenderer().createReport(detailReport);

        System.out.println(result);

    }

    private List<Object> createDetailReport() {
        List<TimeSlice> slices = createSlices(null, dateTimeUtil.getCalendar(2001, 1 - 1, 1, 15, 30, 1, 2), 7, cat1);
        return DetailReportCalculator.createStatistics(slices);
    }

    private List<TimeSlice> createSlices(List<TimeSlice> result, Calendar calendar, int ItemCount, TimeSliceCategory category) {
        if (result == null)
            result = new ArrayList<TimeSlice>(ItemCount);

        for (int i = 1, add = 1; i <= ItemCount; i++, add += 2) {
            long start = calendar.getTimeInMillis();
            calendar.add(Calendar.HOUR, 1);
            long end = calendar.getTimeInMillis();
            result.add(new TimeSlice(i).setStartTime(start).setEndTime(end).setCategory(category).setNotes("#" + i));
            calendar.add(Calendar.DAY_OF_YEAR, add);
        }

        return result;
    }
}
