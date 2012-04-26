package com.zettsett.timetracker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class RemoveTimeSliceActivity extends FilterActivity {
	public RemoveTimeSliceActivity() {
		super(R.string.label_delete_time_interval_data, R.string.cmd_delete);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	
	private void doRemove() {
		TimeSliceCategory selectedCategory = (TimeSliceCategory) catSpinner.getSelectedItem();
		
		long selectedCategoryID = (selectedCategory != null) ? selectedCategory.getRowId() : TimeSliceCategory.NOT_SAVED;

		boolean ignoreDates = this.filter.isIgnoreDates();
		mTimeSliceDBAdapter.deleteForDateRange(
				ignoreDates ? TimeSlice.NO_TIME_VALUE : filter.getStartTime(), 
				ignoreDates ? TimeSlice.NO_TIME_VALUE : filter.getEndTime(),
				selectedCategoryID);
		String message = getStatusMessage(R.string.format_message_interval_deleted);
		Toast.makeText(
				getApplicationContext(),
				message, Toast.LENGTH_LONG).show();
		finish();
	}

	@Override
	protected void onOkCLick() {
		String message = getStatusMessage(R.string.format_question_delete_time_intervals);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.title_confirm_removal);
		builder.setMessage(message).setCancelable(false).setPositiveButton(R.string.btn_yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						doRemove();

						Intent resultIntent = new Intent((Intent) null);
						resultIntent.putExtra(Global.FILTER_PARAMETER, filter);
						setResult(Activity.RESULT_OK, resultIntent);
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
