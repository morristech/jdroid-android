package com.jdroid.android.sample.ui.google.inappbilling;

import android.support.v4.app.Fragment;

import com.jdroid.android.activity.FragmentContainerActivity;

public class GoogleInAppBillingActivity extends FragmentContainerActivity {

	@Override
	protected Class<? extends Fragment> getFragmentClass() {
		return GoogleInAppBillingFragment.class;
	}
}