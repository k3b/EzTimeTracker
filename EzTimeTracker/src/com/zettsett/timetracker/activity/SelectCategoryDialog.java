package com.zettsett.timetracker.activity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.Settings;
import com.zettsett.timetracker.TimeTrackerManager;
import com.zettsett.timetracker.model.TimeSliceCategory;

/**
 * Ask for Category
 * @author EVE
 *
 */
public class SelectCategoryDialog extends Dialog {

	private final ListView list;
	private final TimeSliceCategory newItemPlaceholder;

	public SelectCategoryDialog(Context context, int style, TimeSliceCategory newItemPlaceholder) {
		super(context, style);
		this.newItemPlaceholder = newItemPlaceholder;
		// setTitle("Punch In for Activity");
		this.list = new ListView(context);
		this.list.setAdapter(createCategoryListAdapter());
		LinearLayout contentView = new LinearLayout(context);
		contentView.setOrientation(LinearLayout.VERTICAL);
		contentView.addView(this.list);
		setContentView(contentView);
	}

	@Override
	public void show()
	{
		this.list.setAdapter(createCategoryListAdapter());

		super.show();
	}

	private ArrayAdapter<TimeSliceCategory> createCategoryListAdapter() {
		long currentDateTime = (Settings.getHideInactiveCategories()) 
					? TimeTrackerManager.currentTimeMillis()
					: TimeSliceCategory.MIN_VALID_DATE;			

		return CategoryListAdapterDetailed.createAdapter(getContext(),
				R.layout.punchin_list_view_row, false, this.newItemPlaceholder, currentDateTime, "SelectCategoryDialog");
	}
	
	public SelectCategoryDialog setCategoryCallback(final CategorySetter callback) {
		this.list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				TimeSliceCategory cat = (TimeSliceCategory) list.getItemAtPosition(position);
				hide();
				callback.setCategory(cat);
			}

		});
		return this;
	}
}
