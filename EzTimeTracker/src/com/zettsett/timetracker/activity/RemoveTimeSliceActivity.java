package com.zettsett.timetracker.activity;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.database.DatabaseInstance;
import com.zettsett.timetracker.database.TimeSliceDBAdapter;

public class RemoveTimeSliceActivity extends Activity {
	private TimeSliceDBAdapter mTimeSliceDBAdapter;
	private boolean mRemoveAll = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remove_ts);
		setTitle("Delete Time Interval Data");
		DatabaseInstance.initialize(this);
		mTimeSliceDBAdapter = new TimeSliceDBAdapter(this);
		Button removalButton = (Button) findViewById(R.id.button_remove_ts);
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
		final CheckBox emailCheckBox = (CheckBox) findViewById(R.id.checkbox_remove_ts_all);
		emailCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayout_remove_ts_dates);
				if (emailCheckBox.isChecked()) {
					ll.setVisibility(View.GONE);
					mRemoveAll = true;
				} else {
					ll.setVisibility(View.VISIBLE);
					mRemoveAll = false;
				}
			}
		});

	}

	private void doRemove() {
		if (mRemoveAll) {
			mTimeSliceDBAdapter.deleteAll();
			Toast.makeText(getApplicationContext(),
					"All time intervals have been removed from the system.", Toast.LENGTH_LONG)
					.show();
		} else {
			DatePicker fromPicker = (DatePicker) findViewById(R.id.DatePicker_remove_ts_start);
			Calendar startCalendar = DateTimeFormatter.getCalendar(fromPicker.getYear(), fromPicker
					.getMonth(), fromPicker.getDayOfMonth());
			startCalendar.set(Calendar.HOUR_OF_DAY, 0);
			startCalendar.set(Calendar.MINUTE, 0);
			startCalendar.set(Calendar.SECOND, 1);
			long startDate = startCalendar.getTimeInMillis();
			// Toast.makeText(getApplicationContext(),
			// "Time intervals from " + DateFormat.format("hh:mm:ssaa",
			// startDate),
			// Toast.LENGTH_LONG)
			// .show();

			DatePicker toPicker = (DatePicker) findViewById(R.id.DatePicker_remove_ts_end);
			Calendar endCalendar = DateTimeFormatter.getCalendar(toPicker.getYear(), toPicker
					.getMonth(), toPicker.getDayOfMonth());
			endCalendar.set(Calendar.HOUR_OF_DAY, 23);
			endCalendar.set(Calendar.MINUTE, 59);
			endCalendar.set(Calendar.SECOND, 59);
			long endDate = endCalendar.getTimeInMillis();
			mTimeSliceDBAdapter.deleteForDateRange(startDate, endDate);
			Toast.makeText(
					getApplicationContext(),
					"Time intervals from " + getFormattedDate(R.id.DatePicker_remove_ts_start)
							+ " through " + getFormattedDate(R.id.DatePicker_remove_ts_end)
							+ " removed.", Toast.LENGTH_LONG).show();
		}
		finish();
	}

	private CharSequence getFormattedDate(int datPickerId) {
		DatePicker fromPicker = (DatePicker) findViewById(datPickerId);
		Calendar startCalendar = DateTimeFormatter.getCalendar(fromPicker.getYear(), fromPicker
				.getMonth(), fromPicker.getDayOfMonth());
		return DateTimeFormatter.getLongDateStr(startCalendar.getTimeInMillis());
	}

	public void confirmRemoval() {
		String message;
		if (mRemoveAll) {
			message = "Are you sure you want to permanently delete all time intervals?";
		} else {
			message = "Are you sure you want to permanently delete time intervals from "
					+ getFormattedDate(R.id.DatePicker_remove_ts_start) + " through "
					+ getFormattedDate(R.id.DatePicker_remove_ts_end)
					+ "? This operation cannot be reversed.";
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Confirm Removal");
		builder.setMessage(message).setCancelable(false).setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						doRemove();
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

}
