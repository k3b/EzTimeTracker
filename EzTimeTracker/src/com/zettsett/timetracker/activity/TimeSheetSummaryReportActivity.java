package com.zettsett.timetracker.activity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

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
public class TimeSheetSummaryReportActivity extends Activity implements IReportInterface {
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
	private FilterParameter mReportFilter;
	private static FilterParameter mRangeFilter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mTimeSliceRepository = new TimeSliceRepository(this);
		mRangeFilter = ReportFramework.getLastFilter(savedInstanceState, SAVED_REPORT_FILTER, mRangeFilter);

		mReportFramework = new ReportFramework(this, mRangeFilter);
		if (savedInstanceState != null) {
			mReportDateGrouping = (ReportDateGrouping) savedInstanceState
					.getSerializable(SAVED_REPORT_GROUPING());
			mReportMode = (ReportModes) savedInstanceState.getSerializable(SAVED_REPORT_MODE);
		}
		loadDataIntoReport(this.getIntent().getIntExtra(TimeSheetSummaryReportActivity.MENU_ID, 0));
	}

	protected String SAVED_REPORT_GROUPING() {
		return "reportDateGrouping";
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		ReportFramework.setLastFilter(outState, SAVED_REPORT_FILTER, mRangeFilter);		
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
		groupDateMenu.add(0, MENU_ITEM_GROUP_YARLY, 2, R.string.menu_select_date_grouping_yearly);
		mReportFramework.onPrepareOptionsMenu(menu);
		if (mReportMode == ReportModes.BY_DATE) {
			menu.add(0, MENU_ITEM_GROUP_CATEGORY, 1, R.string.menu_switch_to_category_headers);
		} else {
			menu.add(0, MENU_ITEM_GROUP_CATEGORY, 1, R.string.menu_switch_to_date_headers);
		}
		return result;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		FilterParameter filter = createFilter(v);
		if (filter != null) {
			Log.i(Global.LOG_CONTEXT, "Detailreport: "  + filter );

			menu.add(0, MENU_ITEM_REPORT, 0, getString(R.string.cmd_report));			
		}
		this.mReportFilter = filter;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_REPORT:
			showDetailReport();
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	private void showDetailReport() {
		if (mReportFilter != null) {
			TimeSheetDetailReportActivity.showActivity(this, mReportFilter);
		}
	}

	private FilterParameter createFilter(View v) {
		TimeSliceCategory category = getTimeSliceCategory(v);
		if (category != null) {
			FilterParameter filter = new FilterParameter().setCategoryId(category.getRowId());
			if (this.mReportMode == ReportModes.BY_DATE) {
				int pos = this.mReportViewList.indexOf(v);
				while(--pos >= 0) {
					Long date = getLong(this.mReportViewList.get(pos));
					if (date != null) {
						return setFilterDate(filter,this.mReportDateGrouping, date);
					}
				}
			}
			return filter.setIgnoreDates(true);
		} else {
			Long date = getLong(v);
			if (date != null) {
				FilterParameter filter = setFilterDate(new FilterParameter(),this.mReportDateGrouping, date);
				if (this.mReportMode == ReportModes.BY_CATEGORY) {
					int pos = this.mReportViewList.indexOf(v);
					while(--pos >= 0) {
						category = getTimeSliceCategory(v);
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

	private TimeSliceCategory getTimeSliceCategory(View v) {
		Object tag = v.getTag();
		if ((tag != null) && (tag instanceof TimeSliceCategory)){
			return (TimeSliceCategory) tag;
		}
		return null;
	}
	
	private Long getLong(View v) {
		Object tag = v.getTag();
		if ((tag != null) && (tag instanceof Long)){
			return (Long) tag;
		}
		return null;
	}
	
	private FilterParameter setFilterDate(FilterParameter filterParameter,
			ReportDateGrouping mReportDateGrouping, Long startDate) {
		final long start = startDate.longValue();
		final long end = getEndTime(mReportDateGrouping, start);
		return filterParameter.setStartTime(start).setEndTime(end);
	}

	private long getEndTime(ReportDateGrouping mReportDateGrouping,
			final long start) {
		DateTimeUtil dtu = DateTimeFormatter.getInstance();
		if (mReportDateGrouping == ReportDateGrouping.DAILY) {
			return dtu.addDays(start, 1); 
		} else if (mReportDateGrouping == ReportDateGrouping.WEEKLY) {
			return dtu.addDays(start, 7); 
		} else if (mReportDateGrouping == ReportDateGrouping.MONTHLY) {
			return dtu.getStartOfMonth(dtu.addDays(start, 31)); 
		} else if (mReportDateGrouping == ReportDateGrouping.YEARLY) {
			return dtu.getStartOfYear(dtu.addDays(start, 366)); 
		}
		
		throw new IllegalArgumentException("Unknown mReportDateGrouping " + mReportDateGrouping);
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
		case MENU_ITEM_GROUP_YARLY:
			mReportDateGrouping = ReportDateGrouping.YEARLY;
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
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null) {
			mRangeFilter = this.mReportFramework.onActivityResult(intent, mRangeFilter);
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
		TimeSheetSummaryCalculator reportDataStructure = loadReportDataStructures();

		Log.i(Global.LOG_CONTEXT, "loadReportDataStructures:"  + (System.currentTimeMillis() - performanceMeasureStart) );
		performanceMeasureStart = System.currentTimeMillis();

		Map<String, Map<String, Long>> reportData = reportDataStructure.getReportData();
		Map<String, Long> dates = reportDataStructure.getDates();
		Map<String, TimeSliceCategory> categoties = reportDataStructure.getCategoties();
		for (String header : reportData.keySet()) {
			Map<String, Long> reportRows = reportData.get(header);
			TextView headerTextView = new TextView(this);
			headerTextView.setText(header);
			headerTextView.setTextColor(Color.GREEN);
			if (mReportMode == ReportModes.BY_DATE) {
				headerTextView.setTag(dates.get(header));
			} else {
				headerTextView.setTag(categoties.get(header));
			}

			mReportViewList.add(headerTextView);
			registerForContextMenu(headerTextView);

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
				if (mReportMode == ReportModes.BY_DATE) {
					rowTextView.setTag(categoties.get(rowCaption));
				} else {
					rowTextView.setTag(dates.get(rowCaption));
				}
				registerForContextMenu(rowTextView);
				rowsView.addView(rowTextView);
			}
		}
		Log.i(Global.LOG_CONTEXT, "generated report:"  + (System.currentTimeMillis() - performanceMeasureStart) );
		performanceMeasureStart = System.currentTimeMillis();
	}
	
	/**
	 * 
	 * @return 
	 * Map<categoryName, Map<startDate.toString(), totalDurationsWithinSubinterval>> or
	 * Map<startDate.toString(), Map<categoryName, totalDurationsWithinSubinterval>>
	 */
	private TimeSheetSummaryCalculator loadReportDataStructures() {
		FilterParameter rangeFilter = mRangeFilter;

		List<TimeSlice> timeSlices = mTimeSliceRepository.fetchTimeSlices(rangeFilter, rangeFilter.isIgnoreDates());
		
		TimeSheetSummaryCalculator summaries = new TimeSheetSummaryCalculator(mReportMode,
				mReportDateGrouping, timeSlices);
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
