package com.zettsett.timetracker;

import java.util.Calendar;

import android.text.format.DateFormat;

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

	private static String timeFormatString = "h:mmaa";
	
	public static Calendar getCalendar(int year, int monthOfYear, int dayOfMonth) {
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, monthOfYear);
		c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		return c;
	}

	public static void initializeCurrentTimeFormat() {
		if("ampm".equals(Settings.getCurrentTimeFormat())) {
			timeFormatString = "h:mmaa";
		} else {
			timeFormatString = "kk:mm";
		}
	}

	public static String formatTimePerCurrentSettings(long timeMilliSecs) {
			
		return DateFormat.format(timeFormatString, timeMilliSecs).toString();
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

}
