package com.zettsett.timetracker.activity;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.database.TimeSliceCategoryRepsitory;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class CategoryListAdapterDetailed extends ArrayAdapter<TimeSliceCategory> {

	/**
	 * Corresponds to filter ignore catagory or edit to create a new category
	 */
	public static final int NO_CATEGORY = -1;
	
	private final Context context;
	
	/**
	 * false means short disply only with categoryname but witout description
	 */
	private final boolean withDescription;
	private final int viewId;
	
	/**
	 * Prefex to be prepended before each category name
	 */
	private final String namePrefix;
	
	/**
	 * Prefex to be prepended befor each category description
	 */
	private final String descriptionPrefix;
	
	/**
	 * dateTime when isActive should be tested
	 */
	private final long currentDateTime;
	
	public CategoryListAdapterDetailed(Context context, int textViewResourceId,
			List<TimeSliceCategory> items, boolean withDescription, long currentDateTime) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.context = context;
		this.withDescription = withDescription;
		this.viewId = textViewResourceId;
		this.namePrefix = context.getString(R.string.category_name) + " ";
		this.descriptionPrefix = context.getString(R.string.category_description) + " ";
		this.currentDateTime = currentDateTime;
	}

	private final List<TimeSliceCategory> items;

	public static CategoryListAdapterDetailed createAdapter(
			Context context, int viewId, boolean withDescription, TimeSliceCategory firstElement, long currentDateTime) {
		TimeSliceCategoryRepsitory repository = new TimeSliceCategoryRepsitory(context);

		List<TimeSliceCategory> categories = repository
				.fetchAllTimeSliceCategories(currentDateTime);
		
		if(firstElement != null)
		{
			categories.add(0, firstElement);
		}
		return new CategoryListAdapterDetailed(context,
				viewId, categories, withDescription, currentDateTime);
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
			TextView nameView = (TextView) view
					.findViewById(R.id.category_list_view_name_field);
			TextView descriptionView = (TextView) view
					.findViewById(R.id.category_list_view_description_field);
			TextView activeView = (TextView) view
				.findViewById(R.id.category_list_view_active_field);
			
			if(withDescription) {
				nameView.setText(this.namePrefix + category.getCategoryName());
				String description = category.getDescription();
				if (description.length() > 0) {
					descriptionView.setText(this.descriptionPrefix + description);
				} else {
					descriptionView.setHeight(0);
				}
				String active = category.getActiveDate();
				if (active.length() > 0) {
					activeView.setText(active);
				} else {
					activeView.setHeight(0);
				}
			} else {
				nameView.setText(category.getCategoryName());
				descriptionView.setHeight(0);
				activeView.setHeight(0);
			}
			
			boolean isActive = category.isActive(this.currentDateTime);
			if (!isActive) {
				nameView.setTextColor(Color.GRAY);
			}
		}
		return view;
	}
}
