package com.zettsett.timetracker;

public class Global {
	public static final String LOG_CONTEXT = "TimeTracker";
	public static final String CMD_STOP = "stop";
	public static final String CMD_START = "start";

	public static final String REFRESH_GUI = "com.zettsett.timetracker.action.REFRESH_GUI";

	/**
	 * used to return modified filter from filter editor activity back to
	 * calling activity.<br/>
	 * todo: hide behind public static filter activity api
	 */
	public static final String EXTRA_FILTER = "filter";

	/**
	 * used to return modified filter from timeslice editor activity back to
	 * calling activity.<br/>
	 * todo: hide behind public static filter activity api
	 */
	public static final String EXTRA_TIMESLICE = "time_slice";

	private static boolean debugEnabled = false;
	private static boolean infoEnabled = false;

	public static boolean isDebugEnabled() {
		return Global.debugEnabled; // Log.isLoggable(Global.LOG_CONTEXT,
									// Log.DEBUG); //
		// Log.isLoggable() does not work :-(
	}

	public static void setDebugEnabled(final boolean prefValue) {
		Global.debugEnabled = prefValue;
	}

	public static boolean isInfoEnabled() {
		return Global.infoEnabled; // Log.isLoggable(Global.LOG_CONTEXT,
									// Log.INFO) //
		// Log.isLoggable() does not work :-(
	}

	public static void setInfoEnabled(final boolean prefValue) {
		Global.infoEnabled = prefValue;
	}

}
