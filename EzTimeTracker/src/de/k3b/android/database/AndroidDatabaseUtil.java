package de.k3b.android.database;

import android.content.ContentValues;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;

import java.util.Map;

/**
 * Created by k3b on 02.08.2014.
 */
public class AndroidDatabaseUtil {

    /**
     * Read the entire contents of a cursor row and store them in a ContentValues.
     *
     * @param cursor the cursor to read from.
     * @param values the {@link java.util.Map} to put the row into.
     */
    public static void cursorRowToContentValues(Cursor cursor, Map<String, String> values) {
        AbstractWindowedCursor awc =
                (cursor instanceof AbstractWindowedCursor) ? (AbstractWindowedCursor) cursor : null;

        String[] columns = cursor.getColumnNames();
        int length = columns.length;
        for (int i = 0; i < length; i++) {
            if (awc != null && awc.isBlob(i)) {
                values.put(columns[i], cursor.getBlob(i).toString());
            } else {
                values.put(columns[i], cursor.getString(i));
            }
        }
    }

    /**
     * converts from android independeant {@link java.util.HashMap} to android dependent {@link android.content.ContentValues}
     */
    public static ContentValues toContentValues(Map<String, String> src) {
        ContentValues result = new ContentValues();

        for (String name : src.keySet()) {
            put(result, name, src.get(name));
        }
        return result;
    }

    private static void put(final ContentValues result, final String name, final Object value) {
        if (value == null) result.put(name, (String) value);
        result.put(name, value.toString());
    }
}
