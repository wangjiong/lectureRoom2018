package com.lecture.main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
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

import com.lecture.data.DbData;
import com.lecture.data.DownloadBean;
import com.lecture.data.ProgramBean;
import com.lecture.data.UnitBean;
import com.lecture.media.R;

public class MyMediaController extends MediaController {
	// 数据
	private UnitBean unitBean;
	private String[] Urls;
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

	public MyMediaController(Activity arg0, UnitBean arg1, final String[] arg2) {
		super(arg0);
		// 数据
		this.mActivity = arg0;
		this.unitBean = arg1;
		this.Urls = arg2;
		// 布局
		mView = LayoutInflater.from(getContext()).inflate(R.layout.media_controller, null);
		back = (TextView) mView.findViewById(R.id.back);
		title = (TextView) mView.findViewById(R.id.title);
		time = (TextView) mView.findViewById(R.id.time);
		power = (ImageView) mView.findViewById(R.id.power);
		download = (TextView) mView.findViewById(R.id.media_controller_download);
		// 添加响应事件
		title.setText("《" + unitBean.getTitle() + "》 第" + unitBean.getEpisode() + "集 " + unitBean.getName());
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mActivity.finish();
			}
		});
		// 下载事件
		download.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (isDownload) {
					Toast.makeText(mActivity, "正在下载", Toast.LENGTH_SHORT).show();
					return;
				}
				Toast.makeText(mActivity, "开始下载", Toast.LENGTH_SHORT).show();
				// 创建文件夹
				file = DbData.createDownload(unitBean);
				if (file.listFiles() != null) {
					segment = file.listFiles().length;
					if(segment==0)
						segment++;
					if (segment == unitBean.getSegment()) {
						Toast.makeText(mActivity, "下载完成", Toast.LENGTH_SHORT).show();
						return;
					} else {
						File f = new File(file + "/movie" + (segment > 9 ? segment : ("0" + segment)) + ".mp4");
						if (f.exists())
							f.delete();
					}
				}
				download();
				isDownload = true;
			}
		});
	}

	// 下载
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void download() {
		FinalHttp fh = new FinalHttp();
		handler = fh.download(Urls[segment - 1], new AjaxParams(), file + "/movie" + (segment > 9 ? segment : ("0" + segment)) + ".mp4", true, new AjaxCallBack() {
			@Override
			public void onLoading(long count, long current) {

			}

			@Override
			public void onSuccess(Object t) {
				if (segment != unitBean.getSegment()) {
					segment++;
					download();
					if (PersonDownload.adapter != null && PersonDownload.downloadBeans != null) {
						PersonDownload.downloadBeans = DbData.readDownload();
						Collections.reverse(PersonDownload.downloadBeans);
						PersonDownload.adapter.notifyDataSetChanged();
					}

				} else {
					Toast.makeText(mActivity, "下载完成", Toast.LENGTH_SHORT).show();
					if (PersonDownload.adapter != null && PersonDownload.downloadBeans != null) {
						PersonDownload.downloadBeans = DbData.readDownload();
						Collections.reverse(PersonDownload.downloadBeans);
						PersonDownload.adapter.notifyDataSetChanged();
					}
					isDownload = false;
				}
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				System.out.println("onFailure    " + "t" + t.toString() + "    errorNo" + errorNo + "    strMsg" + strMsg);
				download();
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
