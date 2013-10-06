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
	private static final String SAVED_REPORT_MODE = "reportMode";
	private static final int MENU_ITEM_GROUP_DAILY = Menu.FIRST;
	private static final int MENU_ITEM_GROUP_WEEKLY = Menu.FIRST + 1;
	private static final int MENU_ITEM_GROUP_MONTHLY = Menu.FIRST + 2;
	private static final int MENU_ITEM_GROUP_YARLY = Menu.FIRST + 3;
	private static final int MENU_ITEM_GROUP_CATEGORY = Menu.FIRST + 4;
	private static final int MENU_ITEM_REPORT = Menu.FIRST + 5;

	public static final String MENU_ID = "MENU_ID";
	private static final String SAVED_REPORT_FILTER = "SummaryReportFilter";

	public enum ReportDateGrouping {
		DAILY, WEEKLY, MONTHLY, YEARLY
	}

	public enum ReportModes {
		BY_DATE, BY_CATEGORY
	}

	private ReportFramework mReportFramework;
	private TimeSliceRepository mTimeSliceRepository;
	private ReportDateGrouping mReportDateGrouping = ReportDateGrouping.WEEKLY;
	private ReportModes mReportMode = ReportModes.BY_DATE;
	private List<TextView> mReportViewList;
	private TimeSliceFilterParameter mReportFilter;
	private static TimeSliceFilterParameter mRangeFilter;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.mTimeSliceRepository = new TimeSliceRepository(this);
		TimeSheetSummaryReportActivity.mRangeFilter = ReportFramework
				.getLastFilter(savedInstanceState,
						TimeSheetSummaryReportActivity.SAVED_REPORT_FILTER,
						TimeSheetSummaryReportActivity.mRangeFilter);

		this.mReportFramework = new ReportFramework(this,
				TimeSheetSummaryReportActivity.mRangeFilter);
		if (savedInstanceState != null) {
			this.mReportDateGrouping = (ReportDateGrouping) savedInstanceState
					.getSerializable(this.SAVED_REPORT_GROUPING());
			this.mReportMode = (ReportModes) savedInstanceState
					.getSerializable(TimeSheetSummaryReportActivity.SAVED_REPORT_MODE);
		}
		this.loadDataIntoReport(this.getIntent().getIntExtra(
				TimeSheetSummaryReportActivity.MENU_ID, 0));
	}

	protected String SAVED_REPORT_GROUPING() {
		return "reportDateGrouping";
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		ReportFramework.setLastFilter(outState,
				TimeSheetSummaryReportActivity.SAVED_REPORT_FILTER,
				TimeSheetSummaryReportActivity.mRangeFilter);
		outState.putSerializable(this.SAVED_REPORT_GROUPING(),
				this.mReportDateGrouping);
		outState.putSerializable(
				TimeSheetSummaryReportActivity.SAVED_REPORT_MODE,
				this.mReportMode);
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
		this.mReportFramework.onPrepareOptionsMenu(menu);
		if (this.mReportMode == ReportModes.BY_DATE) {
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
		this.mReportFilter = filter;
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
		if (this.mReportFilter != null) {
			TimeSheetDetailReportActivity
					.showActivity(this, this.mReportFilter);
		}
	}

	private TimeSliceFilterParameter createFilter(final View v) {
		TimeSliceCategory category = this.getTimeSliceCategory(v);
		if (category != null) {
			final TimeSliceFilterParameter filter = new TimeSliceFilterParameter()
					.setCategoryId(category.getRowId());
			if (this.mReportMode == ReportModes.BY_DATE) {
				int pos = this.mReportViewList.indexOf(v);
				while (--pos >= 0) {
					final Long date = this.getLong(this.mReportViewList
							.get(pos));
					if (date != null) {
						return this.setFilterDate(filter,
								this.mReportDateGrouping, date);
					}
				}
			}
			return filter.setIgnoreDates(true);
		} else {
			final Long date = this.getLong(v);
			if (date != null) {
				final TimeSliceFilterParameter filter = this.setFilterDate(
						new TimeSliceFilterParameter(), this.mReportDateGrouping, date);
				if (this.mReportMode == ReportModes.BY_CATEGORY) {
					int pos = this.mReportViewList.indexOf(v);
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

		throw new IllegalArgumentException("Unknown mReportDateGrouping "
				+ mReportDateGrouping);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_GROUP_DAILY:
			this.mReportDateGrouping = ReportDateGrouping.DAILY;
			this.loadDataIntoReport(0);
			break;
		case MENU_ITEM_GROUP_WEEKLY:
			this.mReportDateGrouping = ReportDateGrouping.WEEKLY;
			this.loadDataIntoReport(0);
			break;
		case MENU_ITEM_GROUP_MONTHLY:
			this.mReportDateGrouping = ReportDateGrouping.MONTHLY;
			this.loadDataIntoReport(0);
			break;
		case MENU_ITEM_GROUP_YARLY:
			this.mReportDateGrouping = ReportDateGrouping.YEARLY;
			this.loadDataIntoReport(0);
			break;

		case MENU_ITEM_GROUP_CATEGORY:
			if (this.mReportMode == ReportModes.BY_CATEGORY) {
				this.mReportMode = ReportModes.BY_DATE;
			} else {
				this.mReportMode = ReportModes.BY_CATEGORY;
			}
			this.loadDataIntoReport(0);
			break;
		default:
			this.mReportFramework
					.setReportType(ReportFramework.ReportTypes.SUMMARY);
			this.mReportFramework.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null) {
			TimeSheetSummaryReportActivity.mRangeFilter = this.mReportFramework
					.onActivityResult(intent,
							TimeSheetSummaryReportActivity.mRangeFilter);
			this.loadDataIntoReport(0);
		}
	}

	@Override
	public void loadDataIntoReport(final int reportType) {
		long performanceMeasureStart = System.currentTimeMillis();

		switch (reportType) {
		case R.id.summary_day:
			this.mReportDateGrouping = ReportDateGrouping.DAILY;
			this.mReportMode = ReportModes.BY_DATE;
			break;
		case R.id.summary_month:
			this.mReportDateGrouping = ReportDateGrouping.MONTHLY;
			this.mReportMode = ReportModes.BY_DATE;
			break;
		case R.id.summary_week:
			this.mReportDateGrouping = ReportDateGrouping.WEEKLY;
			this.mReportMode = ReportModes.BY_DATE;
			break;
		case R.id.category_day:
			this.mReportDateGrouping = ReportDateGrouping.DAILY;
			this.mReportMode = ReportModes.BY_CATEGORY;
			break;
		case R.id.category_month:
			this.mReportDateGrouping = ReportDateGrouping.MONTHLY;
			this.mReportMode = ReportModes.BY_CATEGORY;
			break;
		case R.id.category_week:
			this.mReportDateGrouping = ReportDateGrouping.WEEKLY;
			this.mReportMode = ReportModes.BY_CATEGORY;
			break;
		}
		this.setContentView(this.mReportFramework.buildViews());
		this.mReportViewList = this.mReportFramework
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
		for (final String header : reportData.keySet()) {
			final Map<String, Long> reportRows = reportData.get(header);
			final TextView headerTextView = new TextView(this);
			headerTextView.setText(header);
			headerTextView.setTextColor(Color.GREEN);
			if (this.mReportMode == ReportModes.BY_DATE) {
				headerTextView.setTag(dates.get(header));
			} else {
				headerTextView.setTag(categoties.get(header));
			}

			this.mReportViewList.add(headerTextView);
			this.registerForContextMenu(headerTextView);

			this.mReportFramework.getLinearScroller().addView(headerTextView);
			final LayoutParams layoutParams = new LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(0, 5, 0, 5);
			final LinearLayout rowsView = new LinearLayout(this);
			rowsView.setOrientation(LinearLayout.VERTICAL);
			this.mReportFramework.getLinearScroller().getMainLayout()
					.addView(rowsView, layoutParams);
			for (final String rowCaption : reportRows.keySet()) {
				final long totalTimeInMillis = reportRows.get(rowCaption);
				final TextView rowTextView = new TextView(this);
				this.mReportViewList.add(rowTextView);
				rowTextView.setText("    " + rowCaption + ": "
						+ this.timeInMillisToText(totalTimeInMillis));
				if (this.mReportMode == ReportModes.BY_DATE) {
					rowTextView.setTag(categoties.get(rowCaption));
				} else {
					rowTextView.setTag(dates.get(rowCaption));
				}
				this.registerForContextMenu(rowTextView);
				rowsView.addView(rowTextView);
			}
		}
		Log.i(Global.LOG_CONTEXT,
				"generated report:"
						+ (System.currentTimeMillis() - performanceMeasureStart));
		performanceMeasureStart = System.currentTimeMillis();
	}

	/**
	 * 
	 * @return Map<categoryName, Map<startDate.toString(),
	 *         totalDurationsWithinSubinterval>> or Map<startDate.toString(),
	 *         Map<categoryName, totalDurationsWithinSubinterval>>
	 */
	private TimeSheetSummaryCalculator loadReportDataStructures() {
		final TimeSliceFilterParameter rangeFilter = TimeSheetSummaryReportActivity.mRangeFilter;

		final List<TimeSlice> timeSlices = this.mTimeSliceRepository
				.fetchList(rangeFilter);

		final TimeSheetSummaryCalculator summaries = new TimeSheetSummaryCalculator(
				this.mReportMode, this.mReportDateGrouping, timeSlices);
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
