package de.k3b.timetracker.activity;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.k3b.timetracker.R;
import de.k3b.timetracker.TimeTrackerManager;
import de.k3b.timetracker.database.TimeSliceCategoryRepsitory;
import de.k3b.timetracker.model.TimeSliceCategory;

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
		TimeSliceCategory.setCurrentDateTime(TimeTrackerManager
				.currentTimeMillis());
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		final TimeSliceCategory obj = this.getItem(position);

		View itemView = convertView;
		if (itemView == null) {
			itemView = this.createItemView();
		}

		this.setItemContent(itemView, obj);
		return itemView;
	}

	private View createItemView() {
		View itemView;
		final LayoutInflater vi = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		itemView = vi.inflate(this.viewId, null);

		if ((this.itemHight == 0) && (this.withDescription)) {
			this.itemHight = itemView.findViewById(R.id.description)
					.getHeight();

		}
		return itemView;
	}

	private void setItemContent(final View itemView, final TimeSliceCategory obj) {
		if (obj != null) {
			final TextView nameView = (TextView) itemView
					.findViewById(R.id.name);
			final TextView descriptionView = (TextView) itemView
					.findViewById(R.id.description);
			final TextView activeView = (TextView) itemView
					.findViewById(R.id.active);

			if (this.withDescription) {
				this.setTextViewContent(descriptionView, obj.getDescription());
				this.setTextViewContent(activeView, obj.getActiveDate());
				this.setTextViewContent(nameView, obj.toString());
			} else {
				this.setTextViewContent(descriptionView, null);
				this.setTextViewContent(activeView, null);
				this.setTextViewContent(nameView, obj.toString());
			}
		}
	}

	private void setTextViewContent(final TextView view, final String text) {
		if (view != null) {
			if (this.itemHight == 0) {
				this.itemHight = view.getHeight();
			}
			if ((text != null) && (text.length() > 0)) {
				view.setText(text);
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
