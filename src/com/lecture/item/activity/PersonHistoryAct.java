package com.lecture.item.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import android.widget.Toast;

import com.lecture.data.DbData;
import com.lecture.data.DownloadBean;
import com.lecture.data.HistoryBean;
import com.lecture.media.R;
import com.lecture.util.Param;

public class PersonHistoryAct extends Activity implements OnItemClickListener, OnItemLongClickListener {
	public static int REQUEST_CODE = 100;
	// 数据
	final int LIMIT = 30;
	List<HistoryBean> mHistoryBeans = new ArrayList<HistoryBean>(LIMIT);
	HistoryBean mHistoryBean;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		initView();
	}

	private void initData() {
		List<HistoryBean> temp = null;
		if (DbData.sFinalDb != null) {
			temp = DbData.sFinalDb.findAll(HistoryBean.class);
			Collections.reverse(temp);
			if (temp != null && temp.size() > LIMIT) {
				for (int i = 0; i < LIMIT; i++) {
					mHistoryBeans.add(temp.get(i));
				}
			} else if (temp != null) {
				mHistoryBeans = temp;
			}
		}
	}

	private void initView() {
		setContentView(R.layout.person_history);
		ListView listView = (ListView) findViewById(R.id.history_listView);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		TextView back = (TextView) findViewById(R.id.history_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	BaseAdapter adapter = new BaseAdapter() {
		@Override
		public int getCount() {
			// TODO 自动生成的方法存根
			return mHistoryBeans.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO 自动生成的方法存根
			return mHistoryBeans.get(position);
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
				convertView = LayoutInflater.from(PersonHistoryAct.this).inflate(R.layout.person_history_item, null);
				holder.imageView = (ImageView) convertView.findViewById(R.id.image_intro);
				holder.textView = (TextView) convertView.findViewById(R.id.text_intro);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.imageView.setImageBitmap(DbData.getImageFromAssetsFile("img" + mHistoryBeans.get(position).getIdTime() + ".jpg"));
			holder.textView.setText("名称：《" + mHistoryBeans.get(position).getTitle() + "》\n总集数：" + mHistoryBeans.get(position).getNum() + "\n作者：" + mHistoryBeans.get(position).getAuthor() + "\n首播时间：" + mHistoryBeans.get(position).getIdTime().substring(0, 4) + "-"
					+ mHistoryBeans.get(position).getIdTime().substring(4, 6) + "-" + mHistoryBeans.get(position).getIdTime().substring(6) + "\n播放集数：" + mHistoryBeans.get(position).getEpisode() + "\n标题：" + mHistoryBeans.get(position).getName() + "\n播放时间："
					+ new SimpleDateFormat("mm:ss").format(new Date(mHistoryBeans.get(position).getPlayTime())));

			return convertView;
		}
	};

	class ViewHolder {
		public ImageView imageView;
		public TextView textView;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		mHistoryBean = mHistoryBeans.get(position);
		int i = 0;
		for (; i < DbData.sDownloadBeans.size(); i++) {
			DownloadBean d = DbData.sDownloadBeans.get(i);
			if ((mHistoryBean.getTitle() + mHistoryBean.getEpisode()).equals(d.getTitle() + d.getEpisode())) {
				break;
			}
		}
		if (i == DbData.sDownloadBeans.size()) { // 该视频并没有下载完成
			if (!DbData.isNetworkConnected(PersonHistoryAct.this)) {
				Toast.makeText(PersonHistoryAct.this, "当前网络不可用", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		Intent intent = new Intent(this, MovieAct.class);
		intent.putExtra(Param.FROM_TYPE, 2);
		intent.putExtra(Param.PLAY_TIME, mHistoryBean.getPlayTime());
		intent.putExtra(Param.TITLE_KEY, mHistoryBean.getTitle());
		intent.putExtra(Param.EPISODE_KEY, mHistoryBean.getEpisode());
		startActivityForResult(intent, PersonHistoryAct.REQUEST_CODE);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		mHistoryBean = mHistoryBeans.get(position);
		dialog(position);
		return true;
	}

	protected void dialog(final int position) {
		AlertDialog.Builder builder = new Builder(PersonHistoryAct.this);
		builder.setMessage("确认要删除吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mHistoryBeans.remove(position);
				adapter.notifyDataSetChanged();
				new Thread() {
					public void run() {
						DbData.sFinalDb.delete(mHistoryBean);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			long playTime = data.getLongExtra(Param.PLAY_TIME, 0);
			mHistoryBeans.remove(mHistoryBean);
			if (playTime != -1) { // -1代表已经播放完毕
				mHistoryBeans.add(0, mHistoryBean);
				mHistoryBean.setPlayTime(playTime);
			}
			adapter.notifyDataSetChanged();
		}
	}
}
