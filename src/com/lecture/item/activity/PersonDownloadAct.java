package com.lecture.item.activity;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
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

import com.lecture.data.DbData;
import com.lecture.data.DownloadBean;
import com.lecture.media.R;

public class PersonDownloadAct extends Activity implements OnItemClickListener, OnItemLongClickListener {
	// 数据
	public static List<DownloadBean> downloadBeans = null;
	public static MyBaseAdapter adapter = null;
	public static DownloadBean downloadBean = null;
	public static String[] Urls;
	// 布局
	ListView listView;
	boolean isFirstStart = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		initView();
	}

	private void initData() {
		downloadBeans = DbData.readDownload();
		Collections.reverse(downloadBeans);
		adapter = new MyBaseAdapter();
	}

	private void initView() {
		setContentView(R.layout.person_download);
		listView = (ListView) findViewById(R.id.download_listView);
		listView.setAdapter(adapter);
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
			return downloadBeans.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO 自动生成的方法存根
			return downloadBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO 自动生成的方法存根
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(PersonDownloadAct.this).inflate(R.layout.person_download_item, null);
				holder.imageView = (ImageView) convertView.findViewById(R.id.image_intro);
				holder.textView = (TextView) convertView.findViewById(R.id.text_intro);
				holder.textViewDownload = (TextView) convertView.findViewById(R.id.text_download);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.imageView.setImageBitmap(DbData.getImageFromAssetsFile("img" + DbData.getProgramBeanIdByUnitBeanTitle(downloadBeans.get(position).getTitle()) + ".jpg"));
			holder.textView.setText("名称：《" + downloadBeans.get(position).getTitle() + "》\n集数：" + downloadBeans.get(position).getEpisode() + "\n标题：" + downloadBeans.get(position).getName());
			holder.textViewDownload.setText("\n下载进度：" + downloadBeans.get(position).getDownload() + "%");
			return convertView;
		}
	};

	class ViewHolder {
		public ImageView imageView;
		public TextView textView;
		public TextView textViewDownload;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		downloadBean = downloadBeans.get(position);
		File[] files = DbData.fileDownload.listFiles();
		files = files[files.length - position - 1].listFiles();
		Urls = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			Urls[i] = files[i].toString();
		}
		Arrays.sort(Urls);
		Intent intent = new Intent(this, MovieDownloadAct.class);
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		dialog(position);
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isFirstStart == true) {
			initData();
			listView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		} else {
			isFirstStart = false;
		}
	}

	protected void dialog(final int position) {
		AlertDialog.Builder builder = new Builder(PersonDownloadAct.this);
		builder.setMessage("确认要删除吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				File[] files = DbData.fileDownload.listFiles();
				DbData.deleteDir(files[files.length - position - 1]);
				downloadBeans.remove(position);
				adapter.notifyDataSetChanged();
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
}
