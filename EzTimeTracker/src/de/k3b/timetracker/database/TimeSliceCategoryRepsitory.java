package de.k3b.timetracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.k3b.android.database.AndroidDatabaseUtil;
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
        return AndroidDatabaseUtil.toContentValues(TimeSliceCategorySql.asHashMap(category));
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
                    TimeSliceCategorySql.columnList(), TimeSliceCategorySql.COL_CATEGORY_NAME +
                            " = ?",
                    new String[]{name}, null, null, null
            );
            if (cur.moveToNext()) {
                return this.fillTimeSliceCategoryFromCursor(cur, null);
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

    public List<TimeSliceCategory> fetchAllTimeSliceCategories(
            final long currentDateTime, final String debugContext) {
        List<TimeSliceCategory> result = new ArrayList<TimeSliceCategory>();
        Cursor cur = null;
        final String filter = TimeSliceCategorySql.createCategoryListFilter(currentDateTime);
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
                    TimeSliceCategorySql.columnList(),
                    filter,
                    null,
                    null,
                    null,
                    "lower(" + TimeSliceCategorySql.COL_CATEGORY_NAME
                            + ")"
            ); // order by name
            final HashMap<String, String> values = new HashMap<String, String>();

            while (cur.moveToNext()) {
                final TimeSliceCategory cat = this
                        .fillTimeSliceCategoryFromCursor(cur, values);
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

    private TimeSliceCategory fillTimeSliceCategoryFromCursor(final Cursor cur, HashMap<String, String> values) {
        final TimeSliceCategory cat = new TimeSliceCategory();
        if (!cur.isAfterLast()) {
            if (values == null) values = new HashMap<String, String>();
            AndroidDatabaseUtil.cursorRowToContentValues(cur, values);

            TimeSliceCategorySql.fromMap(cat, values);
        }
        return cat;
    }

    /**
     * Delete item by rowID.
     *
     * @return true if item found and deleted.
     */
    public boolean delete(final long rowId) {
        return TimeSliceCategoryRepsitory.DB.getWritableDatabase().delete(
                TimeSliceCategorySql.TIME_SLICE_CATEGORY_TABLE,
                TimeSliceCategorySql.getWhereByPK(rowId), null) > 0;
    }

    public long update(final TimeSliceCategory timeSliceCategory) {
        return TimeSliceCategoryRepsitory.DB.getWritableDatabase().update(
                TimeSliceCategorySql.TIME_SLICE_CATEGORY_TABLE,
                this.timeSliceCategoryContentValuesList(timeSliceCategory),
                TimeSliceCategorySql.getWhereByPK(timeSliceCategory.getRowId()), null);
    }

    public TimeSliceCategory fetchByRowID(final long rowId) throws SQLException {

        Cursor cur = null;

        try {
            cur = TimeSliceCategoryRepsitory.DB.getWritableDatabase().query(
                    true, TimeSliceCategorySql.TIME_SLICE_CATEGORY_TABLE,
                    TimeSliceCategorySql.columnList(),
                    TimeSliceCategorySql.getWhereByPK(rowId),
                    null, null, null, null,
                    null);
            if ((cur != null) && (cur.moveToFirst())) {
                return this.fillTimeSliceCategoryFromCursor(cur, null);
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
