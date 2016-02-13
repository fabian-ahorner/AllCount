package com.bitflake.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class LongObservable extends SettingsObservable<Long> {
	public LongObservable(String id, Long defaultValue) {
		super(id, defaultValue);
	}

	@Override
	protected void save(Long value, Editor e, String key) {
		e.putLong(key, value);
	}

	@Override
	protected Long load(SharedPreferences sp, String key) {
		return sp.getLong(key, getDefaultValue());
	}

	@Override
	protected String getKey() {
		return "long";
	}

	public void increment() {
		set(get() + 1);
	}
}
