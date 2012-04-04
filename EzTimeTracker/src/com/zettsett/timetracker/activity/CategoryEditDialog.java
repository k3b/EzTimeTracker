package com.zettsett.timetracker.activity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class CategoryEditDialog extends Dialog  {
	private final EditText catNameField;
	private final EditText catDescField;
	private final Button saveButton;
	private final Button cancelButton;

	private TimeSliceCategory mCategory;

	public CategoryEditDialog(Context context, 
			final CategorySetter owner) {
		super(context);
		setContentView(R.layout.edit_category);
		catNameField = (EditText) findViewById(R.id.edit_time_category_name_field);
		catDescField = (EditText) findViewById(R.id.edit_time_category_desc_field);
		saveButton = (Button) findViewById(R.id.edit_time_category_save_button);
		cancelButton = (Button) findViewById(R.id.edit_time_category_cancel_button);

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
			String caption = String.format(this.getContext().getString(R.string.format_title_edit_category).toString(), mCategory.getCategoryName());
			setTitle(caption);
			catNameField.setText(mCategory.getCategoryName());
			catDescField.setText(mCategory.getDescription());
		}
		super.show();
	}
}
