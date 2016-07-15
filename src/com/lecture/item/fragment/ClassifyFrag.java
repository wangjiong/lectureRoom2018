package com.lecture.item.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.lecture.data.ProgramBean;
import com.lecture.item.activity.ClassifyAct;
import com.lecture.item.adapter.MoreAdapter;
import com.lecture.media.R;
import com.lecture.util.Param;
import com.lecture.util.Util;

public class ClassifyFrag extends Fragment {
	// 数据
	ArrayList<ProgramBean> mMorePrograms = new ArrayList<ProgramBean>();
	String[] texts = new String[] { "排行版", "经典荟萃", "夏商周", "春秋战国", "秦", "汉", "三国", "晋", "隋", "唐", "宋", "元", "明", "清", "近代" };
	int[] images = new int[] { R.drawable.c14, R.drawable.c15, R.drawable.c01, R.drawable.c02, R.drawable.c03, R.drawable.c04, R.drawable.c05, R.drawable.c06, R.drawable.c07, R.drawable.c08, R.drawable.c09, R.drawable.c10, R.drawable.c11, R.drawable.c12, R.drawable.c13 };
	// 布局
	GridView gridView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
	}

	private void initData() {
		for (int i = 0; i < images.length; i++) {
			ProgramBean programBean = new ProgramBean();
			programBean.drawable = getActivity().getResources().getDrawable(images[i]);
			programBean.setName(texts[i]);
			mMorePrograms.add(programBean);
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// 布局
		gridView = new GridView(getActivity());
		gridView.setNumColumns(3);
		gridView.setHorizontalSpacing(Util.dip2px(getActivity(), 2));
		gridView.setVerticalSpacing(Util.dip2px(getActivity(), 10));
		gridView.setColumnWidth(10);
		gridView.setSelector(new ColorDrawable(Color.BLACK));
		gridView.setAdapter(new MoreAdapter(getActivity(), mMorePrograms));
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO 自动生成的方法存根
				Intent intent = new Intent(getActivity(), ClassifyAct.class);
				intent.putExtra(Param.CLASSIFY_TYPE, position);
				startActivity(intent);
			}
		});
		return gridView;
	}
}
