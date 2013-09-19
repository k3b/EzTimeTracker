package com.zettsett.timetracker.activity;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.database.*;
import com.zettsett.timetracker.model.TimeSliceCategory;

public class CategoryListActivity extends ListActivity implements CategorySetter {
	private static final int MENU_ADD_CATEGORY = Menu.FIRST;
	private static final int EDIT_MENU_ID = Menu.FIRST + 1;
	private static final int DELETE_MENU_ID = Menu.FIRST + 2;
	private TimeSliceCategory categoryClicked;
	private final TimeSliceCategoryRepsitory categoryRepository = new TimeSliceCategoryRepsitory(
			this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.category_list);
		registerForContextMenu(getListView());
		refreshCategoryList();
	}

	private void refreshCategoryList() {
		setListAdapter(CategoryListAdapterDetailed.createAdapter(this,
				R.layout.category_list_view_row, true, TimeSliceCategory.NO_CATEGORY, TimeSliceCategory.MIN_VALID_DATE));

	}

	public void setCategory(TimeSliceCategory category) {
		if (category == TimeSliceCategory.NO_CATEGORY) {
			showCategoryEditDialog(null);
			return;
		} else if (category.getRowId() == TimeSliceCategory.NOT_SAVED) {
			categoryRepository.createTimeSliceCategory(category);
		} else {
			categoryRepository.update(category);
		}
		refreshCategoryList();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		categoryClicked = (TimeSliceCategory) getListView().getItemAtPosition(
				((AdapterContextMenuInfo) menuInfo).position);
		
		if (categoryClicked != null)
			menu.setHeaderTitle("" + categoryClicked.getCategoryName());
		menu.add(0, EDIT_MENU_ID, 0, R.string.cmd_edit);
		menu.add(0, DELETE_MENU_ID, 0, R.string.cmd_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case EDIT_MENU_ID:
			if (categoryClicked == TimeSliceCategory.NO_CATEGORY) {
				showCategoryEditDialog(null);
			} else {
				showCategoryEditDialog(categoryClicked);
			}
			return true;
		case DELETE_MENU_ID:
			TimeSliceRepository timeSliceRepository = new TimeSliceRepository(this);
			if (timeSliceRepository.categoryHasTimeSlices(categoryClicked)) {
				showDialog(DELETE_MENU_ID);
			} else {
				categoryRepository.delete(categoryClicked.getRowId());
			}
			refreshCategoryList();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private CategoryEditDialog edit = null;
	public void showCategoryEditDialog(TimeSliceCategory category)
	{
		if (this.edit == null)
		{
			this.edit = new CategoryEditDialog(this, this);
		}
		this.edit.setCategory(category);
		showDialog(EDIT_MENU_ID);
	}
	
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case EDIT_MENU_ID:
			case MENU_ADD_CATEGORY:
				return this.edit;
			case DELETE_MENU_ID:
				return createDeleteWarningDialog();
		}

	    return null;
	}
	
	private Dialog createDeleteWarningDialog() {
		final CharSequence[] items = { "Go ahead and delete it.", "Don't delete it." };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Warning: time is already assigned to " + categoryClicked + ":");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					categoryRepository.delete(categoryClicked.getRowId());
					refreshCategoryList();
				}
			}
		});
		return builder.create();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ADD_CATEGORY, 0, "Create a new category.");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD_CATEGORY:
			showCategoryEditDialog(null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
