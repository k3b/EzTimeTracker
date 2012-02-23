package com.zettsett.timetracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RemoteTimeTrackerReceiver extends BroadcastReceiver {

	public static StringBuffer message = new StringBuffer();
	
	private static final String LOG_CONTEXT = "TimeTrackerReceiver";

	@Override
	public void onReceive (Context context, Intent intent) {
		if (Log.isLoggable(LOG_CONTEXT, Log.ERROR))
		{
			Log.e(LOG_CONTEXT, "onReceive(intent='" + intent + "')");
		}
//		message
//			.append("onReceive(context='").append(context)
//			.append("', Data='").append(intent.getData())
//			.append("', intent='").append(intent).append("')");
		String data = intent.getDataString();
		if (data != null)
		{
			String[] parts = data.split(":");
			message
			.append(parts[1]).append(" ").append((parts.length > 2) ? parts[2] : "").append("\n");
			;
		}
	}

}
