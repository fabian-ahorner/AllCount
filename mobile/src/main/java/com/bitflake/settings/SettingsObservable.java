package com.bitflake.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public abstract class SettingsObservable<T> {
	public static final String STORAGE_ID = "storage";
	private static SharedPreferences sharedPreferences;
	private String id;
	private T defaultValue;

	protected abstract void save(T value, Editor e, String key);

	protected abstract T load(SharedPreferences sp, String key);

	protected abstract String getKey();

	public void set(T value) {
		Editor e = sharedPreferences.edit();
		save(value, e, id);
		e.commit();
	}

	public T get() {
		return load(sharedPreferences, id);
	}


	public static void init(Context context) {
		sharedPreferences = context.getSharedPreferences(STORAGE_ID,
				Context.MODE_PRIVATE);
	}

	public SettingsObservable(String id, T defaultValue) {
		this.id = id + "_" + getKey();
		this.defaultValue = defaultValue;
	}

	protected T getDefaultValue() {
		return defaultValue;
	}
}
