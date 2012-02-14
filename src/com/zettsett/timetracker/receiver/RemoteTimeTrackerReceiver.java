package com.zettsett.timetracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RemoteTimeTrackerReceiver extends BroadcastReceiver {

	private static final String LOG_CONTEXT = "TimeTrackerReceiver";

	@Override
	public void onReceive (Context context, Intent intent) {
		if (Log.isLoggable(LOG_CONTEXT, Log.DEBUG))
		{
			Log.d(LOG_CONTEXT, "onReceive(context='" + context + "', intent='" + intent + "')");
		}
	}

}
