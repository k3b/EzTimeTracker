package com.zettsett.timetracker.activity;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.LinearLayout;
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
 * TODO reimplement as ListViewActivity? See tutorial
 * http://www.vogella.com/articles/AndroidListView/article.html
 */
public class TimeSheetDetailReportActivity extends Activity implements
		IReportInterface {
	/**
	 * Used to transfer optional filter between parent activity and this.
	 */
	private static final String SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME = "DetailReportFilter";

	// menu ids
	private static final int EDIT_MENU_ID = Menu.FIRST;
	private static final int DELETE_MENU_ID = Menu.FIRST + 1;
	private static final int ADD_MENU_ID = Menu.FIRST + 2;
	private static final int SHOW_NOTES_MENU_ID = Menu.FIRST + 3;
	private static final int ID_EDIT_TIME_SLICE = Menu.FIRST + 4;
	private static final int ID_ADD_TIME_SLICE = Menu.FIRST + 5;

	// dependent services
	private TimeSliceRepository timeSliceRepository;
	private ReportFramework reportFramework;

	// form controls
	private LinearLayout mainLayout;
	private List<TextView> reportViewItemList;

	// current state
	private TimeSlice currentSelectedTimeSliceUsedForMenu;
	private long lastSelectedDateUsedForAddMenu;

	/**
	 * if reportitems should be generated with timeslice-notes or not.<br>
	 * Toggeld via option-menu
	 */
	private boolean showNotes = true;

	/**
	 * If null do not save changed filter. Else name where current filter should
	 * be persisted to.
	 */
	private String currentBundelPersistRangeFilterName;

	/**
	 * current range filter used to fill report.<br/>
	 * static to surwive if this activity is discarded in filter activity.
	 */
	private static TimeSliceFilterParameter currentRangeFilter;

	/**
	 * Used in options-menue for context sensitive delete
	 */
	private TimeSliceFilterParameter currentSelectedListItemRangeFilterUsedForMenu = null;

	/**
	 * Show report with customized filter from SummaryReport-Drilldown and
	 * others.<br/>
	 * Call from mainmenu directly via intent without this method.<br/>
	 * 
	 * @param context
	 *            needed to start this activity from parent activity.
	 * @param filter
	 *            customized filter that can be discarded after finish.
	 */
	public static void showActivity(final Context context,
			final TimeSliceFilterParameter filter) {
		final Intent intent = new Intent().setClass(context,
				TimeSheetDetailReportActivity.class);

		intent.putExtra(
				TimeSheetDetailReportActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME,
				filter);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.timeSliceRepository = new TimeSliceRepository(this);

		final Intent intent = this.getIntent();
		final TimeSliceFilterParameter rangeFilter = (TimeSliceFilterParameter) intent
				.getExtras()
				.get(TimeSheetDetailReportActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME);
		if (rangeFilter == null) {
			// not created with parameter so restore last instance value
			TimeSheetDetailReportActivity.currentRangeFilter = ReportFramework
					.getLastFilter(savedInstanceState,
							this.currentBundelPersistRangeFilterName,
							TimeSheetDetailReportActivity.currentRangeFilter);
			this.currentBundelPersistRangeFilterName = TimeSheetDetailReportActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME; // must
			// als
			// be
			// saved
			// for
			// next
			// time
		} else {
			TimeSheetDetailReportActivity.currentRangeFilter = rangeFilter;
			this.currentBundelPersistRangeFilterName = null; // can be discarded
		}

		this.reportFramework = new ReportFramework(this,
				TimeSheetDetailReportActivity.currentRangeFilter);
		this.loadDataIntoReport(0);
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
	protected void onSaveInstanceState(final Bundle outState) {
		if (this.currentBundelPersistRangeFilterName != null) {
			// filter must be saved
			ReportFramework.setLastFilter(outState,
					this.currentBundelPersistRangeFilterName,
					TimeSheetDetailReportActivity.currentRangeFilter);
		} else {
			// current filter should be discarded. Restore previous filter
			TimeSheetDetailReportActivity.currentRangeFilter = ReportFramework
					.getLastFilter(
							outState,
							TimeSheetDetailReportActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME,
							TimeSheetDetailReportActivity.currentRangeFilter);
		}
	}

	private void initScrollview() {
		this.setContentView(this.reportFramework.buildViews());
		this.mainLayout = new LinearLayout(this);
		this.mainLayout.setOrientation(LinearLayout.VERTICAL);
		this.reportFramework.getLinearScroller().addView(this.mainLayout);
		this.reportViewItemList = this.reportFramework
				.initializeTextViewsForExportList();
	}

	/**
	 * Apparently one needs to wait for the UI to "settle" before the scroll
	 * will work. post seems to do the trick.
	 */
	private void initialScrollToEnd() {
		this.reportFramework.getLinearScroller().getScrollView()
				.post(new Runnable() {
					@Override
					public void run() {
						TimeSheetDetailReportActivity.this.reportFramework
								.getLinearScroller().getScrollView()
								.fullScroll(View.FOCUS_DOWN);
					}
				});
	}

	@Override
	public void loadDataIntoReport(final int id) {
		long performanceMeasureStart = System.currentTimeMillis();

		this.initScrollview();
		String lastStartDate = "";
		final TimeSliceFilterParameter rangeFilter = TimeSheetDetailReportActivity.currentRangeFilter;
		final List<TimeSlice> timeSlices = this.timeSliceRepository
				.fetchList(rangeFilter);
		Log.i(Global.LOG_CONTEXT,
				"fetchTimeSlicesByDateRange:"
						+ (System.currentTimeMillis() - performanceMeasureStart));
		performanceMeasureStart = System.currentTimeMillis();
		this.addDateHeaderLine(rangeFilter.toString(), null, Color.YELLOW);
		TimeSliceFilterParameter headerFilter = null;
		int itemCount = 0;
		for (final TimeSlice aSlice : timeSlices) {
			if (!lastStartDate.equals(aSlice.getStartDateStr())) {
				lastStartDate = aSlice.getStartDateStr();
				final long startTime = aSlice.getStartTime();
				headerFilter = new TimeSliceFilterParameter()
						.setStartTime(startTime);
				this.addDateHeaderLine(lastStartDate, headerFilter, Color.GREEN);
			}

			headerFilter.setEndTime(aSlice.getStartTime());

			this.addTimeSliceLine(aSlice);
			itemCount++;
		}

		if (itemCount == 0) {
			this.addDateHeaderLine(this.getString(R.string.message_no_data),
					null, Color.YELLOW);
		}
		Log.i(Global.LOG_CONTEXT,
				"addTimeSliceLine:"
						+ (System.currentTimeMillis() - performanceMeasureStart));
		this.initialScrollToEnd();
	}

	private void addTimeSliceLine(final TimeSlice aSlice) {
		final TextView sliceReportLine = new TextView(this);
		sliceReportLine.setTag(aSlice);
		final StringBuilder sliceReportText = new StringBuilder();
		sliceReportText.append("  ").append(aSlice.getTitleWithDuration());
		final int lineOneEnd = sliceReportText.length();
		final boolean showNotes = (this.showNotes
				&& (aSlice.getNotes() != null) && (aSlice.getNotes().length() > 0));
		if (showNotes) {
			sliceReportText.append("\n").append("    ")
					.append(aSlice.getNotes());
		}
		sliceReportLine.setText(sliceReportText.toString(),
				TextView.BufferType.SPANNABLE);
		if (showNotes) {
			final Spannable str = (Spannable) sliceReportLine.getText();
			str.setSpan(new ForegroundColorSpan(android.graphics.Color.GRAY),
					lineOneEnd, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		this.registerForContextMenu(sliceReportLine);
		this.mainLayout.addView(sliceReportLine);
		this.reportViewItemList.add(sliceReportLine);
	}

	private TextView addDateHeaderLine(final String dateText,
			final TimeSliceFilterParameter filter, final int color) {
		final TextView startDateLine = new TextView(this);
		startDateLine.setText(dateText);
		startDateLine.setTextColor(color);
		if (filter != null) {
			startDateLine.setTag(filter);
			this.registerForContextMenu(startDateLine);
			startDateLine.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(final View view, final MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						((TextView) view).setTextColor(Color.rgb(0, 128, 0));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						((TextView) view).setTextColor(Color.GREEN);
					}
					return false;
				}
			});
		}
		this.mainLayout.addView(startDateLine);
		this.reportViewItemList.add(startDateLine);
		return startDateLine;
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final Object tag = v.getTag();
		if (tag instanceof TimeSlice) {
			menu.add(0, TimeSheetDetailReportActivity.ADD_MENU_ID, 0,
					this.getString(R.string.menu_report_add_new_time_interval));
			menu.add(0, TimeSheetDetailReportActivity.EDIT_MENU_ID, 0,
					this.getString(R.string.menu_text_edit));
			menu.add(0, TimeSheetDetailReportActivity.DELETE_MENU_ID, 0,
					this.getString(R.string.cmd_delete));
			this.currentSelectedTimeSliceUsedForMenu = (TimeSlice) v.getTag();
			this.lastSelectedDateUsedForAddMenu = this.currentSelectedTimeSliceUsedForMenu
					.getStartTime();
		} else if (tag instanceof TimeSliceFilterParameter) {
			menu.add(0, TimeSheetDetailReportActivity.ADD_MENU_ID, 0,
					this.getString(R.string.menu_report_add_new_time_interval));
			menu.add(0, TimeSheetDetailReportActivity.DELETE_MENU_ID, 0,
					this.getString(R.string.cmd_delete));
			this.currentSelectedTimeSliceUsedForMenu = null;
			this.currentSelectedListItemRangeFilterUsedForMenu = (TimeSliceFilterParameter) v
					.getTag();
			this.lastSelectedDateUsedForAddMenu = this.currentSelectedListItemRangeFilterUsedForMenu
					.getStartTime();
		}
	}

	/**
	 * handle result from edit/changeFilter/delete
	 */
	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null) {
			final TimeSlice updatedTimeSlice = (TimeSlice) intent.getExtras()
					.get(Global.EXTRA_TIMESLICE);

			if (updatedTimeSlice != null) {
				// after Edit saveNew/updateExisting Timeslice
				if (updatedTimeSlice.getRowId() == TimeSlice.IS_NEW_TIMESLICE) {
					this.timeSliceRepository.create(updatedTimeSlice);
				} else {
					this.timeSliceRepository.update(updatedTimeSlice);
				}
			} else if (resultCode == ReportFilterActivity.RESULT_FILTER_CHANGED) {
				// after filter change: remeber new filter
				TimeSheetDetailReportActivity.currentRangeFilter = this.reportFramework
						.onActivityResult(
								intent,
								TimeSheetDetailReportActivity.currentRangeFilter);
			}

			this.loadDataIntoReport(0);
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case EDIT_MENU_ID:
			this.onCommandEditTimeSlice();
			return true;
		case DELETE_MENU_ID:
			this.onCommandDeleteTimeSlice();
			return true;
		case ADD_MENU_ID:
			this.onCommandAddTimeSlice();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void onCommandEditTimeSlice() {
		TimeSliceEditActivity.showTimeSliceEditActivity(this,
				this.currentSelectedTimeSliceUsedForMenu,
				TimeSheetDetailReportActivity.ID_EDIT_TIME_SLICE);
	}

	private void onCommandDeleteTimeSlice() {
		TimeSliceFilterParameter parameter;
		if (this.currentSelectedTimeSliceUsedForMenu != null) {
			parameter = new TimeSliceFilterParameter().setParameter(
					this.currentSelectedTimeSliceUsedForMenu).setEndTime(
					this.currentSelectedTimeSliceUsedForMenu.getStartTime());
		} else {
			parameter = this.currentSelectedListItemRangeFilterUsedForMenu;
		}
		TimeSliceRemoveActivity.showActivity(this, parameter);
	}

	private void onCommandAddTimeSlice() {
		final TimeSlice newSlice = new TimeSlice().setStartTime(
				this.lastSelectedDateUsedForAddMenu).setEndTime(
				this.lastSelectedDateUsedForAddMenu);
		TimeSliceEditActivity.showTimeSliceEditActivity(this, newSlice,
				TimeSheetDetailReportActivity.ID_ADD_TIME_SLICE);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final boolean result = super.onCreateOptionsMenu(menu);
		menu.clear();
		menu.add(0, TimeSheetDetailReportActivity.ADD_MENU_ID, 0,
				this.getString(R.string.menu_report_add_new_time_interval));
		if (this.showNotes) {
			menu.add(0, TimeSheetDetailReportActivity.SHOW_NOTES_MENU_ID, 0,
					this.getString(R.string.menu_report_exclude_notes));
		} else {
			menu.add(0, TimeSheetDetailReportActivity.SHOW_NOTES_MENU_ID, 0,
					this.getString(R.string.menu_report_include_notes));
		}
		this.reportFramework.onPrepareOptionsMenu(menu);

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case ADD_MENU_ID:
			final Calendar c = Calendar.getInstance();
			final long now = c.getTimeInMillis();
			final TimeSlice newSlice = new TimeSlice().setStartTime(now)
					.setEndTime(now);
			TimeSliceEditActivity.showTimeSliceEditActivity(this, newSlice,
					TimeSheetDetailReportActivity.ADD_MENU_ID);

			break;
		case SHOW_NOTES_MENU_ID:
			if (this.showNotes) {
				this.showNotes = false;
			} else {
				this.showNotes = true;
			}
			this.loadDataIntoReport(0);
			break;
		default:
			this.reportFramework
					.setReportType(ReportFramework.ReportTypes.TIMESHEET);
			this.reportFramework.onOptionsItemSelected(item);
		}

		return true;
	}

}
