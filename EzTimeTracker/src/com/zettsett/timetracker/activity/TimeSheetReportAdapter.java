package com.zettsett.timetracker.activity;

import java.util.List;
import java.util.Locale;

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
	private final ReportDateGrouping reportDateGrouping;

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
		this.reportDateGrouping = reportDateGrouping;
		this.showNotes = showNotes;

		TimeSliceCategory.setCurrentDateTime(TimeTrackerManager
				.currentTimeMillis());
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		final Object obj = this.getItem(position);

		View itemView = convertView;
		if (obj.getClass().isAssignableFrom(Long.class)) {
			itemView = this.createItemView(R.layout.header_list_view_row,
					convertView, parent);
			this.setItemContent(itemView, obj);
		} else if (obj.getClass().isAssignableFrom(TimeSliceCategory.class)) {
			itemView = this.createItemView(R.layout.header_list_view_row,
					convertView, parent);

			this.setItemContent(itemView, obj);
		} else if (obj.getClass()
				.isAssignableFrom(ReportItemWithDuration.class)) {
			itemView = this.createItemView(R.layout.name_list_view_row,
					convertView, parent);

			this.setItemContent(itemView, obj);
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

	private String getValueGeneric(final Object obj) {
		if (obj.getClass().isAssignableFrom(TimeSlice.class)) {
			return this.getValue((TimeSlice) obj);
		} else if (obj.getClass().isAssignableFrom(TimeSliceCategory.class)) {
			return this.getValue((TimeSliceCategory) obj);
		} else if (obj.getClass().isAssignableFrom(Long.class)) {
			return this.getValue((Long) obj);
		} else if (obj.getClass()
				.isAssignableFrom(ReportItemWithDuration.class)) {
			return this.getValue((ReportItemWithDuration) obj);
		} else {
			return "";
		}
	}

	private String getValue(final long obj) {
		String currentStartDateText;
		if (this.reportDateGrouping == ReportDateGrouping.DAILY) {
			currentStartDateText = TimeSheetSummaryCalculator2.dt
					.getLongDateStr(obj);
		} else if (this.reportDateGrouping == ReportDateGrouping.WEEKLY) {
			currentStartDateText = TimeSheetSummaryCalculator2.dt
					.getWeekStr(obj);
		} else if (this.reportDateGrouping == ReportDateGrouping.MONTHLY) {
			currentStartDateText = TimeSheetSummaryCalculator2.dt
					.getMonthStr(obj);
		} else if (this.reportDateGrouping == ReportDateGrouping.YEARLY) {
			currentStartDateText = TimeSheetSummaryCalculator2.dt
					.getYearString(obj);
		} else {
			throw new IllegalArgumentException("Unknown ReportDateGrouping "
					+ this.reportDateGrouping);
		}
		return currentStartDateText;
	}

	private String getValue(final TimeSlice obj) {
		return obj.getTitleWithDuration();
	}

	private String getValue(final TimeSliceCategory obj) {
		return obj.toString(); // .getCategoryName();
	}

	private String getValue(final ReportItemWithDuration obj) {
		return this.getValueGeneric(obj.subKey) + ": "
				+ this.timeInMillisToText(obj.duration, false);
	}

	private String timeInMillisToText(final long totalTimeInMillis,
			final boolean longVersion) {
		final long minutes = (totalTimeInMillis / (1000 * 60)) % 60;
		final long hours = totalTimeInMillis / (1000 * 60 * 60);

		String hoursWord;
		if (hours == 1) {
			hoursWord = this.getContext().getString(R.string.hoursWord1);
		} else {
			hoursWord = this.getContext().getString(R.string.hoursWordN);
		}

		if (longVersion) {
			String minutesWord;

			if (minutes == 1) {
				minutesWord = this.getContext()
						.getString(R.string.minutesWord1);
			} else {
				minutesWord = this.getContext()
						.getString(R.string.minutesWordN);
			}
			final String timeString = hours + " " + hoursWord + ", " + minutes
					+ " " + minutesWord;
			return timeString;
		}
		return String.format(Locale.GERMANY, " %1$d %3$s %2$02d", hours,
				minutes, hoursWord);
	}

	private void setItemContent(final View view, final Object obj) {
		final TextView nameView = (TextView) view.findViewById(R.id.name);
		nameView.setText(this.getValueGeneric(obj));
		view.setTag(obj);
	}
}
