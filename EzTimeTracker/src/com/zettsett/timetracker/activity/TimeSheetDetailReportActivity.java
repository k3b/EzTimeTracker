package com.zettsett.timetracker.activity;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.database.TimeSliceRepository;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.report.IReportInterface;

/*
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

/**
 * Detail report grouped by date with optional date-filter
 * 
 * TODO reimplement as ListViewActivity? See tutorial http://www.vogella.com/articles/AndroidListView/article.html
 */
public class TimeSheetDetailReportActivity extends Activity implements IReportInterface {
	public static final String SAVED_REPORT_FILTER = "DetailReportFilter"; // can be used as intent-extra to specify a different filter
	
	private static final int EDIT_MENU_ID = Menu.FIRST;
	private static final int DELETE_MENU_ID = Menu.FIRST + 1;
	private static final int ADD_MENU_ID = Menu.FIRST + 2;
	private static final int SHOW_DESC_MENU_ID = Menu.FIRST + 3;
	private static final int ID_EDIT_TIME_SLICE = Menu.FIRST + 4;
	private static final int ID_ADD_TIME_SLICE = Menu.FIRST + 5;
	
	private TimeSliceRepository mTimeSliceRepository;
	private LinearLayout mMainLayout;
	private TimeSlice mCurrentSelectedTimeSlice;
	private long mCurrentSelectedDate;
	private FilterParameter mCurrentSelectionFilter = null;
	private static FilterParameter mRangeFilter;
	
	private ReportFramework mReportFramework;
	private List<TextView> mReportViewList;
	private boolean mShowNotes = true;

	private String instanceFilter;

	public static void showActivity(Context parent, FilterParameter filter) {
		Intent intent = new Intent().setClass(parent, TimeSheetDetailReportActivity.class);
		
		intent.putExtra(TimeSheetDetailReportActivity.SAVED_REPORT_FILTER, filter);
		parent.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mTimeSliceRepository = new TimeSliceRepository(this);
				
		Intent intent = this.getIntent();
		FilterParameter rangeFilter = (FilterParameter) intent.getExtras().get(SAVED_REPORT_FILTER); 
		if (rangeFilter == null) {
			mRangeFilter = ReportFramework.getLastFilter(savedInstanceState, instanceFilter, mRangeFilter);
			this.instanceFilter = SAVED_REPORT_FILTER;	// must als be saved for next time
		} else {
			mRangeFilter = rangeFilter;
			this.instanceFilter = null; // can be discarded
		}
		
		mReportFramework = new ReportFramework(this, mRangeFilter);
		loadDataIntoReport(0);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (this.instanceFilter != null) {
			// filter must be saved
			ReportFramework.setLastFilter(outState, this.instanceFilter, mRangeFilter);		
		} else {
			// current filter should be discarded. Restore previous filter
			mRangeFilter = ReportFramework.getLastFilter(outState, SAVED_REPORT_FILTER, mRangeFilter);					
		}
	}

	private void initScrollview() {
		setContentView(mReportFramework.buildViews());
		mMainLayout = new LinearLayout(this);
		mMainLayout.setOrientation(LinearLayout.VERTICAL);
		mReportFramework.getLinearScroller().addView(mMainLayout);
		mReportViewList = mReportFramework.initializeTextViewsForExportList();
	}

	/**
	 * Apparently one needs to wait for the UI to "settle" before the scroll
	 * will work. post seems to do the trick.
	 */
	private void initialScrollToEnd() {
		mReportFramework.getLinearScroller().getScrollView().post(new Runnable() {
			public void run() {
				mReportFramework.getLinearScroller().getScrollView().fullScroll(
						ScrollView.FOCUS_DOWN);
			}
		});
	}
	
	public void loadDataIntoReport(int id) {
		long performanceMeasureStart = System.currentTimeMillis();

		initScrollview();
		String lastStartDate = "";
		FilterParameter rangeFilter = mRangeFilter;
		List<TimeSlice> timeSlices = mTimeSliceRepository.fetchTimeSlices(
				rangeFilter, rangeFilter.isIgnoreDates());
		Log.i(Global.LOG_CONTEXT, "fetchTimeSlicesByDateRange:"  + (System.currentTimeMillis() - performanceMeasureStart) );
		performanceMeasureStart = System.currentTimeMillis();
		FilterParameter headerFilter = null;
		for (TimeSlice aSlice : timeSlices) {
			if (!lastStartDate.equals(aSlice.getStartDateStr())) {
				lastStartDate = aSlice.getStartDateStr();
				long startTime = aSlice.getStartTime();
				headerFilter = new FilterParameter().setStartTime(startTime);
				addDateHeaderLine(lastStartDate, headerFilter);
			}
			
			headerFilter.setEndTime(aSlice.getStartTime());
			
			addTimeSliceLine(aSlice);
		}
		Log.i(Global.LOG_CONTEXT, "addTimeSliceLine:"  + (System.currentTimeMillis() - performanceMeasureStart) );
		initialScrollToEnd();
	}

