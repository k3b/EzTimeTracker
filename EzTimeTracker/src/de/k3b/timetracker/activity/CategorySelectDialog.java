package de.k3b.timetracker.activity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import de.k3b.timetracker.R;
import de.k3b.timetracker.SettingsImpl;
import de.k3b.timetracker.TimeTrackerManager;
import de.k3b.timetracker.model.TimeSliceCategory;

/**
 * Ask for Category
 *
 * @author EVE
 */
public class CategorySelectDialog extends Dialog {

    private final ListView list;
    private final TimeSliceCategory newItemPlaceholder;

    public CategorySelectDialog(final Context context, final int style,
                                final TimeSliceCategory newItemPlaceholder) {
        super(context, style);
        this.newItemPlaceholder = newItemPlaceholder;
        // setTitle("Punch In for Activity");
        this.list = new ListView(context);
        this.list.setAdapter(this.createCategoryListAdapter());
        final LinearLayout contentView = new LinearLayout(context);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.addView(this.list);
        this.setContentView(contentView);
    }

    @Override
    public void show() {
        this.list.setAdapter(this.createCategoryListAdapter());

        super.show();
    }

    private ArrayAdapter<TimeSliceCategory> createCategoryListAdapter() {
        final long currentDateTime = (SettingsImpl.getHideInactiveCategories()) ? TimeTrackerManager
                .currentTimeMillis() : TimeSliceCategory.MIN_VALID_DATE;

        return CategoryListAdapterDetailed.createAdapter(this.getContext(),
                R.layout.name_list_view_row, false,
                this.newItemPlaceholder, currentDateTime,
                "CategorySelectDialog");
    }

    public CategorySelectDialog setCategoryCallback(
            final ICategorySetter callback) {
        this.list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0, final View arg1,
                                    final int position, final long arg3) {
                final TimeSliceCategory cat = (TimeSliceCategory) CategorySelectDialog.this.list
                        .getItemAtPosition(position);
                CategorySelectDialog.this.hide();
                callback.setCategory(cat);
            }

        });
        return this;
    }
}
