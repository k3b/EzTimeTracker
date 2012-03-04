package com.zettsett.timetracker.receiver;

import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.TimeTrackerManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
			mgr.reloadSessionData();
			if (Global.CMD_START.equalsIgnoreCase(cmd) && (category != null)) 
			{
				long elapsedRealtime = mgr.currentTimeMillis();
				mgr.punchInClock(category, elapsedRealtime);
				addNotes(mgr, 3, parts);
			} else 	if (Global.CMD_STOP.equalsIgnoreCase(cmd)) 
			{
				long elapsedRealtime = mgr.currentTimeMillis();
				addNotes(mgr, 2, parts);
				mgr.punchOutClock(elapsedRealtime, "");
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

	private void addNotes(TimeTrackerManager mgr, int offset, String[] parts) {
		while (offset > parts.length)
		{
			mgr.addNotes(parts[offset]);
			offset++;
		}
	}

}
