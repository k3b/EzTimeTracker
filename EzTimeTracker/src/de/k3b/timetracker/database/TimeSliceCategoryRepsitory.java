package de.k3b.timetracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.k3b.timetracker.Global;
import de.k3b.timetracker.R;
import de.k3b.timetracker.model.TimeSliceCategory;

/**
 * handles android specific database persistance for {@link de.k3b.timetracker.model.TimeSliceCategory}.
 */
public class TimeSliceCategoryRepsitory implements ICategoryRepsitory {

    private static final DatabaseInstance DB = DatabaseInstance
            .getCurrentInstance();
    private final Context context;

    public TimeSliceCategoryRepsitory(final Context context) {
        super();
        this.context = context;
    }

    private ContentValues timeSliceCategoryContentValuesList(
            final TimeSliceCategory category) {
        final ContentValues values = new ContentValues();
        values.put(TimeSliceCategorySql.COL_CATEGORY_NAME,
                category.getCategoryName());
        values.put(TimeSliceCategorySql.COL_DESCRIPTION,
                category.getDescription());

        if (DatabaseHelper.DATABASE_VERSION >= DatabaseHelper.DATABASE_VERSION_4_CATEGORY_ACTIVE) {
            values.put(TimeSliceCategorySql.COL_START_TIME,
                    category.getStartTime());
            values.put(TimeSliceCategorySql.COL_END_TIME,
                    category.getEndTime());
        }
        return values;
    }

    /* (non-Javadoc)
     * @see de.k3b.timetracker.database.ICategoryRepsitory#getOrCreateTimeSlice(java.lang.String)
     */
    @Override
    public TimeSliceCategory getOrCreateCategory(final String name) {
        Cursor cur = null;
        try {
            cur = TimeSliceCategoryRepsitory.DB.getWritableDatabase().query(
                    TimeSliceCategorySql.TIME_SLICE_CATEGORY_TABLE,
                    this.columnList(), TimeSliceCategorySql.COL_CATEGORY_NAME +
                            " = ?",
                    new String[]{name}, null, null, null
            );
            if (cur.moveToNext()) {
                return this.fillTimeSliceCategoryFromCursor(cur);
            }
        } finally {
            if (cur != null) {
                cur.close();
            }
        }

        return this.createTimeSliceCategoryFromName(name);
    }

    /**
     * Creates new TimeSliceCategory in database.
     *
     * @return newID generated for dbItem.
     */
    public long createTimeSliceCategory(final TimeSliceCategory category) {

        final long newID = TimeSliceCategoryRepsitory.DB.getWritableDatabase()
                .insert(TimeSliceCategorySql.TIME_SLICE_CATEGORY_TABLE, null,
                        this.timeSliceCategoryContentValuesList(category));
        category.setRowId((int) newID);
        return newID;
    }

    private TimeSliceCategory createTimeSliceCategoryFromName(final String name) {
        final TimeSliceCategory category = new TimeSliceCategory();
        category.setCategoryName(name);
        this.createTimeSliceCategory(category);
        return category;
    }

    /**
     * Internal helper that returns all colums supported by db-model
     */
    private String[] columnList() {
        final List<String> columns = new ArrayList<String>();
        columns.add(TimeSliceCategorySql.COL_PK);
        columns.add(TimeSliceCategorySql.COL_CATEGORY_NAME);
        columns.add(TimeSliceCategorySql.COL_DESCRIPTION);

        if (DatabaseHelper.DATABASE_VERSION >= DatabaseHelper.DATABASE_VERSION_4_CATEGORY_ACTIVE) {
            columns.add(TimeSliceCategorySql.COL_START_TIME);
            columns.add(TimeSliceCategorySql.COL_END_TIME);
        }
        return columns.toArray(new String[columns.size()]);
    }

    public List<TimeSliceCategory> fetchAllTimeSliceCategories(
            final long currentDateTime, final String debugContext) {
        List<TimeSliceCategory> result = new ArrayList<TimeSliceCategory>();
        Cursor cur = null;
        final String filter = this.createCategoryListFilter(currentDateTime);
        if (Global.isDebugEnabled()) {
            Log.d(Global.LOG_CONTEXT,
                    debugContext
                            + "-TimeSliceCategoryRepsitory.fetchAllTimeSliceCategories("
                            + filter + ")"
            );
        }
        try {
            cur = TimeSliceCategoryRepsitory.DB.getWritableDatabase().query(
                    TimeSliceCategorySql.TIME_SLICE_CATEGORY_TABLE,
                    this.columnList(),
                    filter,
                    null,
                    null,
                    null,
                    "lower(" + TimeSliceCategorySql.COL_CATEGORY_NAME
                            + ")"
            ); // order by name
            while (cur.moveToNext()) {
                final TimeSliceCategory cat = this
                        .fillTimeSliceCategoryFromCursor(cur);
                result.add(cat);
            }
        } finally {
            if (cur != null) {
                cur.close();
            }
        }

        // Database does not contain categories yet, create them
        if (result.size() == 0) {
            this.createInitialDemoCategoriesFromResources();
            result = this.fetchAllTimeSliceCategories(currentDateTime,
                    debugContext); // reload the demo items
        }
        return result;
    }

