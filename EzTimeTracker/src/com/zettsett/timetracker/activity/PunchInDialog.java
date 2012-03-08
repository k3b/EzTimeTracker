package com.zettsett.timetracker.activity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.model.TimeSliceCategory;
import com.zettsett.timetracker.model.TimeSliceCategoryAdapter;

public class PunchInDialog extends Dialog {

	private final ListView list;

	public PunchInDialog(Context context, int style) {
		super(context, style);
		// setTitle("Punch In for Activity");
		list = new ListView(context);
		list.setAdapter(TimeSliceCategoryAdapter.getTimeSliceCategoryAdapterFromDB(context,
				R.layout.punchin_list_view_row, false));
		LinearLayout contentView = new LinearLayout(context);
		contentView.setOrientation(LinearLayout.VERTICAL);
		contentView.addView(list);
		setContentView(contentView);
	}

	public void setCallback(final MainActivity callback) {
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				TimeSliceCategory cat = (TimeSliceCategory) list.getItemAtPosition(position);
				callback.punchInClock(cat);
				hide();
			}

		});
	}

}
