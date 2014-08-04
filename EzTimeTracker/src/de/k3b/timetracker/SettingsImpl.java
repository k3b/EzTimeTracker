package de.k3b.timetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import de.k3b.android.LoggerImpl;
import de.k3b.common.Logger;

public class SettingsImpl implements Settings {
    private static final Logger logger = new LoggerImpl(Global.LOG_CONTEXT);

    /**
     * data of the one and only SettingsImpl instance.
     */
    private static SettingsImpl ourInstance = new SettingsImpl();
    private static boolean publicDatabase = false;
    private static boolean hideInactiveCategories = false;
    private static int minPunchOutTreshholdInSecs = 1;
    private static int minPunchInTreshholdInSecs = 1;

    private SettingsImpl() {
    }

    public static void init(final Context context) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SettingsImpl.minPunchInTreshholdInSecs = SettingsImpl
                .getPrefValue(prefs, "minPunchInTreshholdInSecs",
                        SettingsImpl.minPunchInTreshholdInSecs);
        SettingsImpl.minPunchOutTreshholdInSecs = SettingsImpl.getPrefValue(prefs,
                "minPunchOutTreshholdInSecs",
                SettingsImpl.minPunchOutTreshholdInSecs);
        SettingsImpl.publicDatabase = SettingsImpl.getPrefValue(prefs,
                "publicDatabase", SettingsImpl.publicDatabase);
        SettingsImpl.hideInactiveCategories = SettingsImpl.getPrefValue(prefs,
                "hideInactiveCategories", SettingsImpl.hideInactiveCategories);

        Global.setInfoEnabled(SettingsImpl.getPrefValue(prefs, "isInfoEnabled",
                Global.isInfoEnabled()));
        Global.setDebugEnabled(SettingsImpl.getPrefValue(prefs, "isDebugEnabled",
                Global.isDebugEnabled()));
        Global.setLogger(logger);
    }

    public static SettingsImpl getInstance() {
        return ourInstance;
    }

    /**
     * New Punchin within same Category if longer away than this (in seconds).<br/>
     * Else append to previous.
     */
    public static long getMinPunchInTreshholdInMilliSecs() {
        return 1000l * SettingsImpl.minPunchInTreshholdInSecs;
    }

    public static void setMinPunchInTreshholdInMilliSecs(final long value) {
        SettingsImpl.minPunchInTreshholdInSecs = (int) (value / 1000l);
    }

    public static boolean getHideInactiveCategories() {
        return SettingsImpl.hideInactiveCategories;
    }

    public static boolean isPublicDatabase() {
        return SettingsImpl.publicDatabase;
    }

    /**
     * Since this value comes from a text-editor it is stored as string.
     * Conversion to int must be done yourself.
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

    public static Logger getLogger() {
        return logger;
    }

    /**
     * Punchout only if longer than this (in seconds). Else discard.
     */
    @Override
    public long getMinPunchOutTreshholdInMilliSecs() {
        return 1000l * SettingsImpl.minPunchOutTreshholdInSecs;
    }

    public static void setMinPunchOutTreshholdInMilliSecs(final long value) {
        SettingsImpl.minPunchOutTreshholdInSecs = (int) (value / 1000l);
    }
}
