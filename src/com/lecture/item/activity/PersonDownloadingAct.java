package com.lecture.item.activity;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lecture.data.DbData;
import com.lecture.data.DownloadBean;
import com.lecture.data.UnitBean;
import com.lecture.media.R;
import com.lecture.util.DownLoad;
import com.lecture.util.Param;
import com.lecture.util.Util;

public class PersonDownloadingAct extends Activity implements OnItemClickListener, OnItemLongClickListener {
	// 数据
	public static MyBaseAdapter sAdapter;
	List<DownloadBean> mDownloadingBeans;
	DownloadBean mDownloadingBean;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		initView();
	}

	private void initData() {
		mDownloadingBeans = DbData.sDownloadingBeans;
		sAdapter = new MyBaseAdapter();
	}

	private void initView() {
		setContentView(R.layout.person_download);
		ListView listView = (ListView) findViewById(R.id.download_listView);
		((TextView) findViewById(R.id.person)).setText("正在下载");
		listView.setAdapter(sAdapter);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		TextView back = (TextView) findViewById(R.id.download_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	public class MyBaseAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			// TODO 自动生成的方法存根
			return mDownloadingBeans.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO 自动生成的方法存根
			return mDownloadingBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO 自动生成的方法存根
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(PersonDownloadingAct.this).inflate(R.layout.person_download_item, null);
				holder.imageView = (ImageView) convertView.findViewById(R.id.image_intro);
				holder.textView = (TextView) convertView.findViewById(R.id.text_intro);
				holder.textViewDownload = (TextView) convertView.findViewById(R.id.text_download);
				holder.DownloadState = (TextView) convertView.findViewById(R.id.download_state);
				holder.stopDown = (TextView) convertView.findViewById(R.id.stop_down);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.imageView.setImageBitmap(DbData.getImageFromAssetsFile("img" + DbData.getProgramBeanIdByUnitBeanTitle(mDownloadingBeans.get(position).getTitle()) + ".jpg"));
			holder.textView.setText("名称：《" + mDownloadingBeans.get(position).getTitle() + "》\n播放集数：" + mDownloadingBeans.get(position).getEpisode() + "\n标题：" + mDownloadingBeans.get(position).getName());
			Log.i("onLoading", "\n下载进度：" + mDownloadingBeans.get(position).getDownload() + "%");
			holder.textViewDownload.setText("\n下载进度：" + mDownloadingBeans.get(position).getDownload() + "%");
			if (DownLoad.sDownloading.contains(mDownloadingBeans.get(position).getTitle() + mDownloadingBeans.get(position).getEpisode())) {
				holder.DownloadState.setText("状态：下载中");
				holder.stopDown.setText("暂停");
			} else {
				holder.DownloadState.setText("状态：暂停");
				holder.stopDown.setText("下载");
			}
			holder.stopDown.setVisibility(View.VISIBLE);
			holder.stopDown.setTag(mDownloadingBeans.get(position));
			holder.stopDown.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (Util.isFastDoubleClick()) {
						Toast.makeText(PersonDownloadingAct.this, "不要频繁点击下载哦~", Toast.LENGTH_SHORT).show();
						return;
					}
					DownloadBean downloadBean = (DownloadBean) v.getTag();
					if (holder.stopDown.getText().equals("下载") && downloadBean != null) {
						if (new DownLoad(DbData.getUnitBeanByTitleAndEpisode(downloadBean.getTitle(), Integer.parseInt(downloadBean.getEpisode()))).downLoad()) {
							holder.stopDown.setText("暂停");
							holder.DownloadState.setText("状态：下载中");
						}
					} else {
						String s = downloadBean.getTitle() + downloadBean.getEpisode();
						if (DownLoad.sHandlerMap.get(s) != null) {
							DownLoad.sHandlerMap.get(s).stop();
							DownLoad.sDownloading.remove(s);
						}
						holder.stopDown.setText("下载");
						holder.DownloadState.setText("状态：暂停");
					}
				}
			});
			return convertView;
		}
	};

	class ViewHolder {
		public ImageView imageView;
		public TextView textView;
		public TextView textViewDownload;
		public TextView DownloadState;
		public TextView stopDown;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		mDownloadingBean = mDownloadingBeans.get(position);
		if (mDownloadingBean.getDownload() < 1) {
			Toast.makeText(PersonDownloadingAct.this, "下载数据太少了哦~", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent(PersonDownloadingAct.this, MovieAct.class);
		intent.putExtra(Param.FROM_TYPE, 3);
		intent.putExtra(Param.TITLE_KEY, mDownloadingBean.getTitle());
		intent.putExtra(Param.EPISODE_KEY, mDownloadingBean.getEpisode());
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		mDownloadingBean = mDownloadingBeans.get(position);
		dialog(position);
		return true;
	}

	protected void dialog(final int position) {
		AlertDialog.Builder builder = new Builder(PersonDownloadingAct.this);
		builder.setMessage("确认要删除吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (position >= mDownloadingBeans.size()) {
					dialog.dismiss();
					return;
				}
				mDownloadingBeans.remove(position);
				sAdapter.notifyDataSetChanged();
				if (DownLoad.sDownloading.contains(mDownloadingBean.getTitle() + mDownloadingBean.getEpisode())) {
					DownLoad.sDownloading.remove(mDownloadingBean.getTitle() + mDownloadingBean.getEpisode());
					String s = mDownloadingBean.getTitle() + mDownloadingBean.getEpisode();
					DownLoad.sHandlerMap.get(s).stop();
				}
				new Thread() {
					public void run() {
						DbData.sFinalDb.delete(mDownloadingBean);
						File[] files = DbData.fileDownload.listFiles();
						for (int i = 0; i < files.length; i++) {
							if (files[i].toString().contains(mDownloadingBean.getTitle() + " " + mDownloadingBean.getEpisode())) {
								DbData.deleteDir(files[i]);
								break;
							}
						}
					}
				}.start();
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		sAdapter = null;
		for (DownloadBean mDownloadingBean : mDownloadingBeans) {
			if (DownLoad.sDownloading.contains(mDownloadingBean.getTitle() + mDownloadingBean.getEpisode())) {
				// 创建记录下载进度的文件
				DbData.createDownloadState(DbData.createDownload(new UnitBean(mDownloadingBean.getTitle(), Integer.parseInt(mDownloadingBean.getEpisode()), mDownloadingBean.getName(), mDownloadingBean.getSegmentTotal())), mDownloadingBean.getDownload(), mDownloadingBean.getSegment());
			}
		}
	}
}
