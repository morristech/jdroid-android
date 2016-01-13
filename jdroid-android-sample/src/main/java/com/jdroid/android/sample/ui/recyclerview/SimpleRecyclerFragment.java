package com.jdroid.android.sample.ui.recyclerview;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jdroid.android.fragment.FragmentHelper;
import com.jdroid.android.recycler.AbstractRecyclerFragment;
import com.jdroid.android.recycler.RecyclerViewAdapter;
import com.jdroid.android.recycler.RecyclerViewType;
import com.jdroid.android.sample.R;
import com.jdroid.android.sample.usecase.SampleUseCase;
import com.jdroid.java.utils.IdGenerator;

public class SimpleRecyclerFragment extends AbstractRecyclerFragment {

	private SampleUseCase sampleUseCase;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sampleUseCase = getInstance(SampleUseCase.class);
	}

	@Override
	public void onResume() {
		super.onResume();
		onResumeUseCase(sampleUseCase, this, FragmentHelper.UseCaseTrigger.ONCE);
	}

	@Override
	public void onPause() {
		super.onPause();
		onPauseUseCase(sampleUseCase, this);
	}

	@Override
	public void onFinishUseCase() {
		executeOnUIThread(new Runnable() {
			@Override
			public void run() {
				setAdapter(new RecyclerViewAdapter(new StringRecyclerViewType(), sampleUseCase.getItems()));
				dismissLoading();
			}
		});
	}

	@Override
	public Integer getMenuResourceId() {
		return R.menu.recycler_menu;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add:
				getAdapter().addItem(IdGenerator.getIntId().toString());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public class StringRecyclerViewType extends RecyclerViewType<String, StringViewHolder> {

		@Override
		protected Integer getLayoutResourceId() {
			return R.layout.item;
		}

		@Override
		protected Class<String> getItemClass() {
			return String.class;
		}

		@Override
		public RecyclerView.ViewHolder createViewHolderFromView(View view) {
			StringViewHolder viewHolder = new StringViewHolder(view);
			viewHolder.textView = (TextView)view.findViewById(R.id.name);
			return viewHolder;
		}

		@Override
		public void fillHolderFromItem(String item, StringViewHolder holder) {
			holder.textView.setText(item);
		}

		@Override
		public AbstractRecyclerFragment getAbstractRecyclerFragment() {
			return SimpleRecyclerFragment.this;
		}

		@Override
		public void onItemSelected(String item, View view) {
			getAdapter().removeItem(item);
		}
	}

	public static class StringViewHolder extends RecyclerView.ViewHolder {

		public TextView textView;

		public StringViewHolder(View itemView) {
			super(itemView);
		}
	}
}