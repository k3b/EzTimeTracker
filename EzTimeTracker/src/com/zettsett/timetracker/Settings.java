package com.zettsett.timetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings {

	private static int minTrashholdInSecs = 1;
	private static boolean publicDatabase = false;

	public static void init(Context context) {
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);		
			minTrashholdInSecs = Integer.parseInt(prefs.getString("minTrashholdInSecs", Integer.toString(minTrashholdInSecs)));
			publicDatabase = Boolean.parseBoolean(prefs.getString("publicDatabase", Boolean.toString(publicDatabase)));
		} catch (RuntimeException ex) {
			Log.w(Global.LOG_CONTEXT, "Load Settings failed.",ex);
		}
	}

	public static long getMinminTrashholdInMilliSecs() {
		return 1000l * minTrashholdInSecs ;
	}

}
