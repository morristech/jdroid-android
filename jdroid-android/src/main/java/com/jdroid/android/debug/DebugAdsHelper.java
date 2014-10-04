package com.jdroid.android.debug;

import android.app.Activity;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import com.jdroid.android.R;
import com.jdroid.android.context.AppContext;

public class DebugAdsHelper {
	
	public static void initPreferences(final Activity activity, PreferenceScreen preferenceScreen) {
		
		PreferenceCategory preferenceCategory = new PreferenceCategory(activity);
		preferenceCategory.setTitle(R.string.ads);
		preferenceScreen.addPreference(preferenceCategory);
		
		CheckBoxPreference checkBoxPreference = new CheckBoxPreference(activity);
		checkBoxPreference.setKey(AppContext.ADS_ENABLED);
		checkBoxPreference.setTitle(R.string.adsEnabledTitle);
		checkBoxPreference.setSummary(R.string.adsEnabledDescription);
		preferenceCategory.addPreference(checkBoxPreference);
	}
}
