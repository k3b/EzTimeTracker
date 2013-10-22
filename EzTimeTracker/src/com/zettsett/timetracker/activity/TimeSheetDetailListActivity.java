package com.zettsett.timetracker.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.database.TimeSliceRepository;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.report.IReportInterface;

import de.k3b.common.ItemWithRowId;
import de.k3b.util.DateTimeUtil;

public class TimeSheetDetailListActivity extends ListActivity implements
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
				TimeSheetDetailListActivity.class);

		intent.putExtra(
				TimeSheetDetailListActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME,
				filter);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.time_slice_list);

		this.timeSliceRepository = new TimeSliceRepository(this);

		final Intent intent = this.getIntent();
		final TimeSliceFilterParameter rangeFilter = (TimeSliceFilterParameter) intent
				.getExtras()
				.get(TimeSheetDetailListActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME);
		if (rangeFilter == null) {
			// not created with parameter so restore last instance value
			TimeSheetDetailListActivity.currentRangeFilter = ReportFramework
					.getLastFilter(savedInstanceState,
							this.currentBundelPersistRangeFilterName,
							TimeSheetDetailListActivity.currentRangeFilter);
			this.currentBundelPersistRangeFilterName = TimeSheetDetailListActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME; // must
		} else {
			TimeSheetDetailListActivity.currentRangeFilter = rangeFilter;
			this.currentBundelPersistRangeFilterName = null; // can be discarded
		}

		this.reportFramework = new ReportFramework(this,
				TimeSheetDetailListActivity.currentRangeFilter);
		this.loadDataIntoReport(0);
		// this.registerForContextMenu(this.getListView());
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		if (this.currentBundelPersistRangeFilterName != null) {
			// filter must be saved
			ReportFramework.setLastFilter(outState,
					this.currentBundelPersistRangeFilterName,
					TimeSheetDetailListActivity.currentRangeFilter);
		} else {
			// current filter should be discarded. Restore previous filter
			TimeSheetDetailListActivity.currentRangeFilter = ReportFramework
					.getLastFilter(
							outState,
							TimeSheetDetailListActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME,
							TimeSheetDetailListActivity.currentRangeFilter);
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
				if (updatedTimeSlice.getRowId() == ItemWithRowId.IS_NEW_TIMESLICE) {
					this.timeSliceRepository.create(updatedTimeSlice);
				} else {
					this.timeSliceRepository.update(updatedTimeSlice);
				}
			} else if (resultCode == ReportFilterActivity.RESULT_FILTER_CHANGED) {
				// after filter change: remeber new filter
				TimeSheetDetailListActivity.currentRangeFilter = this.reportFramework
						.onActivityResult(intent,
								TimeSheetDetailListActivity.currentRangeFilter);
			}

			this.loadDataIntoReport(0);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final boolean result = super.onCreateOptionsMenu(menu);
		menu.clear();
		this.addMenuItems(menu);

		return result;
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		// final ListView listView = this.getListView();
		// final View selectedView = listView.getSelectedView();
		// final Object tag = (selectedView != null) ? selectedView.getTag()
		// : listView.getFocusedChild();
		final Object tag = v.getTag();
		if (tag instanceof TimeSlice) {
			menu.add(0, TimeSheetDetailListActivity.ADD_MENU_ID, 0,
					this.getString(R.string.menu_report_add_new_time_interval));
			menu.add(0, TimeSheetDetailListActivity.EDIT_MENU_ID, 0,
					this.getString(R.string.menu_text_edit));
			menu.add(0, TimeSheetDetailListActivity.DELETE_MENU_ID, 0,
					this.getString(R.string.cmd_delete));
			this.currentSelectedTimeSliceUsedForMenu = (TimeSlice) tag;
			this.lastSelectedDateUsedForAddMenu = this.currentSelectedTimeSliceUsedForMenu
					.getStartTime();
		} else if (tag instanceof Long) {
			menu.add(0, TimeSheetDetailListActivity.ADD_MENU_ID, 0,
					this.getString(R.string.menu_report_add_new_time_interval));
			menu.add(0, TimeSheetDetailListActivity.DELETE_MENU_ID, 0,
					this.getString(R.string.cmd_delete));
			this.currentSelectedTimeSliceUsedForMenu = null;
			this.lastSelectedDateUsedForAddMenu = (Long) tag;
			this.currentSelectedListItemRangeFilterUsedForMenu = new TimeSliceFilterParameter()
					.setStartTime(this.lastSelectedDateUsedForAddMenu)
					.setEndTime(
							DateTimeFormatter.getInstance().addDays(
									this.lastSelectedDateUsedForAddMenu, 1));
		}
	}

	private void addMenuItems(final Menu menu) {

		menu.add(0, TimeSheetDetailListActivity.ADD_MENU_ID, 0,
				this.getString(R.string.menu_report_add_new_time_interval));
		if (this.showNotes) {
			menu.add(0, TimeSheetDetailListActivity.SHOW_NOTES_MENU_ID, 0,
					this.getString(R.string.menu_report_exclude_notes));
		} else {
			menu.add(0, TimeSheetDetailListActivity.SHOW_NOTES_MENU_ID, 0,
					this.getString(R.string.menu_report_include_notes));
		}
		this.reportFramework.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		this.onMenuItemSelected(item);
		return true;
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
				TimeSheetDetailListActivity.ID_EDIT_TIME_SLICE);
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
				TimeSheetDetailListActivity.ID_ADD_TIME_SLICE);
	}

	private boolean onMenuItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case ADD_MENU_ID:
			final Calendar c = Calendar.getInstance();
			final long now = c.getTimeInMillis();
			final TimeSlice newSlice = new TimeSlice().setStartTime(now)
					.setEndTime(now);
			TimeSliceEditActivity.showTimeSliceEditActivity(this, newSlice,
					TimeSheetDetailListActivity.ADD_MENU_ID);
			return true;
		case SHOW_NOTES_MENU_ID:
			if (this.showNotes) {
				this.showNotes = false;
			} else {
				this.showNotes = true;
			}
			this.loadDataIntoReport(0);
			return true;
		default:
			this.reportFramework
					.setReportType(ReportFramework.ReportTypes.TIMESHEET);
			return this.reportFramework.onOptionsItemSelected(item);
		}
	}

	@Override
	public void loadDataIntoReport(final int reportType) {
		long performanceMeasureStart = System.currentTimeMillis();

		final TimeSliceFilterParameter rangeFilter = TimeSheetDetailListActivity.currentRangeFilter;
		final List<TimeSlice> timeSlices = this.timeSliceRepository
				.fetchList(rangeFilter);
		Log.i(Global.LOG_CONTEXT,
				"fetchTimeSlicesByDateRange:"
						+ (System.currentTimeMillis() - performanceMeasureStart));
		performanceMeasureStart = System.currentTimeMillis();

		this.setTitle(rangeFilter.toString());

		final List<Object> items = this.loadData(timeSlices);
		Log.i(Global.LOG_CONTEXT, "Convert Data:"
				+ (System.currentTimeMillis() - performanceMeasureStart));
		performanceMeasureStart = System.currentTimeMillis();

		this.setListAdapter(new TimeSheetDetailReportAdapter(this, items,
				this.showNotes));
		Log.i(Global.LOG_CONTEXT,
				"Create adapter:"
						+ (System.currentTimeMillis() - performanceMeasureStart));
	}

	private List<Object> loadData(final List<TimeSlice> timeSlices) {
		int itemCount = 0;
		long lastStartDate = 0;

		final DateTimeUtil formatter = DateTimeFormatter.getInstance();

		final List<Object> items = new ArrayList<Object>();

		for (final TimeSlice aSlice : timeSlices) {
			final long startDate = formatter.getStartOfDay(aSlice
					.getStartTime());
			if (lastStartDate != startDate) {
				lastStartDate = startDate;
				items.add(Long.valueOf(lastStartDate)); // , Color.GREEN);
			}

			items.add(aSlice);
			itemCount++;
		}

		return items;
	}
}