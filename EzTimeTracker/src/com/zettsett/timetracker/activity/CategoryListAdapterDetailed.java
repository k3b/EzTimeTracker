package com.zettsett.timetracker.activity;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.TimeTrackerManager;
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
	
	/**
	 * The Resource used for the adapter
	 */
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
	 * Items that are displayed in the adapter
	 */
	private final List<TimeSliceCategory> items;

	/**
	 * Workaround for recycled Items: 
	 * Sometimes Text is not visible because previos 
	 * ItemHeight==0 is sometimes remembered.
	 */
	private int itemHight = 0;
	
	public static ArrayAdapter<TimeSliceCategory> createAdapter(
			Context context, int viewId, boolean withDescription, TimeSliceCategory firstElement, 
			long currentDateTimeDatabaseLoad, String debugContext) {
		TimeSliceCategoryRepsitory repository = new TimeSliceCategoryRepsitory(context);

		List<TimeSliceCategory> categories = repository
				.fetchAllTimeSliceCategories(currentDateTimeDatabaseLoad, debugContext + "-CategoryListAdapterDetailed");
		
		
		if(firstElement != null)
		{
			categories.add(0, firstElement);
		}
		return new CategoryListAdapterDetailed(context,
				viewId, categories, withDescription);
	}

	private CategoryListAdapterDetailed(Context context, int textViewResourceId,
			List<TimeSliceCategory> items, boolean withDescription) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.context = context;
		this.withDescription = withDescription;
		this.viewId = textViewResourceId;
		this.namePrefix = context.getString(R.string.category_name) + " ";
		this.descriptionPrefix = context.getString(R.string.category_description) + " ";
		TimeSliceCategory.setCurrentDateTime(TimeTrackerManager.currentTimeMillis());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(viewId, null);
			
			if ((itemHight == 0) && (withDescription)) {
				itemHight = view
							.findViewById(R.id.category_list_view_description_field)
							.getHeight();
				
			}
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
				setContent(descriptionView, this.descriptionPrefix, category.getDescription());
				setContent(activeView, "", category.getActiveDate());
				setContent(nameView, this.namePrefix, category.toString());
			} else {
				setContent(descriptionView, "", null);
				setContent(activeView, "", null);
				setContent(nameView, "", category.toString());
			}
		}
		return view;
	}

	private void setContent(TextView view, String prefix, String text) {
		if (view != null) {
			if (itemHight == 0) {
				itemHight = view.getHeight();
			}
			if ((text != null) && (text.length() > 0)) {
				view.setText(prefix + text);
				if (itemHight == 0) {
					itemHight = view.getHeight();
				}
				if ((itemHight > 0) && (view.getHeight() == 0)) {
					// Workaround for recycled Items: 
					// Sometimes Text is not visible because previos 
					// ItemHeight==0 is sometimes remembered.
					view.setHeight(this.itemHight);					
				}
			} else {
				view.setHeight(0);
			}
		}
	}
}
