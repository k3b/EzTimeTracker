package com.zettsett.timetracker.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.Settings;
import com.zettsett.timetracker.database.TimeSliceCategoryRepsitory;
import com.zettsett.timetracker.database.TimeSliceRepository;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;
import com.zettsett.timetracker.report.IReportInterface;

import de.k3b.common.ItemWithRowId;
import de.k3b.util.DateTimeUtil;

/**
 * Detail report grouped by date with optional date-category-note-filter.<br/>
 * Called by main activity PunchInPunchOutActivity or by Drill-Down-Report from
 * elsewhere with context daterange and/or category.
 * 
 */
public class TimeSheetDetailListActivity extends BaseReportListActivity
		implements IReportInterface, ICategorySetter {
	// menu ids

	// dependent services
	private TimeSliceRepository timeSliceRepository;
	private final TimeSliceCategoryRepsitory categoryRepository = new TimeSliceCategoryRepsitory(
			this);

	// current state
	private TimeSlice currentSelectedTimeSliceUsedForMenu;
	private long lastSelectedDateUsedForAddMenu;

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
	 * @param idOnOkResultCode
	 * @param profileName
	 *            where the content should be saved/restored in bundle
	 */
	public static void showActivity(final Context context,
			final TimeSliceFilterParameter filter, final int idOnOkResultCode) {
		if (Global.isDebugEnabled()) {
			Log.d(Global.LOG_CONTEXT, context.getClass().getSimpleName()
					+ " > TimeSheetDetailListActivity.showActivity("
					+ Global.EXTRA_FILTER + ") = '" + filter + "'");
		}

		final Intent intent = new Intent().setClass(context,
				TimeSheetDetailListActivity.class);

		intent.putExtra(Global.EXTRA_FILTER, filter);
		intent.putExtra(Global.EXTRA_RESULTID, idOnOkResultCode);

		if (idOnOkResultCode != 0) {
			((Activity) context).startActivityForResult(intent,
					idOnOkResultCode);
		} else {
			context.startActivity(intent);
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.resultRangeFilter = null;
		this.setContentView(R.layout.time_slice_list);
		this.registerForContextMenu(this.getListView());

		this.timeSliceRepository = new TimeSliceRepository(this,
				Settings.isPublicDatabase());

		final Intent intent = this.getIntent();

		final TimeSliceFilterParameter rangeFilter = (TimeSliceFilterParameter) intent
				.getExtras().get(Global.EXTRA_FILTER);
		this.idOnOkResultCode = intent.getExtras()
				.getInt(Global.EXTRA_RESULTID);
		if (rangeFilter == null) {
			if (Global.isDebugEnabled()) {
				Log.d(Global.LOG_CONTEXT,
						"TimeSheetDetailListActivity.onCreate: not created with parameter so restore last instance value");
			}

			// not created with parameter so restore last instance value
			this.currentRangeFilter = BaseReportListActivity.getLastFilter(
					this, savedInstanceState, Global.EXTRA_FILTER,
					this.currentRangeFilter);
		} else {
			if (Global.isDebugEnabled()) {
				Log.d(Global.LOG_CONTEXT,
						"TimeSheetDetailListActivity.onCreate: with parameter "
								+ rangeFilter);
			}
			this.currentRangeFilter = rangeFilter;
		}

		this.setDefaultsToFilterDatesIfNeccesary(this.currentRangeFilter);
		this.loadDataIntoReport(0);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.detailreport, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menue_add:
			final Calendar c = Calendar.getInstance();
			final long now = c.getTimeInMillis();
			final TimeSlice newSlice = new TimeSlice().setStartTime(now)
					.setEndTime(now);
			TimeSliceEditActivity.showTimeSliceEditActivity(this, newSlice,
					R.id.menue_add);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		final Object tag = this.getListView().getItemAtPosition(
				((AdapterContextMenuInfo) menuInfo).position);

		if (tag instanceof TimeSlice) {
			final MenuInflater inflater = this.getMenuInflater();
			inflater.inflate(R.menu.context_timeslice, menu);

			this.currentSelectedTimeSliceUsedForMenu = (TimeSlice) tag;
			this.lastSelectedDateUsedForAddMenu = this.currentSelectedTimeSliceUsedForMenu
					.getEndTime();
		} else if (tag instanceof Long) {
			this.currentSelectedTimeSliceUsedForMenu = null;
			this.lastSelectedDateUsedForAddMenu = (Long) tag;
			this.currentSelectedListItemRangeFilterUsedForMenu = this
					.createDrillDownFilter()
					.setStartTime(this.lastSelectedDateUsedForAddMenu)
					.setEndTime(
							DateTimeFormatter.getInstance().addDays(
									this.lastSelectedDateUsedForAddMenu, 1));
		}
	}

	private TimeSliceFilterParameter createDrillDownFilter() {
		final TimeSliceFilterParameter defaults = this.currentRangeFilter;
		return new TimeSliceFilterParameter().setNotes(defaults.getNotes())
				.setNotesNotNull(defaults.isNotesNotNull())
				.setCategoryId(defaults.getCategoryId());
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menue_edit:
			this.onCommandEditTimeSlice();
			return true;
		case R.id.menue_delete:
			this.onCommandDeleteTimeSlice();
			return true;
		case R.id.menue_add:
			this.onCommandAddTimeSlice();
			return true;
		case R.id.menue_edit_category:
			this.onCommandEditCategory();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void onCommandEditTimeSlice() {
		TimeSliceEditActivity.showTimeSliceEditActivity(this,
				this.currentSelectedTimeSliceUsedForMenu, R.id.menue_edit);
	}

	private void onCommandDeleteTimeSlice() {
		TimeSliceFilterParameter parameter;
		if (this.currentSelectedTimeSliceUsedForMenu != null) {
			parameter = this
					.createDrillDownFilter()
					.setParameter(this.currentSelectedTimeSliceUsedForMenu)
					.setEndTime(
							this.currentSelectedTimeSliceUsedForMenu
									.getStartTime());
		} else {
			parameter = this.currentSelectedListItemRangeFilterUsedForMenu;
		}
		TimeSliceRemoveActivity.showActivity(this, parameter);
	}

	private void onCommandEditCategory() {
		if (this.edit == null) {
			this.edit = new CategoryEditDialog(this, this);
		}
		this.edit.setCategory(this.currentSelectedTimeSliceUsedForMenu
				.getCategory());
		this.showDialog(R.id.menue_edit_category);
	}

	private CategoryEditDialog edit = null;
	private TimeSliceFilterParameter resultRangeFilter = null;

	/**
	 * Result from edit dialog
	 */
	@Override
	public void setCategory(final TimeSliceCategory category) {
		if (category.getRowId() == TimeSliceCategory.NOT_SAVED) {
			this.categoryRepository.createTimeSliceCategory(category);
		} else {
			this.categoryRepository.update(category);
		}
		this.loadDataIntoReport(0);
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case R.id.menue_edit_category:
			return this.edit;
		}

		return null;
	}

	private void onCommandAddTimeSlice() {
		final TimeSlice newSlice = new TimeSlice().setStartTime(
				this.lastSelectedDateUsedForAddMenu).setEndTime(
				this.lastSelectedDateUsedForAddMenu);
		if (this.currentSelectedTimeSliceUsedForMenu != null) {
			newSlice.setCategory(this.currentSelectedTimeSliceUsedForMenu
					.getCategory());
		}
		TimeSliceEditActivity.showTimeSliceEditActivity(this, newSlice,
				R.id.menue_add);
	}

	@Override
	protected List<Object> loadData() {
		long performanceMeasureStart = System.currentTimeMillis();

		final TimeSliceFilterParameter rangeFilter = this.currentRangeFilter;
		final List<TimeSlice> timeSlices = this.timeSliceRepository
				.fetchList(rangeFilter);
		if (Global.isInfoEnabled()) {
			Log.i(Global.LOG_CONTEXT,
					"fetchTimeSlicesByDateRange:"
							+ (System.currentTimeMillis() - performanceMeasureStart));
			performanceMeasureStart = System.currentTimeMillis();
		}

		final List<Object> listItems = this.loadData(timeSlices);
		if (Global.isInfoEnabled()) {
			Log.i(Global.LOG_CONTEXT,
					"Convert Data:"
							+ (System.currentTimeMillis() - performanceMeasureStart));
		}
		return listItems;
	}

	@Override
	public void loadDataIntoReport(final int reportType) {
		final long globalPerformanceMeasureStart = System.currentTimeMillis();
		final List<Object> listItems = this.loadData();

		final int newSelection = this.convertLastSelection(this.getListView(),
				listItems);

		final long performanceMeasureStart = System.currentTimeMillis();

		this.setListAdapter(new TimeSheetReportAdapter(this, listItems,
				this.showNotes));
		if (Global.isInfoEnabled()) {
			Log.i(Global.LOG_CONTEXT,
					"Create adapter:"
							+ (System.currentTimeMillis() - performanceMeasureStart));
		}

		// scroll to end
		this.getListView().post(new Runnable() {
			@Override
			public void run() {
				// Select the last row so it will scroll into view...
				TimeSheetDetailListActivity.this.getListView().setSelection(
						newSelection);
				final float loadTime = 0.0001f * (System.currentTimeMillis() - globalPerformanceMeasureStart);
				TimeSheetDetailListActivity.this
						.setTitle(TimeSheetDetailListActivity.this.currentRangeFilter
								.toString()
								+ " ("
								+ listItems.size()
								+ "/"
								+ String.format("%.1f", loadTime) + " sec)");

			}
		});

	}

	/**
	 * gets data from first visible item and locates it in newListItems.
	 * 
	 * @return last item pos if not found
	 */
	private int convertLastSelection(final ListView listView,
			final List<Object> newListItems) {
		// get old first visible item infos
		final int oldItemCount = listView.getCount();
		if (oldItemCount == 0) {
			return newListItems.size() - 1;
		}
		final int lastListViewTopPos = listView.getFirstVisiblePosition();
		final Object lastListViewTopItem = listView
				.getItemAtPosition(lastListViewTopPos);

		// translate to newListItems position
		int newSelection = (lastListViewTopItem == null) ? -1 : newListItems
				.indexOf(lastListViewTopItem);
		if (newSelection == -1) {
			newSelection = lastListViewTopPos;
		}
		if (newSelection >= newListItems.size()) {
			newSelection = newListItems.size() - 1;
		}
		return newSelection;
	}

	private List<Object> loadData(final List<TimeSlice> timeSlices) {
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
		}

		return items;
	}

	/**
	 * handle result from edit/changeFilter/delete
	 */
	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == TimeSliceRemoveActivity.RESULT_DELETE_OK) {
			this.loadDataIntoReport(0);
		} else {
			if (intent != null) {
				final TimeSlice updatedTimeSlice = (TimeSlice) intent
						.getExtras().get(Global.EXTRA_TIMESLICE);

				if (updatedTimeSlice != null) {
					// after Edit saveNew/updateExisting Timeslice
					if (updatedTimeSlice.getRowId() == ItemWithRowId.IS_NEW_TIMESLICE) {
						this.timeSliceRepository.create(updatedTimeSlice);
					} else {
						this.timeSliceRepository.update(updatedTimeSlice);
					}
				} else if (resultCode == ReportFilterActivity.RESULT_FILTER_CHANGED) {
					// after filter change: remeber new filter
					this.currentRangeFilter = super.onActivityResult(intent,
							this.currentRangeFilter);

					this.resultRangeFilter = this.currentRangeFilter;
					final Intent intent2 = new Intent();
					intent2.putExtra(Global.EXTRA_FILTER,
							this.resultRangeFilter);
					this.setResult(this.idOnOkResultCode, intent2);

				}

				this.loadDataIntoReport(0);
			}
		}
	}

	@Override
	protected String getDefaultReportName() {
		return this.getString(R.string.default_export_ts_name);
	}

	@Override
	protected String getEMailSummaryLine() {
		final String appName = this.getString(R.string.app_name);
		return String.format(this.getString(R.string.default_mail_ts_subject),
				appName);
	}
}
