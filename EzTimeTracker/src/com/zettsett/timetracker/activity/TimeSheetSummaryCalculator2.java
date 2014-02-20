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

	public List<Object> toList(
			final Map<Object, Map<Object, ReportItemWithDuration>> summary) {
		final ArrayList<Object> result = new ArrayList<Object>();

		for (final Object superKey : summary.keySet()) {
			result.add(superKey);
			final Map<Object, ReportItemWithDuration> subMap = summary
					.get(superKey);
			for (final Object subKey : subMap.keySet()) {
				final ReportItemWithDuration item = subMap.get(subKey);
				result.add(item);
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
	public Map<Object, Map<Object, ReportItemWithDuration>> createSummaryMap(
			final ReportModes reportMode,
			final ReportDateGrouping reportDateGrouping,
			final List<TimeSlice> timeSlices, final boolean showNotes) {
		Map<Object, Map<Object, ReportItemWithDuration>> summaries;

		if (reportMode == ReportModes.BY_DATE) {
			summaries = new LinkedHashMap<Object, Map<Object, ReportItemWithDuration>>();
		} else if (reportMode == ReportModes.BY_CATEGORY) {
			summaries = new TreeMap<Object, Map<Object, ReportItemWithDuration>>();
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

			final Map<Object, ReportItemWithDuration> subGroup = this
					.getSubMap(summaries, reportMode, currentStartDateKey,
							currentCategoryKey);

			final Object subKey = this.getSubKey(reportMode,
					currentStartDateKey, currentCategoryKey);

			TimeSheetSummaryCalculator2.increment(subGroup, subKey,
					aSlice.getEndTime() - rawStartTime,
					(showNotes) ? aSlice.getNotes() : null);
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

	private Map<Object, ReportItemWithDuration> getSubMap(
			final Map<Object, Map<Object, ReportItemWithDuration>> summaries,
			final ReportModes reportMode, final Object currentStartDatekey,
			final Object currentCategoryKey) {
		Object superKey;
		if (reportMode == ReportModes.BY_DATE) {
			superKey = currentStartDatekey;
		} else {
			superKey = currentCategoryKey;
		}
		Map<Object, ReportItemWithDuration> group = summaries.get(superKey);
		if (group == null) {
			if (reportMode == ReportModes.BY_DATE) {
				group = new TreeMap<Object, ReportItemWithDuration>();
			} else {
				group = new LinkedHashMap<Object, ReportItemWithDuration>();
			}
			summaries.put(superKey, group);
		}
		return group;
	}

	private static void increment(
			final Map<Object, ReportItemWithDuration> map, final Object key,
			final long diffValue, final String notes) {
		ReportItemWithDuration timeSum = map.get(key);
		if (timeSum == null) {
			timeSum = new ReportItemWithDuration(key, 0, null);
		}
		timeSum.incrementDuration(diffValue);
		timeSum.appendNotes(notes);
		map.put(key, timeSum);
	}

	public static List<Object> loadData(final ReportModes reportMode,
			final ReportDateGrouping reportDateGrouping,
			final List<TimeSlice> timeSlices, final boolean showNotes) {
		final TimeSheetSummaryCalculator2 summaries = new TimeSheetSummaryCalculator2();
		final Map<Object, Map<Object, ReportItemWithDuration>> map = summaries
				.createSummaryMap(reportMode, reportDateGrouping, timeSlices,
						showNotes);
		return summaries.toList(map);
	}
}
