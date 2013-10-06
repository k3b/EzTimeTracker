package com.zettsett.timetracker.activity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.activity.TimeSheetSummaryReportActivity.ReportDateGrouping;
import com.zettsett.timetracker.activity.TimeSheetSummaryReportActivity.ReportModes;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

import de.k3b.util.DateTimeUtil;

public class TimeSheetSummaryCalculator {
	private Map<String, Map<String, Long>> summaries;
	private final Map<String, TimeSliceCategory> categoties;
	private final Map<String, Long> dates;

	public TimeSheetSummaryCalculator(final ReportModes mReportMode,
			final ReportDateGrouping mReportDateGrouping,
			final List<TimeSlice> timeSlices) {
		if (mReportMode == ReportModes.BY_DATE) {
			this.summaries = new LinkedHashMap<String, Map<String, Long>>();
		} else if (mReportMode == ReportModes.BY_DATE) {
			this.summaries = new TreeMap<String, Map<String, Long>>();
		} else {
			throw new IllegalArgumentException("Unknown ReportModes "
					+ mReportMode);
		}

		this.categoties = new LinkedHashMap<String, TimeSliceCategory>();
		this.dates = new TreeMap<String, Long>();

		final DateTimeUtil dt = DateTimeFormatter.getInstance();

		for (final TimeSlice aSlice : timeSlices) {
			final long rawStartTime = aSlice.getStartTime();
			long currentStartDate;
			String currentStartDateText;

			if (mReportDateGrouping == ReportDateGrouping.DAILY) {
				currentStartDate = dt.getStartOfDay(rawStartTime);
				currentStartDateText = dt.getShortDateStr(currentStartDate);
			} else if (mReportDateGrouping == ReportDateGrouping.WEEKLY) {
				currentStartDate = dt.getStartOfWeek(rawStartTime);
				currentStartDateText = dt.getWeekStr(currentStartDate);
			} else if (mReportDateGrouping == ReportDateGrouping.MONTHLY) {
				currentStartDate = dt.getStartOfMonth(rawStartTime);
				currentStartDateText = dt.getMonthStr(currentStartDate);
			} else if (mReportDateGrouping == ReportDateGrouping.YEARLY) {
				currentStartDate = dt.getStartOfYear(rawStartTime);
				currentStartDateText = dt.getYearString(currentStartDate);
			} else {
				throw new IllegalArgumentException(
						"Unknown ReportDateGrouping " + mReportDateGrouping);
			}
			this.dates.put(currentStartDateText, currentStartDate);

			final String categoryName = aSlice.getCategoryName();
			this.categoties.put(categoryName, aSlice.getCategory());

			final Map<String, Long> subGroup = this.getSubMap(mReportMode,
					currentStartDateText, categoryName);

			final String reportLine = this.getReportLine(mReportMode,
					currentStartDateText, categoryName);

			TimeSheetSummaryCalculator.increment(subGroup, reportLine,
					aSlice.getEndTime() - rawStartTime);
		} // foreach TimeSlice
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
