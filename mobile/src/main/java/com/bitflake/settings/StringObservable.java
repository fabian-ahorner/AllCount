package com.bitflake.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class StringObservable extends SettingsObservable<String> {
	public StringObservable(String id, String defaultValue) {
		super(id, defaultValue);
	}

	@Override
	protected void save(String value, Editor e, String key) {
		e.putString(key, value);
	}

	@Override
	protected String load(SharedPreferences sp, String key) {
		return sp.getString(key, getDefaultValue());
	}

	@Override
	protected String getKey() {
		return "string";
	}
}
