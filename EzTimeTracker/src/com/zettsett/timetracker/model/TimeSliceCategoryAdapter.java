package com.zettsett.timetracker.model;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.database.TimeSliceCategoryRepsitory;

public class TimeSliceCategoryAdapter extends ArrayAdapter<TimeSliceCategory> {

	public static final int NO_CATEGORY = -1;
	private final Context context;
	private final boolean withDescription;
	private final int viewId;
	
	public TimeSliceCategoryAdapter(Context context, int textViewResourceId,
			List<TimeSliceCategory> items, boolean withDescription) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.context = context;
		this.withDescription = withDescription;
		this.viewId = textViewResourceId;
	}

	private final List<TimeSliceCategory> items;

	public static TimeSliceCategoryAdapter getTimeSliceCategoryAdapterFromDB(
			Context context, int viewId, boolean withDescription, TimeSliceCategory firstElement, long currentDateTime) {
		TimeSliceCategoryRepsitory repository = new TimeSliceCategoryRepsitory(context);

		List<TimeSliceCategory> categories = repository
				.fetchAllTimeSliceCategories(currentDateTime);
		
		if(firstElement != null)
		{
			categories.add(0, firstElement);
		}
		return new TimeSliceCategoryAdapter(context,
				viewId, categories, withDescription);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(viewId, null);
		}
		TimeSliceCategory category = items.get(position);
		if (category != null) {
			TextView tt = (TextView) view
					.findViewById(R.id.category_list_view_name_field);
			TextView bt = (TextView) view
					.findViewById(R.id.category_list_view_description_field);
			if(withDescription) {
				tt.setText("Name: " + category.getCategoryName());
				bt.setText("Description: " + category.getDescription());
			} else {
				tt.setText(category.getCategoryName());
			}
		}
		return view;
	}
}
