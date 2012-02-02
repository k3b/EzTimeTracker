package com.zettsett.timetracker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.LinearLayout.LayoutParams;

public class LinearScroller {
	public LinearLayout mainLayout;
	public ViewGroup scrollView;
	private Context mContext;
	private LayoutParams mLayoutParams;

	public LinearScroller(Context context) {
		mContext = context;
		initScrollview();
	}

	public LinearScroller(Context context, LayoutParams layoutParams) {
		mContext = context;
		mLayoutParams = layoutParams;
		initScrollview();
	}

	public LinearLayout getMainLayout() {
		return mainLayout;
	}

	public ScrollView getScrollView() {
		return (ScrollView)scrollView;
	}

	public void initScrollview() {
		mainLayout = new LinearLayout(mContext);
		mainLayout.setOrientation(LinearLayout.VERTICAL);
		scrollView = new ScrollView(mContext);
		if (mLayoutParams != null) {
			scrollView.addView(mainLayout, mLayoutParams);
		} else {
			scrollView.addView(mainLayout);
		}
	}

	public void addView(View child) {
		mainLayout.addView(child);
	}

}