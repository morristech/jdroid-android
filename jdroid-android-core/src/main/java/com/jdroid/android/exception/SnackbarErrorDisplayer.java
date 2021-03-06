package com.jdroid.android.exception;

import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.jdroid.android.snackbar.SnackbarBuilder;

public class SnackbarErrorDisplayer extends AbstractErrorDisplayer {

	private SnackbarBuilder snackbarBuilder = new SnackbarBuilder();

	@Override
	public void onDisplayError(FragmentActivity activity, String title, String description, Throwable throwable) {
		if (activity != null) {
			snackbarBuilder.setDescription(description);
			Snackbar snackbar = snackbarBuilder.build(activity);
			snackbar.show();
		}
	}

	public void setActionTextResId(@StringRes int actionTextResId) {
		snackbarBuilder.setActionTextResId(actionTextResId);
	}

	public void setOnClickListener(View.OnClickListener onClickListener) {
		snackbarBuilder.setOnClickListener(onClickListener);
	}

	public void setParentLayoutId(@IdRes int parentLayoutId) {
		snackbarBuilder.setParentLayoutId(parentLayoutId);
	}

	public void setDuration(@Snackbar.Duration int duration) {
		snackbarBuilder.setDuration(duration);
	}
}
