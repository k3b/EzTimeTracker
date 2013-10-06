package com.zettsett.timetracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.TimeTrackerManager;

public class RemoteTimeTrackerBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (Log.isLoggable(Global.LOG_CONTEXT, Log.INFO)) {
			Log.i(Global.LOG_CONTEXT, "onReceive(context='" + context
					+ "', intent='" + intent + "')");
		}

		final String data = intent.getDataString();
		if (data != null) {
			final String[] parts = data.split(":");

			final String cmd = (parts.length > 1) ? parts[1] : null;
			final String category = (parts.length > 2) ? parts[2] : null;

			final TimeTrackerManager mgr = new TimeTrackerManager(context);
			mgr.reloadSessionData();
			if (Global.CMD_START.equalsIgnoreCase(cmd) && (category != null)
					&& (category.length() > 0)) {
				final long elapsedRealtime = TimeTrackerManager
						.currentTimeMillis();
				mgr.punchInClock(category, elapsedRealtime);
				this.addNotes(mgr, 3, parts);
			} else if (Global.CMD_STOP.equalsIgnoreCase(cmd)) {
				final long elapsedRealtime = TimeTrackerManager
						.currentTimeMillis();
				this.addNotes(mgr, 2, parts);
				mgr.punchOutClock(elapsedRealtime, "");
			} else {
				Log.e(Global.LOG_CONTEXT, "unknown cmd='" + cmd
						+ "', catrory='" + category + "' in intend='" + intent
						+ "'");
			}
			mgr.saveState();

			final Intent intentRefreshGui = new Intent();
			intentRefreshGui.setAction(Global.REFRESH_GUI);

			context.sendBroadcast(intentRefreshGui);

		}
	}

	private void addNotes(final TimeTrackerManager mgr, int offset,
			final String[] parts) {
		while (offset < parts.length) {
			mgr.addNotes(parts[offset]);
			offset++;
		}
	}

}
