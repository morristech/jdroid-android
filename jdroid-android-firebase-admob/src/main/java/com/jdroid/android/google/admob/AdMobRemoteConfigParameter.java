package com.jdroid.android.google.admob;

import com.jdroid.android.firebase.remoteconfig.RemoteConfigParameter;

public enum AdMobRemoteConfigParameter implements RemoteConfigParameter {

	ADS_ENABLED("ADS_ENABLED", false),
	ADMOB_APP_ID("ADMOB_APP_ID"),
	DEFAULT_AD_UNIT_ID("DEFAULT_AD_UNIT_ID");

	private String key;
	private Object defaultValue;

	AdMobRemoteConfigParameter(String key) {
		this(key, null);
	}

	AdMobRemoteConfigParameter(String key, Object defaultValue) {
		this.key = key;
		this.defaultValue = defaultValue;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public Object getDefaultValue() {
		return AdMobAppModule.get().getAdMobAppContext().getBuildConfigValue(key, defaultValue);
	}

	@Override
	public Boolean isABTestingExperiment() {
		return false;
	}
}