package com.zettsett.timetracker.activity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.EmailUtilities;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.report.ReportOutput;
import com.zettsett.timetracker.report.ReprtExportEngine;

/**
 * Common Handling for Report-Generation and display.
 */
abstract class ReportFramework extends ListActivity
{
	private static final int MENU_ITEM_EXPORT_SD = Menu.FIRST + 12;
	private static final int MENU_ITEM_EXPORT_EMAIL = Menu.FIRST + 13;
	private static final int MENU_ITEM_SET_FILTER = Menu.FIRST + 21;

	// current state
	/**
	 * current range filter used to fill report.<br/>
	 */
	protected TimeSliceFilterParameter currentRangeFilter;

	protected ReportDateGrouping reportDateGrouping = ReportDateGrouping.DAILY;

	/**
	 * creates filter if null. Fixes start/end to meaningfull defaults if empty.
	 */
	public ReportFramework setDefaultsToFilterDatesIfNeccesary(
			final TimeSliceFilterParameter filter) {
		this.currentRangeFilter = (filter != null) ? filter
				: new TimeSliceFilterParameter();

		final Date currDate = new Date();

		if (this.currentRangeFilter.getStartTime() == TimeSlice.NO_TIME_VALUE) {
			// start = now-2months
			final Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			calendar.add(Calendar.MONTH, -2);
			final long startTime = calendar.getTimeInMillis();
			this.currentRangeFilter.setStartTime(startTime);
		}

		if (this.currentRangeFilter.getEndTime() == TimeSlice.NO_TIME_VALUE) {
			// end = now+1week
			final Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			calendar.add(Calendar.WEEK_OF_YEAR, 1);
			final long endTime = calendar.getTimeInMillis();
			this.currentRangeFilter.setEndTime(endTime);
		}
		return this;
	}

	/**
	 * called by parent Report Action to append common menuitem.
	 */
	@Override
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
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_SET_FILTER:
			ReportFilterActivity.showActivity(this, this.currentRangeFilter);
			break;
		case MENU_ITEM_EXPORT_SD:
			ReprtExportEngine.exportToSD(this.getDefaultReportName(), this,
					ReportOutput.makeFormatter(this.loadData(),
							new ReportItemFormatterEx(this,
									this.reportDateGrouping)));
			break;
		case MENU_ITEM_EXPORT_EMAIL:
			final ReportOutput outPutter = ReportOutput.makeFormatter(this
					.loadData(), new ReportItemFormatterEx(this,
					this.reportDateGrouping));
			outPutter.setLineTerminator("\n");
			EmailUtilities.send("", this.getEMailSummaryLine(), this,
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

		this.setDefaultsToFilterDatesIfNeccesary(newRangeFilter);
		return newRangeFilter;
	}

	abstract protected String getDefaultReportName();

	abstract protected String getEMailSummaryLine();

	abstract protected List<Object> loadData();

	/**
	 * retrieves filter from bundle
	 * 
	 * @param owner
	 *            TODO
	 * @param savedInstanceState
	 *            : where filter infos are stored
	 * @param parameterBundleName
	 *            : the name of the filter. Every context has a different name.
	 * @param notFoundValue
	 *            : value returend if not found
	 * 
	 * @return filter or parameterName
	 */
	public static TimeSliceFilterParameter getLastFilter(final Object owner,
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
		if (Global.isDebugEnabled()) {
			Log.d(Global.LOG_CONTEXT, owner.getClass().getSimpleName()
					+ " > ReportFramework.getLastFilter(" + parameterBundleName
					+ ") = '" + rangeFilter + "'");
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
	public void setLastFilter(final Bundle savedInstanceState,
			final String parameterBundleName,
			final TimeSliceFilterParameter rangeFilter) {
		if (Global.isDebugEnabled()) {
			Log.d(Global.LOG_CONTEXT, this.getClass().getSimpleName()
					+ " > ReportFramework.setLastFilter(" + parameterBundleName
					+ "='" + rangeFilter + "')");
		}
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
