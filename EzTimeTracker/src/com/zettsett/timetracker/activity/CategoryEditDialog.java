package com.zettsett.timetracker.activity;

import java.util.Calendar;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.android.widgets.DateSlider.AlternativeDateSlider;
import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.zetter.androidTime.R;
import com.zettsett.timetracker.TimeTrackerManager;
import com.zettsett.timetracker.database.TimeSliceRepository;
import com.zettsett.timetracker.model.TimeSliceCategory;

/**
 * Editor for a Category
 * 
 * @author EVE
 */
public class CategoryEditDialog extends Dialog {
	protected static final int GET_END_DATETIME = 0;
	protected static final int GET_START_DATETIME = 1;
	protected static final int GET_END_DATETIME_NOW = 2;
	protected static final int GET_START_DATETIME_NOW = 3;

	private final EditText catNameField;
	private final EditText catDescField;
	private final Button saveButton;
	private final Button cancelButton;
	private Button mTimeInButton;
	private Button mTimeOutButton;
	private TextView usage;

	// define the listener which is called once a user selected the date.
	private final DateSlider.OnDateSetListener mDateTimeSetListenerStart = new DateSlider.OnDateSetListener() {
		@Override
		public void onDateSet(final DateSlider view, final Calendar selectedDate) {
			// update the dateText view with the corresponding date
			CategoryEditDialog.this.mCategory.setStartTime(selectedDate
					.getTimeInMillis());
			CategoryEditDialog.this.setTimeTexts();
		}
	};

	// define the listener which is called once a user selected the date.
	private final DateSlider.OnDateSetListener mDateTimeSetListenerEnd = new DateSlider.OnDateSetListener() {
		@Override
		public void onDateSet(final DateSlider view, final Calendar selectedDate) {
			// update the dateText view with the corresponding date
			CategoryEditDialog.this.mCategory.setEndTime(selectedDate
					.getTimeInMillis());
			CategoryEditDialog.this.setTimeTexts();
		}
	};

	private TimeSliceCategory mCategory;

