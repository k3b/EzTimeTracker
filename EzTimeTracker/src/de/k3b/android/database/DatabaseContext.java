package de.k3b.android.database;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class DatabaseContext extends ContextWrapper {

	private static final String DEBUG_CONTEXT = "DatabaseContext";

	public DatabaseContext(final Context base) {
		super(base);
	}

	@Override
	public File getDatabasePath(final String name) {
		final File sdcard = Environment.getExternalStorageDirectory();
		String dbfile = sdcard.getAbsolutePath() + File.separator + "databases"
				+ File.separator + name;
		if (!dbfile.endsWith(".db")) {
			dbfile += ".db";
		}

		final File result = new File(dbfile);

		if (!result.getParentFile().exists()) {
			result.getParentFile().mkdirs();
		}

		if (Log.isLoggable(DatabaseContext.DEBUG_CONTEXT, Log.INFO)) {
			Log.i(DatabaseContext.DEBUG_CONTEXT, "getDatabasePath(" + name
					+ ") = " + result.getAbsolutePath());
		}

		return result;
	}

	@Override
	public SQLiteDatabase openOrCreateDatabase(final String name,
			final int mode, final SQLiteDatabase.CursorFactory factory) {
		final SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(
				this.getDatabasePath(name), null);
		// SQLiteDatabase result = super.openOrCreateDatabase(name, mode,
		// factory);
		if (Log.isLoggable(DatabaseContext.DEBUG_CONTEXT, Log.INFO)) {
			Log.i(DatabaseContext.DEBUG_CONTEXT, "openOrCreateDatabase(" + name
					+ ",,) = " + result.getPath());
		}
		return result;
	}
}
