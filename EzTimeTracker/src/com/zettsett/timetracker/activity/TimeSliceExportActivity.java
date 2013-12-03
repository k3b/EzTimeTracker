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
import com.zettsett.timetracker.Settings;
import com.zettsett.timetracker.database.TimeSliceRepository;
import com.zettsett.timetracker.model.TimeSlice;

public class TimeSliceExportActivity extends Activity implements
		RadioGroup.OnCheckedChangeListener {
	private static final int GET_START_DATETIME = 0;
	private static final int GET_END_DATETIME = 1;

	private static final String CSV_LINE_SEPERATOR = "\n";
	private static final String CSV_FIELD_SEPERATOR = ",";

	private RadioGroup mRadioGroup;
	private boolean mExportAll = true;
	private long mFromDate, mToDate;
	private boolean mEmailData = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(R.string.export_data_to_csv_file);
		this.setContentView(R.layout.time_slice_export_settings);
		this.initializeDateRanges();
		this.mRadioGroup = (RadioGroup) this
				.findViewById(R.id.radio_group_data_export);
		this.mRadioGroup.setOnCheckedChangeListener(this);
		final Button exportButton = (Button) this
				.findViewById(R.id.button_data_export);
		exportButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				TimeSliceExportActivity.this.writeData();
			}
		});

		final Button fromButton = (Button) this
				.findViewById(R.id.button_data_export_from);
		fromButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				TimeSliceExportActivity.this
						.showDialog(TimeSliceExportActivity.GET_START_DATETIME);
			}
		});
		final Button toButton = (Button) this
				.findViewById(R.id.button_data_export_to);
		toButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				TimeSliceExportActivity.this
						.showDialog(TimeSliceExportActivity.GET_END_DATETIME);
			}
		});
		final CheckBox emailCheckBox = (CheckBox) this
				.findViewById(R.id.checkbox_data_export_email);
		emailCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final LinearLayout ll = (LinearLayout) TimeSliceExportActivity.this
						.findViewById(R.id.linear_layout_data_export_filename);
				if (emailCheckBox.isChecked()) {
					ll.setVisibility(View.GONE);
					TimeSliceExportActivity.this.mEmailData = true;
				} else {
					ll.setVisibility(View.VISIBLE);
					TimeSliceExportActivity.this.mEmailData = false;
				}
			}
		});
		this.assignFromToDateLabels();
	}

	private EditText getFilenameEditText() {
		return (EditText) this
				.findViewById(R.id.edit_text_data_export_filename);
	}

	private void initializeDateRanges() {
		final Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR, 23);
		calendar.set(Calendar.MINUTE, 59);
		this.mToDate = calendar.getTimeInMillis();
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.roll(Calendar.MONTH, false);
		this.mFromDate = calendar.getTimeInMillis();
	}

	private void assignFromToDateLabels() {
		final Button fromButton = (Button) this
				.findViewById(R.id.button_data_export_from);
		String label = String
				.format(this.getText(R.string.formatStartDate).toString(),
						DateTimeFormatter.getInstance().getShortDateStr(
								this.mFromDate));
		fromButton.setText(label);

		final Button toButton = (Button) this
				.findViewById(R.id.button_data_export_to);
		label = String.format(this.getText(R.string.formatEndDate).toString(),
				DateTimeFormatter.getInstance().getShortDateStr(this.mToDate));
		toButton.setText(label);
	}

	// define the listener which is called once a user selected the date.
	private final DateSlider.OnDateSetListener mDateTimeSetListenerStart = new DateSlider.OnDateSetListener() {
		@Override
		public void onDateSet(final DateSlider view, final Calendar selectedDate) {
			// update the dateText view with the corresponding date
			TimeSliceExportActivity.this.mFromDate = selectedDate
					.getTimeInMillis();
			TimeSliceExportActivity.this.assignFromToDateLabels();
		}
	};

	// define the listener which is called once a user selected the date.
	private final DateSlider.OnDateSetListener mDateTimeSetListenerEnd = new DateSlider.OnDateSetListener() {
		@Override
		public void onDateSet(final DateSlider view, final Calendar selectedDate) {
			// update the dateText view with the corresponding date
			TimeSliceExportActivity.this.mToDate = selectedDate
					.getTimeInMillis();
			TimeSliceExportActivity.this.assignFromToDateLabels();
		}
	};

	@Override
	public Dialog onCreateDialog(final int id) {
		// this method is called after invoking 'showDialog' for the first time
		// here we initiate the corresponding DateSlideSelector and return the
		// dialog to its caller

		// get today's date and time
		final Calendar c = Calendar.getInstance();

		switch (id) {
		case GET_START_DATETIME:
			c.setTimeInMillis(this.mFromDate);
			return new DefaultDateSlider(this, this.mDateTimeSetListenerStart,
					c);
		case GET_END_DATETIME:
			c.setTimeInMillis(this.mToDate);
			return new DefaultDateSlider(this, this.mDateTimeSetListenerEnd, c);
		}
		return null;
	}

	@Override
	public void onCheckedChanged(final RadioGroup group, final int checkedId) {
		final TableLayout dateRangeLayout = (TableLayout) this
				.findViewById(R.id.table_layout_date_range);
		if (checkedId == R.id.radio_button_data_export_range) {
			dateRangeLayout.setVisibility(View.VISIBLE);
			this.mExportAll = false;
		} else {
			dateRangeLayout.setVisibility(View.GONE);
			this.mExportAll = true;
		}
	}

	private void writeData() {
		final TimeSliceRepository mTimeSliceRepository = new TimeSliceRepository(
				this, Settings.isPublicDatabase());
		List<TimeSlice> timeSlices;

		final TimeSliceFilterParameter filter = new TimeSliceFilterParameter()
				.setStartTime(this.mFromDate).setEndTime(this.mToDate)
				.setIgnoreDates(this.mExportAll);
		timeSlices = mTimeSliceRepository.fetchList(filter);
		final StringBuilder output = new StringBuilder();
		output.append("Start, End, Category Name, Category Description, Notes")
				.append(TimeSliceExportActivity.CSV_LINE_SEPERATOR);
		for (final TimeSlice aTimeSlice : timeSlices) {
			output.append(
					DateTimeFormatter.getInstance().getIsoDateTimeStr(
							aTimeSlice.getStartTime())).append(
					TimeSliceExportActivity.CSV_FIELD_SEPERATOR);
			output.append(
					DateTimeFormatter.getInstance().getIsoDateTimeStr(
							aTimeSlice.getEndTime())).append(
					TimeSliceExportActivity.CSV_FIELD_SEPERATOR);
			output.append(aTimeSlice.getCategoryName()).append(
					TimeSliceExportActivity.CSV_FIELD_SEPERATOR);
			output.append(aTimeSlice.getCategoryDescription()).append(
					TimeSliceExportActivity.CSV_FIELD_SEPERATOR);
			output.append(aTimeSlice.getNotes().replace(
					TimeSliceExportActivity.CSV_LINE_SEPERATOR, " "));
			output.append(TimeSliceExportActivity.CSV_LINE_SEPERATOR);
		}
		if (this.mEmailData) {
			final String appName = this.getString(R.string.app_name);
			final String subject = String.format(
					this.getString(R.string.export_email_subject), appName);

			EmailUtilities.send("", subject, this, output.toString());
		} else {
			final FileUtilities fileUtil = new FileUtilities(this);
			fileUtil.write(this.getFilenameEditText().getText().toString(),
					output.toString());
		}
	}
}
