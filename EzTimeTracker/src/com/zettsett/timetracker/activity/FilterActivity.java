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

public abstract class FilterActivity  extends Activity {

	protected static final int GET_END_DATETIME = 0;
	protected static final int GET_START_DATETIME = 1;
	protected static final int GET_END_DATETIME_NOW = 2;
	protected static final int GET_START_DATETIME_NOW = 3;
	
	protected static final DatabaseInstance CURRENT_DB_INSTANCE = DatabaseInstance.getCurrentInstance();
	protected TimeSliceRepository mTimeSliceRepository;
	protected Button mTimeInButton;
	protected Button mTimeOutButton;
	protected Spinner mCatSpinner;
	protected CheckBox mAllDatesCheckBox ;

	private  final int mFilterResultCodeOnOk;
	protected FilterParameter mFilter = null;
	
    // define the listener which is called once a user selected the date.
	protected DateSlider.OnDateSetListener mDateTimeSetListenerStart =
        new DateSlider.OnDateSetListener() {
			public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
				mFilter.setStartTime(selectedDate.getTimeInMillis());
        		mTimeInButton.setText(getFormattedStartTime());
            }
    };

    // define the listener which is called once a user selected the date.
    protected DateSlider.OnDateSetListener mDateTimeSetListenerEnd =
        new DateSlider.OnDateSetListener() {
			public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
				mFilter.setEndTime(selectedDate.getTimeInMillis());
        		mTimeOutButton.setText(getFormattedEndTime());
            }
    };
	private int mIdCmdOk;
	private int mIdCaption;
	private ArrayAdapter<TimeSliceCategory> allCategoriesAdapter;

	public FilterActivity(int idCaption, int idCmdOk, int filterResultCodeOnOk) {
		this.mIdCaption = idCaption;
		this.mIdCmdOk = idCmdOk;
		this.mFilterResultCodeOnOk = filterResultCodeOnOk;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.mFilter = FilterActivity.getFilterParameter(this);
		
		setContentView(R.layout.remove_ts);
		setTitle(this.mIdCaption);
		CURRENT_DB_INSTANCE.initialize(this);
		mTimeSliceRepository = new TimeSliceRepository(this);
		Button okButton = (Button) findViewById(R.id.cmd_delete);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onOkCLick();
			}
		});
		okButton.setText(this.mIdCmdOk);
		Button cancelButton = (Button) findViewById(R.id.button_remove_ts_cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mAllDatesCheckBox = (CheckBox) findViewById(R.id.checkbox_remove_ts_all);
		mAllDatesCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateTimeFilter();
			}
		});

		mCatSpinner = (Spinner) findViewById(R.id.spinnerEditTimeSliceCategory);
		this.allCategoriesAdapter=TimeSliceCategory.getCategoryAdapter(this, TimeSliceCategory.NO_CATEGORY, TimeSliceCategory.MIN_VALID_DATE); 
		mCatSpinner.setAdapter( this.allCategoriesAdapter );
		FilterActivity.selectSpinner(mCatSpinner, mFilter.getCategoryId());

		mTimeInButton = (Button) findViewById(R.id.EditTimeIn);
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
		mTimeOutButton = (Button) findViewById(R.id.EditTimeOut);
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
		
		if (mFilter.getEndTime() == TimeSlice.NO_TIME_VALUE && mFilter.getStartTime() == TimeSlice.NO_TIME_VALUE)
		{
			mAllDatesCheckBox.setChecked(true);
		}
		updateTimeFilter();
	}

	private void updateTimeFilter() {
		LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayout_remove_ts_dates);
		if (mAllDatesCheckBox.isChecked()) {
			ll.setVisibility(View.GONE);
			this.mFilter.setIgnoreDates(true);
		} else {
			ll.setVisibility(View.VISIBLE);
			this.mFilter.setIgnoreDates(false);
			if (this.mFilter.getStartTime() == TimeSlice.NO_TIME_VALUE)
				this.mFilter.setStartTime(System.currentTimeMillis());
			if (this.mFilter.getEndTime() == TimeSlice.NO_TIME_VALUE)
				this.mFilter.setEndTime(System.currentTimeMillis());
		}
		
		mTimeInButton.setText(getFormattedStartTime());
		mTimeOutButton.setText(getFormattedEndTime());
	}

 
    
   
	private String getFormattedEndTime() {
		return getFormattedTime(R.string.formatEndDate, mFilter.getEndTime(), "");
	}

	private String getFormattedStartTime() {
		return getFormattedTime(R.string.formatStartDate, 
				mFilter.getStartTime(), "");
	}

	private String getFormattedTime(int idFormat, long dateTimeValue, String emptyReplacement) {
		String dateTimeStr = DateTimeFormatter.getDateTimeStr(dateTimeValue, emptyReplacement);
		return String.format(this.getText(idFormat).toString(), 
				dateTimeStr);
	}

	protected String getStatusMessage(int idFormatMessage) {
		String ignoreText = getText(R.string.filter_ignore).toString();
		String categoryName =ignoreText;

		TimeSliceCategory selectedCategory = getCurrentCategory();
		
		if ((selectedCategory != null) && (selectedCategory != TimeSliceCategory.NO_CATEGORY))
		{
			categoryName = selectedCategory.getCategoryName();
		}
		
		boolean ignoreDates = this.mFilter.isIgnoreDates();
		String startTime = getFormattedTime(R.string.formatStartDate, ignoreDates ? TimeSlice.NO_TIME_VALUE : mFilter.getStartTime(), ignoreText);
		String endTime = getFormattedTime(R.string.formatEndDate, ignoreDates ? TimeSlice.NO_TIME_VALUE : mFilter.getEndTime(), ignoreText);
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
        	c.setTimeInMillis(mFilter.getStartTime());
            return new DateTimeMinuteSlider(this,mDateTimeSetListenerStart,c);
        case GET_START_DATETIME_NOW:
        	c.setTimeInMillis(TimeTrackerManager.currentTimeMillis());
            return new DateTimeMinuteSlider(this,mDateTimeSetListenerStart,c);
        case GET_END_DATETIME:
        	c.setTimeInMillis(mFilter.getEndTime());
            return new DateTimeMinuteSlider(this,mDateTimeSetListenerEnd,c);
        case GET_END_DATETIME_NOW:
        	c.setTimeInMillis(TimeTrackerManager.currentTimeMillis());
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
		int currentCategoryID = getCategoryId(currentCategory);
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
	
	protected void onOkCLick()
	{
		TimeSliceCategory currentCategory = getCurrentCategory();
		int currentCategoryID = getCategoryId(currentCategory);
		mFilter.setCategoryId(currentCategoryID);		
	}

	@Override public void finish()
	{
		Intent intent = new Intent();
		intent.putExtra(Global.EXTRA_FILTER, mFilter);
		setResult(mFilterResultCodeOnOk, intent);
		super.finish();
	}
	
	protected static int getCategoryId(TimeSliceCategory category) {
		return (category != null) ? category.getRowId() : TimeSliceCategory.NOT_SAVED;
	}

	protected TimeSliceCategory getCurrentCategory() {
		return (TimeSliceCategory)mCatSpinner.getSelectedItem();
	}
}
