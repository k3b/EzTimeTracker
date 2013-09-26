package com.zettsett.timetracker.activity;

import java.util.Calendar;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.googlecode.android.widgets.DateSlider.AlternativeDateSlider;
import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.zetter.androidTime.R;
import com.zettsett.timetracker.TimeTrackerManager;
import com.zettsett.timetracker.model.TimeSliceCategory;

/**
 * Editor for a Category
 * @author EVE
 */
public class CategoryEditDialog extends Dialog  {
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

    // define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateTimeSetListenerStart =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
            	mCategory.setStartTime(selectedDate.getTimeInMillis());
        		setTimeTexts();
            }
    };

    // define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateTimeSetListenerEnd =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
            	mCategory.setEndTime(selectedDate.getTimeInMillis());
        		setTimeTexts();
            }
    };
	
	private TimeSliceCategory mCategory;

	public CategoryEditDialog(Context context, 
			final CategorySetter owner) {
		super(context);
		setContentView(R.layout.edit_category);
		catNameField = (EditText) findViewById(R.id.edit_time_category_name_field);
		catDescField = (EditText) findViewById(R.id.edit_time_category_desc_field);
		saveButton = (Button) findViewById(R.id.edit_time_category_save_button);
		cancelButton = (Button) findViewById(R.id.edit_time_category_cancel_button);

		mTimeInButton = (Button) findViewById(R.id.EditTimeIn);
		mTimeInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(GET_START_DATETIME);
			}
		});
		mTimeInButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				showDialog(GET_START_DATETIME_NOW);
				return true;
			}
		});

		mTimeOutButton = (Button) findViewById(R.id.EditTimeOut);
		mTimeOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(GET_END_DATETIME);
			}
		});
		mTimeOutButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				showDialog(GET_END_DATETIME_NOW);
				return true;
			}
		});		
		
		catNameField.setWidth(200);
		catDescField.setWidth(404);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCategory.setCategoryName(catNameField.getText().toString());
				mCategory.setDescription(catDescField.getText().toString());
				owner.setCategory(mCategory);
				dismiss();
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
	}

	private void setTimeTexts() {
		String label = String.format(this.getContext().getText(R.string.formatStartDate).toString(),this.mCategory.getStartDateStr());
		mTimeInButton.setText(label);

		label = String.format(this.getContext().getText(R.string.formatEndDate).toString(),this.mCategory.getEndTimeStr());
		mTimeOutButton.setText(label);
	}

	public void setCategory(TimeSliceCategory category) {
		mCategory = category;
	}
	
	@Override
	public void show() {
		if (mCategory == null) {
			mCategory = new TimeSliceCategory();
			setTitle(R.string.title_creating_a_new_category);
			catNameField.setText("");
			catDescField.setText("");
		} else {
			String caption = String.format(this.getContext().getString(R.string.format_title_edit_category).toString(), mCategory.getCategoryName(), mCategory.getRowId());
			setTitle(caption);
			catNameField.setText(mCategory.getCategoryName());
			catDescField.setText(mCategory.getDescription());
			setTimeTexts();
		}
		super.show();
	}
	
	private void showDialog(int id) {
		Dialog dlg = createDialog(id);
		if (dlg != null) {
			dlg.show();
		}
	}
	
	private Dialog createDialog(int id) {
    	// get today's date and time
        final Calendar c = Calendar.getInstance();
        
        switch (id) {
        case GET_START_DATETIME:
        	c.setTimeInMillis(this.mCategory.getStartTime());
            return new AlternativeDateSlider(this.getContext(),mDateTimeSetListenerStart,c);
        case GET_START_DATETIME_NOW:
        	c.setTimeInMillis(TimeTrackerManager.currentTimeMillis());
            return new AlternativeDateSlider(this.getContext(),mDateTimeSetListenerStart,c);
        case GET_END_DATETIME:
        	c.setTimeInMillis(this.mCategory.getEndTime());
            return new AlternativeDateSlider(this.getContext(),mDateTimeSetListenerEnd,c);
        case GET_END_DATETIME_NOW:
        	c.setTimeInMillis(TimeTrackerManager.currentTimeMillis());
            return new AlternativeDateSlider(this.getContext(),mDateTimeSetListenerEnd,c);
        }
        return null;
	}

}
