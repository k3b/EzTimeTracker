package de.k3b.widgets;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public abstract class ActivityWithBackgroundLoading extends Activity {

	public static final String LOG_TAG = "AsyncTask";

	// initialize our progress dialog/bar
	private ProgressDialog mProgressDialog;
	private static final int DIALOG_DOWNLOAD_PROGRESS = 17513;

	protected void executeBackGroudTask(final String... parameters) {
		// executing the AsyncTask
		new MyAsyncTask().execute(parameters);
	}

	private class MyAsyncTask extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			ActivityWithBackgroundLoading.this
					.showDialog(ActivityWithBackgroundLoading.DIALOG_DOWNLOAD_PROGRESS);
		}

		@Override
		protected String doInBackground(final String... parameters) {

			try {
				ActivityWithBackgroundLoading.this.doInBackground(parameters);
			} catch (final Exception e) {
				Log.d(ActivityWithBackgroundLoading.LOG_TAG, e.getMessage());
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(final String... progress) {
			Log.d(ActivityWithBackgroundLoading.LOG_TAG, progress[0]);
			ActivityWithBackgroundLoading.this.mProgressDialog
					.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected void onPostExecute(final String unused) {
			// dismiss the dialog after the file was downloaded
			ActivityWithBackgroundLoading.this
					.dismissDialog(ActivityWithBackgroundLoading.DIALOG_DOWNLOAD_PROGRESS);
		}
	}

	// our progress bar settings
	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case DIALOG_DOWNLOAD_PROGRESS: // we set this to 0
			this.mProgressDialog = new ProgressDialog(this);
			this.mProgressDialog.setMessage("Downloading file...");
			this.mProgressDialog.setIndeterminate(false);
			this.mProgressDialog.setMax(100);
			this.mProgressDialog
					.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			this.mProgressDialog.setCancelable(true);
			this.mProgressDialog.show();
			return this.mProgressDialog;
		default:
			return super.onCreateDialog(id);
		}
	}

	abstract protected void doInBackground(String[] parameters);
}
