package com.lecture.item.activity;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lecture.data.DbData;
import com.lecture.data.HistoryBean;
import com.lecture.media.R;

public class PersonHistoryAct extends Activity implements OnItemClickListener {
	// 数据
	public static HistoryBean historyBean = null;
	List<HistoryBean> historyBeans = null;
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
		historyBeans = DbData.readHistory();
		Collections.reverse(historyBeans);
	}

	private void initView() {
		setContentView(R.layout.person_history);
		listView = (ListView) findViewById(R.id.history_listView);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
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
			return historyBeans.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO 自动生成的方法存根
			return historyBeans.get(position);
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
			holder.imageView.setImageBitmap(DbData.getImageFromAssetsFile("img" + historyBeans.get(position).getId() + ".jpg"));
			holder.textView.setText("名称：《" + historyBeans.get(position).getTitle() + "》\n集数：" + historyBeans.get(position).getNum() + "\n作者：" + historyBeans.get(position).getAuthor() + "\n首播时间：" + historyBeans.get(position).getId().substring(0, 4) + "-"
					+ historyBeans.get(position).getId().substring(4, 6) + "-" + historyBeans.get(position).getId().substring(6) + "\n播放集数：" + historyBeans.get(position).getEpisode() + "\n标题：" + historyBeans.get(position).getName() + "\n播放时间："
					+ new SimpleDateFormat("mm:ss").format(new Date(Integer.parseInt(historyBeans.get(position).getTime()))));

			return convertView;
		}
	};

	class ViewHolder {
		public ImageView imageView;
		public TextView textView;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		historyBean = historyBeans.get(position);
		System.out.println(historyBean.getTitle());
		Intent intent = new Intent(this, MovieHistoryAct.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isFirstStart) {
			initData();
			listView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		} else {
			isFirstStart = false;
		}
	}

}
