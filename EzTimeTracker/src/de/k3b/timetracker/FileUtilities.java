package de.k3b.timetracker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import de.k3b.timetracker.R;

public class FileUtilities {
	private Writer writer;
	private String absolutePath;
	private final Context context;

	public FileUtilities(final Context context) {
		super();
		this.context = context;
	}

	public void write(String fileName, final String extension, final String data) {
		if (!fileName.contains(".")) {
			fileName = fileName + "." + extension;
		}
		write(fileName, data);
	}
	
	public void write(final String fileName, final String data) {
		final File root = Environment.getExternalStorageDirectory();
		final File outDir = new File(root.getAbsolutePath(),
				this.context.getString(R.string.export_directory));
		final File outputFile = new File(outDir, fileName);
		try {
			if (!outDir.isDirectory()) {
				outDir.mkdir();
			}

			if (!outDir.isDirectory()) {
				throw new IOException(this.context.getString(
						R.string.format_error_create_directory,
						outDir.getAbsolutePath()));
			}
			this.writer = new BufferedWriter(new FileWriter(outputFile));
			this.writer.write(data);
			final String message = String.format(
					this.context.getText(
							R.string.format_message_successfully_saved_file)
							.toString(), outputFile.getAbsolutePath());
			Toast.makeText(this.context.getApplicationContext(), message,
					Toast.LENGTH_LONG).show();
			this.writer.close();
		} catch (final IOException e) {
			Log.w("eztt", e.getMessage(), e);
			final String message = String.format(
					this.context.getText(R.string.format_error_saved_file)
							.toString(), outputFile.getAbsolutePath(), e
							.getMessage());

			Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
		}

	}

	public Writer getWriter() {
		return this.writer;
	}

	public String getAbsolutePath() {
		return this.absolutePath;
	}

}
