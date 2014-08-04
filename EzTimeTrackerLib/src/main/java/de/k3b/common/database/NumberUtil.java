package de.k3b.common.database;

import java.util.Map;

import de.k3b.timetracker.Global;

/**
 * Used to convert Numbers in database-dtos from string to number.
 * Created by k3b on 02.08.2014.
 */
public class NumberUtil {
    public static int getInt(String tableName, final Map<String, String> src, String key, int notFoundValue) {
        String value = src.get(key);
        if ((value != null) && (value.length() > 0)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Global.getLogger().w(tableName + "." + key + "=" + value + ":" + e.getMessage() + ". Using default " + notFoundValue + " instead.", e);
            }
        }
        return notFoundValue;
    }

    public static long getLong(String tableName, final Map<String, String> src, String key, long notFoundValue) {
        String value = src.get(key);
        if ((value != null) && (value.length() > 0)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                Global.getLogger().w(tableName + "." + key + "=" + value + ":" + e.getMessage() + ". Using default " + notFoundValue + " instead.", e);
            }
        }
        return notFoundValue;
    }
}
