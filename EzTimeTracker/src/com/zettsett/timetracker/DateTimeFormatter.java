package com.zettsett.timetracker;

import java.util.Calendar;
import java.util.Date;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.text.format.DateFormat;
import android.util.Log;

/**
 * Copyright 2010 Eric Zetterbaum ezetter@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
public class DateTimeFormatter {

	public static final long MILLIS_IN_A_DAY = 24 * 60 * 60 * 1000;

	final private static java.text.DateFormat shortDateformatter = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM);
	final private static java.text.DateFormat shortTimeformatter = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT);
	final private static java.text.DateFormat dowFormatter = new SimpleDateFormat("E ");
	/*
	private static DateTimeFormatter instance = null;
	
	public static DateTimeFormatter getInstance()
	{
		if (instance == null) instance = new DateTimeFormatter();
		
		return instance;
	}
	*/
	
	public static Calendar getCalendar(int year, int monthOfYear, int dayOfMonth) {
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, monthOfYear);
		c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		return c;
	}

	public static StringBuilder hrColMin(long timeMilliSecs, boolean alwaysIncludeHours, boolean includeSeconds) {
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

	public static String getLongDateStr(long dateTime) {
		if (dateTime == 0) {
			return "";
		} else {
			return dowFormatter.format(new Date(dateTime)) + getShortDateStr(dateTime);
		}
	}

	public static String getShortDateStr(long dateTime) {
		if (dateTime == 0) {
			return "";
		} else {
			return shortDateformatter.format(new Date(dateTime));
		}
	}

	public static String getTimeString(long dateTime) {
		if (dateTime == 0) {
			return "";
		} else {
			return shortTimeformatter.format(new Date(dateTime)); // DateFormat.format(timeFormatString, timeMilliSecs).toString();
		}
	}

	public static String getDateTimeStr(long dateTime) {
		if (dateTime == 0) {
			return "";
		} else {
			return DateTimeFormatter.getLongDateStr(dateTime) + " " + DateTimeFormatter.getTimeString(dateTime);
		}
	}

	public static String getRfcDateTimeStr(long dateTime) {
		// todo
		return getDateTimeStr(dateTime);
	}

	public static String getMonthStr(long startTime) {
		if (startTime == 0) {
			return "";
		} else {
			return DateFormat.format("MMMM yyyy", startTime).toString();
		}
	}
	
	public static String getWeekStr(long dateTime) {
		if (dateTime == 0) {
			return "";
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(dateTime);
			int dow = cal.get(Calendar.DAY_OF_WEEK);
			
			if (dow > 0)
			{
				cal.add(Calendar.DAY_OF_MONTH, - dow);
			}
			
			return getShortDateStr(cal.getTimeInMillis());
		}
	}

	public static long parseDate(String mDateSelectedForAdd) {
		try {
			return shortDateformatter.parse(
					mDateSelectedForAdd).getTime();
		} catch (ParseException e) {
			Log.w(Global.LOG_CONTEXT,"cannot reconvert " + mDateSelectedForAdd + " to dateTime using " + shortDateformatter,e);
			return 0;
		}
	}
}
