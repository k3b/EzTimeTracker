package com.zettsett.timetracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.TimeTrackerManager;

/**
 * Allow other apps to remotly PunchIn/PunchOut via broadcast-url-datastring.<br/>
 * Supported urls:<br/>
 * - START:category{:notes}' or<br/>
 * - STOP{:notes}.<br/>
 * 
 * @author EVE
 * 
 */
public class RemoteTimeTrackerBroadcastReceiver extends BroadcastReceiver {

	private static final String USAGE = "Expected datastring 'cmd:"
			+ Global.CMD_START + ":category{:notes}' or 'cmd:"
			+ Global.CMD_STOP + "{:notes}'.";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final String datastring = intent.getDataString();

		final String dbgContext = "BroadcastReceiver.onReceive(context='"
				+ context + "', intent='" + intent + "', datastring='"
				+ datastring + "')";

		if (datastring != null) {
			if (Global.isInfoEnabled()) {
				Log.i(Global.LOG_CONTEXT, dbgContext);
			}

			final String[] parts = datastring.split(":");

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
				Log.e(Global.LOG_CONTEXT, dbgContext
						+ " syntaxerror in datastring: cmd='" + cmd
						+ "', categrory='" + category + "'. "
						+ RemoteTimeTrackerBroadcastReceiver.USAGE);
			}
			mgr.saveSessionData();

			final Intent intentRefreshGui = new Intent();
			intentRefreshGui.setAction(Global.REFRESH_GUI);

			context.sendBroadcast(intentRefreshGui);

		} else {
			Log.e(Global.LOG_CONTEXT, dbgContext + " :no datastring. "
					+ RemoteTimeTrackerBroadcastReceiver.USAGE);
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
