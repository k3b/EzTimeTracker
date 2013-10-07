package com.zettsett.timetracker.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.EmailUtilities;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.LinearScroller;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.report.ReportOutput;
import com.zettsett.timetracker.report.ReprtExportEngine;

/**
 * Common Handling for Report-Generation and display.
 */
public class ReportFramework implements Serializable {
	private static final long serialVersionUID = 394933866214361393L;

	private static final int MENU_ITEM_EXPORT_SD = Menu.FIRST + 12;
	private static final int MENU_ITEM_EXPORT_EMAIL = Menu.FIRST + 13;
	private static final int MENU_ITEM_SET_FILTER = Menu.FIRST + 21;

	public enum ReportTypes {
		TIMESHEET, SUMMARY
	};

	// gui elements
	private final Activity activity;
	private LinearScroller scrollView;
	private List<TextView> reportViewListItems;

	// current state
	private TimeSliceFilterParameter filter;
	private ReportTypes reportType;

	ReportFramework(final Activity activity,
			final TimeSliceFilterParameter filter) {
		super();
		this.initializeDateRanges(filter);
		this.activity = activity;
	}

	private void initializeDateRanges(final TimeSliceFilterParameter filter) {
		this.filter = (filter != null) ? filter
				: new TimeSliceFilterParameter();

		final Date currDate = new Date();
		long startTime = this.filter.getStartTime();
		if (startTime == TimeSlice.NO_TIME_VALUE) {
			final Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.roll(Calendar.MONTH, false);
			calendar.roll(Calendar.MONTH, false); // -= 2 months
			startTime = calendar.getTimeInMillis();
			if (startTime > currDate.getTime()) {
				calendar.roll(Calendar.YEAR, false);
				startTime = calendar.getTimeInMillis();
			}
		}
		this.filter.setStartTime(startTime);

		long endTime = this.filter.getEndTime();
		if (endTime == TimeSlice.NO_TIME_VALUE) {
			final Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			endTime = calendar.getTimeInMillis();
			calendar.roll(Calendar.WEEK_OF_YEAR, true); // += 1 week to see also
														// entries that where
														// made after the
														// programstart.
		}
		this.filter.setEndTime(endTime);
	}

	/**
	 * called by parent Report Action to append common menuitem.
	 */
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final boolean result = true;
		final SubMenu exportMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 2,
				R.string.menu_export_report);
		exportMenu.add(Menu.NONE, ReportFramework.MENU_ITEM_EXPORT_SD, 1,
				R.string.menu_export_report_to_sd_card);
		exportMenu.add(Menu.NONE, ReportFramework.MENU_ITEM_EXPORT_EMAIL, 2,
				R.string.menu_email_report);

		menu.add(Menu.NONE, ReportFramework.MENU_ITEM_SET_FILTER, 2,
				R.string.menu_filter);

		return result;
	}

	/**
	 * called by parent Report Action to do process common menuactions.
	 */
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_SET_FILTER:
			ReportFilterActivity.showActivity(this.activity, this.filter);
			break;
		case MENU_ITEM_EXPORT_SD:
			ReprtExportEngine.exportToSD(this.getDefaultReportName(),
					this.activity,
					ReportOutput.makeFormatter(this.reportViewListItems));
			break;
		case MENU_ITEM_EXPORT_EMAIL:
			final ReportOutput outPutter = ReportOutput
					.makeFormatter(this.reportViewListItems);
			outPutter.setTerminator("\n");
			EmailUtilities.send("", this.getEMailSummaryLine(), this.activity,
					outPutter.getOutput());
			break;
		}

		return true;
	}

	public TimeSliceFilterParameter onActivityResult(final Intent intent,
			final TimeSliceFilterParameter previosRangeFilter) {
		TimeSliceFilterParameter newRangeFilter = (TimeSliceFilterParameter) intent
				.getExtras().get(Global.EXTRA_FILTER);
	
		if (newRangeFilter == null) {
			newRangeFilter = previosRangeFilter;
		}
	
		this.initializeDateRanges(newRangeFilter);
		return newRangeFilter;
	}

	private String getDefaultReportName() {
		String name;
		if (this.reportType == ReportTypes.TIMESHEET) {
			name = this.activity.getString(R.string.default_export_ts_name);
		} else {
			name = this.activity.getString(R.string.default_export_sum_name);
		}
		return name;
	}

	private String getEMailSummaryLine() {
		final String appName = this.activity.getString(R.string.app_name);
		String summary;
		if (this.reportType == ReportTypes.TIMESHEET) {
			summary = String.format(
					this.activity.getString(R.string.default_mail_ts_subject),
					appName);
		} else {
			summary = String.format(
					this.activity.getString(R.string.default_mail_sum_subject),
					appName);
		}
		return summary;

	}

	LinearScroller getLinearScroller() {
		return this.scrollView;
	}

	LinearLayout buildViews() {
		this.activity.setContentView(R.layout.time_slice_report_framework);
		final LinearLayout contentView = (LinearLayout) this.activity
				.findViewById(R.id.report_frame);
		contentView.setOrientation(LinearLayout.VERTICAL);
		this.scrollView = new LinearScroller(this.activity);
		contentView.addView(this.scrollView.getScrollView());
		return contentView;
	}

	public List<TextView> initializeTextViewsForExportList() {
		final List<TextView> tvList = new ArrayList<TextView>();
		this.reportViewListItems = tvList;
		return tvList;
	}

	public ReportFramework setReportType(final ReportTypes reportType) {
		this.reportType = reportType;
		return this;
	}

	/**
	 * retrieves filter from bundle
	 * 
	 * @param savedInstanceState
	 *            : where filter infos are stored
	 * @param parameterBundleName
	 *            : the name of the filter. Every context has a different name.
	 * @param notFoundValue
	 *            : value returend if not found
	 * @return filter or parameterName
	 */
	public static TimeSliceFilterParameter getLastFilter(
			final Bundle savedInstanceState, final String parameterBundleName,
			final TimeSliceFilterParameter notFoundValue) {
		TimeSliceFilterParameter rangeFilter = null;
		if (savedInstanceState != null) {
			final Serializable filter = savedInstanceState
					.getSerializable(parameterBundleName);

			if (filter instanceof TimeSliceFilterParameter) {
				rangeFilter = (TimeSliceFilterParameter) filter;
			}
		}

		if (rangeFilter == null) {
			rangeFilter = notFoundValue;
		}

		if (rangeFilter == null) {
			rangeFilter = new TimeSliceFilterParameter();
		}

		return rangeFilter;
	}

	/**
	 * saves filter to bundle
	 * 
	 * @param savedInstanceState
	 *            : where filter infos are stored
	 * @param parameterBundleName
	 *            : the name of the filter. Every context has a different name.
	 * @param rangeFilter
	 *            : value to be saved
	 */
	public static void setLastFilter(final Bundle savedInstanceState,
			final String parameterBundleName,
			final TimeSliceFilterParameter rangeFilter) {
		savedInstanceState.putSerializable(parameterBundleName, rangeFilter);
	}

	/**
	 * @return endtime with time set to 23:59 from rangeFilter
	 */
	public static long getFixedEndTime(
			final TimeSliceFilterParameter rangeFilter) {
		final Calendar c = Calendar.getInstance();
		c.setTime(new Date(rangeFilter.getEndTime()));
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		final long endDate = c.getTimeInMillis();
		return endDate;
	}

}
