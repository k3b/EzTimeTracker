package com.zettsett.timetracker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.database.TimeSliceRepository;

public class TimeSliceRemoveActivity extends FilterActivity {
	public static final int RESULT_DELETE_OK = 19284;

	public TimeSliceRemoveActivity() {
		super(R.string.label_delete_time_interval_data, R.string.cmd_delete,
				TimeSliceRemoveActivity.RESULT_DELETE_OK);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	private void doRemove() {
		final int itemsDeleted = TimeSliceRepository.deleteForDateRange(
				this.filter, this.filter.isIgnoreDates());
		final String message = this
				.getStatusMessage(R.string.format_message_interval_deleted)
				+ itemsDeleted;
		Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_LONG)
				.show();
		this.finish();
	}

	@Override
	protected void onOkCLick() {
		super.onOkCLick();
		final int count = TimeSliceRepository.getCount(this.filter,
				this.filter.isIgnoreDates());

		if (count <= 0) {
			final String message = this.getText(R.string.no_items_found)
					.toString();
			Toast.makeText(this.getApplicationContext(), message,
					Toast.LENGTH_LONG).show();
		} else {
			final String message = this
					.getStatusMessage(R.string.format_question_delete_time_intervals);

			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final String title = this.getText(R.string.title_confirm_removal)
					.toString();

			builder.setTitle(title + count);
			builder.setMessage(message)
					.setCancelable(false)
					.setPositiveButton(R.string.btn_yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									TimeSliceRemoveActivity.this.doRemove();

									final Intent resultIntent = new Intent();
									resultIntent
											.putExtra(
													Global.EXTRA_FILTER,
													TimeSliceRemoveActivity.this.filter);
									TimeSliceRemoveActivity.this.setResult(
											Activity.RESULT_OK, resultIntent);
								}
							})
					.setNegativeButton(R.string.btn_no,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									dialog.cancel();
								}
							});

			final AlertDialog alert = builder.create();
			alert.show();
		}
	}

	public static void showActivity(final Activity owner,
			final FilterParameter filter) {
		final Intent intent = new Intent().setClass(owner,
				TimeSliceRemoveActivity.class);
		intent.putExtra(Global.EXTRA_FILTER, filter); // item.getItemId());
		owner.startActivityForResult(intent, 0);
	}
}
