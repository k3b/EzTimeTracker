package com.zettsett.timetracker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.database.TimeSliceDBAdapter;
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
		int itemsDeleted = TimeSliceDBAdapter.deleteForDateRange(this.filter, this.filter.isIgnoreDates());
		String message = getStatusMessage(R.string.format_message_interval_deleted) + itemsDeleted;
		Toast.makeText(
				getApplicationContext(),
				message, Toast.LENGTH_LONG).show();
		finish();
	}

	@Override
	protected void onOkCLick() {
		super.onOkCLick();
		int count = TimeSliceDBAdapter.getCount(this.filter, this.filter.isIgnoreDates());
		
		if (count <= 0) {
			String message = getText(R.string.no_items_found).toString();
			Toast.makeText(
					getApplicationContext(),
					message, Toast.LENGTH_LONG).show();
		} else {
			String message = getStatusMessage(R.string.format_question_delete_time_intervals);
	
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			String title = getText(R.string.title_confirm_removal).toString();
			
			builder.setTitle(title + count);
			builder
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.btn_yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							doRemove();
	
							Intent resultIntent = new Intent();
							resultIntent.putExtra(Global.EXTRA_FILTER, filter);
							setResult(Activity.RESULT_OK, resultIntent);
						}
					})
				.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
			
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

}
