package com.zettsett.timetracker.activity;

import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.Settings;
import com.zettsett.timetracker.activity.TimeSheetSummaryCalculator2.ReportModes;
import com.zettsett.timetracker.database.TimeSliceCategoryRepsitory;
import com.zettsett.timetracker.database.TimeSliceRepository;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;
import com.zettsett.timetracker.report.IReportInterface;

import de.k3b.util.DateTimeUtil;

public class TimeSheetSummaryListActivity extends ListActivity implements
		IReportInterface, ICategorySetter {
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
	private static final int MENU_ITEM_EDIT_CATEGORY = Menu.FIRST + 6;

	private static final int DELETE_MENU_ID = Menu.FIRST + 20;

	// dependent services
	private ReportFramework reportFramework;
	private final TimeSliceRepository timeSliceRepository = new TimeSliceRepository(
			this, Settings.isPublicDatabase());

	private final TimeSliceCategoryRepsitory categoryRepository = new TimeSliceCategoryRepsitory(
			this);

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

	private TimeSliceCategory currentSelectedCategory;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.time_slice_list);
		this.registerForContextMenu(this.getListView());
		TimeSheetSummaryListActivity.currentRangeFilter = ReportFramework
				.getLastFilter(
						savedInstanceState,
						TimeSheetSummaryListActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME,
						TimeSheetSummaryListActivity.currentRangeFilter);

		this.reportFramework = new ReportFramework(this,
				TimeSheetSummaryListActivity.currentRangeFilter);
		if (savedInstanceState != null) {
			this.reportDateGrouping = (ReportDateGrouping) savedInstanceState
					.getSerializable(TimeSheetSummaryListActivity.SAVED_REPORT_GROUPING_BUNDLE_NAME);
			this.reportMode = (ReportModes) savedInstanceState
					.getSerializable(TimeSheetSummaryListActivity.SAVED_REPORT_MODE);
		}
		this.loadDataIntoReport(this.getIntent().getIntExtra(
				TimeSheetSummaryListActivity.SAVED_MENU_ID_BUNDLE_NAME, 0));

		// scroll to end
		this.getListView().post(new Runnable() {
			@Override
			public void run() {
				// Select the last row so it will scroll into view...
				TimeSheetSummaryListActivity.this.getListView().setSelection(
						TimeSheetSummaryListActivity.this.getListView()
								.getCount() - 1);
			}
		});
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		ReportFramework
				.setLastFilter(
						outState,
						TimeSheetSummaryListActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME,
						TimeSheetSummaryListActivity.currentRangeFilter);
		outState.putSerializable(
				TimeSheetSummaryListActivity.SAVED_REPORT_GROUPING_BUNDLE_NAME,
				this.reportDateGrouping);
		outState.putSerializable(
				TimeSheetSummaryListActivity.SAVED_REPORT_MODE, this.reportMode);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final boolean result = super.onCreateOptionsMenu(menu);
		menu.clear();
		final SubMenu groupDateMenu = menu.addSubMenu(0, Menu.NONE, 0,
				R.string.menu_select_date_grouping);
		groupDateMenu.add(0,
				TimeSheetSummaryListActivity.MENU_ITEM_GROUP_DAILY, 0,
				R.string.menu_select_date_grouping_daily);
		groupDateMenu.add(0,
				TimeSheetSummaryListActivity.MENU_ITEM_GROUP_WEEKLY, 1,
				R.string.menu_select_date_grouping_weekly);
		groupDateMenu.add(0,
				TimeSheetSummaryListActivity.MENU_ITEM_GROUP_MONTHLY, 2,
				R.string.menu_select_date_grouping_monthly);
		groupDateMenu.add(0,
				TimeSheetSummaryListActivity.MENU_ITEM_GROUP_YARLY, 2,
				R.string.menu_select_date_grouping_yearly);
		this.reportFramework.onPrepareOptionsMenu(menu);
		if (this.reportMode == ReportModes.BY_DATE) {
			menu.add(0, TimeSheetSummaryListActivity.MENU_ITEM_GROUP_CATEGORY,
					1, R.string.menu_switch_to_category_headers);
		} else {
			menu.add(0, TimeSheetSummaryListActivity.MENU_ITEM_GROUP_CATEGORY,
					1, R.string.menu_switch_to_date_headers);
		}
		return result;
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
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final int position = ((AdapterContextMenuInfo) menuInfo).position;

		final TimeSliceFilterParameter filter = this.createFilter(position);
		if (filter != null) {
			if (Global.isInfoEnabled()) {
				Log.i(Global.LOG_CONTEXT, "Detailreport: " + filter);
			}
			menu.add(0, TimeSheetSummaryListActivity.MENU_ITEM_REPORT, 0,
					this.getString(R.string.cmd_report));
			menu.add(0, TimeSheetSummaryListActivity.DELETE_MENU_ID, 0,
					this.getString(R.string.cmd_delete));

		}
		this.currentSelectedListItemRangeFilterUsedForMenu = filter;

		this.currentSelectedCategory = this.getTimeSliceCategory(position);
		if (this.currentSelectedCategory != null) {
			menu.add(0, TimeSheetSummaryListActivity.MENU_ITEM_EDIT_CATEGORY,
					0, this.getString(R.string.cmd_edit_category));
		}
	}

	private Object getItemAtPosition(final int position) {
		final Object item = this.getListView().getItemAtPosition(position);
		if (item.getClass().isAssignableFrom(ReportItemWithDuration.class)) {
			return ((ReportItemWithDuration) item).subKey;
		}
		return item;
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_REPORT:
			this.showDetailReport();
			return true;
		case DELETE_MENU_ID:
			this.onCommandDeleteTimeSlice();
			return true;
		case MENU_ITEM_EDIT_CATEGORY:
			this.onCommandEditCategory();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void showDetailReport() {
		if (this.currentSelectedListItemRangeFilterUsedForMenu != null) {
			TimeSheetDetailListActivity.showActivity(this,
					this.currentSelectedListItemRangeFilterUsedForMenu);
		}
	}

	private void onCommandDeleteTimeSlice() {
		TimeSliceRemoveActivity.showActivity(this,
				this.currentSelectedListItemRangeFilterUsedForMenu);
	}

	private void onCommandEditCategory() {
		if (this.edit == null) {
			this.edit = new CategoryEditDialog(this, this);
		}
		this.edit.setCategory(this.currentSelectedCategory);
		this.showDialog(TimeSheetSummaryListActivity.MENU_ITEM_EDIT_CATEGORY);
	}

	private CategoryEditDialog edit = null;

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
		case MENU_ITEM_EDIT_CATEGORY:
			return this.edit;
		}

		return null;
	}

	private TimeSliceFilterParameter createFilter(final int position) {
		TimeSliceFilterParameter filter = null;
		String context = "";
		try {
			TimeSliceCategory category = this.getTimeSliceCategory(position);
			if (category != null) {
				filter = this.createDrillDownFilter().setCategoryId(
						category.getRowId());
				if (this.reportMode == ReportModes.BY_DATE) {
					int pos = position;
					while (--pos >= 0) {
						final Long date = this.getLong(pos);
						if (date != null) {
							context = "ReportModes.BY_DATE currentSelectedCategory + super date";
							return this.setFilterDate(filter,
									this.reportDateGrouping, date);
						}
					}
					context = "ReportModes.BY_DATE currentSelectedCategory. no super date";
				} else {
					context = "ReportModes.BY_CATEGORY currentSelectedCategory";
				}

				return filter.setIgnoreDates(true);
			} else {
				final Long date = this.getLong(position);
				if (date != null) {
					filter = this.setFilterDate(this.createDrillDownFilter(),
							this.reportDateGrouping, date);
					if (this.reportMode == ReportModes.BY_CATEGORY) {
						int pos = position;
						while (--pos >= 0) {
							category = this.getTimeSliceCategory(pos);
							if (category != null) {
								context = "ReportModes.BY_CATEGORY date + super currentSelectedCategory";
								return filter
										.setCategoryId(category.getRowId());
							}
						}
						context = "ReportModes.BY_CATEGORY date. no super currentSelectedCategory";
					} else {
						context = "ReportModes.BY_DATE date";
					}
					return filter;
				}
			}
			context = "Neither currentSelectedCategory nor date selected";
			filter = null;
			return filter;
		} finally {
			if (Global.isDebugEnabled()) {
				Log.d(Global.LOG_CONTEXT, "createFilterFromViewItemTag("
						+ context + ") :" + filter);
			}
		}
	}

	private TimeSliceFilterParameter createDrillDownFilter() {
		final TimeSliceFilterParameter defaults = TimeSheetSummaryListActivity.currentRangeFilter;
		return new TimeSliceFilterParameter().setNotes(defaults.getNotes())
				.setNotesNotNull(defaults.isNotesNotNull());
	}

	private TimeSliceCategory getTimeSliceCategory(final int position) {
		final Object tag = this.getItemAtPosition(position);
		if ((tag != null) && (tag instanceof TimeSliceCategory)) {
			return (TimeSliceCategory) tag;
		}
		return null;
	}

	private Long getLong(final int position) {
		final Object tag = this.getItemAtPosition(position);
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
	public void loadDataIntoReport(final int reportType) {
		long performanceMeasureStart = System.currentTimeMillis();
		final long globalPerformanceMeasureStart = performanceMeasureStart;

		this.setReportType(reportType);

		final TimeSliceFilterParameter rangeFilter = TimeSheetSummaryListActivity.currentRangeFilter;

		final List<TimeSlice> timeSlices = this.timeSliceRepository
				.fetchList(rangeFilter);

		final List<Object> listItems = TimeSheetSummaryCalculator2.loadData(
				this.reportMode, this.reportDateGrouping, timeSlices);

		if (Global.isInfoEnabled()) {
			Log.i(Global.LOG_CONTEXT,
					"loadReportDataStructures:"
							+ (System.currentTimeMillis() - performanceMeasureStart));
			performanceMeasureStart = System.currentTimeMillis();
		}

		final int newSelection = this.convertLastSelection(this.getListView(),
				listItems);

		this.setListAdapter(new TimeSheetReportAdapter(this, listItems,
				this.reportDateGrouping));
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
				TimeSheetSummaryListActivity.this.getListView().setSelection(
						newSelection);
				final float loadTime = 0.0001f * (System.currentTimeMillis() - globalPerformanceMeasureStart);
				TimeSheetSummaryListActivity.this.setTitle(rangeFilter
						.toString()
						+ " ("
						+ timeSlices.size()
						+ "/"
						+ String.format("%.1f", loadTime) + " sec)");
			}
		});

	}

	private void setReportType(final int reportType) {
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

	/**
	 * handle result from edit/changeFilter/delete
	 */
	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null) {
			if (resultCode == ReportFilterActivity.RESULT_FILTER_CHANGED) {
				TimeSheetSummaryListActivity.currentRangeFilter = this.reportFramework
						.onActivityResult(intent,
								TimeSheetSummaryListActivity.currentRangeFilter);
			}
			this.loadDataIntoReport(0);
		}
	}

}
