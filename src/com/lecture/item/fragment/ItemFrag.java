package com.lecture.item.fragment;

import com.lecture.media.R;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class ItemFrag extends Fragment {
	private TextView recommend = null;
	private TextView classify = null;
	private TextView search = null;
	private TextView account = null;
	private Callbacks mCallbacks;

	public interface Callbacks {
		public void onItemSelected(Integer id);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.item_fragment, container, false);
		recommend = (TextView) view.findViewById(R.id.recommend);
		classify = (TextView) view.findViewById(R.id.classify);
		search = (TextView) view.findViewById(R.id.search);
		account = (TextView) view.findViewById(R.id.account);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException("ItemFragment");
		}
		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		recommend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallbacks.onItemSelected(1);
			}
		});
		classify.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallbacks.onItemSelected(2);
			}
		});
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallbacks.onItemSelected(3);
			}
		});
		account.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallbacks.onItemSelected(4);
			}
		});
	}
}
