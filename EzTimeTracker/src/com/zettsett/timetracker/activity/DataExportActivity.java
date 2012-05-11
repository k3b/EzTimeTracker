package com.zettsett.timetracker.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TableLayout;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DefaultDateSlider;
import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.EmailUtilities;
import com.zettsett.timetracker.FileUtilities;
import com.zettsett.timetracker.database.TimeSliceDBAdapter;
import com.zettsett.timetracker.model.TimeSlice;

public class DataExportActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
	private static final int GET_START_DATETIME = 0;
	private static final int GET_END_DATETIME = 1;

	private static final String CSV_LINE_SEPERATOR = "\n";
	private static final String CSV_FIELD_SEPERATOR = ",";
	
	private RadioGroup mRadioGroup;
	private boolean mExportAll = true;
	private long mFromDate, mToDate;
	private boolean mEmailData = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.export_data_to_csv_file);
		setContentView(R.layout.data_export);
		initializeDateRanges();
		mRadioGroup = (RadioGroup) findViewById(R.id.radio_group_data_export);
		mRadioGroup.setOnCheckedChangeListener(this);
		Button exportButton = (Button) findViewById(R.id.button_data_export);
		exportButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				writeData();
			}
		});
		
		Button fromButton = (Button) findViewById(R.id.button_data_export_from);
		fromButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(GET_START_DATETIME);
			}
		});
		Button toButton = (Button) findViewById(R.id.button_data_export_to);
		toButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(GET_END_DATETIME);
			}
		});
		final CheckBox emailCheckBox = (CheckBox) findViewById(R.id.checkbox_data_export_email);
		emailCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LinearLayout ll = (LinearLayout) findViewById(R.id.linear_layout_data_export_filename);
				if (emailCheckBox.isChecked()) {
					ll.setVisibility(View.GONE);
					mEmailData = true;
				} else {
					ll.setVisibility(View.VISIBLE);
					mEmailData = false;
				}
			}
		});
		assignFromToDateLabels();
	}

	private EditText getFilenameEditText() {
		return (EditText) findViewById(R.id.edit_text_data_export_filename);
	}

	private void initializeDateRanges() {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR, 23);
		calendar.set(Calendar.MINUTE, 59);
		mToDate = calendar.getTimeInMillis();
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.roll(Calendar.MONTH, false);
		mFromDate = calendar.getTimeInMillis();
	}

	private void assignFromToDateLabels() {
		Button fromButton = (Button) findViewById(R.id.button_data_export_from);
		String label = String.format(this.getText(R.string.formatStartDate).toString(), 
				DateTimeFormatter.getShortDateStr(mFromDate));
		fromButton.setText(label);

		Button toButton = (Button) findViewById(R.id.button_data_export_to);
		label = String.format(this.getText(R.string.formatEndDate).toString(), 
				DateTimeFormatter.getShortDateStr(mToDate));
		toButton.setText(label);
	}

    // define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateTimeSetListenerStart =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
            	mFromDate = selectedDate.getTimeInMillis();
    			assignFromToDateLabels();
            }
    };

    // define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateTimeSetListenerEnd =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
            	mToDate = selectedDate.getTimeInMillis();
    			assignFromToDateLabels();
            }
    };

    @Override
    public Dialog onCreateDialog(int id) {
        // this method is called after invoking 'showDialog' for the first time
        // here we initiate the corresponding DateSlideSelector and return the dialog to its caller
    	
    	// get today's date and time
        final Calendar c = Calendar.getInstance();
        
        switch (id) {
        case GET_START_DATETIME:
        	c.setTimeInMillis(mFromDate);
            return new DefaultDateSlider(this,mDateTimeSetListenerStart,c);
        case GET_END_DATETIME:
        	c.setTimeInMillis(mToDate);
            return new DefaultDateSlider(this,mDateTimeSetListenerEnd,c);
        }
        return null;
    }

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		TableLayout dateRangeLayout = (TableLayout) findViewById(R.id.table_layout_date_range);
		if (checkedId == R.id.radio_button_data_export_range) {
			dateRangeLayout.setVisibility(View.VISIBLE);
			mExportAll = false;
		} else {
			dateRangeLayout.setVisibility(View.GONE);
			mExportAll = true;
		}
	}

	private void writeData() {
		TimeSliceDBAdapter mTimeSliceDBAdapter = new TimeSliceDBAdapter(this);
		List<TimeSlice> timeSlices;

		FilterParameter filter = new FilterParameter().setStartTime(mFromDate).setEndTime(mToDate);
		timeSlices = mTimeSliceDBAdapter.fetchTimeSlices(filter, mExportAll);
		StringBuilder output = new StringBuilder();
		output.append("Start, End, Category Name, Category Description, Notes").append(CSV_LINE_SEPERATOR);
		for (TimeSlice aTimeSlice : timeSlices) {
			output.append(DateTimeFormatter.getIsoDateTimeStr(aTimeSlice.getStartTime())).append(CSV_FIELD_SEPERATOR);
			output.append(DateTimeFormatter.getIsoDateTimeStr(aTimeSlice.getEndTime())).append(CSV_FIELD_SEPERATOR);
			output.append(aTimeSlice.getCategoryName()).append(CSV_FIELD_SEPERATOR);
			output.append(aTimeSlice.getCategoryDescription()).append(CSV_FIELD_SEPERATOR);
			output.append(aTimeSlice.getNotes().replace(CSV_LINE_SEPERATOR, " "));
			output.append(CSV_LINE_SEPERATOR);
		}
		if (mEmailData) {
			String appName = getString(R.string.app_name);
			String subject = String.format(getString(R.string.export_email_subject), appName);

			EmailUtilities.send("", subject, this, output
					.toString());
		} else {
			FileUtilities fileUtil = new FileUtilities(this);
			fileUtil.write(getFilenameEditText().getText().toString(), output.toString());
		}
	}
}
