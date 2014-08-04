package de.k3b.timetracker.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateTimeMinuteSlider;

import java.util.Calendar;

import de.k3b.timetracker.DateTimeFormatter;
import de.k3b.timetracker.Global;
import de.k3b.timetracker.R;
import de.k3b.timetracker.TimeSliceFilterParameter;
import de.k3b.timetracker.TimeTrackerManager;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;

public abstract class FilterActivity extends Activity {

    // const
    private static final int GET_END_DATETIME = 0;
    private static final int GET_START_DATETIME = 1;
    private static final int GET_END_DATETIME_NOW = 2;
    private static final int GET_START_DATETIME_NOW = 3;

    // context infos
    private final int idOnOkResultCode;
    private final int textIdCmdOk;
    private final int textIdCaption;
    protected TimeSliceFilterParameter filter = null;
    // define the listener which is called once a user selected the date.
    private final DateSlider.OnDateSetListener listenerOnStartDateChanged = new DateSlider.OnDateSetListener() {
        @Override
        public void onDateSet(final DateSlider view, final Calendar selectedDate) {
            FilterActivity.this.saveForm(FilterActivity.this.filter);
            FilterActivity.this.filter.setStartTime(FilterActivity.this
                    .fixTime(selectedDate.getTimeInMillis()));
            FilterActivity.this.loadForm(FilterActivity.this.filter);
        }
    };
    // define the listener which is called once a user selected the date.
    private final DateSlider.OnDateSetListener listenerOnEndDateChanged = new DateSlider.OnDateSetListener() {
        @Override
        public void onDateSet(final DateSlider view, final Calendar selectedDate) {
            FilterActivity.this.saveForm(FilterActivity.this.filter);
            FilterActivity.this.filter.setEndTime(FilterActivity.this
                    .fixTime(selectedDate.getTimeInMillis()));
            FilterActivity.this.loadForm(FilterActivity.this.filter);
        }
    };
    // controlls
    private LinearLayout datesContainer;
    private Spinner categorySpinner;
    private CheckBox allDatesCheckBox;
    // save & cancel button are local
    private Button timeInButton;
    private Button timeOutButton;
    private CheckBox notesNotNullCheckBox;
    private EditText notesEdit;
    private ArrayAdapter<TimeSliceCategory> allCategoriesAdapter;

    public FilterActivity(final int textIdCaption, final int textIdCmdOk,
                          final int idOnOkResultCode) {
        this.textIdCaption = textIdCaption;
        this.textIdCmdOk = textIdCmdOk;
        this.idOnOkResultCode = idOnOkResultCode;
    }

