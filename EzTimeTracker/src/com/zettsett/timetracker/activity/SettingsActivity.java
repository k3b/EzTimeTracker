package com.zettsett.timetracker.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.Settings;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onPause() {
		super.onPause();
		Settings.initializeCurrentTimeFormatSetting(this);
	}

}