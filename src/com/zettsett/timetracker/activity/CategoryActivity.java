package com.zettsett.timetracker.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.database.DatabaseInstance;
import com.zettsett.timetracker.database.TimeSliceCategoryDBAdapter;
import com.zettsett.timetracker.database.TimeSliceDBAdapter;
import com.zettsett.timetracker.model.TimeSliceCategory;
import com.zettsett.timetracker.model.TimeSliceCategoryAdapter;

public class CategoryActivity extends ListActivity {
	private static final int MENU_ADD_CATEGORY = Menu.FIRST;
	private static final int EDIT_MENU_ID = Menu.FIRST + 1;
	private static final int DELETE_MENU_ID = Menu.FIRST + 2;
	private TimeSliceCategory categoryClicked;
	private final TimeSliceCategoryDBAdapter timeSliceCategoryDBAdapter = new TimeSliceCategoryDBAdapter(
			this);
	private boolean updating;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DatabaseInstance.open();
		setContentView(R.layout.category_list);
		registerForContextMenu(getListView());
		refreshCategoryList();
	}

	private void refreshCategoryList() {
		setListAdapter(TimeSliceCategoryAdapter.getTimeSliceCategoryAdapterFromDB(this,
				R.layout.category_list_view_row, true));

	}

	void onEditDialogSave(TimeSliceCategory category) {
		if (updating) {
			timeSliceCategoryDBAdapter.update(category);
		} else {
			timeSliceCategoryDBAdapter.createTimeSliceCategory(category);
		}
		refreshCategoryList();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		categoryClicked = (TimeSliceCategory) getListView().getItemAtPosition(
				((AdapterContextMenuInfo) menuInfo).position);
		menu.setHeaderTitle("" + categoryClicked.getCategoryName());
		menu.add(0, EDIT_MENU_ID, 0, "Edit");
		menu.add(0, DELETE_MENU_ID, 0, "Delete");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case EDIT_MENU_ID:
			updating = true;
			new CategoryEditDialog(this).buildEditDialog(categoryClicked, this).show();
			return true;
		case DELETE_MENU_ID:
			TimeSliceDBAdapter timeSliceDBAdapter = new TimeSliceDBAdapter(this);
			if (timeSliceDBAdapter.categoryHasTimeSlices(categoryClicked)) {
				showDeleteWarningDialog();
			} else {
				timeSliceCategoryDBAdapter.delete(categoryClicked.getRowId());
			}
			refreshCategoryList();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void showDeleteWarningDialog() {
		final CharSequence[] items = { "Go ahead and delete it.", "Don't delete it." };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Warning: time is already assigned to " + categoryClicked + ":");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					timeSliceCategoryDBAdapter.delete(categoryClicked.getRowId());
					refreshCategoryList();
				}
			}
		});
		builder.create().show();
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
			updating = false;
			new CategoryEditDialog(this).buildEditDialog(null, this).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
