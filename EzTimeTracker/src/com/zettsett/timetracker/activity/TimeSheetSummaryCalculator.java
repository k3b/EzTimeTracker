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
	private Map<String, TimeSliceCategory> categoties;
	private Map<String, Long> dates;
	
	public TimeSheetSummaryCalculator(ReportModes mReportMode,
			ReportDateGrouping mReportDateGrouping, List<TimeSlice> timeSlices)
	{
		if (mReportMode == ReportModes.BY_DATE) {
			summaries = new LinkedHashMap<String, Map<String, Long>>();
		} else if (mReportMode == ReportModes.BY_DATE) {
			summaries = new TreeMap<String, Map<String, Long>>();
		} else {
			throw new IllegalArgumentException("Unknown ReportModes " + mReportMode);
		}

		categoties = new LinkedHashMap<String, TimeSliceCategory>();
		dates = new TreeMap<String, Long>();
		
		DateTimeUtil dt = DateTimeFormatter.getInstance();

		for (TimeSlice aSlice : timeSlices) {
			long rawStartTime = aSlice.getStartTime();
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
				throw new IllegalArgumentException("Unknown ReportDateGrouping " + mReportDateGrouping);
			}
			dates.put(currentStartDateText, currentStartDate);

			String categoryName = aSlice.getCategoryName();
			categoties.put(categoryName, aSlice.getCategory());

			Map<String, Long> subGroup = getSubMap(mReportMode,
					currentStartDateText, categoryName);
			
			String reportLine = getReportLine(mReportMode,
					currentStartDateText, categoryName);
			
			increment(subGroup, reportLine, aSlice.getEndTime() - rawStartTime);
		} // foreach TimeSlice
	}

	private String getReportLine(ReportModes mReportMode,
			String currentStartDateText, String categoryName) {
		String reportLine = null;
		if (mReportMode == ReportModes.BY_DATE) {
			reportLine = categoryName;
		} else {
			reportLine = currentStartDateText;
		}
		return reportLine;
	}

	private Map<String, Long> getSubMap(ReportModes mReportMode,
			String currentStartDateText, String categoryName) {
		String header;
		if (mReportMode == ReportModes.BY_DATE) {
			header = currentStartDateText;
		} else {
			header = categoryName;
		}
		Map<String, Long> group = summaries.get(header);
		if (group == null) {
			if (mReportMode == ReportModes.BY_DATE) {
				group = new TreeMap<String, Long>();
			} else {
				group = new LinkedHashMap<String, Long>();
			}
			summaries.put(header, group);
		}
		return group;
	}

	private static void increment(Map<String, Long> map, String key, long diffValue) {
		Long timeSum = map.get(key);
		if (timeSum == null) {
			timeSum = Long.valueOf(0);
		}
		map.put(key, timeSum + diffValue);
	}
	
	public Map<String, Map<String, Long>> getReportData() { return summaries; }
	public Map<String, TimeSliceCategory> getCategoties() { return categoties; }
	public Map<String, Long> getDates() { return dates; }
}
