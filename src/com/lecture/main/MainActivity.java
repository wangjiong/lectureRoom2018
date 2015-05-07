package com.lecture.main;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.lecture.data.DbData;
import com.lecture.media.R;

public class MainActivity extends Activity implements ItemFragment.Callbacks {
	ItemRecommend itemRecommend;
	ItemPerson itemPerson;
	int fragment = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		initView();
	}

	private void initData() {
		new DbData(this);
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
			if (itemRecommend != null) {
				ft.show(itemRecommend);
			} else {
				itemRecommend = new ItemRecommend();
				ft.add(R.id.content, itemRecommend);
			}
			break;
		case 4:
			if (itemPerson != null) {
				ft.show(itemPerson);
			} else {
				itemPerson = new ItemPerson();
				ft.add(R.id.content, itemPerson);
			}
			break;
		}
		ft.commit();
	}

	public void hideFragments(FragmentTransaction ft) {
		if (itemRecommend != null)
			ft.hide(itemRecommend);
		if (itemPerson != null)
			ft.hide(itemPerson);
	}

	private long mExitTime;

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 1000) {
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();

			} else {
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}