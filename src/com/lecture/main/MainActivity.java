package com.lecture.main;

import android.app.Activity;
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
		itemRecommend = new ItemRecommend();
		itemPerson = new ItemPerson();
		getFragmentManager().beginTransaction()
				.replace(R.id.content, itemRecommend).commit();
	}

	@Override
	public void onItemSelected(Integer id) {
		switch (id) {
		case 1:
			if (fragment != 1) {
				getFragmentManager().beginTransaction()
						.replace(R.id.content, itemRecommend).commit();
				fragment = 1;
			}
			break;
		case 4:
			if (fragment != 4) {
				getFragmentManager().beginTransaction()
						.replace(R.id.content, itemPerson).commit();
				fragment = 4;
			}
			break;
		}
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