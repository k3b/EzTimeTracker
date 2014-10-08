package de.k3b.timetracker.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.k3b.timetracker.R;
import de.k3b.timetracker.TimeTrackerManager;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;
import de.k3b.timetracker.report.DurationFormatterAndroid;
import de.k3b.timetracker.report.IReportItemFormatter;
import de.k3b.timetracker.report.ReportDateGrouping;
import de.k3b.timetracker.report.ReportItemFormatter;
import de.k3b.timetracker.report.ReportItemWithStatistics;

/**
 * creates and fills item in listview.<br/>
 * Contains header items of type Long as date or TimeSliceCategory<br />
 * and detail items of type TimeSlice or ReportItemWithStatistics.<br/>
 * <p/>
 * This class was inspired by http://stackoverflow.com/questions/3825377 .<br/>
 */
class TimeSheetReportAdapter extends ArrayAdapter<Object> {

    private final boolean showNotes;
    private final IReportItemFormatter formatter;

    public TimeSheetReportAdapter(final Context context,
                                  final List<Object> objects, final boolean showNotes) {
        this(context, objects, showNotes, ReportDateGrouping.DAILY);
    }

    public TimeSheetReportAdapter(final Context context,
                                  final List<Object> objects, final boolean showNotes,
                                  final ReportDateGrouping reportDateGrouping) {
        super(context, 0, objects);
        this.showNotes = showNotes;

        this.formatter = new ReportItemFormatter(new DurationFormatterAndroid(context), reportDateGrouping);

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
        } else if (itemClass.isAssignableFrom(ReportItemWithStatistics.class)) {
            final ReportItemWithStatistics reportItem = (ReportItemWithStatistics) item;
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
