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
import com.zettsett.timetracker.database.TimeSliceCategoryDBAdapter;
import com.zettsett.timetracker.database.TimeSliceDBAdapter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class TimeSliceEditActivity extends Activity  implements CategorySetter {
	protected static final int GET_END_DATETIME = 0;
	protected static final int GET_START_DATETIME = 1;
	private static final int EDIT_CATEGORY_ID = 99;
	
	private TimeSlice mTimeSlice;
	private Button mTimeInButton;
	private Button mTimeOutButton;
	private Spinner catSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_time_slice);
		int rowId = getIntent().getIntExtra("row_id", TimeSlice.IS_NEW_TIMESLICE);
		long date = getIntent().getLongExtra("date", TimeSlice.NO_TIME_VALUE);
		initialize(rowId, date);
	}

	private void initialize(int rowId, long date) {
		setContentView(R.layout.edit_time_slice);
		catSpinner = (Spinner) findViewById(R.id.spinnerEditTimeSliceCategory);
		catSpinner.setAdapter( TimeSliceCategory.getCategoryAdapter(this, TimeSliceCategory.NO_CATEGORY));
		if (rowId != TimeSlice.IS_NEW_TIMESLICE) {
			mTimeSlice = TimeSliceDBAdapter.getTimeSliceDBAdapter(this).fetchByRowID(rowId);
			TimeSliceCategory currentCategory = mTimeSlice
					.getCategory();
			FilterActivity.selectSpinner(catSpinner, currentCategory);
		} else {
			mTimeSlice = new TimeSlice();
			mTimeSlice.setStartTime(date);
			mTimeSlice.setEndTime(date);
			mTimeSlice.setCategory((TimeSliceCategory) catSpinner.getAdapter().getItem(0));
			mTimeSlice.setRowId(TimeSlice.IS_NEW_TIMESLICE);
		}
		
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

		mTimeInButton = (Button) findViewById(R.id.EditTimeIn);
		mTimeInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mTimeSlice.setNotes((getNotesEditText()).getText().toString());
				showDialog(GET_START_DATETIME);
			}
		});
		mTimeOutButton = (Button) findViewById(R.id.EditTimeOut);
		mTimeOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mTimeSlice.setNotes((getNotesEditText()).getText().toString());
				showDialog(GET_END_DATETIME);
			}
		});
		Button saveButton = (Button) findViewById(R.id.ButtonSaveTimeSlice);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mTimeSlice.setNotes((getNotesEditText()).getText().toString());
				if (validate()) {
					Intent intent = new Intent();
					Bundle b = new Bundle();
					b.putSerializable(Global.EXTRA_FORMAT, mTimeSlice);
					intent.putExtra(Global.EXTRA_DATA, b);
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
            	mTimeSlice.setStartTime(selectedDate.getTimeInMillis());
        		setTimeTexts();
            }
    };

    // define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateTimeSetListenerEnd =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
            	mTimeSlice.setEndTime(selectedDate.getTimeInMillis());
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
        
        switch (id) {
        case GET_START_DATETIME:
        	c.setTimeInMillis(mTimeSlice.getStartTime());
            return new DateTimeMinuteSlider(this,mDateTimeSetListenerStart,c);
        case GET_END_DATETIME:
        	c.setTimeInMillis(mTimeSlice.getEndTime());
            return new DateTimeMinuteSlider(this,mDateTimeSetListenerEnd,c);
		case EDIT_CATEGORY_ID:
			return this.edit;
        }
        return null;
    }

	private EditText getNotesEditText() {
		return (EditText) findViewById(R.id.edit_text_ts_notes);
	}

	private boolean validate() {
		if (mTimeSlice.getEndTime() < mTimeSlice.getStartTime()) {
			Toast.makeText(getApplicationContext(),
					"Invalid input: end time must be after start time.", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	private void setTimeTexts() {
		String label = String.format(this.getText(R.string.formatStartDate).toString(), 
				DateTimeFormatter.getDateTimeStr(mTimeSlice.getStartTime()));
		mTimeInButton.setText(label);

		label = String.format(this.getText(R.string.formatEndDate).toString(), 
				DateTimeFormatter.getDateTimeStr(mTimeSlice.getEndTime()));
		mTimeOutButton.setText(label);
		
		setTitle(mTimeSlice.getStartDateStr());
		getNotesEditText().setText(mTimeSlice.getNotes());
	}

	@Override
	public void setCategory(TimeSliceCategory newCategory) {
		if (newCategory == TimeSliceCategory.NO_CATEGORY)
		{
			// selected item to create new category "?"
			showCategoryEditDialog(null);
		} else if (newCategory.getRowId() == TimeSliceCategory.NOT_SAVED) {
			// result of create new category
			
			TimeSliceCategoryDBAdapter timeSliceCategoryDBAdapter = new TimeSliceCategoryDBAdapter(
					this);
			timeSliceCategoryDBAdapter.createTimeSliceCategory(newCategory);
			ArrayAdapter<TimeSliceCategory> categoryAdapter = TimeSliceCategory.getCategoryAdapter(this, TimeSliceCategory.NO_CATEGORY);
			catSpinner.setAdapter( categoryAdapter);		
			int newPosition = categoryAdapter.getPosition(newCategory);
			catSpinner.setSelection(newPosition);
			mTimeSlice.setCategory(newCategory);
		} else {
			mTimeSlice.setCategory(newCategory);
		}
	}
}
