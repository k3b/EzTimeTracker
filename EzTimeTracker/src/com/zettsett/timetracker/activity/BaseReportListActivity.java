package com.zettsett.timetracker.activity;

import java.io.Serializable;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.SendUtilities;
import com.zettsett.timetracker.report.ReportOutput;
import com.zettsett.timetracker.report.ReprtExportEngine;

/**
 * Common Handling for Report-Generation and display.
 */
abstract class BaseReportListActivity extends ListActivity {
	private static final int MENU_ITEM_EXPORT_SD = Menu.FIRST + 12;
	private static final int MENU_ITEM_EXPORT_SEND = Menu.FIRST + 13;
	private static final int MENU_ITEM_SET_FILTER = Menu.FIRST + 21;

	// current state
	/**
	 * current range filter used to fill report.<br/>
	 */
	protected TimeSliceFilterParameter currentRangeFilter;

	protected int idOnOkResultCode = 0;

	private ReportDateGrouping reportDateGrouping = ReportDateGrouping.DAILY;

	/**
	 * if reportitems should be generated with timeslice-notes or not.<br>
	 * Toggeld via option-menu
	 */
	protected boolean showNotes = true;

	/**
	 * creates filter if null. Fixes start/end to meaningfull defaults if empty.
	 */
	protected BaseReportListActivity setDefaultsToFilterDatesIfNeccesary(
			final TimeSliceFilterParameter filter) {
		this.currentRangeFilter = TimeSliceFilterParameter
				.filterWithDefaultsIfNeccessary(this.currentRangeFilter);
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
		exportMenu.add(Menu.NONE, BaseReportListActivity.MENU_ITEM_EXPORT_SD,
				1, R.string.menu_export_report_to_sd_card);
		exportMenu.add(Menu.NONE, BaseReportListActivity.MENU_ITEM_EXPORT_SEND,
				2, R.string.menu_send_report);

		menu.add(Menu.NONE, BaseReportListActivity.MENU_ITEM_SET_FILTER, 2,
				R.string.menu_filter);

		return result;
	}

	/**
	 * called by parent Report Action to do process common menuactions.
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		ReportOutput sdFormatter = null;
		switch (item.getItemId()) {
		case MENU_ITEM_SET_FILTER:
			ReportFilterActivity.showActivity(this, this.currentRangeFilter);
			break;
		case MENU_ITEM_EXPORT_SD:
			sdFormatter = this.createFormatter();
			ReprtExportEngine.exportToSD(this.getDefaultReportName(), this,
					sdFormatter);
			break;
		case MENU_ITEM_EXPORT_SEND:
			sdFormatter = this.createFormatter();
			sdFormatter.setLineTerminator("\n");
			SendUtilities.send("", this.getEMailSummaryLine(), this,
					sdFormatter.getOutput());
			break;
		}

		return true;
	}

	private ReportOutput createFormatter() {
		return ReportOutput.makeFormatter(this.loadData(),
				new ReportItemFormatterEx(this, this.getReportDateGrouping(),
						this.showNotes));
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
	protected static TimeSliceFilterParameter getLastFilter(final Object owner,
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
					+ " > BaseReportListActivity.getLastFilter("
					+ parameterBundleName + ") = '" + rangeFilter + "'");
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
	protected void setLastFilter(final Bundle savedInstanceState,
			final String parameterBundleName,
			final TimeSliceFilterParameter rangeFilter) {
		if (Global.isDebugEnabled()) {
			Log.d(Global.LOG_CONTEXT, this.getClass().getSimpleName()
					+ " > BaseReportListActivity.setLastFilter("
					+ parameterBundleName + "='" + rangeFilter + "')");
		}
		savedInstanceState.putSerializable(parameterBundleName, rangeFilter);
	}

	protected void setReportDateGrouping(
			final ReportDateGrouping reportDateGrouping) {
		this.reportDateGrouping = reportDateGrouping;
	}

	protected ReportDateGrouping getReportDateGrouping() {
		return this.reportDateGrouping;
	}

}
