package com.lecture.main;

import java.util.HashMap;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.HttpHandler;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.lecture.data.DbData;
import com.lecture.data.ProgramBean;
import com.lecture.item.fragment.ClassifyFrag;
import com.lecture.item.fragment.ItemFrag;
import com.lecture.item.fragment.PersonFrag;
import com.lecture.item.fragment.RecommendFrag;
import com.lecture.item.fragment.SearchFrag;
import com.lecture.media.R;
import com.lecture.util.DownLoad;

public class MainActivity extends Activity implements ItemFrag.Callbacks {
	RecommendFrag recommendFrag;
	ClassifyFrag classifyFrag;
	SearchFrag searchFrag;
	PersonFrag personFrag;
	int fragment = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
	}

	private void initView() {
		setContentView(R.layout.main);
		onItemSelected(1);
	}

	@Override
	public void onItemSelected(Integer id) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		hideFragments(ft);
		switch (id) {
		case 1:
			if (recommendFrag != null) {
				ft.show(recommendFrag);
			} else {
				recommendFrag = new RecommendFrag();
				classifyFrag = new ClassifyFrag();
				ft.add(R.id.content, classifyFrag);
				ft.hide(classifyFrag);
				ft.add(R.id.content, recommendFrag);
			}
			break;
		case 2:
			if (classifyFrag != null) {
				ft.show(classifyFrag);
			} else {
				classifyFrag = new ClassifyFrag();
				ft.add(R.id.content, classifyFrag);
			}
			break;
		case 3:
			if (searchFrag != null) {
				ft.show(searchFrag);
			} else {
				searchFrag = new SearchFrag();
				ft.add(R.id.content, searchFrag);
			}
			break;
		case 4:
			if (personFrag != null) {
				ft.show(personFrag);
			} else {
				personFrag = new PersonFrag();
				ft.add(R.id.content, personFrag);
			}
			break;
		}
		ft.commit();
	}

	public void hideFragments(FragmentTransaction ft) {
		if (recommendFrag != null)
			ft.hide(recommendFrag);
		if (classifyFrag != null)
			ft.hide(classifyFrag);
		if (searchFrag != null)
			ft.hide(searchFrag);
		if (personFrag != null)
			ft.hide(personFrag);
	}

	private long mExitTime;

	@SuppressWarnings("rawtypes")
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 1000) {
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				for (HashMap.Entry<String, HttpHandler> entry : DownLoad.sHandlerMap.entrySet()) {
					entry.getValue().stop();
				}
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}