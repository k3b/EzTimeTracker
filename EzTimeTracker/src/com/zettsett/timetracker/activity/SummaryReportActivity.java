package com.zettsett.timetracker.activity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.database.TimeSliceDBAdapter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.report.ReportInterface;

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
public class SummaryReportActivity extends Activity implements ReportInterface {
	private static final String SAVED_REPORT_MODE = "reportMode";
	private static final int MENU_ITEM_GROUP_DAILY = Menu.FIRST;
	private static final int MENU_ITEM_GROUP_WEEKLY = Menu.FIRST + 1;
	private static final int MENU_ITEM_GROUP_MONTHLY = Menu.FIRST + 2;
	private static final int MENU_ITEM_GROUP_CATEGORY = Menu.FIRST + 3;
	public static final String MENU_ID = "MENU_ID";
	private static final String SAVED_REPORT_FILTER = "DetailReportFilter";

	private enum ReportDateGrouping {
		DAILY, WEEKLY, MONTHLY
	}

	private enum ReportModes {
		BY_DATE, BY_CATEGORY
	}

	private ReportFramework mReportFramework;
	private TimeSliceDBAdapter mTimeSliceDBAdapter;
	private ReportDateGrouping mReportDateGrouping = ReportDateGrouping.WEEKLY;
	private ReportModes mReportMode = ReportModes.BY_DATE;
	private List<TextView> mReportViewList;
	private FilterParameter mRangeFilter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mTimeSliceDBAdapter = new TimeSliceDBAdapter(this);
		this.mRangeFilter = ReportFramework.getLastFilter(savedInstanceState, SAVED_REPORT_FILTER);

