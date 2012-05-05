package com.zettsett.timetracker.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
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
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.database.TimeSliceDBAdapter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;
import com.zettsett.timetracker.report.ReportInterface;

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
 */
public class TimeSheetReportActivity extends Activity implements ReportInterface {
	private static final int EDIT_MENU_ID = Menu.FIRST;
	private static final int DELETE_MENU_ID = Menu.FIRST + 1;
	private static final int ADD_MENU_ID = Menu.FIRST + 2;
	private static final int SHOW_DESC_MENU_ID = Menu.FIRST + 3;
	private TimeSliceDBAdapter mTimeSliceDBAdapter;
	private LinearLayout mMainLayout;
	private final Map<View, Integer> mRowToSliceRowIdMap = new HashMap<View, Integer>();
	private int mChosenRowId;
	private ReportFramework mReportFramework;
	private List<TextView> mReportViewList;
	private String mDateSelectedForAdd;
	private boolean mShowNotes = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mTimeSliceDBAdapter = new TimeSliceDBAdapter(this);
		mReportFramework = new ReportFramework(this, this);
		if (savedInstanceState != null) {
			mReportFramework.setStartDateRange(savedInstanceState.getLong("StartDateRange"));
			mReportFramework.setEndDateRange(savedInstanceState.getLong("EndDateRange"));
		}
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
		outState.putLong("StartDateRange", mReportFramework.getStartDateRange());
		outState.putLong("EndDateRange", mReportFramework.getEndDateRange());
	}

	public void loadDataIntoReport(int id) {
		long performanceMeasureStart = System.currentTimeMillis();

		initScrollview();
		String lastStartDate = "";
		final Calendar c = Calendar.getInstance();
		c.setTime(new Date(mReportFramework.getEndDateRange()));
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		long endDate = c.getTimeInMillis();

		List<TimeSlice> timeSlices = mTimeSliceDBAdapter.fetchTimeSlicesByDateRange(
				mReportFramework.getStartDateRange(), endDate);
		Log.i(Global.LOG_CONTEXT, "fetchTimeSlicesByDateRange:"  + (System.currentTimeMillis() - performanceMeasureStart) );
		performanceMeasureStart = System.currentTimeMillis();
		for (TimeSlice aSlice : timeSlices) {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			if (!lastStartDate.equals(aSlice.getStartDateStr())) {
				lastStartDate = aSlice.getStartDateStr();
				addDateHeaderLine(lastStartDate);
			}
			addTimeSliceLine(aSlice);
		}
		Log.i(Global.LOG_CONTEXT, "addTimeSliceLine:"  + (System.currentTimeMillis() - performanceMeasureStart) );
		initialScrollToEnd();
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

	private void addTimeSliceLine(TimeSlice aSlice) {
		TextView sliceReportLine = new TextView(this);
		mRowToSliceRowIdMap.put(sliceReportLine, aSlice.getRowId());
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
		sliceReportLine.setTag("Detail");
		mMainLayout.addView(sliceReportLine);
		mReportViewList.add(sliceReportLine);
	}

	private TextView addDateHeaderLine(String dateText) {
		TextView startDateLine = new TextView(this);
		startDateLine.setText(dateText);
		startDateLine.setTextColor(Color.GREEN);
		registerForContextMenu(startDateLine);
		startDateLine.setTag("Header");
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
		if (v.getTag().equals("Detail")) {
			menu.add(0, EDIT_MENU_ID, 0, getString(R.string.menu_text_edit));
			menu.add(0, DELETE_MENU_ID, 0, getString(R.string.cmd_delete));
			mChosenRowId = mRowToSliceRowIdMap.get(v);
		} else if (v.getTag().equals("Header")) {
			menu.add(0, ADD_MENU_ID, 0, getString(R.string.menu_report_add_new_time_interval));
			mDateSelectedForAdd = (String) ((TextView) v).getText();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null) {
			TimeSlice updatedTimeSlice = (TimeSlice) intent.getExtras().get(Global.EXTRA_TIMESLICE); 
			if (updatedTimeSlice.getRowId() == TimeSlice.IS_NEW_TIMESLICE) {
				mTimeSliceDBAdapter.createTimeSlice(updatedTimeSlice);
			} else {
				mTimeSliceDBAdapter.updateTimeSlice(updatedTimeSlice);
			}
			loadDataIntoReport(0);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case EDIT_MENU_ID:
			TimeSliceEditActivity.showTimeSliceEditDialog(this, mChosenRowId);
			return true;
		case DELETE_MENU_ID:
			buildDeleteDialog();
			return true;
		case ADD_MENU_ID:
			long now = DateTimeFormatter.parseDate(mDateSelectedForAdd);
			TimeSlice newSlice = new TimeSlice().setStartTime(now).setEndTime(now);
			TimeSliceEditActivity.showTimeSliceEditDialog(this, newSlice);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void buildDeleteDialog() {
		Intent intent = new Intent().setClass(this, RemoveTimeSliceActivity.class);
		intent.putExtra(SummaryReportActivity.MENU_ID, TimeSliceCategory.NOT_SAVED); //  item.getItemId());
		startActivity(intent);
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
			TimeSliceEditActivity.showTimeSliceEditDialog(this, newSlice);

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

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = mReportFramework.onCreateDialog(id);
		if (dialog == null)
		{
			dialog = super.onCreateDialog(id);
		}
		return dialog;
	}
}
