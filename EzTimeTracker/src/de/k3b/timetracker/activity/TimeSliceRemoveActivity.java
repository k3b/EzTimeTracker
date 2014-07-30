package de.k3b.timetracker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import de.k3b.timetracker.Global;
import de.k3b.timetracker.R;
import de.k3b.timetracker.TimeSliceFilterParameter;
import de.k3b.timetracker.database.TimeSliceRepository;

public class TimeSliceRemoveActivity extends FilterActivity {
	public static final int RESULT_DELETE_OK = 19284;

	public static void showActivity(final Activity owner,
			final TimeSliceFilterParameter filter) {
		final Intent intent = new Intent().setClass(owner,
				TimeSliceRemoveActivity.class);
		intent.putExtra(Global.EXTRA_FILTER, filter); // item.getItemId());
		owner.startActivityForResult(intent, 0);
	}

	public TimeSliceRemoveActivity() {
		super(R.string.label_delete_time_interval_data, R.string.cmd_delete,
				TimeSliceRemoveActivity.RESULT_DELETE_OK);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	private void doRemove() {
		final int itemsDeleted = TimeSliceRepository.delete(this.filter);
		final String message = this
				.getStatusMessage(R.string.format_message_interval_deleted)
				+ itemsDeleted;
		Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_LONG)
				.show();

		final Intent resultIntent = new Intent();
		resultIntent.putExtra(Global.EXTRA_FILTER,
				TimeSliceRemoveActivity.this.filter);
		this.setResult(Activity.RESULT_OK, resultIntent);
		super.onOkCLick();
	}

	/**
	 * After pessing "Ok i want to delete this item" ask "are you shure?" via
	 * popup
	 */
	@Override
	protected void onOkCLick() {
		final int count = TimeSliceRepository.getCount(this.filter);

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
}
