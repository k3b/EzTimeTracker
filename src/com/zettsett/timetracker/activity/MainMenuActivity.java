package com.zettsett.timetracker.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.zetter.androidTime.R;

public class MainMenuActivity extends ListActivity {

	private final List<ActivityProfile> activityList = new ArrayList<ActivityProfile>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
		setupList();
	}

	private void setupList() {
		addActivity(MainActivity.class, getString(R.string.main_menu_MainActivity_name),
				getString(R.string.main_menu_MainActivity_desc));
		addActivity(TimeSheetReportActivity.class,
				getString(R.string.main_menu_TimeSheetReportActivity_name),
				getString(R.string.main_menu_TimeSheetReportActivity_desc));
		addActivity(SummaryReportActivity.class,
				getString(R.string.main_menu_SummaryReportActivity_name),
				getString(R.string.main_menu_SummaryReportActivity_desc));
		addActivity(CategoryActivity.class, getString(R.string.main_menu_CategoryActivity_name),
				getString(R.string.main_menu_CategoryActivity_desc));
		addActivity(DataExportActivity.class,
				getString(R.string.main_menu_DataExportActivity_name),
				getString(R.string.main_menu_DataExportActivity_desc));
		addActivity(RemoveTimeSliceActivity.class,
				getString(R.string.main_menu_RemoveTimeSliceActivity_name),
				getString(R.string.main_menu_RemoveTimeSliceActivity_desc));
		addActivity(SettingsActivity.class,
				getString(R.string.main_menu_SettingsActivity_name),
				getString(R.string.main_menu_SettingsActivity_desc));
		ActivityProfileAdapter adapter = new ActivityProfileAdapter(this,
				android.R.layout.simple_list_item_1, activityList);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				ActivityProfile ap = (ActivityProfile) getListView().getItemAtPosition(position);
				launchActivity(ap.activity);
			}
		});

	}

	private void addActivity(Class<? extends Activity> activity, String brief, String full) {
		ActivityProfile ap = new ActivityProfile();
		ap.activity = activity;
		ap.briefDescription = brief;
		ap.fullDescription = full;
		activityList.add(ap);
	}

	private void launchActivity(Class<? extends Activity> activity) {
		Intent i = new Intent(this, activity);
		startActivity(i);

	}

	private class ActivityProfile {
		String briefDescription;
		String fullDescription;
		Class<? extends Activity> activity;
	}

	private class ActivityProfileAdapter extends ArrayAdapter<ActivityProfile> {
		private final List<ActivityProfile> items;
		private final Context context;

		public ActivityProfileAdapter(Context context, int textViewResourceId,
				List<ActivityProfile> objects) {
			super(context, textViewResourceId, objects);
			this.items = objects;
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater vi = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = vi.inflate(R.layout.main_menu_row, null);
			}
			ActivityProfile category = items.get(position);
			if (category != null) {
				TextView tt = (TextView) view.findViewById(R.id.text_view_main_menu_row_name);
				TextView bt = (TextView) view.findViewById(R.id.text_view_main_menu_row_desc);
				tt.setText(category.briefDescription);
				bt.setText(category.fullDescription);
			}
			return view;
		}

	}
}
