package de.k3b.timetracker.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

import de.k3b.timetracker.Factory;
import de.k3b.timetracker.R;
import de.k3b.timetracker.TimeSliceFilterParameter;
import de.k3b.timetracker.database.TimeSliceCategoryRepsitory;
import de.k3b.timetracker.database.TimeSliceRepository;
import de.k3b.timetracker.model.TimeSliceCategory;

public class CategoryListActivity extends ListActivity implements
        ICategorySetter {
    private static final int MENU_ADD_CATEGORY = Menu.FIRST;
    private static final int EDIT_MENU_ID = Menu.FIRST + 1;
    private static final int DELETE_MENU_ID = Menu.FIRST + 2;
    private static final int REPORT_MENU_ID = Menu.FIRST + 3;
    private TimeSliceCategoryRepsitory categoryRepository = null;
    private TimeSliceCategory categoryClicked;
    private CategoryEditDialog edit = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.category_list);
        categoryRepository = Factory.getInstance().createTimeSliceCategoryRepsitory(
                this);
        this.registerForContextMenu(this.getListView());
        this.refreshCategoryList();
    }

    private void refreshCategoryList() {
        this.setListAdapter(CategoryListAdapterDetailed.createAdapter(this,
                R.layout.category_list_view_row, true,
                TimeSliceCategory.NO_CATEGORY,
                TimeSliceCategory.MIN_VALID_DATE, "CategoryListActivity"));

    }

    @Override
    public void setCategory(final TimeSliceCategory category) {
        if (!TimeSliceCategory.isValid(category)) {
            this.showCategoryEditDialog(null);
            return;
        } else if (category.getRowId() == TimeSliceCategory.NOT_SAVED) {
            this.categoryRepository.createTimeSliceCategory(category);
        } else {
            this.categoryRepository.update(category);
        }
        this.refreshCategoryList();
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
                                    final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        this.categoryClicked = (TimeSliceCategory) this
                .getListView()
                .getItemAtPosition(((AdapterContextMenuInfo) menuInfo).position);

        if (this.categoryClicked != null) {
            menu.setHeaderTitle("" + this.categoryClicked.getCategoryName());
        }
        menu.add(0, CategoryListActivity.EDIT_MENU_ID, 0, R.string.cmd_edit);
        menu.add(0, CategoryListActivity.DELETE_MENU_ID, 0, R.string.cmd_delete);
        if (TimeSliceCategory.isValid(this.categoryClicked)) {
            menu.add(0, CategoryListActivity.REPORT_MENU_ID, 0,
                    R.string.cmd_report);
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case EDIT_MENU_ID:
                if (!TimeSliceCategory.isValid(this.categoryClicked)) {
                    this.showCategoryEditDialog(null);
                } else {
                    this.showCategoryEditDialog(this.categoryClicked);
                }
                return true;
            case DELETE_MENU_ID:
                final TimeSliceRepository timeSliceRepository = Factory.getInstance().createTimeSliceRepository(
                        this);

                final int count = timeSliceRepository.getCount(this.categoryClicked);
                if (count > 0) {
                    this.showDialog(CategoryListActivity.DELETE_MENU_ID);
                } else {
                    this.categoryRepository.delete(this.categoryClicked.getRowId());
                }
                this.refreshCategoryList();
                return true;
            case REPORT_MENU_ID:
                this.showDetailReport();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showDetailReport() {
        if (TimeSliceCategory.isValid(this.categoryClicked)) {
            final TimeSliceFilterParameter filter = new TimeSliceFilterParameter()
                    .setCategoryId(this.categoryClicked.getRowId())
                    .setIgnoreDates(true);
            TimeSheetDetailListActivity.showActivity(this, filter, 0);
        }
    }

    public void showCategoryEditDialog(final TimeSliceCategory category) {
        if (this.edit == null) {
            this.edit = new CategoryEditDialog(this, this);
        }
        this.edit.setCategory(category);
        this.showDialog(CategoryListActivity.EDIT_MENU_ID);
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case EDIT_MENU_ID:
            case MENU_ADD_CATEGORY:
                return this.edit;
            case DELETE_MENU_ID:
                return this.createDeleteWarningDialog();
        }

        return null;
    }

    private Dialog createDeleteWarningDialog() {
        final CharSequence[] items = {
                this.getResources().getString(R.string.cmd_delete_confirm), // "Go ahead and delete it.",
                this.getResources().getString(R.string.cmd_delete_cancel), // "Don't delete it.",
                this.getResources().getString(R.string.cmd_report) // Details
        };
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getResources().getString(
                R.string.cmd_delete_warning) // "Warning already in use : "
                + this.categoryClicked.getCategoryName());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int item) {
                if (item == 0) {
                    CategoryListActivity.this.categoryRepository
                            .delete(CategoryListActivity.this.categoryClicked
                                    .getRowId());
                    CategoryListActivity.this.refreshCategoryList();
                }
                if (item == 2) {
                    CategoryListActivity.this.showDetailReport();
                }
            }
        });
        return builder.create();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, CategoryListActivity.MENU_ADD_CATEGORY, 0,
                "Create a new category.");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_CATEGORY:
                this.showCategoryEditDialog(null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