	public CategoryEditDialog(final Context context, final ICategorySetter owner) {
		super(context);
		this.setContentView(R.layout.category_edit);
		this.catNameField = (EditText) this
				.findViewById(R.id.edit_time_category_name_field);
		this.catDescField = (EditText) this
				.findViewById(R.id.edit_time_category_desc_field);
		this.saveButton = (Button) this
				.findViewById(R.id.edit_time_category_save_button);
		this.cancelButton = (Button) this
				.findViewById(R.id.edit_time_category_cancel_button);

		this.mTimeInButton = (Button) this.findViewById(R.id.EditTimeIn);

		this.usage = (TextView) this.findViewById(R.id.category_usage);

		this.usage.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(final View v) {
				CategoryEditDialog.this.saveChangesAndExit(owner);
				if ((CategoryEditDialog.this.mCategory != null)
						&& (CategoryEditDialog.this.mCategory != TimeSliceCategory.NO_CATEGORY)) {
					final TimeSliceFilterParameter filter = new TimeSliceFilterParameter()
							.setCategoryId(
									CategoryEditDialog.this.mCategory
											.getRowId()).setIgnoreDates(true);
					TimeSheetDetailListActivity
							.showActivity(context, filter, 0);
				}
				return true;
			}
		});

		this.mTimeInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				CategoryEditDialog.this
						.showDialog(CategoryEditDialog.GET_START_DATETIME);
			}
		});
		this.mTimeInButton
				.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(final View v) {
						CategoryEditDialog.this
								.showDialog(CategoryEditDialog.GET_START_DATETIME_NOW);
						return true;
					}
				});

		this.mTimeOutButton = (Button) this.findViewById(R.id.EditTimeOut);
		this.mTimeOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				CategoryEditDialog.this
						.showDialog(CategoryEditDialog.GET_END_DATETIME);
			}
		});
		this.mTimeOutButton
				.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(final View v) {
						CategoryEditDialog.this
								.showDialog(CategoryEditDialog.GET_END_DATETIME_NOW);
						return true;
					}
				});

		this.catNameField.setWidth(200);
		this.catDescField.setWidth(404);
		this.saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				CategoryEditDialog.this.saveChangesAndExit(owner);
			}

		});
		this.cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				CategoryEditDialog.this.cancel();
			}
		});
	}

	private void saveChangesAndExit(final ICategorySetter owner) {
		this.mCategory.setCategoryName(this.catNameField.getText().toString());
		this.mCategory.setDescription(this.catDescField.getText().toString());
		if (owner != null) {
			owner.setCategory(this.mCategory);
		}
		this.dismiss();
	}

	private void setTimeTexts() {
		String label = String.format(
				this.getContext().getText(R.string.formatStartDate).toString(),
				this.mCategory.getStartDateStr());
		this.mTimeInButton.setText(label);

		label = String.format(this.getContext().getText(R.string.formatEndDate)
				.toString(), this.mCategory.getEndTimeStr());
		this.mTimeOutButton.setText(label);
	}

	public void setCategory(final TimeSliceCategory category) {
		this.mCategory = category;
	}

	@Override
	public void show() {
		if (this.mCategory == null) {
			this.mCategory = new TimeSliceCategory();
			this.setTitle(R.string.title_creating_a_new_category);
			this.catNameField.setText("");
			this.catDescField.setText("");
		} else {
			final String caption = String.format(
					this.getContext()
							.getString(R.string.format_title_edit_category)
							.toString(), this.mCategory.getCategoryName(),
					this.mCategory.getRowId());
			this.setTitle(caption);
			this.catNameField.setText(this.mCategory.getCategoryName());
			this.catDescField.setText(this.mCategory.getDescription());
			this.setTimeTexts();
		}

		this.showUsage();

		super.show();
	}

	private void showUsage() {
		final TimeSliceFilterParameter filterParam = new TimeSliceFilterParameter()
				.setCategoryId(this.mCategory.getRowId()).setIgnoreDates(true);

		final int itemCount = TimeSliceRepository.getCount(filterParam);
		if (itemCount > 0) {
			final int hours = (int) TimeSliceRepository
					.getTotalDurationInHours(filterParam);
			final String label = String.format(
					this.getContext().getText(R.string.category_usage_format)
							.toString(), itemCount, hours);
			this.usage.setText(label);
			this.usage.setVisibility(View.VISIBLE);
		} else {
			this.usage.setVisibility(View.INVISIBLE);
		}
	}

	private void showDialog(final int id) {
		final Dialog dlg = this.createDialog(id);
		if (dlg != null) {
			dlg.show();
		}
	}

	private Dialog createDialog(final int id) {
		// get today's date and time
		final Calendar c = Calendar.getInstance();

		switch (id) {
		case GET_START_DATETIME:
			final long startTime = this.mCategory.getStartTime();
			c.setTimeInMillis((startTime == TimeSliceCategory.MIN_VALID_DATE) ? TimeTrackerManager
					.currentTimeMillis() : startTime);
			return new AlternativeDateSlider(this.getContext(),
					this.mDateTimeSetListenerStart, c);

		case GET_START_DATETIME_NOW:
			c.setTimeInMillis(TimeTrackerManager.currentTimeMillis());
			return new AlternativeDateSlider(this.getContext(),
					this.mDateTimeSetListenerStart, c);

		case GET_END_DATETIME:
			final long endTime = this.mCategory.getEndTime();
			c.setTimeInMillis((endTime == TimeSliceCategory.MAX_VALID_DATE) ? TimeTrackerManager
					.currentTimeMillis() : endTime);
			return new AlternativeDateSlider(this.getContext(),
					this.mDateTimeSetListenerEnd, c);
		case GET_END_DATETIME_NOW:
			c.setTimeInMillis(TimeTrackerManager.currentTimeMillis());
			return new AlternativeDateSlider(this.getContext(),
					this.mDateTimeSetListenerEnd, c);
		}
		return null;
	}

}
