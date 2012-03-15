package com.zettsett.timetracker.activity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class CategoryEditDialog extends Dialog {

	public CategoryEditDialog(Context context) {
		super(context);
	}

	private TimeSliceCategory mCategory;

	public CategoryEditDialog buildEditDialog(TimeSliceCategory category,
			final CategoryActivity owner) {
		mCategory = category;
		setContentView(R.layout.edit_category);
		final EditText catNameField = (EditText) findViewById(R.id.edit_time_category_name_field);
		final EditText catDescField = (EditText) findViewById(R.id.edit_time_category_desc_field);
		final Button saveButton = (Button) findViewById(R.id.edit_time_category_save_button);
		final Button cancelButton = (Button) findViewById(R.id.edit_time_category_cancel_button);
		if (category == null) {
			mCategory = new TimeSliceCategory();
			setTitle(R.string.title_creating_a_new_category);
		} else {
			String caption = String.format(owner.getString(R.string.format_title_edit_category).toString(), mCategory.getCategoryName());
			setTitle(caption);
		}
		catNameField.setWidth(200);
		catDescField.setWidth(404);
		if (category != null) {
			catNameField.setText(mCategory.getCategoryName());
			catDescField.setText(mCategory.getDescription());
		} else {
			catNameField.setText("");
			catDescField.setText("");
			
		}
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCategory.setCategoryName(catNameField.getText().toString());
				mCategory.setDescription(catDescField.getText().toString());
				owner.onEditDialogSave(mCategory);
				dismiss();
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});

		return this;
	}

}
