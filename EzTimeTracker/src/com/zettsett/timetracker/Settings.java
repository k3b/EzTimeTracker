package com.zettsett.timetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings {

	private static int minTrashholdInSecs = 1;
	private static boolean publicDatabase = false;
	private static boolean hideInactiveCategories = false;

	public static void init(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);	
		minTrashholdInSecs = getPrefValue(prefs, "minTrashholdInSecs", minTrashholdInSecs);
		publicDatabase = getPrefValue(prefs, "publicDatabase", publicDatabase);
		hideInactiveCategories = getPrefValue(prefs, "hideInactiveCategories", hideInactiveCategories);
	}

	public static long getMinminTrashholdInMilliSecs() {
		return 1000l * minTrashholdInSecs ;
	}

	public static boolean getHideInactiveCategories()
	{
		return hideInactiveCategories; 
	}

	/**
	 * Since this value comes from a text-editor it is stored as string. Conversion to int must be done yourself.
	 * @param prefs
	 * @param key
	 * @param notFoundValue
	 * @return
	 */
	private static int getPrefValue(SharedPreferences prefs, String key, int notFoundValue) {
		try {
			return Integer.parseInt(prefs.getString(key, Integer.toString(minTrashholdInSecs)));
		} catch (ClassCastException ex) {
			Log.w(Global.LOG_CONTEXT, "getPrefValue-Integer(" + key + "," + notFoundValue + 
					") failed: " + ex.getMessage());
			return notFoundValue;
		}
	}
	
	private static boolean getPrefValue(SharedPreferences prefs, String key, boolean notFoundValue) {
		try {
			return prefs.getBoolean(key, notFoundValue);
		} catch (ClassCastException ex) {
			Log.w(Global.LOG_CONTEXT, "getPrefValue-Boolean(" + key + "," + notFoundValue + 
					") failed: " + ex.getMessage());			
			return notFoundValue;
		}
	}
}
