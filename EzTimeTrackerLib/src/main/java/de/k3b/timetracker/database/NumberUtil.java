package de.k3b.timetracker.database;

/**
 * Created by EVE on 02.08.2014.
 */
public class NumberUtil {
    static int getInt(String value, int notFoundValue) {
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        }
        return notFoundValue;
    }

    static long getLong(String value, long notFoundValue) {
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
            }
        }
        return notFoundValue;
    }
}