    protected static TimeSliceFilterParameter getFilterParameter(
            final Activity activity) {
        TimeSliceFilterParameter filter = (TimeSliceFilterParameter) activity
                .getIntent().getExtras().get(Global.EXTRA_FILTER);
        if (filter == null) {
            filter = new TimeSliceFilterParameter();
        }
        return filter;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.filter = FilterActivity.getFilterParameter(this);

        this.setContentView(R.layout.time_slice_filter);
        this.setTitle(this.textIdCaption);

        this.defineButtons();

        this.allDatesCheckBox = (CheckBox) this
                .findViewById(R.id.checkbox_filter_ignore_date);
        this.allDatesCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                FilterActivity.this.updateForm();
            }

        });
        this.datesContainer = (LinearLayout) this
                .findViewById(R.id.dates_container);
        this.notesEdit = (EditText) this.findViewById(R.id.edit_text_ts_notes);

        this.notesNotNullCheckBox = (CheckBox) this
                .findViewById(R.id.checkbox_notes_not_null);
        this.notesNotNullCheckBox
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        FilterActivity.this.updateForm();
                    }
                });

        this.categorySpinner = (Spinner) this
                .findViewById(R.id.spinnerEditTimeSliceCategory);
        final long loadReferenceDate = TimeSliceCategory.MIN_VALID_DATE;

        this.allCategoriesAdapter = CategoryListAdapterSimple.createAdapter(
                this, TimeSliceCategory.NO_CATEGORY, loadReferenceDate,
                "FilterActivity");
        this.categorySpinner.setAdapter(this.allCategoriesAdapter);

        this.timeInButton = (Button) this.findViewById(R.id.EditTimeIn);
        this.timeInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                FilterActivity.this
                        .showDialog(FilterActivity.GET_START_DATETIME);
            }
        });
        this.timeInButton
                .setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {
                        FilterActivity.this
                                .showDialog(FilterActivity.GET_START_DATETIME_NOW);
                        return true;
                    }
                });
        this.timeOutButton = (Button) this.findViewById(R.id.EditTimeOut);
        this.timeOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                FilterActivity.this.showDialog(FilterActivity.GET_END_DATETIME);
            }
        });
        this.timeOutButton
                .setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {
                        FilterActivity.this
                                .showDialog(FilterActivity.GET_END_DATETIME_NOW);
                        return true;
                    }
                });

        this.loadForm(this.filter);
    }

    private void defineButtons() {
        final Button okButton = (Button) this.findViewById(R.id.cmd_delete);
        okButton.setText(this.textIdCmdOk);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                FilterActivity.this.onOkCLick();
            }
        });
        final Button cancelButton = (Button) this
                .findViewById(R.id.button_remove_ts_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                FilterActivity.this.finish();
            }
        });
    }

    /**
     * Loads this from controls from filter.
     */
    private void loadForm(final TimeSliceFilterParameter filter) {
        CategorySpinner.selectSpinner(this.categorySpinner,
                filter.getCategoryId());

        boolean ignoreDates = filter.isIgnoreDates();
        if ((filter.getEndTime() == TimeSlice.NO_TIME_VALUE)
                && (filter.getStartTime() == TimeSlice.NO_TIME_VALUE)) {
            ignoreDates = true;
        }
        this.allDatesCheckBox.setChecked(ignoreDates);
        this.datesContainer.setVisibility(ignoreDates ? View.GONE
                : View.VISIBLE);
        this.timeInButton.setText(this.getFormattedTime(
                R.string.formatStartDate, filter.getStartTime(), ""));
        this.timeOutButton.setText(this.getFormattedTime(
                R.string.formatEndDate, filter.getEndTime(), ""));

        this.notesNotNullCheckBox.setChecked(filter.isNotesNotNull());
        this.notesEdit.setVisibility(filter.isNotesNotNull() ? View.GONE
                : View.VISIBLE);
        this.notesEdit.setText(filter.getNotes());
    }

    /**
     * save content of this from controls to filter
     */
    private void saveForm(final TimeSliceFilterParameter filter) {
        filter.setCategoryId(CategorySpinner.getCategoryId(this
                .getCurrentCategory()));

        filter.setIgnoreDates(this.allDatesCheckBox.isChecked());

        // start/end time is saved somewhere else !!!

        filter.setNotesNotNull(this.notesNotNullCheckBox.isChecked());
        filter.setNotes(this.notesEdit.getText().toString());
    }

    private void updateForm() {
        FilterActivity.this.saveForm(FilterActivity.this.filter);
        FilterActivity.this.loadForm(FilterActivity.this.filter);
    }

    private long fixTime(final long time) {
        return (time == TimeSlice.NO_TIME_VALUE) ? System.currentTimeMillis()
                : time;
    }

    private String getFormattedTime(final int idFormat,
                                    final long dateTimeValue, final String emptyReplacement) {
        final String dateTimeStr = DateTimeFormatter.getInstance()
                .getDateTimeStr(dateTimeValue, emptyReplacement);
        return String.format(this.getText(idFormat).toString(), dateTimeStr);
    }

    protected String getStatusMessage(final int idFormatMessage) {
        return String.format(this.getString(idFormatMessage).toString(),
                this.filter.toString(this.getCurrentCategory()));
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        // this method is called after invoking 'showDialog' for the first time
        // here we initiate the corresponding DateSlideSelector and return the
        // dialog to its caller

        // get today's date and time
        final Calendar c = Calendar.getInstance();

        switch (id) {
            case GET_START_DATETIME:
                c.setTimeInMillis(this.filter.getStartTime());
                return new DateTimeMinuteSlider(this,
                        this.listenerOnStartDateChanged, c);
            case GET_START_DATETIME_NOW:
                c.setTimeInMillis(TimeTrackerManager.currentTimeMillis());
                return new DateTimeMinuteSlider(this,
                        this.listenerOnStartDateChanged, c);
            case GET_END_DATETIME:
                c.setTimeInMillis(this.filter.getEndTime());
                return new DateTimeMinuteSlider(this,
                        this.listenerOnEndDateChanged, c);
            case GET_END_DATETIME_NOW:
                c.setTimeInMillis(TimeTrackerManager.currentTimeMillis());
                return new DateTimeMinuteSlider(this,
                        this.listenerOnEndDateChanged, c);
        }
        return null;
    }

    protected void onOkCLick() {
        this.saveForm(this.filter);
        final Intent intent = this.getFinishIntent();
        this.setResult(this.idOnOkResultCode, intent);
        this.finish();
    }

    /**
     * @return Override with result intent with extra-paramaters for the caller
     */
    protected Intent getFinishIntent() {
        final Intent intent = new Intent();
        intent.putExtra(Global.EXTRA_FILTER, this.filter);
        return intent;
    }

    protected TimeSliceCategory getCurrentCategory() {
        return (TimeSliceCategory) this.categorySpinner.getSelectedItem();
    }
}
