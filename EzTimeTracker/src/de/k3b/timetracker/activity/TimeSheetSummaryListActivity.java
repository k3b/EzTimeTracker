package de.k3b.timetracker.activity;

import android.app.Dialog;
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

import java.util.List;

import de.k3b.timetracker.DateTimeFormatter;
import de.k3b.timetracker.FileUtilities;
import de.k3b.timetracker.Global;
import de.k3b.timetracker.R;
import de.k3b.timetracker.SendUtilities;
import de.k3b.timetracker.SettingsImpl;
import de.k3b.timetracker.TimeSliceFilterParameter;
import de.k3b.timetracker.database.TimeSliceCategoryRepsitory;
import de.k3b.timetracker.database.TimeSliceRepository;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;
import de.k3b.timetracker.report.CsvSummaryReportRenderer;
import de.k3b.timetracker.report.DurationFormatterAndroid;
import de.k3b.timetracker.report.ExportSettings;
import de.k3b.timetracker.report.ExportSettingsDto;
import de.k3b.timetracker.report.ReportDateGrouping;
import de.k3b.timetracker.report.ReportItemFormatterEx;
import de.k3b.timetracker.report.ReportItemWithStatistics;
import de.k3b.timetracker.report.SummaryReportCalculator;
import de.k3b.timetracker.report.SummaryReportCalculator.ReportModes;
import de.k3b.timetracker.report.TxtReportRenderer;
import de.k3b.util.DateTimeUtil;

/**
 * List Activity with summary report items: cumulated durations by category or
 * date.
 */
