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
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

/**
 * Contains header items of type Long as date or TimeSliceCategory<br />
 * and detail items of type TimeSlice or ReportItemWithDuration.<br/>
 * 
 * This class was inspired by http://stackoverflow.com/questions/3825377 .<br/>
 */
public class TimeSheetReportAdapter extends ArrayAdapter<Object> {

	private final boolean showNotes;
	private final ReportItemFormatter formatter;

	public TimeSheetReportAdapter(final Context context,
			final List<Object> objects, final boolean showNotes) {
		this(context, objects, showNotes, ReportDateGrouping.DAILY);
	}

	public TimeSheetReportAdapter(final Context context,
			final List<Object> objects,
			final ReportDateGrouping reportDateGrouping) {
		this(context, objects, false, reportDateGrouping);
	}

	private TimeSheetReportAdapter(final Context context,
			final List<Object> objects, final boolean showNotes,
			final ReportDateGrouping reportDateGrouping) {
		super(context, 0, objects);
		this.showNotes = showNotes;

		this.formatter = new ReportItemFormatter(context, reportDateGrouping);

		TimeSliceCategory.setCurrentDateTime(TimeTrackerManager
				.currentTimeMillis());
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		final Object item = this.getItem(position);

		View itemView = convertView;
		final Class<? extends Object> itemClass = item.getClass();
		if (itemClass.isAssignableFrom(Long.class)) {
			itemView = this.createItemView(R.layout.header_list_view_row,
					convertView, parent);
			this.setItemContent(itemView, item);
		} else if (itemClass.isAssignableFrom(TimeSliceCategory.class)) {
			itemView = this.createItemView(R.layout.header_list_view_row,
					convertView, parent);

			this.setItemContent(itemView, item);
		} else if (itemClass.isAssignableFrom(ReportItemWithDuration.class)) {
			final ReportItemWithDuration reportItem = (ReportItemWithDuration) item;
			final boolean showNotes = (this.showNotes && reportItem.hasNotes());

			if (showNotes) {
				itemView = this.createItemView(
						R.layout.name_description_list_view_row, convertView,
						parent);
			} else {
				itemView = this.createItemView(R.layout.name_list_view_row,
						convertView, parent);
			}
			this.setItemContent(itemView, reportItem);
			if (showNotes) {
				final TextView descriptionView = (TextView) itemView
						.findViewById(R.id.description);
				if (descriptionView != null) {
					descriptionView.setText(reportItem.getNotes());
				}
			}
		} else if (itemClass.isAssignableFrom(TimeSlice.class)) {
			final TimeSlice aSlice = (TimeSlice) item;
			final boolean showNotes = (this.showNotes && aSlice.hasNotes());

			if (showNotes) {
				itemView = this.createItemView(
						R.layout.name_description_list_view_row, convertView,
						parent);
			} else {
				itemView = this.createItemView(R.layout.name_list_view_row,
						convertView, parent);
			}
			this.setItemContent(itemView, aSlice);
			if (showNotes) {
				final TextView descriptionView = (TextView) itemView
						.findViewById(R.id.description);
				if (descriptionView != null) {
					descriptionView.setText(aSlice.getNotes());
				}
			}
		}

		return itemView;
	}

	private View createItemView(final int resource, final View convertView,
			final ViewGroup parent) {

		final LayoutInflater layoutInflater = (LayoutInflater) this
				.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View itemView = layoutInflater.inflate(resource, null);
		itemView.setId(resource);
		return itemView;
	}

	private void setItemContent(final View view, final Object obj) {
		final TextView nameView = (TextView) view.findViewById(R.id.name);
		nameView.setText(this.formatter.getValueGeneric(obj));
		view.setTag(obj);
	}
}
