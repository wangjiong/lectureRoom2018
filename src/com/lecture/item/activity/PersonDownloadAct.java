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
import com.lecture.util.Param;

public class PersonDownloadAct extends Activity implements OnItemClickListener, OnItemLongClickListener {
	// ����
	public static MyBaseAdapter sAdapter;
	List<DownloadBean> mDownloadBeans;
	DownloadBean mDownloadBean;
	String[] mUrls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		initView();
	}

	private void initData() {
		mDownloadBeans = DbData.sDownloadBeans;
		sAdapter = new MyBaseAdapter();
	}

	private void initView() {
		setContentView(R.layout.person_download);
		ListView listView = (ListView) findViewById(R.id.download_listView);
		((TextView) findViewById(R.id.person)).setText("�������");
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
			// TODO �Զ����ɵķ������
			return mDownloadBeans.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO �Զ����ɵķ������
			return mDownloadBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO �Զ����ɵķ������
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
			holder.imageView.setImageBitmap(DbData.getImageFromAssetsFile("img" + DbData.getProgramBeanIdByUnitBeanTitle(mDownloadBeans.get(position).getTitle()) + ".jpg"));
			holder.textView.setText("���ƣ���" + mDownloadBeans.get(position).getTitle() + "��\n���ż�����" + mDownloadBeans.get(position).getEpisode() + "\n���⣺" + mDownloadBeans.get(position).getName());
			holder.textViewDownload.setText("\n���ؽ��ȣ�" + mDownloadBeans.get(position).getDownload() + "%");
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
		mDownloadBean = mDownloadBeans.get(position);
		Intent intent = new Intent(this, MovieAct.class);
		intent.putExtra(Param.FROM_TYPE, 1);
		intent.putExtra(Param.TITLE_KEY, mDownloadBean.getTitle());
		intent.putExtra(Param.EPISODE_KEY, mDownloadBean.getEpisode());
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		mDownloadBean = mDownloadBeans.get(position);
		dialog(position);
		return true;
	}

	protected void dialog(final int position) {
		AlertDialog.Builder builder = new Builder(PersonDownloadAct.this);
		builder.setMessage("ȷ��Ҫɾ����");
		builder.setTitle("��ʾ");
		builder.setPositiveButton("ȷ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				File[] files = DbData.fileDownload.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].toString().contains(mDownloadBean.getTitle() + " " + mDownloadBean.getEpisode())) {
						DbData.deleteDir(files[i]);
						break;
					}
				}
				mDownloadBeans.remove(position);
				sAdapter.notifyDataSetChanged();
				new Thread() {
					public void run() {
						DbData.sFinalDb.delete(mDownloadBean);
					}
				}.start();
			}
		});
		builder.setNegativeButton("ȡ��", new OnClickListener() {
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
	}
}
