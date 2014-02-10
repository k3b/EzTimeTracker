package com.zettsett.timetracker.activity;

import java.util.Locale;

import android.content.Context;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

/**
 * Formats reportItems where header item is of type Long (as date) or
 * TimeSliceCategory<br />
 * and detail item is of type TimeSlice or ReportItemWithDuration.<br/>
 * <br/>
 * Used by TimeSheetReportAdapter and ReportExportEngie.<br/>
 */
public class ReportItemFormatter {
	private final Context context;
	private final ReportDateGrouping reportDateGrouping;

	/**
	 * 
	 * @param context
	 *            to access language specific text constants.
	 * @param reportDateGrouping
	 *            how Long header is translated to date.
	 */
	public ReportItemFormatter(final Context context,
			final ReportDateGrouping reportDateGrouping) {
		this.context = context;
		this.reportDateGrouping = reportDateGrouping;
	}

	public String getValueGeneric(final Object obj) {
		if (obj.getClass().isAssignableFrom(TimeSlice.class)) {
			return this.getValue((TimeSlice) obj);
		} else if (obj.getClass().isAssignableFrom(TimeSliceCategory.class)) {
			return this.getValue((TimeSliceCategory) obj);
		} else if (obj.getClass().isAssignableFrom(Long.class)) {
			return this.getValue((Long) obj);
		} else if (obj.getClass()
				.isAssignableFrom(ReportItemWithDuration.class)) {
			return this.getValue((ReportItemWithDuration) obj);
		} else {
			return "";
		}
	}

	protected String getValue(final long obj) {
		String currentStartDateText;
		if (this.reportDateGrouping == ReportDateGrouping.DAILY) {
			currentStartDateText = TimeSheetSummaryCalculator2.dt
					.getLongDateStr(obj);
		} else if (this.reportDateGrouping == ReportDateGrouping.WEEKLY) {
			currentStartDateText = TimeSheetSummaryCalculator2.dt
					.getWeekStr(obj);
		} else if (this.reportDateGrouping == ReportDateGrouping.MONTHLY) {
			currentStartDateText = TimeSheetSummaryCalculator2.dt
					.getMonthStr(obj);
		} else if (this.reportDateGrouping == ReportDateGrouping.YEARLY) {
			currentStartDateText = TimeSheetSummaryCalculator2.dt
					.getYearString(obj);
		} else {
			throw new IllegalArgumentException("Unknown ReportDateGrouping "
					+ this.reportDateGrouping);
		}
		return currentStartDateText;
	}

	protected String getValue(final TimeSliceCategory obj) {
		return obj.toString(); // .getCategoryName();
	}

	protected String getValue(final TimeSlice obj) {
		return obj.getTitleWithDuration();
	}

	protected String getValue(final ReportItemWithDuration obj) {
		return this.getValueGeneric(obj.subKey) + ": "
				+ this.timeInMillisToText(obj.duration, false);
	}

	private String timeInMillisToText(final long totalTimeInMillis,
			final boolean longVersion) {
		final long minutes = (totalTimeInMillis / (1000 * 60)) % 60;
		final long hours = totalTimeInMillis / (1000 * 60 * 60);

		String hoursWord;
		if (hours == 1) {
			hoursWord = this.getContext().getString(R.string.hoursWord1);
		} else {
			hoursWord = this.getContext().getString(R.string.hoursWordN);
		}

		if (longVersion) {
			String minutesWord;

			if (minutes == 1) {
				minutesWord = this.getContext()
						.getString(R.string.minutesWord1);
			} else {
				minutesWord = this.getContext()
						.getString(R.string.minutesWordN);
			}
			final String timeString = hours + " " + hoursWord + ", " + minutes
					+ " " + minutesWord;
			return timeString;
		}
		return String.format(Locale.GERMANY, " %1$d %3$s %2$02d", hours,
				minutes, hoursWord);
	}

	private Context getContext() {
		// TODO Auto-generated method stub
		return this.context;
	}
}
