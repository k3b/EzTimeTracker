package com.zettsett.timetracker.activity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.model.TimeSlice;

import de.k3b.util.DateTimeUtil;

/**
 * Calculator to create summary from raw data.
 * 
 * @author EVE
 * 
 */
public class TimeSheetSummaryCalculator2 {
	public enum ReportModes {
		BY_DATE, BY_CATEGORY
	}

	final static DateTimeUtil dt = DateTimeFormatter.getInstance();

	public List<Object> toList(final Map<Object, Map<Object, Long>> summary) {
		final ArrayList<Object> result = new ArrayList<Object>();

		for (final Object superKey : summary.keySet()) {
			result.add(superKey);
			final Map<Object, Long> subMap = summary.get(superKey);
			for (final Object subKey : subMap.keySet()) {
				result.add(new ReportItemWithDuration(subKey, subMap
						.get(subKey)));
			}
		}
		return result;
	}

	/**
	 * Creates summary
	 * 
	 * @param reportMode
	 *            type of report: DAILY, WEEKLY, MONTHLY, YEARLY
	 * @param reportDateGrouping
	 *            type of report: BY_DATE, BY_CATEGORY
	 * @param timeSlices
	 *            where report is created from
	 */
	public Map<Object, Map<Object, Long>> createSummaryMap(
			final ReportModes reportMode,
			final ReportDateGrouping reportDateGrouping,
			final List<TimeSlice> timeSlices) {
		Map<Object, Map<Object, Long>> summaries;

		if (reportMode == ReportModes.BY_DATE) {
			summaries = new LinkedHashMap<Object, Map<Object, Long>>();
		} else if (reportMode == ReportModes.BY_CATEGORY) {
			summaries = new TreeMap<Object, Map<Object, Long>>();
		} else {
			throw new IllegalArgumentException("Unknown ReportModes "
					+ reportMode);
		}

		for (final TimeSlice aSlice : timeSlices) {
			final long rawStartTime = aSlice.getStartTime();

			final long currentStartDate = this.getDateGroup(reportDateGrouping,
					rawStartTime);

			final Object currentStartDateKey = Long.valueOf(currentStartDate);

			final Object currentCategoryKey = aSlice.getCategory();

			final Map<Object, Long> subGroup = this.getSubMap(summaries,
					reportMode, currentStartDateKey, currentCategoryKey);

			final Object subKey = this.getSubKey(reportMode,
					currentStartDateKey, currentCategoryKey);

			TimeSheetSummaryCalculator2.increment(subGroup, subKey,

			aSlice.getEndTime() - rawStartTime);
		} // foreach TimeSlice
		return summaries;
	}

	private long getDateGroup(final ReportDateGrouping reportDateGrouping,
			final long rawStartTime) {
		long currentStartDate;
		if (reportDateGrouping == ReportDateGrouping.DAILY) {
			currentStartDate = TimeSheetSummaryCalculator2.dt
					.getStartOfDay(rawStartTime);
		} else if (reportDateGrouping == ReportDateGrouping.WEEKLY) {
			currentStartDate = TimeSheetSummaryCalculator2.dt
					.getStartOfWeek(rawStartTime);
		} else if (reportDateGrouping == ReportDateGrouping.MONTHLY) {
			currentStartDate = TimeSheetSummaryCalculator2.dt
					.getStartOfMonth(rawStartTime);
		} else if (reportDateGrouping == ReportDateGrouping.YEARLY) {
			currentStartDate = TimeSheetSummaryCalculator2.dt
					.getStartOfYear(rawStartTime);
		} else {
			throw new IllegalArgumentException("Unknown ReportDateGrouping "
					+ reportDateGrouping);
		}
		return currentStartDate;
	}

	private Object getSubKey(final ReportModes reportMode,
			final Object currentStartDateText, final Object categoryName) {
		Object subKey = null;
		if (reportMode == ReportModes.BY_DATE) {
			subKey = categoryName;
		} else {
			subKey = currentStartDateText;
		}
		return subKey;
	}

	private Map<Object, Long> getSubMap(
			final Map<Object, Map<Object, Long>> summaries,
			final ReportModes reportMode, final Object currentStartDatekey,
			final Object currentCategoryKey) {
		Object superKey;
		if (reportMode == ReportModes.BY_DATE) {
			superKey = currentStartDatekey;
		} else {
			superKey = currentCategoryKey;
		}
		Map<Object, Long> group = summaries.get(superKey);
		if (group == null) {
			if (reportMode == ReportModes.BY_DATE) {
				group = new TreeMap<Object, Long>();
			} else {
				group = new LinkedHashMap<Object, Long>();
			}
			summaries.put(superKey, group);
		}
		return group;
	}

	private static void increment(final Map<Object, Long> map,
			final Object key, final long diffValue) {
		Long timeSum = map.get(key);
		if (timeSum == null) {
			timeSum = Long.valueOf(0);
		}
		map.put(key, timeSum + diffValue);
	}

	public static List<Object> loadData(final ReportModes reportMode,
			final ReportDateGrouping reportDateGrouping,
			final List<TimeSlice> timeSlices) {
		final TimeSheetSummaryCalculator2 summaries = new TimeSheetSummaryCalculator2();
		final Map<Object, Map<Object, Long>> map = summaries.createSummaryMap(
				reportMode, reportDateGrouping, timeSlices);
		return summaries.toList(map);
	}
}