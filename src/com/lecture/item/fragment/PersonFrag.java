package com.lecture.item.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lecture.item.activity.PersonDownloadAct;
import com.lecture.item.activity.PersonHistoryAct;
import com.lecture.media.R;

public class PersonFrag extends Fragment {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
	}

	private void initData() {

	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.item_person, null);
		view.findViewById(R.id.history).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), PersonHistoryAct.class);
				startActivity(intent);
			}
		});
		view.findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), PersonDownloadAct.class);
				startActivity(intent);
			}
		});
		return view;
	}
}
