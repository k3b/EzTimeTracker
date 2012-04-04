package de.k3b.util;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.zettsett.timetracker.Global;

import android.content.Context;
import android.util.Log;

public class SessionDataPersistance<T extends Serializable>  {
	private static final String STATE_COOKY_NAME = "curr_state";

	private Context context;

	public SessionDataPersistance(Context context)
	{
		this.context = context;
	}
	
	public void save(T sessionData) {
		this.context.deleteFile(STATE_COOKY_NAME);
		try {
			ObjectOutputStream out = new ObjectOutputStream(context.openFileOutput(STATE_COOKY_NAME, 0));
			out.writeObject(sessionData);
			out.close();
		} catch (IOException e) {
			Log.e(Global.LOG_CONTEXT, "Error Saving State", e);
		}
	}

	@SuppressWarnings("unchecked")
	public T load() {
		ObjectInputStream in = null;
		T sessionData = null;
		try {
			String[] fileList = context.fileList();
			for (String fileName : fileList) {
				if (fileName.equals(STATE_COOKY_NAME)) {
					in = new ObjectInputStream(context.openFileInput(fileName));
					sessionData  = (T) in.readObject();
				}
			}
		} catch (InvalidClassException e) {
			Log.w(Global.LOG_CONTEXT, "cannot load old session format. Creating new", e);
			sessionData = null;
		} catch (IOException e) {
			Log.e(Global.LOG_CONTEXT, "Error Loading State", e);
		} catch (ClassNotFoundException e) {
			Log.e(Global.LOG_CONTEXT, "Error Loading State", e);
		} finally {
			if (in != null)
			{
				try {
					in.close();
				} catch (IOException e) {
					Log.e(Global.LOG_CONTEXT, "Error Loading State", e);
				}
			}
		}
		return sessionData;
	}


}
