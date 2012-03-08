package com.zettsett.timetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

	private static String currentTimeFormat = "ampm";
	private static int minTrashholdInSecs = 1;

	public static String getCurrentTimeFormat() {
		return currentTimeFormat;
	}
	
	public static void initializeCurrentTimeFormatSetting(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String pref = prefs.getString("timeFormat", null);
		currentTimeFormat = pref;
		DateTimeFormatter.initializeCurrentTimeFormat();
		
		minTrashholdInSecs = Integer.parseInt(prefs.getString("minTrashholdInSecs", Integer.toString(minTrashholdInSecs)));
	}

	public static long getMinminTrashholdInMilliSecs() {
		return 1000l * minTrashholdInSecs ;
	}

}
