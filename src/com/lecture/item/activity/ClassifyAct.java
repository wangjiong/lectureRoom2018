package com.lecture.item.activity;

import java.util.ArrayList;
import java.util.Collections;

import com.lecture.data.DbData;
import com.lecture.data.ProgramBean;
import com.lecture.item.adapter.MoreAdapter;
import com.lecture.util.Param;
import com.lecture.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class ClassifyAct extends Activity {
	ArrayList<ProgramBean> classifyPrograms;
	// ����
	GridView gridView;

	private void initData() {
		int classifyType = getIntent().getIntExtra(Param.CLASSIFY_TYPE, 0);
		classifyPrograms = DbData.getProgramBeansClassify(classifyType);
		for (int i = 0; i < classifyPrograms.size(); i++) {
			classifyPrograms.get(i).setName("��" + classifyPrograms.get(i).getName() + "��");
		}
		Collections.reverse(classifyPrograms);
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
		gridView.setAdapter(new MoreAdapter(this, classifyPrograms));
		this.setContentView(gridView);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Intent intent = new Intent(ClassifyAct.this, MovieIntroAct.class);
				intent.putExtra(Param.MOVIE_KEY, classifyPrograms.get(position).getId());
				startActivity(intent);
			}
		});
	}
}