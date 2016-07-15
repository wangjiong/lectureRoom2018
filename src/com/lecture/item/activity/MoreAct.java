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
	ArrayList<ProgramBean> programBeans;
	// 布局
	GridView gridView;

	private void initData() {
		int moreType = getIntent().getExtras().getInt(Param.MORE_TYPE);
		int recommmnedType = getIntent().getExtras().getInt(Param.RECOMMEND_TYPE);
		String titleType = getIntent().getExtras().getString(Param.TITLE_TYPE);
		String authorType = getIntent().getExtras().getString(Param.AUTHOR_TYPE);
		String timeType = getIntent().getExtras().getString(Param.TIME_TYPE);
		if (moreType != -1) {// 从更多过来的数据
			programBeans = DbData.getProgramBeansMore(moreType);
		} else if (recommmnedType != -1) {// 从推荐过来数据
			programBeans = DbData.getProgramBeansRecommend(recommmnedType);
		} else if (titleType != null) {// 从搜索过来的数据，名称
			programBeans = DbData.getProgramBeanIdByTitle(titleType);
			// 除去重复的值
			ArrayList<ProgramBean> tempPrograms = new ArrayList<ProgramBean>();
			for (int i = 0; i < programBeans.size(); i++) {
				int j = 0;
				for (; j < tempPrograms.size(); j++) {
					if (tempPrograms.get(j).getName().equals(programBeans.get(i).getName())) {
						break;
					}
				}
				if (j == tempPrograms.size()) {
					tempPrograms.add(programBeans.get(i));
				}
			}
			programBeans = tempPrograms;
		} else if (authorType != null) { // 从搜索过来的数据，作者
			programBeans = DbData.getProgramBeansByAuthor(authorType);
			// 除去重复的值
			ArrayList<ProgramBean> tempPrograms = new ArrayList<ProgramBean>();
			for (int i = 0; i < programBeans.size(); i++) {
				int j = 0;
				for (; j < tempPrograms.size(); j++) {
					if (tempPrograms.get(j).getName().equals(programBeans.get(i).getName())) {
						break;
					}
				}
				if (j == tempPrograms.size()) {
					tempPrograms.add(programBeans.get(i));
				}
			}
			programBeans = tempPrograms;
		} else if (timeType != null) {// 从搜索过来的数据，时间
			programBeans = DbData.getProgramBeansByTime(timeType);
		}
		for (int i = 0; i < programBeans.size(); i++) {
			programBeans.get(i).setName("《" + programBeans.get(i).getName() + "》");
			programBeans.get(i).drawable = DbData.getDrawableById(programBeans.get(i).getId());
		}
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
		gridView.setAdapter(new MoreAdapter(this, programBeans));
		this.setContentView(gridView);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent intent = new Intent(MoreAct.this, MovieIntroAct.class);
				intent.putExtra(Param.MOVIE_KEY, programBeans.get(arg2).getId());
				startActivity(intent);
			}
		});
	}
}
