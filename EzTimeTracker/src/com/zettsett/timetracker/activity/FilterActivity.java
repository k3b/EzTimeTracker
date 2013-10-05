package com.zettsett.timetracker.activity;

import java.util.Calendar;

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
import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.TimeTrackerManager;
import com.zettsett.timetracker.database.DatabaseInstance;
import com.zettsett.timetracker.database.TimeSliceRepository;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

public abstract class FilterActivity extends Activity {

	// const
	protected static final int GET_END_DATETIME = 0;
	protected static final int GET_START_DATETIME = 1;
	protected static final int GET_END_DATETIME_NOW = 2;
	protected static final int GET_START_DATETIME_NOW = 3;

	protected static final DatabaseInstance CURRENT_DB_INSTANCE = DatabaseInstance
			.getCurrentInstance();

	// define the listener which is called once a user selected the date.
	protected final DateSlider.OnDateSetListener listenerOnStartDateChanged = new DateSlider.OnDateSetListener() {
		@Override
		public void onDateSet(final DateSlider view, final Calendar selectedDate) {
			FilterActivity.this.saveStartTime(FilterActivity.this.filter,
					selectedDate.getTimeInMillis());
			FilterActivity.this.loadForm(FilterActivity.this.filter);
		}
	};

	// define the listener which is called once a user selected the date.
	protected final DateSlider.OnDateSetListener listenerOnEndDateChanged = new DateSlider.OnDateSetListener() {
		@Override
		public void onDateSet(final DateSlider view, final Calendar selectedDate) {
			FilterActivity.this.saveEndTime(FilterActivity.this.filter,
					selectedDate.getTimeInMillis());
			FilterActivity.this.loadForm(FilterActivity.this.filter);
		}
	};

	// controlls
	protected LinearLayout datesContainer;

	protected Spinner categorySpinner;
	protected CheckBox allDatesCheckBox;
	protected Button timeInButton;
	protected Button timeOutButton;
	protected CheckBox notesNotNullCheckBox;
	protected EditText notesEdit;

	// context infos
	private final int idOnOkResultCode;
	private final int idCmdOk;
	private final int idCaption;

	protected TimeSliceRepository timeSliceRepository;
	private ArrayAdapter<TimeSliceCategory> allCategoriesAdapter;

	protected FilterParameter filter = null;

	public FilterActivity(final int idCaption, final int idCmdOk,
			final int idOnOkResultCode) {
		this.idCaption = idCaption;
		this.idCmdOk = idCmdOk;
		this.idOnOkResultCode = idOnOkResultCode;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.filter = FilterActivity.getFilterParameter(this);

		FilterActivity.CURRENT_DB_INSTANCE.initialize(this);
		this.timeSliceRepository = new TimeSliceRepository(this);

		this.setContentView(R.layout.time_slice_filter);
		this.setTitle(this.idCaption);

		this.defineButtons();

		this.allDatesCheckBox = (CheckBox) this
				.findViewById(R.id.checkbox_filter_ignore_date);
		this.allDatesCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				FilterActivity.this.saveForm(FilterActivity.this.filter);
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
						FilterActivity.this
								.saveForm(FilterActivity.this.filter);
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

		if ((this.filter.getEndTime() == TimeSlice.NO_TIME_VALUE)
				&& (this.filter.getStartTime() == TimeSlice.NO_TIME_VALUE)) {
			this.allDatesCheckBox.setChecked(true);
		}

		this.saveForm(this.filter);
	}

