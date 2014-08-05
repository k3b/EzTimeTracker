package de.k3b.timetracker.report;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.k3b.timetracker.DateTimeFormatter;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.util.DateTimeUtil;

/**
 * Calculator to create summary from raw data.
 *
 * @author EVE
 */
public class SummaryReportCalculator {
    final static DateTimeUtil dt = DateTimeFormatter.getInstance();

    /**
     * update statistics for one item
     */
    private static void updateItemStatistics(
            final Map<Object, ReportItemWithStatistics> supMap,
            final Object key, final long diffTimeValue, final String notes) {
        ReportItemWithStatistics timeSum = supMap.get(key);
        if (timeSum == null) {
            timeSum = new ReportItemWithStatistics(key, 0, 0, null);
        }
        timeSum.incrementDuration(diffTimeValue);
        timeSum.appendNotes(notes);
        supMap.put(key, timeSum);
    }

    /**
     * create a statistics in a guiAdapter friendly format
     */
    public static List<Object> createStatistics(
            final List<TimeSlice> timeSlices, final ReportModes reportMode,
            final ReportDateGrouping reportDateGrouping, final boolean showNotes) {
        final SummaryReportCalculator calculator = new SummaryReportCalculator();
        final Map<Object, Map<Object, ReportItemWithStatistics>> statisticsMap = calculator
                .createStatisticsMap(reportMode, reportDateGrouping,
                        timeSlices, showNotes);
        return calculator.toList(statisticsMap);
    }

    /**
     * Creates the statistics
     *
     * @param reportMode         type of report: DAILY, WEEKLY, MONTHLY, YEARLY
     * @param reportDateGrouping type of report: BY_DATE_AND_CATEGORY, BY_CATEGORY_AND_DATE
     * @param timeSlices         where raw report data comes from
     */
    private Map<Object, Map<Object, ReportItemWithStatistics>> createStatisticsMap(
            final ReportModes reportMode,
            final ReportDateGrouping reportDateGrouping,
            final List<TimeSlice> timeSlices, final boolean showNotes) {
        Map<Object, Map<Object, ReportItemWithStatistics>> statisticsMap;

        if (reportMode == ReportModes.BY_DATE_AND_CATEGORY) {
            statisticsMap = new LinkedHashMap<Object, Map<Object, ReportItemWithStatistics>>();
        } else if (reportMode == ReportModes.BY_CATEGORY_AND_DATE) {
            statisticsMap = new TreeMap<Object, Map<Object, ReportItemWithStatistics>>();
        } else {
            throw new IllegalArgumentException("Unknown ReportModes "
                    + reportMode);
        }

        for (final TimeSlice aSlice : timeSlices) {
            final long rawStartTime = aSlice.getStartTime();

            final long currentStartDate = this.roundDateByReportDateGroup(
                    rawStartTime, reportDateGrouping);

            final Object currentStartDateKey = Long.valueOf(currentStartDate);

            final Object currentCategoryKey = aSlice.getCategory();

            final Map<Object, ReportItemWithStatistics> subGroup = this
                    .getOrCreateSubMap(statisticsMap, reportMode,
                            currentStartDateKey, currentCategoryKey);

            final Object subKey = this.getSubKey(reportMode,
                    currentStartDateKey, currentCategoryKey);

            SummaryReportCalculator.updateItemStatistics(subGroup,
                    subKey, aSlice.getEndTime() - rawStartTime,
                    (showNotes) ? aSlice.getNotes() : null);
        } // foreach TimeSlice
        return statisticsMap;
    }

    /**
     * converst a statisticsMap to a list that can be displayed via a guiadapter
     */
    private List<Object> toList(
            final Map<Object, Map<Object, ReportItemWithStatistics>> statisticsMap) {
        final ArrayList<Object> result = new ArrayList<Object>();

        for (final Object superKey : statisticsMap.keySet()) {
            result.add(superKey);
            final Map<Object, ReportItemWithStatistics> subMap = statisticsMap
                    .get(superKey);
            for (final Object subKey : subMap.keySet()) {
                final ReportItemWithStatistics item = subMap.get(subKey);
                result.add(item);
            }
        }
        return result;
    }

    /**
     * rounds rawDateTime to previous start of reportDateGrouping.<br/>
     * Example: roundDateByReportDateGroup("2014-12-24",
     * ReportDateGrouping.YEARLY) becomes "2014-01-01"
     */
    private long roundDateByReportDateGroup(final long rawDateTime,
                                            final ReportDateGrouping reportDateGrouping) {
        long currentStartDate;
        if (reportDateGrouping == ReportDateGrouping.DAILY) {
            currentStartDate = SummaryReportCalculator.dt
                    .getStartOfDay(rawDateTime);
        } else if (reportDateGrouping == ReportDateGrouping.WEEKLY) {
            currentStartDate = SummaryReportCalculator.dt
                    .getStartOfWeek(rawDateTime);
        } else if (reportDateGrouping == ReportDateGrouping.MONTHLY) {
            currentStartDate = SummaryReportCalculator.dt
                    .getStartOfMonth(rawDateTime);
        } else if (reportDateGrouping == ReportDateGrouping.YEARLY) {
            currentStartDate = SummaryReportCalculator.dt
                    .getStartOfYear(rawDateTime);
        } else {
            throw new IllegalArgumentException("Unknown ReportDateGrouping "
                    + reportDateGrouping);
        }
        return currentStartDate;
    }

    /**
     * returns the subKey depending on reportmode
     */
    private Object getSubKey(final ReportModes reportMode,
                             final Object currentStartDateText, final Object categoryName) {
        Object subKey = null;
        if (reportMode == ReportModes.BY_DATE_AND_CATEGORY) {
            subKey = categoryName;
        } else {
            subKey = currentStartDateText;
        }
        return subKey;
    }

    /**
     * returns the submap of currend key
     */
    private Map<Object, ReportItemWithStatistics> getOrCreateSubMap(
            final Map<Object, Map<Object, ReportItemWithStatistics>> statisticsMap,
            final ReportModes reportMode, final Object currentStartDatekey,
            final Object currentCategoryKey) {
        final Object superKey = this.getSubKey(reportMode, currentCategoryKey,
                currentStartDatekey);
        Map<Object, ReportItemWithStatistics> group = statisticsMap
                .get(superKey);
        if (group == null) {
            if (reportMode == ReportModes.BY_DATE_AND_CATEGORY) {
                group = new TreeMap<Object, ReportItemWithStatistics>();
            } else {
                group = new LinkedHashMap<Object, ReportItemWithStatistics>();
            }
            statisticsMap.put(superKey, group);
        }
        return group;
    }

    /**
     * defines main and subgrouping of report
     */
    public enum ReportModes {
        /**
         * grouped by date+category
         */
        BY_DATE_AND_CATEGORY,

        /**
         * grouped by category+date
         */
        BY_CATEGORY_AND_DATE
    }
}
