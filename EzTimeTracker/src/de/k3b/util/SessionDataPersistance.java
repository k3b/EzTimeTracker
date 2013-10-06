package de.k3b.util;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;
import android.util.Log;

import com.zettsett.timetracker.Global;

public class SessionDataPersistance<T extends Serializable> {
	private static final String STATE_COOKY_NAME = "curr_state";

	private final Context context;

	public SessionDataPersistance(final Context context) {
		this.context = context;
	}

	public void save(final T sessionData) {
		this.context.deleteFile(SessionDataPersistance.STATE_COOKY_NAME);
		try {
			final ObjectOutputStream out = new ObjectOutputStream(
					this.context.openFileOutput(
							SessionDataPersistance.STATE_COOKY_NAME, 0));
			out.writeObject(sessionData);
			out.close();
		} catch (final IOException e) {
			Log.e(Global.LOG_CONTEXT, "Error Saving State", e);
		}
	}

	@SuppressWarnings("unchecked")
	public T load() {
		ObjectInputStream in = null;
		T sessionData = null;
		try {
			final String[] fileList = this.context.fileList();
			for (final String fileName : fileList) {
				if (fileName.equals(SessionDataPersistance.STATE_COOKY_NAME)) {
					in = new ObjectInputStream(
							this.context.openFileInput(fileName));
					sessionData = (T) in.readObject();
				}
			}
		} catch (final InvalidClassException e) {
			Log.w(Global.LOG_CONTEXT,
					"cannot load old session format. Creating new", e);
			sessionData = null;
		} catch (final IOException e) {
			Log.e(Global.LOG_CONTEXT, "Error Loading State", e);
		} catch (final ClassNotFoundException e) {
			Log.e(Global.LOG_CONTEXT, "Error Loading State", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					Log.e(Global.LOG_CONTEXT, "Error Loading State", e);
				}
			}
		}
		return sessionData;
	}

}
