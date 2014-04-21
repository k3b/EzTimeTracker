package de.k3b.timetracker.report;

import java.util.Locale;

import android.content.Context;
import de.k3b.timetracker.R;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;

/**
 * Formats reportItems where header item is of type Long (as date) or
 * TimeSliceCategory<br />
 * and detail item is of type TimeSlice or ReportItemWithStatistics.<br/>
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
		return this.formatValue(this.getValueGenericInternal(obj));
	}

	protected String formatValue(final String obj) {
		return obj;
	}

	protected String getValueGenericInternal(final Object obj) {
		if (obj != null) {
			final Class<? extends Object> objClass = obj.getClass();
			if (objClass.isAssignableFrom(TimeSlice.class)) {
				return this.addField(1, this.getValue((TimeSlice) obj));
			} else if (objClass.isAssignableFrom(TimeSliceCategory.class)) {
				return this.addField(0, this.getValue((TimeSliceCategory) obj));
			} else if (objClass.isAssignableFrom(Long.class)) {
				return this.addField(0, this.getValue((Long) obj));
			} else if (objClass.isAssignableFrom(String.class)) {
				return this.addField(0, this.getValue((String) obj));
			} else if (objClass.isAssignableFrom(ReportItemWithStatistics.class)) {
				return this.addField(1,
						this.getValue((ReportItemWithStatistics) obj));
			}
		}
		return "";
	}

	private String addField(final int level, final String value) {
		return value;
	}

	protected String getValue(final long obj) {
		String currentStartDateText;
		if (this.reportDateGrouping == ReportDateGrouping.DAILY) {
			currentStartDateText = SummaryReportCalculator.dt
					.getLongDateStr(obj);
		} else if (this.reportDateGrouping == ReportDateGrouping.WEEKLY) {
			currentStartDateText = SummaryReportCalculator.dt
					.getWeekStr(obj);
		} else if (this.reportDateGrouping == ReportDateGrouping.MONTHLY) {
			currentStartDateText = SummaryReportCalculator.dt
					.getMonthStr(obj);
		} else if (this.reportDateGrouping == ReportDateGrouping.YEARLY) {
			currentStartDateText = SummaryReportCalculator.dt
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

	protected String getValue(final String obj) {
		return obj;
	}

	protected String getValue(final ReportItemWithStatistics obj) {
		return this.getValueGeneric(obj.getGroupingKey()) + ": "
				+ this.timeInMillisToText(obj.getDuration(), false);
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
