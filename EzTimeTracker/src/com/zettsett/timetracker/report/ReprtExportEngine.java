package com.zettsett.timetracker.report;

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

import com.zetter.androidTime.R;

public class ReprtExportEngine {
	private Writer writer;
	private Context context;
	// private List<TextView> reportList;
	ReportOutput output;
	private String defaultName;

	private ReprtExportEngine() {

	}

	private void buildSaveFileDialog() {
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.file_choose_name);
		dialog.setTitle(R.string.choose_filename);
		final Button saveButton = (Button) dialog.findViewById(R.id.choose_file_name_save_button);
		final Button cancelButton = (Button) dialog
				.findViewById(R.id.choose_file_name_cancel_button);
		final EditText fileNameField = (EditText) dialog
				.findViewById(R.id.choose_file_name_edit_field);
		fileNameField.setText(defaultName);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				export(fileNameField.getText().toString());
				dialog.dismiss();
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		dialog.show();
	}

	public static void exportToSD(String defaultName, Context context, ReportOutput output) {
		ReprtExportEngine exporter = new ReprtExportEngine();
		exporter.context = context;
		exporter.defaultName = defaultName;
		exporter.output = output;
		exporter.buildSaveFileDialog();
	}

	void export(String fileName) {
		String outputPath = fileName;
		try {
			outputPath = open(fileName);
			saveData();
			String message = String.format(context.getString(R.string.format_message_successfully_saved_file) , outputPath);
			Toast.makeText(context.getApplicationContext(),
					message, Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Log.w("eztt", e.getMessage(), e);
			String message = String.format(context.getString(R.string.format_error_saved_file), outputPath, e.getMessage());
			Toast.makeText(context.getApplicationContext(),
					e.getMessage() + message, Toast.LENGTH_LONG).show();
		}
	}

	private String open(String fileName) throws IOException {
		File root = Environment.getExternalStorageDirectory();
		File outDir = new File(root.getAbsolutePath(), "EZ_time_tracker");
		if (!outDir.isDirectory()) {
			outDir.mkdir();
		}
		if (!outDir.isDirectory()) {
			String message = String.format(context.getString(R.string.format_error_create_directory), outDir.getAbsolutePath());
			throw new IOException(
					message);
		}
		File outputFile = new File(outDir, fileName);
		writer = new BufferedWriter(new FileWriter(outputFile));
		return outputFile.getAbsolutePath();
	}

	private void saveData() throws IOException {
		writer.write(output.getOutput());
		writer.close();
	}

}
