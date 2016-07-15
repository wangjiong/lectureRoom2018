package com.lecture.item.view;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.tsz.afinal.http.HttpHandler;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.lecture.data.UnitBean;
import com.lecture.item.activity.PersonDownloadingAct;
import com.lecture.media.R;
import com.lecture.util.DownLoad;
import com.lecture.util.Util;

public class MyMediaControllerView extends MediaController {
	// 数据
	private UnitBean mUnitBean;
	// 布局
	private Activity mActivity;
	private View mView;
	private TextView back;
	public TextView download;
	public TextView title;
	private TextView time;
	private ImageView power;
	// 下载
	HttpHandler<File> handler;
	boolean isDownload = false;
	int segment = 1;
	File file;

	public MyMediaControllerView(Activity arg0, UnitBean arg1, final String[] arg2) {
		super(arg0);
		// 数据
		this.mActivity = arg0;
		this.mUnitBean = arg1;
		// 布局
		mView = LayoutInflater.from(getContext()).inflate(R.layout.media_controller, null);
		back = (TextView) mView.findViewById(R.id.back);
		title = (TextView) mView.findViewById(R.id.title);
		time = (TextView) mView.findViewById(R.id.time);
		power = (ImageView) mView.findViewById(R.id.power);
		download = (TextView) mView.findViewById(R.id.media_controller_download);
		// 添加响应事件
		title.setText("《" + mUnitBean.getTitle() + "》 第" + mUnitBean.getEpisode() + "集 " + mUnitBean.getName());
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mActivity.finish();
			}
		});
		// 下载事件
		download.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (Util.isFastDoubleClick()) {
					Toast.makeText(mActivity, "不要频繁点击下载哦~", Toast.LENGTH_SHORT).show();
					return;
				}
				new DownLoad(mUnitBean).downLoad();
			}
		});
	}

	@Override
	public void show(int timeout) {
		super.show(timeout);
		// 通过SimpleDateFormat获取24小时制时间
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
		time.setText(sdf.format(new Date()));
		// 获取电池电量
		Intent batteryInfoIntent = mActivity.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int level = batteryInfoIntent.getIntExtra("level", 0);
		if (level > 80) {
			power.setBackgroundResource(R.drawable.power01);
		} else if (level > 40) {
			power.setBackgroundResource(R.drawable.power02);
		} else if (level > 15) {
			power.setBackgroundResource(R.drawable.power03);
		} else {
			power.setBackgroundResource(R.drawable.power04);
		}

		((ViewGroup) mActivity.findViewById(android.R.id.content)).removeView(mView);
		((ViewGroup) mActivity.findViewById(android.R.id.content)).addView(mView);
	}

	@Override
	public void hide() {
		super.hide();
		((ViewGroup) mActivity.findViewById(android.R.id.content)).removeView(mView);
	}
}
