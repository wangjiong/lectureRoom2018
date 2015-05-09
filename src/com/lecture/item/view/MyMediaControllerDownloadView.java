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

import com.lecture.data.DownloadBean;
import com.lecture.media.R;

public class MyMediaControllerDownloadView extends MediaController {
	// 数据
	private DownloadBean downloadBean;
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

	public MyMediaControllerDownloadView(Activity arg0,DownloadBean arg1) {
		super(arg0);
		// 数据
		this.mActivity = arg0;
		this.downloadBean = arg1;
		// 布局
		mView = LayoutInflater.from(getContext()).inflate(R.layout.media_controller, null);
		back = (TextView) mView.findViewById(R.id.back);
		title = (TextView) mView.findViewById(R.id.title);
		time = (TextView) mView.findViewById(R.id.time);
		power = (ImageView) mView.findViewById(R.id.power);
		download = (TextView) mView.findViewById(R.id.media_controller_download);
		download.setVisibility(View.GONE);
		// 添加响应事件
		title.setText("《" + downloadBean.getTitle() + "》 第" + downloadBean.getEpisode() + "集 " + downloadBean.getName());
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mActivity.finish();
			}
		});
	}

	@Override
	public void show(int timeout) {
		super.show(timeout);
		// 通过SimpleDateFormat获取24小时制时间
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm" , Locale.getDefault());
		time.setText(sdf.format(new Date()));
		// 获取电池电量
		Intent batteryInfoIntent = mActivity.getApplicationContext().registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
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
