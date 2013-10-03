package de.k3b.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.zettsett.timetracker.model.TimeSlice;

// import android.util.Log;


public class DateTimeUtil {

	public static final long MILLIS_IN_A_DAY = 24 * 60 * 60 * 1000;

	protected final static java.text.DateFormat shortDateformatter = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM);
	final private static java.text.DateFormat shortTimeformatter = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT);
	
	final private static java.text.DateFormat longDateformatter = new SimpleDateFormat("E " 
			+ ((SimpleDateFormat)shortDateformatter).toPattern());
	final private static java.text.DateFormat isoDateTimeformatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	final private static java.text.DateFormat monthformatter = new SimpleDateFormat("MMMM yyyy");

	private long noTimeValue = TimeSlice.NO_TIME_VALUE;

	public DateTimeUtil(final long noTimeValue) {
		this.noTimeValue = noTimeValue;
		
	}
	/*
	private static DateTimeFormatter instance = null;
	
	public static DateTimeFormatter getInstance()
	{
		if (instance == null) instance = new DateTimeFormatter();
		
		return instance;
	}
	*/
	
	public Calendar getCalendar(int year, int monthOfYear, int dayOfMonth) {
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, monthOfYear);
		c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		return c;
	}

	public Calendar getCalendar(int year, int monthOfYear, int dayOfMonth, int hour, int minute, int second, int millisec) {
		final Calendar c = getCalendar(year, monthOfYear, dayOfMonth);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);
		c.set(Calendar.MILLISECOND, millisec);
		return c;
	}

	public StringBuilder hrColMin(long timeMilliSecs, boolean alwaysIncludeHours, boolean includeSeconds) {
		if (timeMilliSecs >= 0)
		{
			long seconds = timeMilliSecs/1000;
			long minutes = (seconds / 60) % 60;
			long hours = seconds / (60 * 60);
			seconds = seconds % 60;
			
			StringBuilder asText = new StringBuilder();
	
			if (alwaysIncludeHours || hours > 0) {
				append2Digits(asText, hours);
				asText.append(":");
			}
			append2Digits(asText, minutes);
			if (includeSeconds)
			{
				asText.append(":");
				append2Digits(asText, seconds);				
			}
			return asText;
		}
		return new StringBuilder();
	}

	private static StringBuilder append2Digits(StringBuilder asText, long value) {
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

	public String getLongDateStr(long dateTime) {
		if (isEmpty(dateTime)) {
			return "";
		} else {
			return longDateformatter.format(new Date(dateTime)); //  + getShortDateStr(dateTime);
		}
	}

	public boolean isEmpty(long dateTime){return (dateTime == noTimeValue);}

	public String getShortDateStr(long dateTime) {
		if (isEmpty(dateTime)) {
			return "";
		} else {
			return shortDateformatter.format(new Date(dateTime));
		}
	}

	public String getTimeString(long dateTime) {
		if (isEmpty(dateTime)) {
			return "";
		} else {
			return shortTimeformatter.format(new Date(dateTime)); // DateFormat.format(timeFormatString, timeMilliSecs).toString();
		}
	}

	public String getDateTimeStr(long dateTime) {
		return getDateTimeStr(dateTime, "");
	}
	
	public String getDateTimeStr(long dateTime, String emptyReplacement) {
		if (isEmpty(dateTime)) {
			return emptyReplacement;
		} else {
			return getLongDateStr(dateTime) + " " + getTimeString(dateTime);
		}
	}

	public String getIsoDateTimeStr(long dateTime) {
		return isoDateTimeformatter.format(new Date(dateTime));
	}

	public String getYearString(long dateTime) {
		if (isEmpty(dateTime)) {
			return "";
		} else {
			return "" + new Date(dateTime).getYear();
		}
	}

	public String getMonthStr(long dateTime) {
		if (isEmpty(dateTime)) {
			return "";
		} else {
			return monthformatter.format(new Date(dateTime)); 
			
		}
	}
	
	public String getWeekStr(long dateTime) {
		if (isEmpty(dateTime)) {
			return "";
		} else {
			return getShortDateStr(getStartOfWeek(dateTime));
		}
	}

	public long getStartOfWeek(long dateTime) {
		Calendar cal = getStartOfDayCal(dateTime);
		int offset = cal.get(Calendar.DAY_OF_WEEK);
		
		if (offset > 0)
		{
			cal.add(Calendar.DAY_OF_MONTH, - offset);
		}
		return cal.getTimeInMillis();
	}

	

	public long getStartOfMonth(long dateTime) {
		Calendar c = getStartOfDayCal(dateTime);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTimeInMillis();
	}

	public long getStartOfYear(long dateTime) {
		Calendar c = getStartOfDayCal(dateTime);
		c.set(Calendar.MONTH, 1-1);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTimeInMillis();
	}

	public long getStartOfDay(long dateTime) {
		Calendar c = getStartOfDayCal(dateTime);
		return c.getTimeInMillis();
	}
	
	public long addDays(long dateTime, int addDays) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(dateTime);
		
		c.add(Calendar.DAY_OF_MONTH, addDays);
		return c.getTimeInMillis();
	}

	private Calendar getStartOfDayCal(long dateTime) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(dateTime);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}

	public long parseDate(String mDateSelectedForAdd) throws ParseException {
		return longDateformatter.parse(
					mDateSelectedForAdd).getTime();
	}
	
	public boolean is24HourView()
	{
		// TODO find out if country has 24h or am/pm display
		Locale locale = Locale.getDefault();
		return locale != Locale.US;

//			// http://stackoverflow.com/questions/4466657/detect-am-pm-vs-24-hr-clock-preference-from-java-locale-information
//	        DateFormat stdFormat = DateFormat.getTimeInstance(DateFormat.SHORT,
//	                Locale.US);
//	        DateFormat localeFormat = DateFormat.getTimeInstance(DateFormat.LONG,
//	                locale);
//	        String midnight = "";
//	        try {
//	            midnight = localeFormat.format(stdFormat.parse("12:00 AM"));
//	        } catch (ParseException ignore) {
//	        }
//	        return midnight.contains("12");
//
//			  Calendar c = Calendar.getInstance();
//			  c.set(Calendar.HOUR_OF_DAY, 23);
//			  return 23 == c.get(Calendar.HOUR);
		  // Calendar.HOUR_OF_DAY

		
	}
}