	private void defineButtons() {
		final Button okButton = (Button) this.findViewById(R.id.cmd_delete);
		okButton.setText(this.idCmdOk);
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
	private void loadForm(final FilterParameter filter) {
		CategorySpinner.selectSpinner(this.categorySpinner,
				this.filter.getCategoryId());

		this.datesContainer.setVisibility((filter.isIgnoreDates()) ? View.GONE
				: View.VISIBLE);
		this.timeInButton.setText(this.getFormattedStartTime());
		this.timeOutButton.setText(this.getFormattedEndTime());

		this.notesEdit.setVisibility(filter.isNotesNotNull() ? View.GONE
				: View.VISIBLE);
		filter.setNotes(this.notesEdit.getText().toString());
	}

	/**
	 * save content of this from controls to filter
	 */
	private void saveForm(final FilterParameter filter) {
		filter.setIgnoreDates(this.allDatesCheckBox.isChecked());

		// start/end time is saved somewhere else !!!
		if (filter.getStartTime() == TimeSlice.NO_TIME_VALUE) {
			filter.setStartTime(System.currentTimeMillis());
		}
		if (filter.getEndTime() == TimeSlice.NO_TIME_VALUE) {
			filter.setEndTime(System.currentTimeMillis());
		}

		this.notesEdit
				.setVisibility(this.notesNotNullCheckBox.isChecked() ? View.GONE
						: View.VISIBLE);
		filter.setNotes(this.notesEdit.getText().toString());
		filter.setNotesNotNull(this.notesNotNullCheckBox.isChecked());
	}

	private void saveStartTime(final FilterParameter filter, final long time) {
		filter.setStartTime((time == TimeSlice.NO_TIME_VALUE) ? System
				.currentTimeMillis() : time);
	}

	private void saveEndTime(final FilterParameter filter, final long time) {
		filter.setEndTime((time == TimeSlice.NO_TIME_VALUE) ? System
				.currentTimeMillis() : time);
	}

	private String getFormattedEndTime() {
		return this.getFormattedTime(R.string.formatEndDate,
				this.filter.getEndTime(), "");
	}

	private String getFormattedStartTime() {
		return this.getFormattedTime(R.string.formatStartDate,
				this.filter.getStartTime(), "");
	}

	private String getFormattedTime(final int idFormat,
			final long dateTimeValue, final String emptyReplacement) {
		final String dateTimeStr = DateTimeFormatter.getInstance()
				.getDateTimeStr(dateTimeValue, emptyReplacement);
		return String.format(this.getText(idFormat).toString(), dateTimeStr);
	}

	protected String getStatusMessage(final int idFormatMessage) {
		final String ignoreText = this.getText(R.string.filter_ignore)
				.toString();
		String categoryName = ignoreText;

		final TimeSliceCategory selectedCategory = this.getCurrentCategory();

		if ((selectedCategory != null)
				&& (selectedCategory != TimeSliceCategory.NO_CATEGORY)) {
			categoryName = selectedCategory.getCategoryName();
		}

		final boolean ignoreDates = this.filter.isIgnoreDates();
		final String startTime = this.getFormattedTime(
				R.string.formatStartDate, ignoreDates ? TimeSlice.NO_TIME_VALUE
						: this.filter.getStartTime(), ignoreText);
		final String endTime = this.getFormattedTime(
				R.string.formatEndDate,
				ignoreDates ? TimeSlice.NO_TIME_VALUE : this.filter
						.getEndTime(), ignoreText);
		return String.format(this.getString(idFormatMessage).toString(),
				startTime, endTime, categoryName);
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

	protected static FilterParameter getFilterParameter(final Activity activity) {
		FilterParameter filter = (FilterParameter) activity.getIntent()
				.getExtras().get(Global.EXTRA_FILTER);
		if (filter == null) {
			filter = new FilterParameter();
		}
		return filter;
	}

	protected void onOkCLick() {
		final TimeSliceCategory currentCategory = this.getCurrentCategory();
		final int currentCategoryID = CategorySpinner
				.getCategoryId(currentCategory);
		this.filter.setCategoryId(currentCategoryID);
	}

	@Override
	public void finish() {
		final Intent intent = new Intent();
		intent.putExtra(Global.EXTRA_FILTER, this.filter);
		this.setResult(this.idOnOkResultCode, intent);
		super.finish();
	}

	protected TimeSliceCategory getCurrentCategory() {
		return (TimeSliceCategory) this.categorySpinner.getSelectedItem();
	}
}
