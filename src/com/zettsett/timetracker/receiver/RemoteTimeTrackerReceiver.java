package com.zettsett.timetracker.receiver;

import java.util.EnumSet;

import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.TimeTrackerManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;

public class RemoteTimeTrackerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive (Context context, Intent intent) {
		if (Log.isLoggable(Global.LOG_CONTEXT, Log.INFO))
		{
			Log.i(Global.LOG_CONTEXT, "onReceive(context='" + context + "', intent='" + intent + "')");
		}
		
		String data = intent.getDataString();
		if (data != null)
		{
			String[] parts = data.split(":");
			
			String cmd = (parts.length > 1) ? parts[1] : null;
			String category = (parts.length > 2) ? parts[2] : null;
			
			TimeTrackerManager mgr = new TimeTrackerManager(context);
			mgr.reloadSessionData(null);
			if (Global.CMD_START.equalsIgnoreCase(cmd) && (category != null)) {
				long elapsedRealtime = SystemClock.elapsedRealtime();
				mgr.punchInClock(category, elapsedRealtime);
			} else 	if (Global.CMD_STOP.equalsIgnoreCase(cmd)) {
				mgr.punchOutClock();
			} else {
				Log.e(Global.LOG_CONTEXT, "unknown cmd='" + cmd + "', catrory='" + category + "' in intend='" + intent + "'");
			}
			mgr.saveState();
			
			Intent intentRefreshGui = new Intent();
			intentRefreshGui
				.setAction(Global.REFRESH_GUI)
				;
			
			context.sendBroadcast(intentRefreshGui);

		}		
	}

}