package de.k3b.timetracker.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import de.k3b.timetracker.R;
import de.k3b.timetracker.activity.ExportSettingsDialog;

public class ReprtExportEngine {
	private Writer writer;
	private Context context;
	// private List<TextView> reportList;
	String output;
	private String defaultName;

	private ReprtExportEngine() {

	}

	private static ExportSettingsDto exportSettings = new ExportSettingsDto();
	
	private ExportSettingsDialog dlgExportSettings = null;
	
	private void showSaveFileDialog() {
		if (dlgExportSettings == null) {
			dlgExportSettings = new ExportSettingsDialog(this.context, exportSettings);
		}
		dlgExportSettings.show();
	}

	public static void exportToSD(final String defaultName,
			final Context context, final String output) {
		final ReprtExportEngine exporter = new ReprtExportEngine();
		exporter.context = context;
		exporter.defaultName = defaultName;
		exporter.output = output;
		exporter.showSaveFileDialog();
	}

	void export(final String fileName) {
		String outputPath = fileName;
		try {
			outputPath = this.open(fileName);
			this.saveData();
			final String message = String
					.format(this.context
							.getString(R.string.format_message_successfully_saved_file),
							outputPath);
			Toast.makeText(this.context.getApplicationContext(), message,
					Toast.LENGTH_LONG).show();
		} catch (final IOException e) {
			Log.w("eztt", e.getMessage(), e);
			final String message = String.format(
					this.context.getString(R.string.format_error_saved_file),
					outputPath, e.getMessage());
			Toast.makeText(this.context.getApplicationContext(),
					e.getMessage() + message, Toast.LENGTH_LONG).show();
		}
	}

	private String open(final String fileName) throws IOException {
		final File root = Environment.getExternalStorageDirectory();
		final File outDir = new File(root.getAbsolutePath(), "EZ_time_tracker");
		if (!outDir.isDirectory()) {
			outDir.mkdir();
		}
		if (!outDir.isDirectory()) {
			final String message = String.format(this.context
					.getString(R.string.format_error_create_directory), outDir
					.getAbsolutePath());
			throw new IOException(message);
		}
		final File outputFile = new File(outDir, fileName);
		this.writer = new BufferedWriter(new FileWriter(outputFile));
		return outputFile.getAbsolutePath();
	}

	private void saveData() throws IOException {
		this.writer.write(this.output);
		this.writer.close();
	}

}
