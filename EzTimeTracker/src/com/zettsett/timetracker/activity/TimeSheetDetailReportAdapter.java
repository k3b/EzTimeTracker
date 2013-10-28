package com.zettsett.timetracker.activity;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.model.TimeSlice;

/**
 * Contains items of type Long as header-date and TimeSlice for the detail info.<br/>
 * 
 * This class was inspired by http://stackoverflow.com/questions/3825377 .<br/>
 */
public class TimeSheetDetailReportAdapter extends ArrayAdapter<Object> {

	private final boolean showNotes;

	public TimeSheetDetailReportAdapter(final Context context,
			final List<Object> objects, final boolean showNotes) {
		super(context, 0, objects);
		this.showNotes = showNotes;
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		final Object obj = this.getItem(position);

		View itemView = convertView;
		if (obj.getClass().isAssignableFrom(Long.class)) {
			itemView = this.createItemView(R.layout.header_list_view_row,
					convertView, parent);
			this.setItemContent((Long) obj, itemView);
		} else if (obj.getClass().isAssignableFrom(TimeSlice.class)) {
			final TimeSlice aSlice = (TimeSlice) obj;
			final boolean showNotes = (this.showNotes
					&& (aSlice.getNotes() != null) && (aSlice.getNotes()
					.length() > 0));

			if (showNotes) {
				itemView = this.createItemView(
						R.layout.name_description_list_view_row, convertView,
						parent);
			} else {
				itemView = this.createItemView(R.layout.name_list_view_row,
						convertView, parent);
			}

			this.setItemContent(aSlice, showNotes, itemView);
		}

		return itemView;
	}

	private View createItemView(final int resource, final View convertView,
			final ViewGroup parent) {

		if ((convertView != null) && (convertView.getId() == resource)) {
			return convertView;
		}

		final LayoutInflater layoutInflater = (LayoutInflater) this
				.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View itemView = layoutInflater.inflate(resource, null);
		itemView.setId(resource);
		return itemView;
	}

	private void setItemContent(final Long obj, final View v) {
		final String value = DateTimeFormatter.getInstance().getLongDateStr(
				obj.longValue());
		final TextView nameView = (TextView) v.findViewById(R.id.name);
		nameView.setText(value);
		v.setTag(obj);
	}

	private void setItemContent(final TimeSlice aSlice,
			final boolean showNotes, final View v) {
		final String value = aSlice.getTitleWithDuration();
		final TextView nameView = (TextView) v.findViewById(R.id.name);
		nameView.setText(value);
		v.setTag(aSlice);
		if (showNotes) {
			final TextView descriptionView = (TextView) v
					.findViewById(R.id.description);
			if (descriptionView != null) {
				descriptionView.setText(aSlice.getNotes());
			}
		}
	}
}
