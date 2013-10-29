package com.zettsett.timetracker.activity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

import de.k3b.util.DateTimeUtil;

/**
 * Calculator to create summary from raw data.
 * 
 * @author EVE
 * 
 */
public class TimeSheetSummaryCalculator {
	public enum ReportDateGrouping {
		DAILY, WEEKLY, MONTHLY, YEARLY
	}

	public enum ReportModes {
		BY_DATE, BY_CATEGORY
	}

	final DateTimeUtil dt = DateTimeFormatter.getInstance();

	private Map<String, Map<String, Long>> summaries;
	private final Map<String, TimeSliceCategory> categoties;
	private final Map<String, Long> dates;

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
	public TimeSheetSummaryCalculator(final ReportModes reportMode,
			final ReportDateGrouping reportDateGrouping,
			final List<TimeSlice> timeSlices) {
		if (reportMode == ReportModes.BY_DATE) {
			this.summaries = new LinkedHashMap<String, Map<String, Long>>();
		} else if (reportMode == ReportModes.BY_CATEGORY) {
			this.summaries = new TreeMap<String, Map<String, Long>>();
		} else {
			throw new IllegalArgumentException("Unknown ReportModes "
					+ reportMode);
		}

		this.categoties = new LinkedHashMap<String, TimeSliceCategory>();
		this.dates = new TreeMap<String, Long>();

		for (final TimeSlice aSlice : timeSlices) {
			final long rawStartTime = aSlice.getStartTime();

			final long currentStartDate = this.getDateGroup(reportDateGrouping,
					rawStartTime);

			final String currentStartDateText = this.getDateGroupText(
					reportDateGrouping, currentStartDate);

			this.dates.put(currentStartDateText, currentStartDate);

			final String categoryName = aSlice.getCategoryName();
			this.categoties.put(categoryName, aSlice.getCategory());

			final Map<String, Long> subGroup = this.getSubMap(reportMode,
					currentStartDateText, categoryName);

			final String reportLine = this.getReportLine(reportMode,
					currentStartDateText, categoryName);

			TimeSheetSummaryCalculator.increment(subGroup, reportLine,
					aSlice.getEndTime() - rawStartTime);
		} // foreach TimeSlice
	}

	private String getDateGroupText(
			final ReportDateGrouping reportDateGrouping,
			final long currentStartDate) {
		String currentStartDateText;
		if (reportDateGrouping == ReportDateGrouping.DAILY) {
			currentStartDateText = this.dt.getLongDateStr(currentStartDate);
		} else if (reportDateGrouping == ReportDateGrouping.WEEKLY) {
			currentStartDateText = this.dt.getWeekStr(currentStartDate);
		} else if (reportDateGrouping == ReportDateGrouping.MONTHLY) {
			currentStartDateText = this.dt.getMonthStr(currentStartDate);
		} else if (reportDateGrouping == ReportDateGrouping.YEARLY) {
			currentStartDateText = this.dt.getYearString(currentStartDate);
		} else {
			throw new IllegalArgumentException("Unknown ReportDateGrouping "
					+ reportDateGrouping);
		}
		return currentStartDateText;
	}

	private long getDateGroup(final ReportDateGrouping reportDateGrouping,
			final long rawStartTime) {
		long currentStartDate;
		if (reportDateGrouping == ReportDateGrouping.DAILY) {
			currentStartDate = this.dt.getStartOfDay(rawStartTime);
		} else if (reportDateGrouping == ReportDateGrouping.WEEKLY) {
			currentStartDate = this.dt.getStartOfWeek(rawStartTime);
		} else if (reportDateGrouping == ReportDateGrouping.MONTHLY) {
			currentStartDate = this.dt.getStartOfMonth(rawStartTime);
		} else if (reportDateGrouping == ReportDateGrouping.YEARLY) {
			currentStartDate = this.dt.getStartOfYear(rawStartTime);
		} else {
			throw new IllegalArgumentException("Unknown ReportDateGrouping "
					+ reportDateGrouping);
		}
		return currentStartDate;
	}

	private String getReportLine(final ReportModes mReportMode,
			final String currentStartDateText, final String categoryName) {
		String reportLine = null;
		if (mReportMode == ReportModes.BY_DATE) {
			reportLine = categoryName;
		} else {
			reportLine = currentStartDateText;
		}
		return reportLine;
	}

	private Map<String, Long> getSubMap(final ReportModes mReportMode,
			final String currentStartDateText, final String categoryName) {
		String header;
		if (mReportMode == ReportModes.BY_DATE) {
			header = currentStartDateText;
		} else {
			header = categoryName;
		}
		Map<String, Long> group = this.summaries.get(header);
		if (group == null) {
			if (mReportMode == ReportModes.BY_DATE) {
				group = new TreeMap<String, Long>();
			} else {
				group = new LinkedHashMap<String, Long>();
			}
			this.summaries.put(header, group);
		}
		return group;
	}

	private static void increment(final Map<String, Long> map,
			final String key, final long diffValue) {
		Long timeSum = map.get(key);
		if (timeSum == null) {
			timeSum = Long.valueOf(0);
		}
		map.put(key, timeSum + diffValue);
	}

	public Map<String, Map<String, Long>> getReportData() {
		return this.summaries;
	}

	public Map<String, TimeSliceCategory> getCategoties() {
		return this.categoties;
	}

	public Map<String, Long> getDates() {
		return this.dates;
	}
}
