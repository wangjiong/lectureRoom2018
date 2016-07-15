package com.lecture.item.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.lecture.data.BaseApi;
import com.lecture.item.activity.PersonAboutAct;
import com.lecture.item.activity.PersonDownloadAct;
import com.lecture.item.activity.PersonDownloadingAct;
import com.lecture.item.activity.PersonHistoryAct;
import com.lecture.media.R;
import com.lecture.util.SharedPrefsUtil;

public class PersonFrag extends Fragment {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
	}

	private void initData() {

	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
		view.findViewById(R.id.downloading).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), PersonDownloadingAct.class);
				startActivity(intent);
			}
		});

		view.findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText et = new EditText(getActivity());
				et.setText(BaseApi.ONLINE);
				new AlertDialog.Builder(getActivity()).setTitle("请输入API").setIcon(android.R.drawable.ic_dialog_info).setView(et).setPositiveButton("确定", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPrefsUtil.putValue(getActivity(), "lecture", SharedPrefsUtil.IP, et.getText().toString());
					}
				}).setNegativeButton("取消", null).show();
			}
		});

		view.findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), PersonAboutAct.class);
				startActivity(intent);
			}
		});
		return view;
	}
}
