package com.zettsett.timetracker.activity;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateTimeMinuteSlider;
import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.TimeTrackerManager;
import com.zettsett.timetracker.database.TimeSliceCategoryRepsitory;
import com.zettsett.timetracker.database.TimeSliceRepository;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class TimeSliceEditActivity extends Activity  implements CategorySetter {
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_time_slice);
		Intent intent = getIntent();
		TimeSlice timeSlice = (TimeSlice) intent.getExtras().get(Global.EXTRA_TIMESLICE);
		initialize(timeSlice);
	}

	private void initialize(TimeSlice timeSlice) {
		setContentView(R.layout.edit_time_slice);
		this.timeSlice = timeSlice;
		
		notesEditText = (EditText) findViewById(R.id.edit_text_ts_notes);
		if (HIDDEN_NOTES.equals(this.timeSlice.getNotes())) {
			notesEditText.setVisibility(View.INVISIBLE);
			findViewById(R.id.LabelNotes).setVisibility(View.INVISIBLE);
		}
		
		catSpinner = (Spinner) findViewById(R.id.spinnerEditTimeSliceCategory);
		if (this.timeSlice.getCategoryId() != HIDDEN) {
			catSpinner.setAdapter( TimeSliceCategory.getCategoryAdapter(this, TimeSliceCategory.NO_CATEGORY, TimeSliceCategory.MIN_VALID_DATE));
			
			TimeSliceCategory currentCategory = this.timeSlice.getCategory();
			
			if (currentCategory == null) {
				currentCategory =(TimeSliceCategory) catSpinner.getAdapter().getItem(0);
			}
			FilterActivity.selectSpinner(catSpinner, currentCategory);
		
			catSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> paramAdapterView,
						View paramView, int paramInt, long paramLong) {
					TimeSliceCategory newCategory = (TimeSliceCategory) catSpinner.getSelectedItem();
					setCategory(newCategory);
				}
	
				@Override
				public void onNothingSelected(AdapterView<?> paramAdapterView) {
					// TODO Auto-generated method stub
					
				}
			});
		} else {
			catSpinner.setVisibility(View.INVISIBLE);
		}

		mTimeInButton = (Button) findViewById(R.id.EditTimeIn);
		if (this.timeSlice.getStartTime() != HIDDEN) {
			mTimeInButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog(GET_START_DATETIME);
				}
			});
			mTimeInButton.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					showDialog(GET_START_DATETIME_NOW);
					return true;
				}
			});
		} else {
			mTimeInButton.setVisibility(View.INVISIBLE);
		}

		mTimeOutButton = (Button) findViewById(R.id.EditTimeOut);
		if (this.timeSlice.getEndTime() != HIDDEN) {
			mTimeOutButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog(GET_END_DATETIME);
				}
			});
			mTimeOutButton.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					showDialog(GET_END_DATETIME_NOW);
					return true;
				}
			});
		} else {
			mTimeOutButton.setVisibility(View.INVISIBLE);
		}
		
		Button saveButton = (Button) findViewById(R.id.ButtonSaveTimeSlice);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimeSliceEditActivity.this.timeSlice.setNotes(notesEditText.getText().toString());
				if (validate()) {
					Intent intent = new Intent();
					intent.putExtra(Global.EXTRA_TIMESLICE, TimeSliceEditActivity.this.timeSlice);
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		});
		Button cancelButton = (Button) findViewById(R.id.ButtonCancelTimeSlice);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		setTimeTexts();
	}

    // define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateTimeSetListenerStart =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
            	timeSlice.setStartTime(selectedDate.getTimeInMillis());
        		setTimeTexts();
            }
    };

    // define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateTimeSetListenerEnd =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
            	timeSlice.setEndTime(selectedDate.getTimeInMillis());
        		setTimeTexts();
            }
    };

	private CategoryEditDialog edit = null;
	public void showCategoryEditDialog(TimeSliceCategory category)
	{
		if (this.edit == null)
		{
			this.edit = new CategoryEditDialog(this, this);
		}
		this.edit.setCategory(category);
		showDialog(EDIT_CATEGORY_ID);
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
        // this method is called after invoking 'showDialog' for the first time
        // here we initiate the corresponding DateSlideSelector and return the dialog to its caller
    	
    	// get today's date and time
        final Calendar c = Calendar.getInstance();
        
		TimeSliceEditActivity.this.timeSlice.setNotes(notesEditText.getText().toString());

        switch (id) {
        case GET_START_DATETIME:
        	c.setTimeInMillis(this.timeSlice.getStartTime());
            return new DateTimeMinuteSlider(this,mDateTimeSetListenerStart,c);
        case GET_START_DATETIME_NOW:
        	c.setTimeInMillis(TimeTrackerManager.currentTimeMillis());
            return new DateTimeMinuteSlider(this,mDateTimeSetListenerStart,c);
        case GET_END_DATETIME:
        	c.setTimeInMillis(this.timeSlice.getEndTime());
            return new DateTimeMinuteSlider(this,mDateTimeSetListenerEnd,c);
        case GET_END_DATETIME_NOW:
        	c.setTimeInMillis(TimeTrackerManager.currentTimeMillis());
            return new DateTimeMinuteSlider(this,mDateTimeSetListenerEnd,c);
		case EDIT_CATEGORY_ID:
			return this.edit;
        }
        return null;
    }

	private boolean validate() {
		long endTime = this.timeSlice.getEndTime();
		if ((endTime != HIDDEN) && (endTime < this.timeSlice.getStartTime())) {
			Toast.makeText(getApplicationContext(),
					"Invalid input: end time must be after start time.", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	private void setTimeTexts() {
		String label = String.format(this.getText(R.string.formatStartDate).toString(), 
				DateTimeFormatter.getDateTimeStr(this.timeSlice.getStartTime()));
		mTimeInButton.setText(label);

		label = String.format(this.getText(R.string.formatEndDate).toString(), 
				DateTimeFormatter.getDateTimeStr(this.timeSlice.getEndTime()));
		mTimeOutButton.setText(label);
		
		setTitle(this.timeSlice.getStartDateStr());
		this.notesEditText.setText(this.timeSlice.getNotes());
	}

	@Override
	public void setCategory(TimeSliceCategory newCategory) {
		if (newCategory == TimeSliceCategory.NO_CATEGORY)
		{
			// selected item to create new category "?"
			showCategoryEditDialog(null);
		} else if (newCategory.getRowId() == TimeSliceCategory.NOT_SAVED) {
			// result of create new category
			
			TimeSliceCategoryRepsitory categoryRepository = new TimeSliceCategoryRepsitory(
					this);
			categoryRepository.createTimeSliceCategory(newCategory);
			ArrayAdapter<TimeSliceCategory> categoryAdapter = TimeSliceCategory.getCategoryAdapter(this, TimeSliceCategory.NO_CATEGORY, TimeSliceCategory.MIN_VALID_DATE);
			catSpinner.setAdapter( categoryAdapter);		
			int newPosition = categoryAdapter.getPosition(newCategory);
			catSpinner.setSelection(newPosition);
			this.timeSlice.setCategory(newCategory);
		} else {
			this.timeSlice.setCategory(newCategory);
		}
	}

	public static void showTimeSliceEditActivity(Activity parentActivity, int rowId, int requestCode) 
	{
		TimeSlice timeSlice = TimeSliceRepository.getTimeSliceDBAdapter(parentActivity).fetchByRowID(rowId);
		showTimeSliceEditActivity(parentActivity, timeSlice, requestCode);
	}

	private static TimeSliceCategory lastCategory = TimeSliceCategory.NO_CATEGORY;
	
	public static void showTimeSliceEditActivity(
			Activity parentActivity, TimeSlice timeSlice, int requestCode) 
	{
		if (timeSlice != null)
		{
			if (timeSlice.getCategoryId() != TimeSliceCategory.NOT_SAVED)
			{
				lastCategory = timeSlice.getCategory();
			} else {
				timeSlice.setCategory(lastCategory);
			}
		}
		
		Intent indent = new Intent(parentActivity, TimeSliceEditActivity.class);
		indent.putExtra(Global.EXTRA_TIMESLICE, timeSlice);
		parentActivity.startActivityForResult(indent, requestCode);
	}

}
