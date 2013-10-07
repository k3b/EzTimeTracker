package com.zettsett.timetracker.activity;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.database.TimeSliceRepository;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;
import com.zettsett.timetracker.report.IReportInterface;

import de.k3b.util.DateTimeUtil;

/**
 * Copyright 2010 Eric Zetterbaum ezetter@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
public class TimeSheetSummaryReportActivity extends Activity implements
		IReportInterface {
	public enum ReportDateGrouping {
		DAILY, WEEKLY, MONTHLY, YEARLY
	}

	public enum ReportModes {
		BY_DATE, BY_CATEGORY
	}

	/**
	 * Used to transfer optional report-type from parent activity to this.
	 */
	public static final String SAVED_MENU_ID_BUNDLE_NAME = "SAVED_MENU_ID_BUNDLE_NAME";

	private static final String SAVED_REPORT_GROUPING_BUNDLE_NAME = "reportDateGrouping";

	/**
	 * Used to transfer optional filter between parent activity and this.
	 */
	private static final String SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME = "SummaryReportFilter";

	// menu ids
	private static final String SAVED_REPORT_MODE = "reportMode";
	private static final int MENU_ITEM_GROUP_DAILY = Menu.FIRST;
	private static final int MENU_ITEM_GROUP_WEEKLY = Menu.FIRST + 1;
	private static final int MENU_ITEM_GROUP_MONTHLY = Menu.FIRST + 2;
	private static final int MENU_ITEM_GROUP_YARLY = Menu.FIRST + 3;
	private static final int MENU_ITEM_GROUP_CATEGORY = Menu.FIRST + 4;
	private static final int MENU_ITEM_REPORT = Menu.FIRST + 5;

	// dependent services
	private ReportFramework reportFramework;
	private TimeSliceRepository timeSliceRepository;

	// form controls
	private List<TextView> reportViewItemList;

	// current state
	/**
	 * current range filter used to fill report.<br/>
	 * static to surwive if this activity is discarded in filter activity.
	 */
	private static TimeSliceFilterParameter currentRangeFilter;

	private ReportDateGrouping reportDateGrouping = ReportDateGrouping.WEEKLY;
	private ReportModes reportMode = ReportModes.BY_DATE;

	/**
	 * Used in options-menue for context sensitive DrillDownMenue
	 */
	private TimeSliceFilterParameter currentSelectedListItemRangeFilterUsedForMenu;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.timeSliceRepository = new TimeSliceRepository(this);
		TimeSheetSummaryReportActivity.currentRangeFilter = ReportFramework
				.getLastFilter(
						savedInstanceState,
						TimeSheetSummaryReportActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME,
						TimeSheetSummaryReportActivity.currentRangeFilter);

		this.reportFramework = new ReportFramework(this,
				TimeSheetSummaryReportActivity.currentRangeFilter);
		if (savedInstanceState != null) {
			this.reportDateGrouping = (ReportDateGrouping) savedInstanceState
					.getSerializable(TimeSheetSummaryReportActivity.SAVED_REPORT_GROUPING_BUNDLE_NAME);
			this.reportMode = (ReportModes) savedInstanceState
					.getSerializable(TimeSheetSummaryReportActivity.SAVED_REPORT_MODE);
		}
		this.loadDataIntoReport(this.getIntent().getIntExtra(
				TimeSheetSummaryReportActivity.SAVED_MENU_ID_BUNDLE_NAME, 0));
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		ReportFramework
				.setLastFilter(
						outState,
						TimeSheetSummaryReportActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME,
						TimeSheetSummaryReportActivity.currentRangeFilter);
		outState.putSerializable(
				TimeSheetSummaryReportActivity.SAVED_REPORT_GROUPING_BUNDLE_NAME,
				this.reportDateGrouping);
		outState.putSerializable(
				TimeSheetSummaryReportActivity.SAVED_REPORT_MODE,
				this.reportMode);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final boolean result = super.onCreateOptionsMenu(menu);
		menu.clear();
		final SubMenu groupDateMenu = menu.addSubMenu(0, Menu.NONE, 0,
				R.string.menu_select_date_grouping);
		groupDateMenu.add(0,
				TimeSheetSummaryReportActivity.MENU_ITEM_GROUP_DAILY, 0,
				R.string.menu_select_date_grouping_daily);
		groupDateMenu.add(0,
				TimeSheetSummaryReportActivity.MENU_ITEM_GROUP_WEEKLY, 1,
				R.string.menu_select_date_grouping_weekly);
		groupDateMenu.add(0,
				TimeSheetSummaryReportActivity.MENU_ITEM_GROUP_MONTHLY, 2,
				R.string.menu_select_date_grouping_monthly);
		groupDateMenu.add(0,
				TimeSheetSummaryReportActivity.MENU_ITEM_GROUP_YARLY, 2,
				R.string.menu_select_date_grouping_yearly);
		this.reportFramework.onPrepareOptionsMenu(menu);
		if (this.reportMode == ReportModes.BY_DATE) {
			menu.add(0,
					TimeSheetSummaryReportActivity.MENU_ITEM_GROUP_CATEGORY, 1,
					R.string.menu_switch_to_category_headers);
		} else {
			menu.add(0,
					TimeSheetSummaryReportActivity.MENU_ITEM_GROUP_CATEGORY, 1,
					R.string.menu_switch_to_date_headers);
		}
		return result;
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final TimeSliceFilterParameter filter = this.createFilter(v);
		if (filter != null) {
			Log.i(Global.LOG_CONTEXT, "Detailreport: " + filter);

			menu.add(0, TimeSheetSummaryReportActivity.MENU_ITEM_REPORT, 0,
					this.getString(R.string.cmd_report));
		}
		this.currentSelectedListItemRangeFilterUsedForMenu = filter;
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_REPORT:
			this.showDetailReport();
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	private void showDetailReport() {
		if (this.currentSelectedListItemRangeFilterUsedForMenu != null) {
			TimeSheetDetailReportActivity.showActivity(this,
					this.currentSelectedListItemRangeFilterUsedForMenu);
		}
	}

	private TimeSliceFilterParameter createFilter(final View v) {
		TimeSliceCategory category = this.getTimeSliceCategory(v);
		if (category != null) {
			final TimeSliceFilterParameter filter = new TimeSliceFilterParameter()
					.setCategoryId(category.getRowId());
			if (this.reportMode == ReportModes.BY_DATE) {
				int pos = this.reportViewItemList.indexOf(v);
				while (--pos >= 0) {
					final Long date = this.getLong(this.reportViewItemList
							.get(pos));
					if (date != null) {
						return this.setFilterDate(filter,
								this.reportDateGrouping, date);
					}
				}
			}
			return filter.setIgnoreDates(true);
		} else {
			final Long date = this.getLong(v);
			if (date != null) {
				final TimeSliceFilterParameter filter = this.setFilterDate(
						new TimeSliceFilterParameter(),
						this.reportDateGrouping, date);
				if (this.reportMode == ReportModes.BY_CATEGORY) {
					int pos = this.reportViewItemList.indexOf(v);
					while (--pos >= 0) {
						category = this.getTimeSliceCategory(v);
						if (category != null) {
							return filter.setCategoryId(category.getRowId());
						}
					}
				}
				return filter;
			}
		}
		return null;
	}

	private TimeSliceCategory getTimeSliceCategory(final View v) {
		final Object tag = v.getTag();
		if ((tag != null) && (tag instanceof TimeSliceCategory)) {
			return (TimeSliceCategory) tag;
		}
		return null;
	}

	private Long getLong(final View v) {
		final Object tag = v.getTag();
		if ((tag != null) && (tag instanceof Long)) {
			return (Long) tag;
		}
		return null;
	}

	private TimeSliceFilterParameter setFilterDate(
			final TimeSliceFilterParameter timeSliceFilterParameter,
			final ReportDateGrouping mReportDateGrouping, final Long startDate) {
		final long start = startDate.longValue();
		final long end = this.getEndTime(mReportDateGrouping, start);
		return timeSliceFilterParameter.setStartTime(start).setEndTime(end);
	}

	private long getEndTime(final ReportDateGrouping mReportDateGrouping,
			final long start) {
		final DateTimeUtil dtu = DateTimeFormatter.getInstance();
		if (mReportDateGrouping == ReportDateGrouping.DAILY) {
			return dtu.addDays(start, 1);
		} else if (mReportDateGrouping == ReportDateGrouping.WEEKLY) {
			return dtu.addDays(start, 7);
		} else if (mReportDateGrouping == ReportDateGrouping.MONTHLY) {
			return dtu.getStartOfMonth(dtu.addDays(start, 31));
		} else if (mReportDateGrouping == ReportDateGrouping.YEARLY) {
			return dtu.getStartOfYear(dtu.addDays(start, 366));
		}

		throw new IllegalArgumentException("Unknown reportDateGrouping "
				+ mReportDateGrouping);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_GROUP_DAILY:
			this.reportDateGrouping = ReportDateGrouping.DAILY;
			this.loadDataIntoReport(0);
			break;
		case MENU_ITEM_GROUP_WEEKLY:
			this.reportDateGrouping = ReportDateGrouping.WEEKLY;
			this.loadDataIntoReport(0);
			break;
		case MENU_ITEM_GROUP_MONTHLY:
			this.reportDateGrouping = ReportDateGrouping.MONTHLY;
			this.loadDataIntoReport(0);
			break;
		case MENU_ITEM_GROUP_YARLY:
			this.reportDateGrouping = ReportDateGrouping.YEARLY;
			this.loadDataIntoReport(0);
			break;

		case MENU_ITEM_GROUP_CATEGORY:
			if (this.reportMode == ReportModes.BY_CATEGORY) {
				this.reportMode = ReportModes.BY_DATE;
			} else {
				this.reportMode = ReportModes.BY_CATEGORY;
			}
			this.loadDataIntoReport(0);
			break;
		default:
			this.reportFramework
					.setReportType(ReportFramework.ReportTypes.SUMMARY);
			this.reportFramework.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null) {
			TimeSheetSummaryReportActivity.currentRangeFilter = this.reportFramework
					.onActivityResult(intent,
							TimeSheetSummaryReportActivity.currentRangeFilter);
			this.loadDataIntoReport(0);
		}
	}

	@Override
	public void loadDataIntoReport(final int reportType) {
		long performanceMeasureStart = System.currentTimeMillis();

		switch (reportType) {
		case R.id.summary_day:
			this.reportDateGrouping = ReportDateGrouping.DAILY;
			this.reportMode = ReportModes.BY_DATE;
			break;
		case R.id.summary_month:
			this.reportDateGrouping = ReportDateGrouping.MONTHLY;
			this.reportMode = ReportModes.BY_DATE;
			break;
		case R.id.summary_week:
			this.reportDateGrouping = ReportDateGrouping.WEEKLY;
			this.reportMode = ReportModes.BY_DATE;
			break;
		case R.id.category_day:
			this.reportDateGrouping = ReportDateGrouping.DAILY;
			this.reportMode = ReportModes.BY_CATEGORY;
			break;
		case R.id.category_month:
			this.reportDateGrouping = ReportDateGrouping.MONTHLY;
			this.reportMode = ReportModes.BY_CATEGORY;
			break;
		case R.id.category_week:
			this.reportDateGrouping = ReportDateGrouping.WEEKLY;
			this.reportMode = ReportModes.BY_CATEGORY;
			break;
		}
		this.setContentView(this.reportFramework.buildViews());
		this.reportViewItemList = this.reportFramework
				.initializeTextViewsForExportList();
		final TimeSheetSummaryCalculator reportDataStructure = this
				.loadReportDataStructures();

		Log.i(Global.LOG_CONTEXT,
				"loadReportDataStructures:"
						+ (System.currentTimeMillis() - performanceMeasureStart));
		performanceMeasureStart = System.currentTimeMillis();

		final Map<String, Map<String, Long>> reportData = reportDataStructure
				.getReportData();
		final Map<String, Long> dates = reportDataStructure.getDates();
		final Map<String, TimeSliceCategory> categoties = reportDataStructure
				.getCategoties();
		this.addDateHeaderLine(
				TimeSheetSummaryReportActivity.currentRangeFilter.toString(),
				Color.YELLOW);
		int itemCount = 0;
		for (final String header : reportData.keySet()) {
			final Map<String, Long> reportRows = reportData.get(header);

			final TextView headerTextView = this.addDateHeaderLine(header,
					Color.GREEN);
			if (this.reportMode == ReportModes.BY_DATE) {
				headerTextView.setTag(dates.get(header));
			} else {
				headerTextView.setTag(categoties.get(header));
			}

			this.registerForContextMenu(headerTextView);
			final LayoutParams layoutParams = new LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(0, 5, 0, 5);
			final LinearLayout rowsView = new LinearLayout(this);
			rowsView.setOrientation(LinearLayout.VERTICAL);
			this.reportFramework.getLinearScroller().getMainLayout()
					.addView(rowsView, layoutParams);
			for (final String rowCaption : reportRows.keySet()) {
				final long totalTimeInMillis = reportRows.get(rowCaption);
				final TextView rowTextView = new TextView(this);
				this.reportViewItemList.add(rowTextView);
				rowTextView.setText("    " + rowCaption + ": "
						+ this.timeInMillisToText(totalTimeInMillis));
				if (this.reportMode == ReportModes.BY_DATE) {
					rowTextView.setTag(categoties.get(rowCaption));
				} else {
					rowTextView.setTag(dates.get(rowCaption));
				}
				this.registerForContextMenu(rowTextView);
				rowsView.addView(rowTextView);
			}
			itemCount++;
		}

		if (itemCount == 0) {
			this.addDateHeaderLine(this.getString(R.string.message_no_data),
					Color.YELLOW);
		}
		Log.i(Global.LOG_CONTEXT,
				"generated report:"
						+ (System.currentTimeMillis() - performanceMeasureStart));
		performanceMeasureStart = System.currentTimeMillis();
	}

	private TextView addDateHeaderLine(final String header, final int color) {
		final TextView headerTextView = new TextView(this);
		headerTextView.setText(header);
		headerTextView.setTextColor(color);
		this.reportViewItemList.add(headerTextView);

		this.reportFramework.getLinearScroller().addView(headerTextView);
		return headerTextView;
	}

	/**
	 * 
	 * @return Map<categoryName, Map<startDate.toString(),
	 *         totalDurationsWithinSubinterval>> or Map<startDate.toString(),
	 *         Map<categoryName, totalDurationsWithinSubinterval>>
	 */
	private TimeSheetSummaryCalculator loadReportDataStructures() {
		final TimeSliceFilterParameter rangeFilter = TimeSheetSummaryReportActivity.currentRangeFilter;

		final List<TimeSlice> timeSlices = this.timeSliceRepository
				.fetchList(rangeFilter);

		final TimeSheetSummaryCalculator summaries = new TimeSheetSummaryCalculator(
				this.reportMode, this.reportDateGrouping, timeSlices);
		return summaries;
	}

	private String timeInMillisToText(final long totalTimeInMillis) {
		final long minutes = (totalTimeInMillis / (1000 * 60)) % 60;
		final long hours = totalTimeInMillis / (1000 * 60 * 60);
		String hoursWord;
		if (hours == 1) {
			hoursWord = this.getString(R.string.hoursWord1);
		} else {
			hoursWord = this.getString(R.string.hoursWordN);
		}
		String minutesWord;
		if (minutes == 1) {
			minutesWord = this.getString(R.string.minutesWord1);
		} else {
			minutesWord = this.getString(R.string.minutesWordN);
		}
		final String timeString = hours + " " + hoursWord + ", " + minutes
				+ " " + minutesWord;
		return timeString;
	}
}
