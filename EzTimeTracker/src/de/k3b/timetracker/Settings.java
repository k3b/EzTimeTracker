package de.k3b.timetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings {

	private static boolean publicDatabase = false;
	private static boolean hideInactiveCategories = false;
	private static int minPunchOutTreshholdInSecs = 1;
	private static int minPunchInTreshholdInSecs = 1;

	public static void init(final Context context) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Settings.minPunchInTreshholdInSecs = Settings
				.getPrefValue(prefs, "minPunchInTreshholdInSecs",
						Settings.minPunchInTreshholdInSecs);
		Settings.minPunchOutTreshholdInSecs = Settings.getPrefValue(prefs,
				"minPunchOutTreshholdInSecs",
				Settings.minPunchOutTreshholdInSecs);
		Settings.publicDatabase = Settings.getPrefValue(prefs,
				"publicDatabase", Settings.publicDatabase);
		Settings.hideInactiveCategories = Settings.getPrefValue(prefs,
				"hideInactiveCategories", Settings.hideInactiveCategories);

		Global.setInfoEnabled(Settings.getPrefValue(prefs, "isInfoEnabled",
				Global.isInfoEnabled()));
		Global.setDebugEnabled(Settings.getPrefValue(prefs, "isDebugEnabled",
				Global.isDebugEnabled()));
	}

	/**
	 * New Punchin within same Category if longer away than this (in seconds).<br/>
	 * Else append to previous.
	 */
	public static long getMinPunchInTreshholdInMilliSecs() {
		return 1000l * Settings.minPunchInTreshholdInSecs;
	}

	public static void setMinPunchInTreshholdInMilliSecs(final long value) {
		Settings.minPunchInTreshholdInSecs = (int) (value / 1000l);
	}

	/**
	 * Punchout only if longer than this (in seconds). Else discard.
	 */
	public static long getMinPunchOutTreshholdInMilliSecs() {
		return 1000l * Settings.minPunchOutTreshholdInSecs;
	}

	public static void setMinPunchOutTreshholdInMilliSecs(final long value) {
		Settings.minPunchOutTreshholdInSecs = (int) (value / 1000l);
	}

	public static boolean getHideInactiveCategories() {
		return Settings.hideInactiveCategories;
	}

	public static boolean isPublicDatabase() {
		return Settings.publicDatabase;
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
					Integer.toString(notFoundValue)));
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