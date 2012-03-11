package com.zettsett.timetracker.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TableLayout;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.EmailUtilities;
import com.zettsett.timetracker.FileUtilities;
import com.zettsett.timetracker.database.TimeSliceDBAdapter;
import com.zettsett.timetracker.model.TimeSlice;

public class DataExportActivity extends Activity implements RadioGroup.OnCheckedChangeListener {

	private static final String CSV_LINE_SEPERATOR = "\n";
	private static final String CSV_FIELD_SEPERATOR = ",";
	
	private RadioGroup mRadioGroup;
	private boolean mExportAll = true;
	private long mFromDate, mToDate;
	private boolean mSettingFrom;
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
				mSettingFrom = true;
				showDatePickerDialog(mFromDate);
			}
		});
		Button toButton = (Button) findViewById(R.id.button_data_export_to);
		toButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSettingFrom = false;
				showDatePickerDialog(mToDate);

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
		fromButton.setText(DateTimeFormatter.getShortDateStr(mFromDate));

		Button toButton = (Button) findViewById(R.id.button_data_export_to);
		toButton.setText(DateTimeFormatter.getShortDateStr(mToDate));
	}

	private void showDatePickerDialog(long dateToShow) {
		final Calendar c = Calendar.getInstance();
		c.setTime(new Date(dateToShow));
		DatePickerDialog d = new DatePickerDialog(this, mDateSetListener, c.get(Calendar.YEAR), c
				.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		d.show();
	}

	private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			final Calendar c = DateTimeFormatter.getCalendar(year, monthOfYear, dayOfMonth);
			if (mSettingFrom) {
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				mFromDate = c.getTimeInMillis();
			} else {
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);
				mToDate = c.getTimeInMillis();
			}
			assignFromToDateLabels();
		}
	};

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

		if (mExportAll) {
			timeSlices = mTimeSliceDBAdapter.fetchAllTimeSlices();
		} else {
			timeSlices = mTimeSliceDBAdapter.fetchTimeSlicesByDateRange(mFromDate, mToDate);
		}
		StringBuilder output = new StringBuilder();
		output.append("Start, End, Category Name, Category Description, Notes").append(CSV_LINE_SEPERATOR);
		for (TimeSlice aTimeSlice : timeSlices) {
			String dateStr = DateTimeFormatter.getRfcDateTimeStr(aTimeSlice.getStartTime());
			output.append(dateStr).append(CSV_FIELD_SEPERATOR);
			output.append(aTimeSlice.getStartTimeStr()).append(CSV_FIELD_SEPERATOR);
			output.append(DateTimeFormatter.getRfcDateTimeStr(aTimeSlice.getEndTime())).append(CSV_FIELD_SEPERATOR);
			output.append(aTimeSlice.getCategory().getCategoryName()).append(CSV_FIELD_SEPERATOR);
			output.append(aTimeSlice.getCategory().getDescription()).append(CSV_FIELD_SEPERATOR);
			output.append(aTimeSlice.getNotes().replace(CSV_LINE_SEPERATOR, " "));
			output.append(CSV_LINE_SEPERATOR);
		}
		if (mEmailData) {
			EmailUtilities.send("", getString(R.string.export_email_subject), this, output
					.toString());
		} else {
			FileUtilities fileUtil = new FileUtilities(this);
			fileUtil.write(getFilenameEditText().getText().toString(), output.toString());
		}
	}
}
