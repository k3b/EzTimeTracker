package de.k3b.timetracker.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.k3b.timetracker.R;
import de.k3b.timetracker.Settings;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onResume() {
		super.onResume();
		Settings.init(this);
	}
}
