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

public class CategoryListAdapterDetailed extends
		ArrayAdapter<TimeSliceCategory> {

	/**
	 * Corresponds to filter ignore catagory or edit to create a new category
	 */
	public static final int NO_CATEGORY = -1;

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
	 * Workaround for recycled Items: Sometimes Text is not visible because
	 * previous ItemHeight==0 is sometimes remembered.
	 */
	private int itemHight = 0;

	public static ArrayAdapter<TimeSliceCategory> createAdapter(
			final Context context, final int viewId,
			final boolean withDescription,
			final TimeSliceCategory firstElement,
			final long currentDateTimeDatabaseLoad, final String debugContext) {
		final TimeSliceCategoryRepsitory repository = new TimeSliceCategoryRepsitory(
				context);

		final List<TimeSliceCategory> categories = repository
				.fetchAllTimeSliceCategories(currentDateTimeDatabaseLoad,
						debugContext + "-CategoryListAdapterDetailed");

		if (firstElement != null) {
			categories.add(0, firstElement);
		}
		return new CategoryListAdapterDetailed(context, viewId, categories,
				withDescription);
	}

	private CategoryListAdapterDetailed(final Context context,
			final int textViewResourceId, final List<TimeSliceCategory> items,
			final boolean withDescription) {
		super(context, textViewResourceId, items);
		this.withDescription = withDescription;
		this.viewId = textViewResourceId;
		this.namePrefix = context.getString(R.string.category_name) + " ";
		this.descriptionPrefix = context
				.getString(R.string.category_description) + " ";
		TimeSliceCategory.setCurrentDateTime(TimeTrackerManager
				.currentTimeMillis());
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			final LayoutInflater vi = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(this.viewId, null);

			if ((this.itemHight == 0) && (this.withDescription)) {
				this.itemHight = view.findViewById(R.id.description)
						.getHeight();

			}
		}
		final TimeSliceCategory category = this.getItem(position);
		if (category != null) {
			final TextView nameView = (TextView) view.findViewById(R.id.name);
			final TextView descriptionView = (TextView) view
					.findViewById(R.id.description);
			final TextView activeView = (TextView) view
					.findViewById(R.id.active);

			if (this.withDescription) {
				this.setContent(descriptionView, this.descriptionPrefix,
						category.getDescription());
				this.setContent(activeView, "", category.getActiveDate());
				this.setContent(nameView, this.namePrefix, category.toString());
			} else {
				this.setContent(descriptionView, "", null);
				this.setContent(activeView, "", null);
				this.setContent(nameView, "", category.toString());
			}
		}
		return view;
	}

	private void setContent(final TextView view, final String prefix,
			final String text) {
		if (view != null) {
			if (this.itemHight == 0) {
				this.itemHight = view.getHeight();
			}
			if ((text != null) && (text.length() > 0)) {
				view.setText(prefix + text);
				if (this.itemHight == 0) {
					this.itemHight = view.getHeight();
				}
				if ((this.itemHight > 0) && (view.getHeight() == 0)) {
					// Workaround for recycled Items:
					// Sometimes Text is not visible because previous
					// ItemHeight==0 is sometimes remembered.
					view.setHeight(this.itemHight);
				}
			} else {
				view.setHeight(0);
			}
		}
	}
}
