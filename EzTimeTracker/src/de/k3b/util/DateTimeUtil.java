package de.k3b.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.zettsett.timetracker.model.TimeSlice;

// import android.util.Log;

public class DateTimeUtil {

	public static final long MILLIS_IN_A_DAY = 24 * 60 * 60 * 1000;

	protected final static java.text.DateFormat shortDateformatter = java.text.DateFormat
			.getDateInstance(java.text.DateFormat.MEDIUM);
	final private static java.text.DateFormat shortTimeformatter = java.text.DateFormat
			.getTimeInstance(java.text.DateFormat.SHORT);

	final private static java.text.DateFormat longDateformatter = new SimpleDateFormat(
			"E "
					+ ((SimpleDateFormat) DateTimeUtil.shortDateformatter)
							.toPattern(),
			Locale.GERMANY);
	final private static java.text.DateFormat isoDateTimeformatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ", Locale.GERMANY);

	final private static java.text.DateFormat monthformatter = new SimpleDateFormat(
			"MMMM yyyy", Locale.GERMANY);

	private long noTimeValue = TimeSlice.NO_TIME_VALUE;

	public DateTimeUtil(final long noTimeValue) {
		this.noTimeValue = noTimeValue;

	}

	/*
	 * private static DateTimeFormatter instance = null;
	 * 
	 * public static DateTimeFormatter getInstance() { if (instance == null)
	 * instance = new DateTimeFormatter();
	 * 
	 * return instance; }
	 */

	public Calendar getCalendar(final int year, final int monthOfYear,
			final int dayOfMonth) {
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, monthOfYear);
		c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		return c;
	}

	public Calendar getCalendar(final int year, final int monthOfYear,
			final int dayOfMonth, final int hour, final int minute,
			final int second, final int millisec) {
		final Calendar c = this.getCalendar(year, monthOfYear, dayOfMonth);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);
		c.set(Calendar.MILLISECOND, millisec);
		return c;
	}

	public StringBuilder hrColMin(final long timeMilliSecs,
			final boolean alwaysIncludeHours, final boolean includeSeconds) {
		if (timeMilliSecs >= 0) {
			long seconds = timeMilliSecs / 1000;
			final long minutes = (seconds / 60) % 60;
			final long hours = seconds / (60 * 60);
			seconds = seconds % 60;

			final StringBuilder asText = new StringBuilder();

			if (alwaysIncludeHours || (hours > 0)) {
				DateTimeUtil.append2Digits(asText, hours);
				asText.append(":");
			}
			DateTimeUtil.append2Digits(asText, minutes);
			if (includeSeconds) {
				asText.append(":");
				DateTimeUtil.append2Digits(asText, seconds);
			}
			return asText;
		}
		return new StringBuilder();
	}

	private static StringBuilder append2Digits(final StringBuilder asText,
			final long value) {
		if (value > 0) {
			if (value < 10) {
				asText.append(0);
			}
			asText.append(value);
		} else {
			asText.append("00");
		}
		return asText;
	}

	public String getLongDateStr(final long dateTime) {
		if (this.isEmpty(dateTime)) {
			return "";
		} else {
			return DateTimeUtil.longDateformatter.format(new Date(dateTime)); // +
																				// getShortDateStr(dateTime);
		}
	}

	public boolean isEmpty(final long dateTime) {
		return (dateTime == this.noTimeValue);
	}

	public String getShortDateStr(final long dateTime) {
		if (this.isEmpty(dateTime)) {
			return "";
		} else {
			return DateTimeUtil.shortDateformatter.format(new Date(dateTime));
		}
	}

	public String getTimeString(final long dateTime) {
		if (this.isEmpty(dateTime)) {
			return "";
		} else {
			return DateTimeUtil.shortTimeformatter.format(new Date(dateTime)); // DateFormat.format(timeFormatString,
																				// timeMilliSecs).toString();
		}
	}

	public String getDateTimeStr(final long dateTime) {
		return this.getDateTimeStr(dateTime, "");
	}

	public String getDateTimeStr(final long dateTime,
			final String emptyReplacement) {
		if (this.isEmpty(dateTime)) {
			return emptyReplacement;
		} else {
			return this.getLongDateStr(dateTime) + " "
					+ this.getTimeString(dateTime);
		}
	}

	public String getIsoDateTimeStr(final long dateTime) {
		return DateTimeUtil.isoDateTimeformatter.format(new Date(dateTime));
	}

	public String getYearString(final long dateTime) {
		if (this.isEmpty(dateTime)) {
			return "";
		} else {
			return "" + this.getStartOfDayCal(dateTime).get(Calendar.YEAR);
		}
	}

	public String getMonthStr(final long dateTime) {
		if (this.isEmpty(dateTime)) {
			return "";
		} else {
			return DateTimeUtil.monthformatter.format(new Date(dateTime));

		}
	}

	public String getWeekStr(final long dateTime) {
		if (this.isEmpty(dateTime)) {
			return "";
		} else {
			return this.getShortDateStr(this.getStartOfWeek(dateTime));
		}
	}

	public long getStartOfWeek(final long dateTime) {
		final Calendar cal = this.getStartOfDayCal(dateTime);
		final int offset = cal.get(Calendar.DAY_OF_WEEK) % 7;

		if (offset > 0) {
			cal.add(Calendar.DAY_OF_MONTH, -offset);
		}
		return cal.getTimeInMillis();
	}

	public long getStartOfMonth(final long dateTime) {
		final Calendar c = this.getStartOfDayCal(dateTime);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTimeInMillis();
	}

	public long getStartOfYear(final long dateTime) {
		final Calendar c = this.getStartOfDayCal(dateTime);
		c.set(Calendar.MONTH, 1 - 1);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTimeInMillis();
	}

	public long getStartOfDay(final long dateTime) {
		final Calendar c = this.getStartOfDayCal(dateTime);
		return c.getTimeInMillis();
	}

	public long addDays(final long dateTime, final int addDays) {
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(dateTime);

		c.add(Calendar.DAY_OF_MONTH, addDays);
		return c.getTimeInMillis();
	}

	private Calendar getStartOfDayCal(final long dateTime) {
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(dateTime);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}

	public long parseDate(final String mDateSelectedForAdd)
			throws ParseException {
		return DateTimeUtil.longDateformatter.parse(mDateSelectedForAdd)
				.getTime();
	}

	public boolean is24HourView() {
		// TODO find out if country has 24h or am/pm display
		final Locale locale = Locale.getDefault();
		return locale != Locale.US;

		// //
		// http://stackoverflow.com/questions/4466657/detect-am-pm-vs-24-hr-clock-preference-from-java-locale-information
		// DateFormat stdFormat = DateFormat.getTimeInstance(DateFormat.SHORT,
		// Locale.US);
		// DateFormat localeFormat = DateFormat.getTimeInstance(DateFormat.LONG,
		// locale);
		// String midnight = "";
		// try {
		// midnight = localeFormat.format(stdFormat.parse("12:00 AM"));
		// } catch (ParseException ignore) {
		// }
		// return midnight.contains("12");
		//
		// Calendar c = Calendar.getInstance();
		// c.set(Calendar.HOUR_OF_DAY, 23);
		// return 23 == c.get(Calendar.HOUR);
		// Calendar.HOUR_OF_DAY

	}
}
