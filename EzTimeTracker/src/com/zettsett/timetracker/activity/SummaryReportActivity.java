package com.zettsett.timetracker.activity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.zetter.androidTime.R;
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
	private static final int MENU_ITEM_GROUP_DAILY = Menu.FIRST;
	private static final int MENU_ITEM_GROUP_WEEKLY = Menu.FIRST + 1;
	private static final int MENU_ITEM_GROUP_MONTHLY = Menu.FIRST + 2;
	private static final int MENU_ITEM_GROUP_CATEGORY = Menu.FIRST + 3;
	public static final String MENU_ID = "MENU_ID";
	private ReportFramework reportFramework;

	private TimeSliceDBAdapter timeSliceDBAdapter;

	private enum ReportDateGrouping {
		DAILY, WEEKLY, MONTHLY
	}

	private enum ReportModes {
		BY_DATE, BY_CATEGORY
	}

	private ReportDateGrouping reportDateGrouping = ReportDateGrouping.WEEKLY;
	private ReportModes reportMode = ReportModes.BY_DATE;
	private List<TextView> reportViewList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		timeSliceDBAdapter = new TimeSliceDBAdapter(this);
		reportFramework = new ReportFramework(this, this);
		if (savedInstanceState != null) {
			reportFramework.setStartDateRange(savedInstanceState.getLong("StartDateRange"));
			reportFramework.setEndDateRange(savedInstanceState.getLong("EndDateRange"));
			reportDateGrouping = (ReportDateGrouping) savedInstanceState
					.getSerializable("reportDateGrouping");
			reportMode = (ReportModes) savedInstanceState.getSerializable("reportMode");
		}
		loadDataIntoReport(this.getIntent().getIntExtra(SummaryReportActivity.MENU_ID, 0));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("StartDateRange", reportFramework.getStartDateRange());
		outState.putLong("EndDateRange", reportFramework.getEndDateRange());
		outState.putSerializable("reportDateGrouping", reportDateGrouping);
		outState.putSerializable("reportMode", reportMode);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.clear();
		SubMenu groupDateMenu = menu.addSubMenu(0, Menu.NONE, 0, R.string.menu_select_date_grouping);
		groupDateMenu.add(0, MENU_ITEM_GROUP_DAILY, 0, R.string.menu_select_date_grouping_daily);
		groupDateMenu.add(0, MENU_ITEM_GROUP_WEEKLY, 1, R.string.menu_select_date_grouping_weekly);
		groupDateMenu.add(0, MENU_ITEM_GROUP_MONTHLY, 2, R.string.menu_select_date_grouping_monthly);
		reportFramework.onPrepareOptionsMenu(menu);
		if (reportMode == ReportModes.BY_DATE) {
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
			reportDateGrouping = ReportDateGrouping.DAILY;
			loadDataIntoReport(0);
			break;
		case MENU_ITEM_GROUP_WEEKLY:
			reportDateGrouping = ReportDateGrouping.WEEKLY;
			loadDataIntoReport(0);
			break;
		case MENU_ITEM_GROUP_MONTHLY:
			reportDateGrouping = ReportDateGrouping.MONTHLY;
			loadDataIntoReport(0);
			break;
		case MENU_ITEM_GROUP_CATEGORY:
			if (reportMode == ReportModes.BY_CATEGORY) {
				reportMode = ReportModes.BY_DATE;
			} else {
				reportMode = ReportModes.BY_CATEGORY;
			}
			loadDataIntoReport(0);
			break;
		default:
			reportFramework.setReportType(ReportFramework.ReportTypes.SUMMARY);
			reportFramework.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return reportFramework.onCreateDialog(id);
	}

	public void loadDataIntoReport(int reportType) {
		switch (reportType)
		{
		    case R.id.summary_day:
				reportDateGrouping = ReportDateGrouping.DAILY;
				reportMode = ReportModes.BY_DATE;
				break;
		    case R.id.summary_month:
				reportDateGrouping = ReportDateGrouping.MONTHLY;
				reportMode = ReportModes.BY_DATE;
				break;
		    case R.id.summary_week:
				reportDateGrouping = ReportDateGrouping.WEEKLY;
				reportMode = ReportModes.BY_DATE;
				break;
		    case R.id.category_day:
				reportDateGrouping = ReportDateGrouping.DAILY;
				reportMode = ReportModes.BY_CATEGORY;
				break;
		    case R.id.category_month:
				reportDateGrouping = ReportDateGrouping.MONTHLY;
				reportMode = ReportModes.BY_CATEGORY;
				break;
		    case R.id.category_week:			
				reportDateGrouping = ReportDateGrouping.WEEKLY;
				reportMode = ReportModes.BY_CATEGORY;
				break;
		}
		setContentView(reportFramework.buildViews());
		reportViewList = reportFramework.initializeTextViewsForExportList();
		Map<String, Map<String, Long>> reportDataStructure = loadReportDataStructures();
		for (String header : reportDataStructure.keySet()) {
			Map<String, Long> reportRows = reportDataStructure.get(header);
			TextView headerTextView = new TextView(this);
			headerTextView.setText(header);
			headerTextView.setTextColor(Color.GREEN);
			reportViewList.add(headerTextView);
			reportFramework.getLinearScroller().addView(headerTextView);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(0, 5, 0, 5);
			LinearLayout rowsView = new LinearLayout(this);
			rowsView.setOrientation(LinearLayout.VERTICAL);
			reportFramework.getLinearScroller().getMainLayout().addView(rowsView, layoutParams);
			for (String rowCaption : reportRows.keySet()) {
				long totalTimeInMillis = reportRows.get(rowCaption);
				TextView rowTextView = new TextView(this);
				reportViewList.add(rowTextView);
				rowTextView.setText("    " + rowCaption + ": "
						+ timeInMillisToText(totalTimeInMillis));
				rowsView.addView(rowTextView);
			}
		}
	}

	private Map<String, Map<String, Long>> loadReportDataStructures() {
		List<TimeSlice> timeSlices = timeSliceDBAdapter.fetchTimeSlicesByDateRange(reportFramework
				.getStartDateRange(), reportFramework.getEndDateRange());
		Map<String, Map<String, Long>> summaries;
		if (reportMode == ReportModes.BY_DATE) {
			summaries = new LinkedHashMap<String, Map<String, Long>>();
		} else {
			summaries = new TreeMap<String, Map<String, Long>>();
		}
		for (TimeSlice aSlice : timeSlices) {
			String header;
			if (reportMode == ReportModes.BY_DATE) {
				if (reportDateGrouping == ReportDateGrouping.WEEKLY) {
					header = String.format(getString(R.string.format_week_of_).toString(),aSlice.getStartWeekStr());
				} else if (reportDateGrouping == ReportDateGrouping.MONTHLY) {
					header = aSlice.getStartMonthStr();
				} else {
					header = aSlice.getStartDateStr();
				}
			} else {
				header = aSlice.getCategoryName();
			}
			Map<String, Long> group = summaries.get(header);
			if (group == null) {
				if (reportMode == ReportModes.BY_DATE) {
					group = new TreeMap<String, Long>();
				} else {
					group = new LinkedHashMap<String, Long>();
				}
				summaries.put(header, group);
			}
			String reportLine = null;
			if (reportMode == ReportModes.BY_DATE) {
				reportLine = aSlice.getCategoryName();
			} else {
				if (reportDateGrouping == ReportDateGrouping.WEEKLY) {
					reportLine = aSlice.getStartWeekStr();
				} else if (reportDateGrouping == ReportDateGrouping.MONTHLY) {
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
