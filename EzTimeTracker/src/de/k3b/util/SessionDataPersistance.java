package de.k3b.util;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;
import android.util.Log;

import com.zettsett.timetracker.Global;

/**
 * Persists session state to local file
 * 
 * @param <T>
 *            Datatype to be persisted
 */
public class SessionDataPersistance<T extends Serializable> implements
		ISessionDataPersistance<T> {
	private final String sessionFileName;
	private final Context context;

	public SessionDataPersistance(final Context context) {
		this(context, "curr_state");
	}

	public SessionDataPersistance(final Context context, final String fileName) {
		this.context = context;
		this.sessionFileName = fileName;
	}

	@Override
	public void save(final T sessionData) {
		this.context.deleteFile(this.sessionFileName);
		try {
			final ObjectOutputStream out = new ObjectOutputStream(
					this.context.openFileOutput(this.sessionFileName, 0));
			out.writeObject(sessionData);
			out.close();
		} catch (final IOException e) {
			Log.e(Global.LOG_CONTEXT, "Error Saving State", e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public T load() {
		ObjectInputStream in = null;
		T sessionData = null;
		try {
			final String[] fileList = this.context.fileList();
			for (final String fileName : fileList) {
				if (fileName.equals(this.sessionFileName)) {
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