public class TimeSheetSummaryListActivity extends BaseReportListActivity
        implements ICategorySetter {
    /**
     * Used to transfer optional report-type from parent activity to this.
     */
    public static final String SAVED_MENU_ID_BUNDLE_NAME = "SAVED_MENU_ID_BUNDLE_NAME";
    private static final String SAVED_REPORT_GROUPING_BUNDLE_NAME = "reportDateGrouping";

    /**
     * Used to transfer optional filter between parent activity and this.
     */
    private static final String SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME = "SummaryReportFilter";
    private static final String SAVED_REPORT_MODE = "reportMode";

    /**
     * current range filter used to fill report.<br/>
     * static to remeber value from last use.
     */
    private static TimeSliceFilterParameter lastRangeFilter;
    private static ExportSettingsDto exportSettings = new ExportSettingsDto();
    // dependent services
    private final TimeSliceRepository timeSliceRepository = new TimeSliceRepository(
            this, SettingsImpl.isPublicDatabase());
    private final TimeSliceCategoryRepsitory categoryRepository = new TimeSliceCategoryRepsitory(
            this);
    // current state
    private ReportModes reportMode = ReportModes.BY_DATE_AND_CATEGORY;
    /**
     * Used in options-menue for context sensitive DrillDownMenue
     */
    private TimeSliceFilterParameter currentSelectedListItemRangeFilterUsedForMenu;
    private TimeSliceCategory currentSelectedCategory;
    private CategoryEditDialog edit = null;
    private ExportSettingsDialog dlgExportSettings = null;

    public TimeSheetSummaryListActivity() {
        this.showNotes = false;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.time_slice_list);
        this.registerForContextMenu(this.getListView());
        this.currentRangeFilter = BaseReportListActivity
                .getLastFilter(
                        this,
                        savedInstanceState,
                        TimeSheetSummaryListActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME,
                        TimeSheetSummaryListActivity.lastRangeFilter);
        TimeSheetSummaryListActivity.lastRangeFilter = this.currentRangeFilter;
        this.setDefaultsToFilterDatesIfNeccesary(this.currentRangeFilter);

        if (savedInstanceState != null) {
            this.setReportDateGrouping((ReportDateGrouping) savedInstanceState
                    .getSerializable(TimeSheetSummaryListActivity.SAVED_REPORT_GROUPING_BUNDLE_NAME));
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
                                .getCount() - 1
                );
            }
        });
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        this.setLastFilter(
                outState,
                TimeSheetSummaryListActivity.SAVED_REPORT_RANGE_FILTER_BUNDLE_NAME,
                this.currentRangeFilter);
        outState.putSerializable(
                TimeSheetSummaryListActivity.SAVED_REPORT_GROUPING_BUNDLE_NAME,
                this.getReportDateGrouping());
        outState.putSerializable(
                TimeSheetSummaryListActivity.SAVED_REPORT_MODE, this.reportMode);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.summaryreport, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    /**
     * called by parent Report Action to append common menuitem.
     */
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        final boolean result = super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_grouping_category)
                .setTitle(
                        (this.reportMode == ReportModes.BY_DATE_AND_CATEGORY) ? R.string.menu_switch_to_category_headers
                                : R.string.menu_switch_to_date_headers
                );
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_grouping_daily:
                this.setReportDateGrouping(ReportDateGrouping.DAILY);
                this.loadDataIntoReport(0);
                break;
            case R.id.menu_grouping_weekly:
                this.setReportDateGrouping(ReportDateGrouping.WEEKLY);
                this.loadDataIntoReport(0);
                break;
            case R.id.menu_grouping_monthly:
                this.setReportDateGrouping(ReportDateGrouping.MONTHLY);
                this.loadDataIntoReport(0);
                break;
            case R.id.menu_grouping_yearly:
                this.setReportDateGrouping(ReportDateGrouping.YEARLY);
                this.loadDataIntoReport(0);
                break;

            case R.id.menu_grouping_category:
                if (this.reportMode == ReportModes.BY_CATEGORY_AND_DATE) {
                    this.reportMode = ReportModes.BY_DATE_AND_CATEGORY;
                } else {
                    this.reportMode = ReportModes.BY_CATEGORY_AND_DATE;
                }
                this.loadDataIntoReport(0);
                break;
            case R.id.menu_export:
                showExportSettingsDialog();
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
                                    final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.context_summary_item, menu);

        final int position = ((AdapterContextMenuInfo) menuInfo).position;

        final TimeSliceFilterParameter filter = this.createFilter(position);
        if (filter != null) {
            if (Global.isInfoEnabled()) {
                Log.i(Global.LOG_CONTEXT, "filter: " + filter);
            }
            menu.findItem(R.id.menue_report).setVisible(true);
            menu.findItem(R.id.menue_delete).setVisible(true);
        }
        this.currentSelectedListItemRangeFilterUsedForMenu = filter;

        this.currentSelectedCategory = this.getTimeSliceCategory(position);
        if (this.currentSelectedCategory != null) {
            menu.findItem(R.id.menue_edit_category).setVisible(true);
        }
    }

    private Object getItemAtPosition(final int position) {
        final Object item = this.getListView().getItemAtPosition(position);
        if (item.getClass().isAssignableFrom(ReportItemWithStatistics.class)) {
            return ((ReportItemWithStatistics) item).getGroupingKey();
        }
        return item;
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menue_report:
                this.showDetailReport();
                return true;
            case R.id.menue_delete:
                this.onCommandDeleteTimeSlice();
                return true;
            case R.id.menue_edit_category:
                this.onCommandEditCategory();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showDetailReport() {
        if (this.currentSelectedListItemRangeFilterUsedForMenu != null) {
            TimeSheetDetailListActivity.showActivity(this,
                    this.currentSelectedListItemRangeFilterUsedForMenu, 0);
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
        this.showDialog(R.id.menue_edit_category);
    }

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

    private TimeSliceFilterParameter createFilter(final int position) {
        TimeSliceFilterParameter filter = null;
        String context = "";
        try {
            TimeSliceCategory category = this.getTimeSliceCategory(position);
            if (category != null) {
                filter = this.createDrillDownFilter().setCategoryId(
                        category.getRowId());
                if (this.reportMode == ReportModes.BY_DATE_AND_CATEGORY) {
                    int pos = position;
                    while (--pos >= 0) {
                        final Long date = this.getLong(pos);
                        if (date != null) {
                            context = "ReportModes.BY_DATE_AND_CATEGORY currentSelectedCategory + super date";
                            return this.setFilterDate(filter,
                                    this.getReportDateGrouping(), date);
                        }
                    }
                    context = "ReportModes.BY_DATE_AND_CATEGORY currentSelectedCategory. no super date";
                } else {
                    context = "ReportModes.BY_CATEGORY_AND_DATE currentSelectedCategory";
                }

                return filter.setIgnoreDates(true);
            } else {
                final Long date = this.getLong(position);
                if (date != null) {
                    filter = this.setFilterDate(this.createDrillDownFilter(),
                            this.getReportDateGrouping(), date);
                    if (this.reportMode == ReportModes.BY_CATEGORY_AND_DATE) {
                        int pos = position;
                        while (--pos >= 0) {
                            category = this.getTimeSliceCategory(pos);
                            if (category != null) {
                                context = "ReportModes.BY_CATEGORY_AND_DATE date + super currentSelectedCategory";
                                return filter
                                        .setCategoryId(category.getRowId());
                            }
                        }
                        context = "ReportModes.BY_CATEGORY_AND_DATE date. no super currentSelectedCategory";
                    } else {
                        context = "ReportModes.BY_DATE_AND_CATEGORY date";
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
        final TimeSliceFilterParameter defaults = this.currentRangeFilter;
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
    protected List<Object> loadData() {
        TimeSheetSummaryListActivity.lastRangeFilter = this.currentRangeFilter;

        final long performanceMeasureStart = System.currentTimeMillis();

        final TimeSliceFilterParameter rangeFilter = this.currentRangeFilter;

        final List<TimeSlice> timeSlices = this.timeSliceRepository
                .fetchList(rangeFilter);

        final List<Object> listItems = SummaryReportCalculator
                .createStatistics(timeSlices, this.reportMode,
                        this.getReportDateGrouping(), this.showNotes);

        if (Global.isInfoEnabled()) {
            Log.i(Global.LOG_CONTEXT,
                    "loadReportDataStructures:"
                            + (System.currentTimeMillis() - performanceMeasureStart)
            );
        }

        return listItems;
    }

    @Override
    public void loadDataIntoReport(final int reportType) {
        final long globalPerformanceMeasureStart = System.currentTimeMillis();
        this.setReportType(reportType);

        final List<Object> listItems = this.loadData();

        final long performanceMeasureStart = System.currentTimeMillis();

        final int newSelection = this.convertLastSelection(this.getListView(),
                listItems);

        this.setListAdapter(new TimeSheetReportAdapter(this, listItems,
                this.showNotes, this.getReportDateGrouping()));
        if (Global.isInfoEnabled()) {
            Log.i(Global.LOG_CONTEXT,
                    "Create adapter:"
                            + (System.currentTimeMillis() - performanceMeasureStart)
            );
        }

        // scroll to end
        this.getListView().post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                TimeSheetSummaryListActivity.this.getListView().setSelection(
                        newSelection);
                final float loadTime = 0.0001f * (System.currentTimeMillis() - globalPerformanceMeasureStart);
                TimeSheetSummaryListActivity.this
                        .setTitle(TimeSheetSummaryListActivity.this.currentRangeFilter
                                .toString()
                                + " ("
                                + listItems.size()
                                + "/"
                                + String.format("%.1f", loadTime) + " sec)");
            }
        });

    }

    private void setReportType(final int reportType) {
        switch (reportType) {
            case R.id.summary_day:
                this.setReportDateGrouping(ReportDateGrouping.DAILY);
                this.reportMode = ReportModes.BY_DATE_AND_CATEGORY;
                break;
            case R.id.summary_month:
                this.setReportDateGrouping(ReportDateGrouping.MONTHLY);
                this.reportMode = ReportModes.BY_DATE_AND_CATEGORY;
                break;
            case R.id.summary_week:
                this.setReportDateGrouping(ReportDateGrouping.WEEKLY);
                this.reportMode = ReportModes.BY_DATE_AND_CATEGORY;
                break;
            case R.id.category_day:
                this.setReportDateGrouping(ReportDateGrouping.DAILY);
                this.reportMode = ReportModes.BY_CATEGORY_AND_DATE;
                break;
            case R.id.category_month:
                this.setReportDateGrouping(ReportDateGrouping.MONTHLY);
                this.reportMode = ReportModes.BY_CATEGORY_AND_DATE;
                break;
            case R.id.category_week:
                this.setReportDateGrouping(ReportDateGrouping.WEEKLY);
                this.reportMode = ReportModes.BY_CATEGORY_AND_DATE;
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
                this.currentRangeFilter = super.onActivityResult(intent,
                        this.currentRangeFilter);
            }
            this.loadDataIntoReport(0);
        }
    }

    @Override
    protected String getDefaultReportName() {
        return this.getString(R.string.default_export_sum_name);
    }

    @Override
    protected String getEMailSummaryLine() {
        final String appName = this.getString(R.string.app_name);
        return String.format(this.getString(R.string.default_mail_sum_subject),
                appName);
    }

    private void showExportSettingsDialog() {
        if (dlgExportSettings == null) {
            dlgExportSettings = new ExportSettingsDialog(this, exportSettings, this);
        }
        dlgExportSettings.show();
    }

    private String createReport(String reportType) {
        ReportItemFormatterEx formatter = new ReportItemFormatterEx(new DurationFormatterAndroid(this), this.getReportDateGrouping(), this.showNotes);
        List<Object> data = this.loadData();
        if (reportType.toLowerCase().startsWith("c")) {
            return new CsvSummaryReportRenderer(formatter, this.showNotes).createReport(data);
        } else {
            return new TxtReportRenderer(formatter).createReport(data);
        }
    }

    @Override
    public void onExport(ExportSettings setting) {
        ExportSettingsDto.copy(exportSettings, setting);

        String exportFormat = exportSettings.getExportFormat();
        String report = createReport(exportFormat);
        if (exportSettings.isUseSendTo()) {
            SendUtilities.send("", this.getEMailSummaryLine(), this, report);
        } else {
            new FileUtilities(this).write(exportSettings.getFileName(), exportFormat, report);
        }
    }
}