    private String createCategoryListFilter(final long currentDateTime) {
        if (currentDateTime != TimeSliceCategory.MIN_VALID_DATE) {
            return "((" + TimeSliceCategorySql.COL_START_TIME + " IS NULL) " +
                    "OR ("
                    + TimeSliceCategorySql.COL_START_TIME + " <= " + currentDateTime + ")) AND (("
                    + TimeSliceCategorySql.COL_END_TIME + " IS NULL " + ") " +
                    "OR (" + TimeSliceCategorySql.COL_END_TIME + " >= " + currentDateTime + "))";
        }
        return null;
    }

    private TimeSliceCategory fillTimeSliceCategoryFromCursor(final Cursor cur) {
        final TimeSliceCategory cat = new TimeSliceCategory();
        if (!cur.isAfterLast()) {
            cat.setRowId(cur.getInt(cur
                    .getColumnIndexOrThrow(TimeSliceCategorySql.COL_PK)));
            cat.setCategoryName(cur.getString(cur
                    .getColumnIndex(TimeSliceCategorySql.COL_CATEGORY_NAME)));
            cat.setDescription(cur.getString(cur
                    .getColumnIndex(TimeSliceCategorySql.COL_DESCRIPTION)));

            if (DatabaseHelper.DATABASE_VERSION >= DatabaseHelper.DATABASE_VERSION_4_CATEGORY_ACTIVE) {
                cat.setStartTime(this.getLong(cur,
                        TimeSliceCategorySql.COL_START_TIME,
                        TimeSliceCategory.MIN_VALID_DATE));
                cat.setEndTime(this.getLong(cur,
                        TimeSliceCategorySql.COL_END_TIME,
                        TimeSliceCategory.MAX_VALID_DATE));
            }
        }
        return cat;
    }

    /**
     * get long value from cursor.
     *
     * @param nullValue returned if column is null
     * @return nullValue if column is null
     */
    private long getLong(final Cursor cursor, final String columnName,
                         final long nullValue) {
        final int colIndex = cursor.getColumnIndexOrThrow(columnName);
        if (!cursor.isNull(colIndex)) {
            return cursor.getLong(colIndex);
        }
        return nullValue;
    }

    /**
     * Delete item by rowID.
     *
     * @return true if item found and deleted.
     */
    public boolean delete(final long rowId) {
        return TimeSliceCategoryRepsitory.DB.getWritableDatabase().delete(
                TimeSliceCategorySql.TIME_SLICE_CATEGORY_TABLE,
                TimeSliceCategorySql.COL_PK + "=" + rowId, null) > 0;
    }

    public long update(final TimeSliceCategory timeSliceCategory) {
        return TimeSliceCategoryRepsitory.DB.getWritableDatabase().update(
                TimeSliceCategorySql.TIME_SLICE_CATEGORY_TABLE,
                this.timeSliceCategoryContentValuesList(timeSliceCategory),
                TimeSliceCategorySql.COL_PK + " = " + timeSliceCategory.getRowId(), null);
    }

    public TimeSliceCategory fetchByRowID(final long rowId) throws SQLException {

        Cursor cur = null;

        try {
            cur = TimeSliceCategoryRepsitory.DB.getWritableDatabase().query(
                    true, TimeSliceCategorySql.TIME_SLICE_CATEGORY_TABLE,
                    this.columnList(),
                    TimeSliceCategorySql.COL_PK + " = ?",
                    new String[]{Long.toString(rowId)}, null, null, null,
                    null);
            if ((cur != null) && (cur.moveToFirst())) {
                return this.fillTimeSliceCategoryFromCursor(cur);
            }
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        Log.e(Global.LOG_CONTEXT,
                "Not found : TimeSliceCategoryDBAdapter.fetchByRowID(rowId = "
                        + rowId + ")"
        );
        return null;
    }

    private void createInitialDemoCategoriesFromResources() {
        final Resources res = this.context.getResources();
        final String[] catNames = res
                .getStringArray(R.array.default_categories);
        for (final String catName : catNames) {
            this.createTimeSliceCategoryFromName(catName);
        }
    }

}
