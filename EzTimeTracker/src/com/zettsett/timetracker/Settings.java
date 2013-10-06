package com.zettsett.timetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings {

	private static int minTrashholdInSecs = 1;
	private static boolean publicDatabase = false;
	private static boolean hideInactiveCategories = false;

	public static void init(final Context context) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Settings.minTrashholdInSecs = Settings.getPrefValue(prefs,
				"minTrashholdInSecs", Settings.minTrashholdInSecs);
		Settings.publicDatabase = Settings.getPrefValue(prefs,
				"publicDatabase", Settings.publicDatabase);
		Settings.hideInactiveCategories = Settings.getPrefValue(prefs,
				"hideInactiveCategories", Settings.hideInactiveCategories);
	}

	public static long getMinminTrashholdInMilliSecs() {
		return 1000l * Settings.minTrashholdInSecs;
	}

	public static boolean getHideInactiveCategories() {
		return Settings.hideInactiveCategories;
	}

	/**
	 * Since this value comes from a text-editor it is stored as string.
	 * Conversion to int must be done yourself.
	 * 
	 * @param prefs
	 * @param key
	 * @param notFoundValue
	 * @return
	 */
	private static int getPrefValue(final SharedPreferences prefs,
			final String key, final int notFoundValue) {
		try {
			return Integer.parseInt(prefs.getString(key,
					Integer.toString(Settings.minTrashholdInSecs)));
		} catch (final ClassCastException ex) {
			Log.w(Global.LOG_CONTEXT, "getPrefValue-Integer(" + key + ","
					+ notFoundValue + ") failed: " + ex.getMessage());
			return notFoundValue;
		}
	}

	private static boolean getPrefValue(final SharedPreferences prefs,
			final String key, final boolean notFoundValue) {
		try {
			return prefs.getBoolean(key, notFoundValue);
		} catch (final ClassCastException ex) {
			Log.w(Global.LOG_CONTEXT, "getPrefValue-Boolean(" + key + ","
					+ notFoundValue + ") failed: " + ex.getMessage());
			return notFoundValue;
		}
	}
}
