package com.bitflake.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class BooleanObservable extends SettingsObservable<Boolean> {
	public BooleanObservable(String id, Boolean defaultValue) {
		super(id, defaultValue);
	}

	@Override
	protected void save(Boolean value, Editor e, String key) {
		e.putBoolean(key, value);
	}

	@Override
	protected Boolean load(SharedPreferences sp, String key) {
		return sp.getBoolean(key, getDefaultValue());
	}

	@Override
	protected String getKey() {
		return "boolean";
	}

	public void invert() {
		set(!get());
	}
}
