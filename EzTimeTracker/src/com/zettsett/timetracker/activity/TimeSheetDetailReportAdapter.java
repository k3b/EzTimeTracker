package com.zettsett.timetracker.activity;

import java.util.List;

import android.app.Activity;
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
		final LayoutInflater layoutInflater = (LayoutInflater) this
				.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View itemView = convertView;
		if (obj.getClass().isAssignableFrom(Long.class)) {
			itemView = layoutInflater.inflate(R.layout.header_list_view_row,
					null);
			this.setupDateItemView((Long) obj, itemView);
			((Activity) this.getContext()).registerForContextMenu(itemView);
		} else if (obj.getClass().isAssignableFrom(TimeSlice.class)) {
			final TimeSlice aSlice = (TimeSlice) obj;
			final boolean showNotes = (this.showNotes
					&& (aSlice.getNotes() != null) && (aSlice.getNotes()
					.length() > 0));

			if (showNotes) {
				itemView = layoutInflater.inflate(
						R.layout.name_description_list_view_row, null);
			} else {
				itemView = layoutInflater.inflate(R.layout.name_list_view_row,
						null);
			}

			this.setupTimeSliceItemView(aSlice, showNotes, itemView);
			((Activity) this.getContext()).registerForContextMenu(itemView);
		}

		return itemView;
	}

	private void setupDateItemView(final Long obj, final View v) {
		final String value = DateTimeFormatter.getInstance().getShortDateStr(
				obj.longValue());
		final TextView nameView = (TextView) v.findViewById(R.id.name);
		nameView.setText(value);
		v.setTag(obj);
	}

	private void setupTimeSliceItemView(final TimeSlice aSlice,
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
