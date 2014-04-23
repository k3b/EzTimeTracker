package de.k3b.timetracker.activity;

import de.k3b.timetracker.R;
import de.k3b.timetracker.report.ExportSettings;
import de.k3b.timetracker.report.ExportSettingsDto;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.*;

/**
 * Dialog to enter export settings
 * @author k3b
 */
public class ExportSettingsDialog extends Dialog implements ExportSettings {

	private Spinner exportFormat;
	private CheckBox useSendTo;
	private View rowFileName;
	private EditText fileName;
	private Button cmdExport;
	private Button cmdCancel;

	public ExportSettingsDialog(Context context, ExportSettings source) {
		super(context);
		this.setContentView(R.layout.export_settings_dialog);
		
		this.exportFormat = (Spinner) this.findViewById(R.id.spinner_export_format);
		this.useSendTo = (CheckBox) this.findViewById(R.id.checkbox_data_export_email);
		this.rowFileName = this.findViewById(R.id.rowFileName);
		this.fileName = (EditText) this.findViewById(R.id.edit_text_data_export_filename);
		this.cmdExport = (Button) this.findViewById(R.id.execute);
		this.cmdCancel = (Button) this.findViewById(R.id.cancel);
		
		this.useSendTo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (isUseSendTo()) {
					rowFileName.setVisibility(View.GONE);
				} else {
					rowFileName.setVisibility(View.VISIBLE);
				}
			}
		});		
		
		this.cmdExport.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				ExportSettingsDialog.this.saveChangesAndExit();
			}

		});
		this.cmdCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				ExportSettingsDialog.this.cancel();
			}
		});
		
		ExportSettingsDto.copy(this, source);
	}

	protected void saveChangesAndExit() {
		// todo
		this.dismiss();
	}

	/* (non-Javadoc)
	 * @see de.k3b.timetracker.report.ExportSettings#getExportFormat()
	 */
	@Override
	public String getExportFormat() {
		return this.exportFormat.getSelectedItem().toString();
	}
	/* (non-Javadoc)
	 * @see de.k3b.timetracker.report.ExportSettings#setExportFormat(java.lang.String)
	 */
	@Override
	public void setExportFormat(String exportFormat) {
		// this.exportFormat.setSelection(position);
	}
	/* (non-Javadoc)
	 * @see de.k3b.timetracker.report.ExportSettings#getFileName()
	 */
	@Override
	public String getFileName() {
		return this.fileName.toString();
	}
	/* (non-Javadoc)
	 * @see de.k3b.timetracker.report.ExportSettings#setFileName(java.lang.String)
	 */
	@Override
	public void setFileName(String fileName) {
		this.fileName.setText(fileName);
	}
	/* (non-Javadoc)
	 * @see de.k3b.timetracker.report.ExportSettings#isUseSendTo()
	 */
	@Override
	public boolean isUseSendTo() {
		return this.useSendTo.isChecked();
	}
	/* (non-Javadoc)
	 * @see de.k3b.timetracker.report.ExportSettings#setUseSendTo(boolean)
	 */
	@Override
	public void setUseSendTo(boolean useSendTo) {
		this.useSendTo.setChecked(useSendTo);
	}

}
