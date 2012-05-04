package com.zettsett.timetracker.activity;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DateTimeMinuteSlider;
import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.database.DatabaseInstance;
import com.zettsett.timetracker.database.TimeSliceDBAdapter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

public abstract class FilterActivity  extends Activity {

	protected static final int GET_END_DATETIME = 0;
	protected static final int GET_START_DATETIME = 1;
	
	protected static final DatabaseInstance CURRENT_DB_INSTANCE = DatabaseInstance.getCurrentInstance();
	protected TimeSliceDBAdapter mTimeSliceDBAdapter;
	protected Button mTimeInButton;
	protected Button mTimeOutButton;
	protected Spinner catSpinner;
	protected CheckBox allDatesCheckBox ;

	protected FilterParameter filter = null;
	
    // define the listener which is called once a user selected the date.
	protected DateSlider.OnDateSetListener mDateTimeSetListenerStart =
        new DateSlider.OnDateSetListener() {
			public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
				filter.setStartTime(selectedDate.getTimeInMillis());
        		mTimeInButton.setText(getFormattedStartTime());
            }
    };

    // define the listener which is called once a user selected the date.
    protected DateSlider.OnDateSetListener mDateTimeSetListenerEnd =
        new DateSlider.OnDateSetListener() {
			public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
				filter.setEndTime(selectedDate.getTimeInMillis());
        		mTimeOutButton.setText(getFormattedEndTime());
            }
    };
	private int idCmdOk;
	private int idCaption;

	public FilterActivity(int idCaption, int idCmdOk) {
		this.idCaption = idCaption;
		this.idCmdOk = idCmdOk;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.filter = FilterActivity.getFilterParameter(this);
		
		setContentView(R.layout.remove_ts);
		setTitle(this.idCaption);
		CURRENT_DB_INSTANCE.initialize(this);
		mTimeSliceDBAdapter = new TimeSliceDBAdapter(this);
		Button okButton = (Button) findViewById(R.id.cmd_delete);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onOkCLick();
			}
		});
		okButton.setText(this.idCmdOk);
		Button cancelButton = (Button) findViewById(R.id.button_remove_ts_cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		allDatesCheckBox = (CheckBox) findViewById(R.id.checkbox_remove_ts_all);
		allDatesCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateTimeFilter();
			}
		});

		catSpinner = (Spinner) findViewById(R.id.spinnerEditTimeSliceCategory);
		catSpinner.setAdapter( TimeSliceCategory.getCategoryAdapter(this, TimeSliceCategory.NO_CATEGORY));
		FilterActivity.selectSpinner(catSpinner, filter.getCategoryId());

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
		
		if (filter.getEndTime() == TimeSlice.NO_TIME_VALUE && filter.getStartTime() == TimeSlice.NO_TIME_VALUE)
		{
			allDatesCheckBox.setChecked(true);
		}
		updateTimeFilter();
	}

	private void updateTimeFilter() {
		LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayout_remove_ts_dates);
		if (allDatesCheckBox.isChecked()) {
			ll.setVisibility(View.GONE);
			this.filter.setIgnoreDates(true);
		} else {
			ll.setVisibility(View.VISIBLE);
			this.filter.setIgnoreDates(false);
			if (this.filter.getStartTime() == TimeSlice.NO_TIME_VALUE)
				this.filter.setStartTime(System.currentTimeMillis());
			if (this.filter.getEndTime() == TimeSlice.NO_TIME_VALUE)
				this.filter.setEndTime(System.currentTimeMillis());
		}
		
		mTimeInButton.setText(getFormattedStartTime());
		mTimeOutButton.setText(getFormattedEndTime());
	}

 
    
   
	private String getFormattedEndTime() {
		return getFormattedTime(R.string.formatEndDate, filter.getEndTime(), "");
	}

	private String getFormattedStartTime() {
		return getFormattedTime(R.string.formatStartDate, 
				filter.getStartTime(), "");
	}

	private String getFormattedTime(int idFormat, long dateTimeValue, String emptyReplacement) {
		String dateTimeStr = DateTimeFormatter.getDateTimeStr(dateTimeValue, emptyReplacement);
		return String.format(this.getText(idFormat).toString(), 
				dateTimeStr);
	}

	protected String getStatusMessage(int idFormatMessage) {
		String ignoreText = getText(R.string.filter_ignore).toString();
		String categoryName =ignoreText;

		TimeSliceCategory selectedCategory = (TimeSliceCategory)catSpinner.getSelectedItem();
		
		if ((selectedCategory != null) && (selectedCategory != TimeSliceCategory.NO_CATEGORY))
		{
			categoryName = selectedCategory.getCategoryName();
		}
		
		boolean ignoreDates = this.filter.isIgnoreDates();
		String startTime = getFormattedTime(R.string.formatStartDate, ignoreDates ? TimeSlice.NO_TIME_VALUE : filter.getStartTime(), ignoreText);
		String endTime = getFormattedTime(R.string.formatEndDate, ignoreDates ? TimeSlice.NO_TIME_VALUE : filter.getEndTime(), ignoreText);
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
        	c.setTimeInMillis(filter.getStartTime());
            return new DateTimeMinuteSlider(this,mDateTimeSetListenerStart,c);
        case GET_END_DATETIME:
        	c.setTimeInMillis(filter.getEndTime());
            return new DateTimeMinuteSlider(this,mDateTimeSetListenerEnd,c);
        }
        return null;
    }
	
	public static FilterParameter getFilterParameter(Activity activity) {
		FilterParameter filter = (FilterParameter) activity.getIntent().getExtras().get(Global.EXTRA_FILTER);
		if (filter == null)
			filter = new FilterParameter();
		return filter;
	}
	
	public static void selectSpinner(Spinner catSpinner , TimeSliceCategory currentCategory) {
		int currentCategoryID = (currentCategory != null) ? currentCategory.getRowId() : TimeSliceCategory.NOT_SAVED;
		selectSpinner(catSpinner, currentCategoryID);
	}

	public static void selectSpinner(Spinner catSpinner, int currentCategoryID) {
		for (int position = 0; position < catSpinner.getCount(); position++) {

			if (((TimeSliceCategory) catSpinner.getItemAtPosition(position)).getRowId() == currentCategoryID) {
				catSpinner.setSelection(position);
				break;
			}
		}
	}	
	
	protected abstract void onOkCLick();
}
