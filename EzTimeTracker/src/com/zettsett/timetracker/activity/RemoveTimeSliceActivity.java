package com.zettsett.timetracker.activity;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateTimeMinuteSlider;
import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.database.DatabaseInstance;
import com.zettsett.timetracker.database.TimeSliceDBAdapter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class RemoveTimeSliceActivity extends Activity {
	protected static final int GET_END_DATETIME = 0;
	protected static final int GET_START_DATETIME = 1;
	
	private static final DatabaseInstance CURRENT_DB_INSTANCE = DatabaseInstance.getCurrentInstance();
	private TimeSliceDBAdapter mTimeSliceDBAdapter;
	private boolean mRemoveAll = false;
	private Button mTimeInButton;
	private Button mTimeOutButton;
	private Spinner catSpinner;

    static private long mStartTime = new Date().getTime();
    static private long mEndTime = new Date().getTime();
	
    // define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateTimeSetListenerStart =
        new DateSlider.OnDateSetListener() {
			public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
            	mStartTime = selectedDate.getTimeInMillis();
        		mTimeInButton.setText(getFormattedStartTime());
            }
    };

    // define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateTimeSetListenerEnd =
        new DateSlider.OnDateSetListener() {
			public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
            	mEndTime = selectedDate.getTimeInMillis();
        		mTimeOutButton.setText(getFormattedEndTime());
            }
    };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remove_ts);
		setTitle(R.string.label_delete_time_interval_data);
		CURRENT_DB_INSTANCE.initialize(this);
		mTimeSliceDBAdapter = new TimeSliceDBAdapter(this);
		Button removalButton = (Button) findViewById(R.id.cmd_delete);
		removalButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmRemoval();
			}
		});
		Button cancelButton = (Button) findViewById(R.id.button_remove_ts_cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		final CheckBox allDatesCheckBox = (CheckBox) findViewById(R.id.checkbox_remove_ts_all);
		allDatesCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayout_remove_ts_dates);
				if (allDatesCheckBox.isChecked()) {
					ll.setVisibility(View.GONE);
					mRemoveAll = true;
				} else {
					ll.setVisibility(View.VISIBLE);
					mRemoveAll = false;
				}
			}
		});

		catSpinner = (Spinner) findViewById(R.id.spinnerEditTimeSliceCategory);
		catSpinner.setAdapter( TimeSliceCategory.getCategoryAdapter(this, TimeSliceCategory.NO_CATEGORY));

		mTimeInButton = (Button) findViewById(R.id.EditTimeIn);
		mTimeInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(GET_START_DATETIME);
			}
		});
		mTimeOutButton = (Button) findViewById(R.id.EditTimeOut);
		mTimeOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(GET_END_DATETIME);
			}
		});
		
		mTimeInButton.setText(getFormattedStartTime());
		mTimeOutButton.setText(getFormattedEndTime());
	}

	private void doRemove() {
		TimeSliceCategory selectedCategory = (TimeSliceCategory) catSpinner.getSelectedItem();
		
		long selectedCategoryID = (selectedCategory != null) ? selectedCategory.getRowId() : TimeSliceCategory.NOT_SAVED;

		mTimeSliceDBAdapter.deleteForDateRange(
				(mRemoveAll) ? TimeSlice.NO_TIME_VALUE : mStartTime, 
				(mRemoveAll) ? TimeSlice.NO_TIME_VALUE : mEndTime,
				selectedCategoryID);
		String message = getStatusMessage(R.string.format_message_interval_deleted);
		Toast.makeText(
				getApplicationContext(),
				message, Toast.LENGTH_LONG).show();
		finish();
	}

	private String getStatusMessage(int idFormatMessage) {
		String ignoreText = getText(R.string.filter_ignore).toString();
		String categoryName =ignoreText;

		TimeSliceCategory selectedCategory = (TimeSliceCategory)catSpinner.getSelectedItem();
		
		if ((selectedCategory != null) && (selectedCategory != TimeSliceCategory.NO_CATEGORY))
		{
			categoryName = selectedCategory.getCategoryName();
		}
		
		String startTime = getFormattedTime(R.string.formatStartDate, (mRemoveAll) ? TimeSlice.NO_TIME_VALUE : mStartTime, ignoreText);
		String endTime = getFormattedTime(R.string.formatEndDate, (mRemoveAll) ? TimeSlice.NO_TIME_VALUE : mEndTime, ignoreText);
		return String.format(getString(idFormatMessage).toString(),
				startTime,
				endTime,
				categoryName
				);
	}

    @Override
    protected Dialog onCreateDialog(int id) {
        // this method is called after invoking 'showDialog' for the first time
        // here we initiate the corresponding DateSlideSelector and return the dialog to its caller
    	
    	// get today's date and time
        final Calendar c = Calendar.getInstance();
        
        switch (id) {
        case GET_START_DATETIME:
        	c.setTimeInMillis(mStartTime);
            return new DateTimeMinuteSlider(this,mDateTimeSetListenerStart,c);
        case GET_END_DATETIME:
        	c.setTimeInMillis(mEndTime);
            return new DateTimeMinuteSlider(this,mDateTimeSetListenerEnd,c);
        }
        return null;
    }

	private String getFormattedEndTime() {
		return getFormattedTime(R.string.formatEndDate, mEndTime, "");
	}

	private String getFormattedStartTime() {
		return getFormattedTime(R.string.formatStartDate, 
				mStartTime, "");
	}

	private String getFormattedTime(int idFormat, long dateTimeValue, String emptyReplacement) {
		String dateTimeStr = DateTimeFormatter.getDateTimeStr(dateTimeValue, emptyReplacement);
		return String.format(this.getText(idFormat).toString(), 
				dateTimeStr);
	}
	
	public void confirmRemoval() {
		String message = getStatusMessage(R.string.format_question_delete_time_intervals);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.title_confirm_removal);
		builder.setMessage(message).setCancelable(false).setPositiveButton(R.string.btn_yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						doRemove();
					}
				}).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

}
