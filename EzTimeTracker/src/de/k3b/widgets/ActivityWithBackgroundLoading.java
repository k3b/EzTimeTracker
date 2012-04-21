package de.k3b.widgets;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public abstract class ActivityWithBackgroundLoading extends Activity {
   
    public static final String LOG_TAG = "AsyncTask";
   
    //initialize our progress dialog/bar
    private ProgressDialog mProgressDialog;
    private static final int DIALOG_DOWNLOAD_PROGRESS = 17513;

    protected void executeBackGroudTask(String... parameters)    
    {
        //executing the AsyncTask
        new MyAsyncTask().execute(parameters);
    }
 
    private class MyAsyncTask extends AsyncTask<String, String, String> {
       
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }
       
        @Override
        protected String doInBackground(String... parameters) {

            try {
            	ActivityWithBackgroundLoading.this.doInBackground(parameters);
            } catch (Exception e) {
                Log.d(LOG_TAG, e.getMessage());
            }
           
            return null;
        }
       
        protected void onProgressUpdate(String... progress) {
             Log.d(LOG_TAG,progress[0]);
             mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            //dismiss the dialog after the file was downloaded
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
        }
    }
   
    //our progress bar settings
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS: //we set this to 0
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading file...");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(true);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return super.onCreateDialog(id);
        }
    }

	abstract protected void doInBackground(String[] parameters);
}
