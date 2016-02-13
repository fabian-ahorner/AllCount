package com.bitflake.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class IntegerObservable extends SettingsObservable<Integer> {
	public IntegerObservable(String id, Integer defaultValue) {
		super(id, defaultValue);
	}

	@Override
	protected void save(Integer value, Editor e, String key) {
		e.putInt(key, value);
	}

	@Override
	protected Integer load(SharedPreferences sp, String key) {
		return sp.getInt(key, getDefaultValue());
	}

	@Override
	protected String getKey() {
		return "int";
	}

	public void increment() {
		set(get() + 1);
	}
}