	private void addTimeSliceLine(TimeSlice aSlice) {
		TextView sliceReportLine = new TextView(this);
		sliceReportLine.setTag(aSlice);
		StringBuilder sliceReportText = new StringBuilder();
		sliceReportText.append("  ").append(aSlice.getTitleWithDuration());
		int lineOneEnd = sliceReportText.length();
		boolean showNotes = (mShowNotes && aSlice.getNotes() != null && aSlice.getNotes().length() > 0);
		if (showNotes) {
			sliceReportText.append("\n").append("    ").append(aSlice.getNotes());
		}
		sliceReportLine.setText(sliceReportText.toString(), TextView.BufferType.SPANNABLE);
		if (showNotes) {
			Spannable str = (Spannable) sliceReportLine.getText();
			str.setSpan(new ForegroundColorSpan(android.graphics.Color.GRAY), lineOneEnd, str
					.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		registerForContextMenu(sliceReportLine);
		mMainLayout.addView(sliceReportLine);
		mReportViewList.add(sliceReportLine);
	}

	private TextView addDateHeaderLine(String dateText, FilterParameter filter) {
		TextView startDateLine = new TextView(this);
		startDateLine.setTag(filter);
		startDateLine.setText(dateText);
		startDateLine.setTextColor(Color.GREEN);
		registerForContextMenu(startDateLine);
		mMainLayout.addView(startDateLine);
		mReportViewList.add(startDateLine);
		startDateLine.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					((TextView) view).setTextColor(Color.rgb(0, 128, 0));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					((TextView) view).setTextColor(Color.GREEN);
				}
				return false;
			}
		});
		return startDateLine;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Object tag = v.getTag();
		if (tag instanceof TimeSlice) {
			menu.add(0, ADD_MENU_ID, 0, getString(R.string.menu_report_add_new_time_interval));
			menu.add(0, EDIT_MENU_ID, 0, getString(R.string.menu_text_edit));
			menu.add(0, DELETE_MENU_ID, 0, getString(R.string.cmd_delete));
			mCurrentSelectedTimeSlice = (TimeSlice) v.getTag();
			mCurrentSelectedDate = mCurrentSelectedTimeSlice.getStartTime();
		} else if (tag instanceof FilterParameter) {
			menu.add(0, ADD_MENU_ID, 0, getString(R.string.menu_report_add_new_time_interval));
			menu.add(0, DELETE_MENU_ID, 0, getString(R.string.cmd_delete));
			mCurrentSelectedTimeSlice = null;
			mCurrentSelectionFilter = (FilterParameter) v.getTag();
			mCurrentSelectedDate = mCurrentSelectionFilter.getStartTime();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null) {
			TimeSlice updatedTimeSlice = (TimeSlice) intent.getExtras().get(Global.EXTRA_TIMESLICE); 
			
			if (updatedTimeSlice != null) {
				if (updatedTimeSlice.getRowId() == TimeSlice.IS_NEW_TIMESLICE) {
					mTimeSliceRepository.createTimeSlice(updatedTimeSlice);
				} else {
					mTimeSliceRepository.updateTimeSlice(updatedTimeSlice);
				}
			} else if (resultCode == ReportFilterActivity.RESULT_FILTER_CHANGED) {
				mRangeFilter = this.mReportFramework.onActivityResult(intent, mRangeFilter);
			}
			loadDataIntoReport(0);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case EDIT_MENU_ID:
			onCommandEditTimeSlice();
			return true;
		case DELETE_MENU_ID:
			onCommandDeleteTimeSlice();
			return true;
		case ADD_MENU_ID:
			onCommandAddTimeSlice();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void onCommandEditTimeSlice() {
		TimeSliceEditActivity.showTimeSliceEditActivity(this, mCurrentSelectedTimeSlice, ID_EDIT_TIME_SLICE);
	}

	private void onCommandDeleteTimeSlice() {
		FilterParameter parameter;
		if (mCurrentSelectedTimeSlice != null)
		{
			parameter = new FilterParameter().setParameter(mCurrentSelectedTimeSlice).setEndTime(mCurrentSelectedTimeSlice.getStartTime());
		} else {
			parameter = mCurrentSelectionFilter;
		}
		TimeSliceRemoveActivity.showActivity(this, parameter);
	}

	private void onCommandAddTimeSlice() {
		TimeSlice newSlice = new TimeSlice().setStartTime(mCurrentSelectedDate).setEndTime(mCurrentSelectedDate);
		TimeSliceEditActivity.showTimeSliceEditActivity(this, newSlice, ID_ADD_TIME_SLICE);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.clear();
		menu.add(0, ADD_MENU_ID, 0, getString(R.string.menu_report_add_new_time_interval));
		if (mShowNotes) {
			menu.add(0, SHOW_DESC_MENU_ID, 0, getString(R.string.menu_report_exclude_notes));
		} else {
			menu.add(0, SHOW_DESC_MENU_ID, 0, getString(R.string.menu_report_include_notes));
		}
		mReportFramework.onPrepareOptionsMenu(menu);

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_MENU_ID:
			Calendar c = Calendar.getInstance();
			long now = c.getTimeInMillis();
			TimeSlice newSlice = new TimeSlice().setStartTime(now).setEndTime(now);
			TimeSliceEditActivity.showTimeSliceEditActivity(this, newSlice, ADD_MENU_ID);

			break;
		case SHOW_DESC_MENU_ID:
			if (mShowNotes) {
				mShowNotes = false;
			} else {
				mShowNotes = true;
			}
			loadDataIntoReport(0);
			break;
		default:
			mReportFramework.setReportType(ReportFramework.ReportTypes.TIMESHEET);
			mReportFramework.onOptionsItemSelected(item);
		}

		return true;
	}
}