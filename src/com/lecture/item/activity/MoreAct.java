package com.lecture.item.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.lecture.data.DbData;
import com.lecture.data.ProgramBean;
import com.lecture.item.adapter.MoreAdapter;
import com.lecture.util.Param;
import com.lecture.util.Util;

public class MoreAct extends Activity {
	// 数据
	ArrayList<ProgramBean> morePrograms;
	// 布局
	GridView gridView;

	private void initData() {
		morePrograms = DbData.getProgramBeansMore();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		gridView = new GridView(this);
		gridView.setNumColumns(3);
		gridView.setHorizontalSpacing(Util.dip2px(this, 2));
		gridView.setVerticalSpacing(Util.dip2px(this, 10));
		gridView.setColumnWidth(10);
		gridView.setSelector(new ColorDrawable(Color.BLACK));
		gridView.setAdapter(new MoreAdapter(this, morePrograms));
		this.setContentView(gridView);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO 自动生成的方法存根
				Intent intent = new Intent(MoreAct.this, MovieIntroAct.class);
				intent.putExtra(Param.MOVIE_KEY, morePrograms.get(arg2).getId());
				startActivity(intent);
			}
		});
	}
}
