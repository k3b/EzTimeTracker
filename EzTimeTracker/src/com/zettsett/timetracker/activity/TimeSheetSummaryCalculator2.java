package com.zettsett.timetracker.activity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;

import com.zetter.androidTime.R;
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
public class TimeSheetSummaryCalculator2 {
	public enum ReportDateGrouping {
		DAILY, WEEKLY, MONTHLY, YEARLY
	}

	public enum ReportModes {
		BY_DATE, BY_CATEGORY
	}

	public class Duration {
		public final Object subKey;
		public final long duration;

		public Duration(final Object subKey, final long duration) {
			this.subKey = subKey;
			this.duration = duration;
		}
	}

	final static DateTimeUtil dt = DateTimeFormatter.getInstance();

	public Object[] toArray(final Map<Object, Map<Object, Long>> summary) {
		final ArrayList<Object> result = new ArrayList<Object>();

		for (final Object superKey : summary.keySet()) {
			result.add(superKey);
			final Map<Object, Long> subMap = summary.get(superKey);
			for (final Object subKey : subMap.keySet()) {
				result.add(new Duration(subKey, subMap.get(subKey)));
			}
		}
		return result.toArray();
	}

	public String toString(final Object item,
			final ReportDateGrouping reportDateGrouping, final Context context) {
		if (item instanceof Long) {
			return this.getDateGroupText2(reportDateGrouping, (Long) item);
		} else if (item instanceof TimeSliceCategory) {
			return ((TimeSliceCategory) item).getCategoryName();
		} else if (item instanceof Duration) {
			final Duration duration = (Duration) item;

			return "  "
					+ this.toString(duration.subKey, reportDateGrouping,
							context) + ": "
					+ this.timeInMillisToText(context, duration.duration);
		}
		throw new IllegalArgumentException("Unknown item type " + item);
	}

	private String timeInMillisToText(final Context context,
			final long totalTimeInMillis) {
		final long minutes = (totalTimeInMillis / (1000 * 60)) % 60;
		final long hours = totalTimeInMillis / (1000 * 60 * 60);
		String hoursWord;
		if (hours == 1) {
			hoursWord = context.getString(R.string.hoursWord1);
		} else {
			hoursWord = context.getString(R.string.hoursWordN);
		}
		String minutesWord;
		if (minutes == 1) {
			minutesWord = context.getString(R.string.minutesWord1);
		} else {
			minutesWord = context.getString(R.string.minutesWordN);
		}
		final String timeString = hours + " " + hoursWord + ", " + minutes
				+ " " + minutesWord;
		return timeString;
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

	private String getDateGroupText2(
			final ReportDateGrouping reportDateGrouping,
			final long currentStartDate) {
		String currentStartDateText;
		if (reportDateGrouping == ReportDateGrouping.DAILY) {
			currentStartDateText = TimeSheetSummaryCalculator2.dt
					.getShortDateStr(currentStartDate);
		} else if (reportDateGrouping == ReportDateGrouping.WEEKLY) {
			currentStartDateText = TimeSheetSummaryCalculator2.dt
					.getWeekStr(currentStartDate);
		} else if (reportDateGrouping == ReportDateGrouping.MONTHLY) {
			currentStartDateText = TimeSheetSummaryCalculator2.dt
					.getMonthStr(currentStartDate);
		} else if (reportDateGrouping == ReportDateGrouping.YEARLY) {
			currentStartDateText = TimeSheetSummaryCalculator2.dt
					.getYearString(currentStartDate);
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
}
