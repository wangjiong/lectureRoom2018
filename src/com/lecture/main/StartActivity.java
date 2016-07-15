package com.lecture.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import com.lecture.data.DbData;
import com.lecture.media.R;

public class StartActivity extends Activity {
	ImageView welcomeImg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		welcomeImg = new ImageView(this);
		welcomeImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
		setContentView(welcomeImg);
		// 初始化数据库非常重要
		new Thread() {
			public void run() {
				new DbData(getApplicationContext());
			}
		}.start();
		AlphaAnimation anima = new AlphaAnimation(0.3f, 1.0f);
		anima.setDuration(3000);// 设置动画显示时间
		welcomeImg.startAnimation(anima);
		anima.setAnimationListener(new AnimationImpl());
	}

	private class AnimationImpl implements AnimationListener {

		@Override
		public void onAnimationStart(Animation animation) {
			welcomeImg.setBackgroundResource(R.drawable.c15);
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			skip(); // 动画结束后跳转到别的页面
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

	}

	private void skip() {
		new Thread() {
			public void run() {
				while (true) {
					if (DbData.sPrepare) {
						StartActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								StartActivity.this.startActivity(new Intent(StartActivity.this, MainActivity.class));
								finish();
							}
						});
						break;
					}
				}
			}
		}.start();
	}
}
