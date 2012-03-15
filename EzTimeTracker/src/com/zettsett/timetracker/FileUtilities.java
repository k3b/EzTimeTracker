package com.zettsett.timetracker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.zetter.androidTime.R;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class FileUtilities {
	private Writer writer;
	private String absolutePath;
	private final Context context;

	public FileUtilities(Context context) {
		super();
		this.context = context;
	}

	public void write(String fileName, String data) {
		File root = Environment.getExternalStorageDirectory();
		File outDir = new File(root.getAbsolutePath(), context.getString(R.string.export_directory));
		File outputFile = new File(outDir, fileName);
		try {
			if (!outDir.isDirectory()) {
				outDir.mkdir();
			}
			
			if (!outDir.isDirectory()) {
				throw new IOException(
						context.getString(R.string.format_error_create_directory, outDir.getAbsolutePath()));
			}
			writer = new BufferedWriter(new FileWriter(outputFile));
			writer.write(data);
			String message = String.format(this.context.getText(R.string.format_message_successfully_saved_file).toString() , outputFile.getAbsolutePath());
			Toast.makeText(context.getApplicationContext(),
					message,
					Toast.LENGTH_LONG).show();
			writer.close();
		} catch (IOException e) {
			Log.w("eztt", e.getMessage(), e);
			String message = String.format(this.context.getText(R.string.format_error_saved_file).toString() , outputFile.getAbsolutePath(), e.getMessage());
			
			Toast.makeText(context, message,
					Toast.LENGTH_LONG).show();
		}

	}

	public Writer getWriter() {
		return writer;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

}
