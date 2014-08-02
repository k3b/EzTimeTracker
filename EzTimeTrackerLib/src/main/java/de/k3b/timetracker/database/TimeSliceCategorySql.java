package de.k3b.timetracker.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.k3b.timetracker.model.TimeSliceCategory;

/**
 * TimeSliceCategory dependent sql with no dependencies to android.<br/>
 * Used by {@link de.k3b.timetracker.database.ICategoryRepsitory} to build sql.<br/>
 * <p/>
 * Scope=package to allow unittesting.<br/>
 */
class TimeSliceCategorySql {
    public static final String TIME_SLICE_CATEGORY_TABLE = "time_slice_category";
    static final String COL_PK = "_id";
    static final String COL_CATEGORY_NAME = "category_name";
    static final String COL_START_TIME = "start_time";
    static final String COL_END_TIME = "end_time";
    static final String COL_DESCRIPTION = "description";
    static final String CREATE_TABLE = "CREATE TABLE " + TIME_SLICE_CATEGORY_TABLE
            + "(" + COL_PK + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_CATEGORY_NAME + " TEXT, "
            + COL_DESCRIPTION + " TEXT,"
            + COL_START_TIME + " DATE,"
            + COL_END_TIME + " DATE" + // v4
            ")";

    /**
     * converts {@link de.k3b.timetracker.model.TimeSliceCategory}  to {@link java.util.HashMap}
     */
    static Map<String, String> asMap(final TimeSliceCategory category) {
        final HashMap<String, String> values = new HashMap<String, String>();
        values.put(TimeSliceCategorySql.COL_CATEGORY_NAME,
                category.getCategoryName());
        values.put(TimeSliceCategorySql.COL_DESCRIPTION,
                category.getDescription());

        values.put(TimeSliceCategorySql.COL_START_TIME,
                Long.toString(category.getStartTime()));
        values.put(TimeSliceCategorySql.COL_END_TIME,
                Long.toString(category.getEndTime()));
        final int rowId = category.getRowId();

        if (rowId != TimeSliceCategory.NOT_SAVED) {
            values.put(TimeSliceCategorySql.COL_PK,
                    Long.toString(rowId));
        }
        return values;
    }

    static void fromMap(final TimeSliceCategory cat, final Map<String, String> values) {
        cat.setRowId(NumberUtil.getInt(values.get(TimeSliceCategorySql.COL_PK), -1));
        cat.setCategoryName(values.get((TimeSliceCategorySql.COL_CATEGORY_NAME)));
        cat.setDescription(values.get((TimeSliceCategorySql.COL_DESCRIPTION)));

        cat.setStartTime(NumberUtil.getLong(values.get(TimeSliceCategorySql.COL_START_TIME),
                TimeSliceCategory.MIN_VALID_DATE));
        cat.setEndTime(NumberUtil.getLong(values.get(TimeSliceCategorySql.COL_END_TIME),
                TimeSliceCategory.MAX_VALID_DATE));
    }

    /**
     * Internal helper that returns all colums supported by db-model
     */
    static String[] columnList() {
        final List<String> columns = new ArrayList<String>();
        columns.add(COL_PK);
        columns.add(COL_CATEGORY_NAME);
        columns.add(COL_DESCRIPTION);

        columns.add(COL_START_TIME);
        columns.add(COL_END_TIME);
        return columns.toArray(new String[columns.size()]);
    }

    static String createCategoryListFilter(final long currentDateTime) {
        if (currentDateTime != TimeSliceCategory.MIN_VALID_DATE) {
            return "((" + COL_START_TIME + " IS NULL) " +
                    "OR ("
                    + COL_START_TIME + " <= " + currentDateTime + ")) AND (("
                    + COL_END_TIME + " IS NULL " + ") " +
                    "OR (" + COL_END_TIME + " >= " + currentDateTime + "))";
        }
        return null;
    }

    static String getWhereByPK(final long rowId) {
        return COL_PK + "=" + rowId;
    }
}
