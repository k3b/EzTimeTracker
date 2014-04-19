package de.k3b.timetracker.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import de.k3b.timetracker.DateTimeFormatter;
import de.k3b.timetracker.FileUtilities;
import de.k3b.timetracker.Global;
import de.k3b.timetracker.R;
import de.k3b.timetracker.SendUtilities;
import de.k3b.timetracker.Settings;
import de.k3b.timetracker.database.TimeSliceCategoryRepsitory;
import de.k3b.timetracker.database.TimeSliceRepository;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;

public class TimeSliceExportActivity extends Activity {
	private static final String CSV_LINE_SEPERATOR = "\n";
	private static final String CSV_FIELD_SEPERATOR = ",";


	/**
	 * static to survive if activity is destroeyed but not persisted to sd
	 * because the upper filter limit must change over time
	 */
	private static TimeSliceFilterParameter currentRangeFilter = TimeSliceFilterParameter
			.filterWithDefaultsIfNeccessary(null);
	
	private boolean mUseSendToInsteadOfFile = false;

	private final TimeSliceCategoryRepsitory categoryRepository = new TimeSliceCategoryRepsitory(
			this);

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(R.string.export_data_to_csv_file);
		this.setContentView(R.layout.time_slice_export_settings);
		
		final Button exportButton = (Button) this
				.findViewById(R.id.button_data_export);
		exportButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				onExport();
			}
		});

		final Button filterButton = (Button) this
				.findViewById(R.id.menu_set_filter);
		filterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				onFilter();
			}
		});

		final Button previewButton = (Button) this
				.findViewById(R.id.button_export_preview);
		previewButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				onPreview();
			}
		});


		// hide/show filename if sendTo chekcbox is changed
		final CheckBox sendToCheckBox = (CheckBox) this
				.findViewById(R.id.checkbox_data_export_email);
		sendToCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				TimeSliceExportActivity.this.mUseSendToInsteadOfFile = sendToCheckBox.isChecked();
				makeVisible(TimeSliceExportActivity.this.mUseSendToInsteadOfFile, R.id.caption_filename , R.id.edit_text_data_export_filename);
			}
		});
		showFilter();
	}

	/**
	 * handle result from edit/changeFilter/delete
	 */
	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent intent) {
		if (intent != null) {
			final TimeSliceFilterParameter updatedTimeSlice = (TimeSliceFilterParameter) intent
					.getExtras().get(Global.EXTRA_FILTER);
			if (updatedTimeSlice != null) {
				currentRangeFilter.setParameter(updatedTimeSlice);
				showFilter();
			}
		}
	}

	private void showFilter() {
		TimeSliceCategory category = null;
	
		int categoryId = currentRangeFilter.getCategoryId();
		if (TimeSliceCategory.isValid(categoryId)) {
			category = categoryRepository.fetchByRowID(categoryId);
		}
		TextView viewItem = (TextView) TimeSliceExportActivity.this
				.findViewById(R.id.filter_value);
		viewItem.setText(currentRangeFilter.toString(category));
	}

	private void makeVisible(boolean isVisible, int...viewItemIDs) {
		for(int viewItemID : viewItemIDs) {
			final View viewItem = TimeSliceExportActivity.this
					.findViewById(viewItemID);
			if (isVisible) {
				viewItem.setVisibility(View.INVISIBLE);
			} else {
				viewItem.setVisibility(View.VISIBLE);
			}
		}
	}

	private EditText getFilenameEditText() {
		return (EditText) this
				.findViewById(R.id.edit_text_data_export_filename);
	}

	private void onFilter() {
		ReportFilterActivity.showActivity(this, currentRangeFilter);
	}
	
	private void onPreview() {
		TimeSheetDetailListActivity.showActivity(this, currentRangeFilter, ReportFilterActivity.RESULT_FILTER_CHANGED);
	}
	
	private void onExport() {
		final StringBuilder output = createCsv(currentRangeFilter);
		if (this.mUseSendToInsteadOfFile) {
			final String appName = this.getString(R.string.app_name);
			final String subject = String.format(
					this.getString(R.string.export_email_subject), appName);

			SendUtilities.send("", subject, this, output.toString());
		} else {
			final FileUtilities fileUtil = new FileUtilities(this);
			fileUtil.write(this.getFilenameEditText().getText().toString(),
					output.toString());
		}
	}

	private StringBuilder createCsv(TimeSliceFilterParameter filter) {
		final TimeSliceRepository mTimeSliceRepository = new TimeSliceRepository(
				this, Settings.isPublicDatabase());
		List<TimeSlice> timeSlices;

		timeSlices = mTimeSliceRepository.fetchList(filter);
		final StringBuilder output = new StringBuilder();
		output.append("Start, End, Category Name, Category Description, Notes")
				.append(TimeSliceExportActivity.CSV_LINE_SEPERATOR);
		for (final TimeSlice aTimeSlice : timeSlices) {
			output.append(
					DateTimeFormatter.getInstance().getIsoDateTimeStr(
							aTimeSlice.getStartTime())).append(
					TimeSliceExportActivity.CSV_FIELD_SEPERATOR);
			output.append(
					DateTimeFormatter.getInstance().getIsoDateTimeStr(
							aTimeSlice.getEndTime())).append(
					TimeSliceExportActivity.CSV_FIELD_SEPERATOR);
			output.append(aTimeSlice.getCategoryName()).append(
					TimeSliceExportActivity.CSV_FIELD_SEPERATOR);
			output.append(aTimeSlice.getCategoryDescription()).append(
					TimeSliceExportActivity.CSV_FIELD_SEPERATOR);
			output.append(aTimeSlice.getNotes().replace(
					TimeSliceExportActivity.CSV_LINE_SEPERATOR, " "));
			output.append(TimeSliceExportActivity.CSV_LINE_SEPERATOR);
		}
		return output;
	}
}
