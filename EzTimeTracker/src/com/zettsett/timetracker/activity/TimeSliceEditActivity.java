package com.zettsett.timetracker.activity;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateTimeMinuteSlider;
import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.Settings;
import com.zettsett.timetracker.TimeTrackerManager;
import com.zettsett.timetracker.database.TimeSliceCategoryRepsitory;
import com.zettsett.timetracker.database.TimeSliceRepository;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class TimeSliceEditActivity extends Activity implements ICategorySetter {
	public static final long HIDDEN = -5;
	public static final String HIDDEN_NOTES = "!%&HIDDEN&%!";

	protected static final int GET_END_DATETIME = 0;
	protected static final int GET_START_DATETIME = 1;
	protected static final int GET_END_DATETIME_NOW = 2;
	protected static final int GET_START_DATETIME_NOW = 3;
	private static final int EDIT_CATEGORY_ID = 99;

	private Button mTimeInButton;
	private Button mTimeOutButton;
	private Spinner catSpinner;
	private EditText notesEditText;

	private TimeSlice timeSlice;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = this.getIntent();
		final TimeSlice timeSlice = (TimeSlice) intent.getExtras().get(
				Global.EXTRA_TIMESLICE);
		this.initialize(timeSlice);
	}

	private void initialize(final TimeSlice timeSlice) {
		this.setContentView(R.layout.time_slice_edit);
		this.timeSlice = timeSlice;

		this.notesEditText = (EditText) this
				.findViewById(R.id.edit_text_ts_notes);
		if (TimeSliceEditActivity.HIDDEN_NOTES
				.equals(this.timeSlice.getNotes())) {
			this.notesEditText.setVisibility(View.INVISIBLE);
			this.findViewById(R.id.LabelNotes).setVisibility(View.INVISIBLE);
		}

		this.catSpinner = (Spinner) this
				.findViewById(R.id.spinnerEditTimeSliceCategory);
		if (this.timeSlice.getCategoryId() != TimeSliceEditActivity.HIDDEN) {
			this.catSpinner.setAdapter(this.createCategoryAdapter(this));

			TimeSliceCategory currentCategory = this.timeSlice.getCategory();

			if (currentCategory == null) {
				currentCategory = (TimeSliceCategory) this.catSpinner
						.getAdapter().getItem(0);
			}
			CategorySpinner.selectSpinner(this.catSpinner, currentCategory);

			this.catSpinner
					.setOnItemSelectedListener(new OnItemSelectedListener() {
						@Override
						public void onItemSelected(
								final AdapterView<?> paramAdapterView,
								final View paramView, final int paramInt,
								final long paramLong) {
							final TimeSliceCategory newCategory = (TimeSliceCategory) TimeSliceEditActivity.this.catSpinner
									.getSelectedItem();
							TimeSliceEditActivity.this.setCategory(newCategory);
						}

						@Override
						public void onNothingSelected(
								final AdapterView<?> paramAdapterView) {
						}
					});
		} else {
			this.catSpinner.setVisibility(View.INVISIBLE);
		}

		this.mTimeInButton = (Button) this.findViewById(R.id.EditTimeIn);
		if (this.timeSlice.getStartTime() != TimeSliceEditActivity.HIDDEN) {
			this.mTimeInButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					TimeSliceEditActivity.this
							.showDialog(TimeSliceEditActivity.GET_START_DATETIME);
				}
			});
			this.mTimeInButton
					.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(final View v) {
							TimeSliceEditActivity.this
									.showDialog(TimeSliceEditActivity.GET_START_DATETIME_NOW);
							return true;
						}
					});
		} else {
			this.mTimeInButton.setVisibility(View.INVISIBLE);
		}

		this.mTimeOutButton = (Button) this.findViewById(R.id.EditTimeOut);
		if (this.timeSlice.getEndTime() != TimeSliceEditActivity.HIDDEN) {
			this.mTimeOutButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					TimeSliceEditActivity.this
							.showDialog(TimeSliceEditActivity.GET_END_DATETIME);
				}
			});
			this.mTimeOutButton
					.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(final View v) {
							TimeSliceEditActivity.this
									.showDialog(TimeSliceEditActivity.GET_END_DATETIME_NOW);
							return true;
						}
					});
		} else {
			this.mTimeOutButton.setVisibility(View.INVISIBLE);
		}

		final Button saveButton = (Button) this
				.findViewById(R.id.ButtonSaveTimeSlice);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				TimeSliceEditActivity.this.timeSlice
						.setNotes(TimeSliceEditActivity.this.notesEditText
								.getText().toString());
				if (TimeSliceEditActivity.this.validate()) {
					final Intent intent = new Intent();
					intent.putExtra(Global.EXTRA_TIMESLICE,
							TimeSliceEditActivity.this.timeSlice);
					TimeSliceEditActivity.this.setResult(Activity.RESULT_OK,
							intent);
					TimeSliceEditActivity.this.finish();
				}
			}
		});
		final Button cancelButton = (Button) this
				.findViewById(R.id.ButtonCancelTimeSlice);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				TimeSliceEditActivity.this.finish();
			}
		});
		this.setTimeTexts();
	}

	private ArrayAdapter<TimeSliceCategory> createCategoryAdapter(
			final TimeSliceEditActivity timeSliceEditActivity) {
		long loadReferenceDate = (Settings.getHideInactiveCategories()) ? TimeTrackerManager
				.currentTimeMillis() : TimeSliceCategory.MIN_VALID_DATE;

		final TimeSliceCategory currentCategory = this.timeSlice.getCategory();

		if (!currentCategory.isActive(loadReferenceDate)) {
			loadReferenceDate = TimeSliceCategory.MIN_VALID_DATE;
		}

		// return CategoryListAdapterDetailed.createAdapter(this,
		// R.layout.category_list_view_row, false,
		// TimeSliceCategory.NO_CATEGORY,
		// loadReferenceDate, "TimeSliceEditActivity");
		return CategoryListAdapterSimple.createAdapter(this,
				TimeSliceCategory.NO_CATEGORY, loadReferenceDate,
				"TimeSliceEditActivity");
	}

	// define the listener which is called once a user selected the date.
	private final DateSlider.OnDateSetListener mDateTimeSetListenerStart = new DateSlider.OnDateSetListener() {
		@Override
		public void onDateSet(final DateSlider view, final Calendar selectedDate) {
			// update the dateText view with the corresponding date
			TimeSliceEditActivity.this.timeSlice.setStartTime(selectedDate
					.getTimeInMillis());
			if (TimeSliceEditActivity.this.timeSlice.getStartTime() > TimeSliceEditActivity.this.timeSlice
					.getEndTime()) {
				TimeSliceEditActivity.this.timeSlice
						.setEndTime(TimeSliceEditActivity.this.timeSlice
								.getStartTime());
			}
			TimeSliceEditActivity.this.setTimeTexts();
		}
	};

	// define the listener which is called once a user selected the date.
	private final DateSlider.OnDateSetListener mDateTimeSetListenerEnd = new DateSlider.OnDateSetListener() {
		@Override
		public void onDateSet(final DateSlider view, final Calendar selectedDate) {
			// update the dateText view with the corresponding date
			TimeSliceEditActivity.this.timeSlice.setEndTime(selectedDate
					.getTimeInMillis());
			if (TimeSliceEditActivity.this.timeSlice.getStartTime() > TimeSliceEditActivity.this.timeSlice
					.getEndTime()) {
				TimeSliceEditActivity.this.timeSlice
						.setStartTime(TimeSliceEditActivity.this.timeSlice
								.getEndTime());
			}

			TimeSliceEditActivity.this.setTimeTexts();
		}
	};

	private CategoryEditDialog edit = null;

	public void showCategoryEditDialog(final TimeSliceCategory category) {
		if (this.edit == null) {
			this.edit = new CategoryEditDialog(this, this);
		}
		this.edit.setCategory(category);
		this.showDialog(TimeSliceEditActivity.EDIT_CATEGORY_ID);
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		// this method is called after invoking 'showDialog' for the first time
		// here we initiate the corresponding DateSlideSelector and return the
		// dialog to its caller

		// get today's date and time
		final Calendar c = Calendar.getInstance();

		TimeSliceEditActivity.this.timeSlice.setNotes(this.notesEditText
				.getText().toString());

		switch (id) {
		case GET_START_DATETIME:
			c.setTimeInMillis(this.timeSlice.getStartTime());
			return new DateTimeMinuteSlider(this,
					this.mDateTimeSetListenerStart, c);
		case GET_START_DATETIME_NOW:
			c.setTimeInMillis(TimeTrackerManager.currentTimeMillis());
			return new DateTimeMinuteSlider(this,
					this.mDateTimeSetListenerStart, c);
		case GET_END_DATETIME:
			c.setTimeInMillis(this.timeSlice.getEndTime());
			return new DateTimeMinuteSlider(this, this.mDateTimeSetListenerEnd,
					c);
		case GET_END_DATETIME_NOW:
			c.setTimeInMillis(TimeTrackerManager.currentTimeMillis());
			return new DateTimeMinuteSlider(this, this.mDateTimeSetListenerEnd,
					c);
		case EDIT_CATEGORY_ID:
			return this.edit;
		}
		return null;
	}

	private boolean validate() {
		final long endTime = this.timeSlice.getEndTime();
		if ((endTime != TimeSliceEditActivity.HIDDEN)
				&& (endTime < this.timeSlice.getStartTime())) {
			Toast.makeText(this.getApplicationContext(),
					"Invalid input: end time must be after start time.",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	private void setTimeTexts() {
		String label = String.format(
				this.getText(R.string.formatStartDate).toString(),
				DateTimeFormatter.getInstance().getDateTimeStr(
						this.timeSlice.getStartTime()));
		this.mTimeInButton.setText(label);

		label = String.format(
				this.getText(R.string.formatEndDate).toString(),
				DateTimeFormatter.getInstance().getDateTimeStr(
						this.timeSlice.getEndTime()));
		this.mTimeOutButton.setText(label);

		this.setTitle(this.timeSlice.getStartDateStr());
		this.notesEditText.setText(this.timeSlice.getNotes());
	}

	@Override
	public void setCategory(final TimeSliceCategory newCategory) {
		if (newCategory == TimeSliceCategory.NO_CATEGORY) {
			// selected item to create new category "?"
			this.showCategoryEditDialog(null);
		} else if (newCategory.getRowId() == TimeSliceCategory.NOT_SAVED) {
			// result of create new category

			final TimeSliceCategoryRepsitory categoryRepository = new TimeSliceCategoryRepsitory(
					this);
			categoryRepository.createTimeSliceCategory(newCategory);
			final ArrayAdapter<TimeSliceCategory> categoryAdapter = this
					.createCategoryAdapter(this);
			this.catSpinner.setAdapter(categoryAdapter);
			final int newPosition = categoryAdapter.getPosition(newCategory);
			this.catSpinner.setSelection(newPosition);
			this.timeSlice.setCategory(newCategory);
		} else {
			this.timeSlice.setCategory(newCategory);
		}
	}

	public static void showTimeSliceEditActivity(final Activity parentActivity,
			final int rowId, final int requestCode) {
		final TimeSlice timeSlice = TimeSliceRepository.getDBAdapter(
				parentActivity).fetchByRowID(rowId);
		TimeSliceEditActivity.showTimeSliceEditActivity(parentActivity,
				timeSlice, requestCode);
	}

	private static TimeSliceCategory lastCategory = TimeSliceCategory.NO_CATEGORY;

	public static void showTimeSliceEditActivity(final Activity parentActivity,
			final TimeSlice timeSlice, final int requestCode) {
		if (timeSlice != null) {
			if (timeSlice.getCategoryId() != TimeSliceCategory.NOT_SAVED) {
				TimeSliceEditActivity.lastCategory = timeSlice.getCategory();
			} else {
				timeSlice.setCategory(TimeSliceEditActivity.lastCategory);
			}
		}

		final Intent indent = new Intent(parentActivity,
				TimeSliceEditActivity.class);
		indent.putExtra(Global.EXTRA_TIMESLICE, timeSlice);
		parentActivity.startActivityForResult(indent, requestCode);
	}

}