		mReportFramework = new ReportFramework(this, this, mRangeFilter);
		if (savedInstanceState != null) {
			mReportDateGrouping = (ReportDateGrouping) savedInstanceState
					.getSerializable(SAVED_REPORT_GROUPING());
			mReportMode = (ReportModes) savedInstanceState.getSerializable(SAVED_REPORT_MODE);
		}
		loadDataIntoReport(this.getIntent().getIntExtra(SummaryReportActivity.MENU_ID, 0));
	}

	protected String SAVED_REPORT_GROUPING() {
		return "reportDateGrouping";
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(SAVED_REPORT_FILTER, this.mRangeFilter);		
		outState.putSerializable(SAVED_REPORT_GROUPING(), mReportDateGrouping);
		outState.putSerializable(SAVED_REPORT_MODE, mReportMode);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.clear();
		SubMenu groupDateMenu = menu.addSubMenu(0, Menu.NONE, 0, R.string.menu_select_date_grouping);
		groupDateMenu.add(0, MENU_ITEM_GROUP_DAILY, 0, R.string.menu_select_date_grouping_daily);
		groupDateMenu.add(0, MENU_ITEM_GROUP_WEEKLY, 1, R.string.menu_select_date_grouping_weekly);
		groupDateMenu.add(0, MENU_ITEM_GROUP_MONTHLY, 2, R.string.menu_select_date_grouping_monthly);
		mReportFramework.onPrepareOptionsMenu(menu);
		if (mReportMode == ReportModes.BY_DATE) {
			menu.add(0, MENU_ITEM_GROUP_CATEGORY, 1, R.string.menu_switch_to_category_headers);
		} else {
			menu.add(0, MENU_ITEM_GROUP_CATEGORY, 1, R.string.menu_switch_to_date_headers);
		}
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_GROUP_DAILY:
			mReportDateGrouping = ReportDateGrouping.DAILY;
			loadDataIntoReport(0);
			break;
		case MENU_ITEM_GROUP_WEEKLY:
			mReportDateGrouping = ReportDateGrouping.WEEKLY;
			loadDataIntoReport(0);
			break;
		case MENU_ITEM_GROUP_MONTHLY:
			mReportDateGrouping = ReportDateGrouping.MONTHLY;
			loadDataIntoReport(0);
			break;
		case MENU_ITEM_GROUP_CATEGORY:
			if (mReportMode == ReportModes.BY_CATEGORY) {
				mReportMode = ReportModes.BY_DATE;
			} else {
				mReportMode = ReportModes.BY_CATEGORY;
			}
			loadDataIntoReport(0);
			break;
		default:
			mReportFramework.setReportType(ReportFramework.ReportTypes.SUMMARY);
			mReportFramework.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return mReportFramework.onCreateDialog(id);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null) {
			loadDataIntoReport(0);
		}
	}

	public void loadDataIntoReport(int reportType) {
		long performanceMeasureStart = System.currentTimeMillis();

		switch (reportType)
		{
		    case R.id.summary_day:
				mReportDateGrouping = ReportDateGrouping.DAILY;
				mReportMode = ReportModes.BY_DATE;
				break;
		    case R.id.summary_month:
				mReportDateGrouping = ReportDateGrouping.MONTHLY;
				mReportMode = ReportModes.BY_DATE;
				break;
		    case R.id.summary_week:
				mReportDateGrouping = ReportDateGrouping.WEEKLY;
				mReportMode = ReportModes.BY_DATE;
				break;
		    case R.id.category_day:
				mReportDateGrouping = ReportDateGrouping.DAILY;
				mReportMode = ReportModes.BY_CATEGORY;
				break;
		    case R.id.category_month:
				mReportDateGrouping = ReportDateGrouping.MONTHLY;
				mReportMode = ReportModes.BY_CATEGORY;
				break;
		    case R.id.category_week:			
				mReportDateGrouping = ReportDateGrouping.WEEKLY;
				mReportMode = ReportModes.BY_CATEGORY;
				break;
		}
		setContentView(mReportFramework.buildViews());
		mReportViewList = mReportFramework.initializeTextViewsForExportList();
		Map<String, Map<String, Long>> reportDataStructure = loadReportDataStructures();

		Log.i(Global.LOG_CONTEXT, "loadReportDataStructures:"  + (System.currentTimeMillis() - performanceMeasureStart) );
		performanceMeasureStart = System.currentTimeMillis();

		for (String header : reportDataStructure.keySet()) {
			Map<String, Long> reportRows = reportDataStructure.get(header);
			TextView headerTextView = new TextView(this);
			headerTextView.setText(header);
			headerTextView.setTextColor(Color.GREEN);
			mReportViewList.add(headerTextView);
			mReportFramework.getLinearScroller().addView(headerTextView);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(0, 5, 0, 5);
			LinearLayout rowsView = new LinearLayout(this);
			rowsView.setOrientation(LinearLayout.VERTICAL);
			mReportFramework.getLinearScroller().getMainLayout().addView(rowsView, layoutParams);
			for (String rowCaption : reportRows.keySet()) {
				long totalTimeInMillis = reportRows.get(rowCaption);
				TextView rowTextView = new TextView(this);
				mReportViewList.add(rowTextView);
				rowTextView.setText("    " + rowCaption + ": "
						+ timeInMillisToText(totalTimeInMillis));
				rowsView.addView(rowTextView);
			}
		}
		Log.i(Global.LOG_CONTEXT, "generated report:"  + (System.currentTimeMillis() - performanceMeasureStart) );
		performanceMeasureStart = System.currentTimeMillis();
	}
	
	private Map<String, Map<String, Long>> loadReportDataStructures() {
		FilterParameter rangeFilter = this.mRangeFilter;

		List<TimeSlice> timeSlices = mTimeSliceDBAdapter.fetchTimeSlices(rangeFilter, rangeFilter.isIgnoreDates());
		Map<String, Map<String, Long>> summaries;
		if (mReportMode == ReportModes.BY_DATE) {
			summaries = new LinkedHashMap<String, Map<String, Long>>();
		} else {
			summaries = new TreeMap<String, Map<String, Long>>();
		}
		for (TimeSlice aSlice : timeSlices) {
			String header;
			if (mReportMode == ReportModes.BY_DATE) {
				if (mReportDateGrouping == ReportDateGrouping.WEEKLY) {
					header = String.format(getString(R.string.format_week_of_).toString(),aSlice.getStartWeekStr());
				} else if (mReportDateGrouping == ReportDateGrouping.MONTHLY) {
					header = aSlice.getStartMonthStr();
				} else {
					header = aSlice.getStartDateStr();
				}
			} else {
				header = aSlice.getCategoryName();
			}
			Map<String, Long> group = summaries.get(header);
			if (group == null) {
				if (mReportMode == ReportModes.BY_DATE) {
					group = new TreeMap<String, Long>();
				} else {
					group = new LinkedHashMap<String, Long>();
				}
				summaries.put(header, group);
			}
			String reportLine = null;
			if (mReportMode == ReportModes.BY_DATE) {
				reportLine = aSlice.getCategoryName();
			} else {
				if (mReportDateGrouping == ReportDateGrouping.WEEKLY) {
					reportLine = aSlice.getStartWeekStr();
				} else if (mReportDateGrouping == ReportDateGrouping.MONTHLY) {
					reportLine = aSlice.getStartMonthStr();
				} else {
					reportLine = aSlice.getStartDateStr();
				}
			}
			Long timeSum = group.get(reportLine);
			if (timeSum == null) {
				timeSum = new Long(0);
			}
			long sliceDuration = aSlice.getEndTime() - aSlice.getStartTime();
			group.put(reportLine, timeSum + sliceDuration);
		}
		return summaries;
	}

	private String timeInMillisToText(long totalTimeInMillis) {
		long minutes = (totalTimeInMillis / (1000 * 60)) % 60;
		long hours = totalTimeInMillis / (1000 * 60 * 60);
		String hoursWord;
		if (hours == 1) {
			hoursWord = getString(R.string.hoursWord1);
		} else {
			hoursWord = getString(R.string.hoursWordN);
		}
		String minutesWord;
		if (minutes == 1) {
			minutesWord = getString(R.string.minutesWord1);
		} else {
			minutesWord = getString(R.string.minutesWordN);
		}
		String timeString = hours + " " + hoursWord + ", " + minutes + " " + minutesWord;
		return timeString;
	}
}
