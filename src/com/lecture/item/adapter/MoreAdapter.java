package com.lecture.item.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.lecture.data.ProgramBean;
import com.lecture.media.R;

public class MoreAdapter extends BaseAdapter {
	ArrayList<ProgramBean> mMorePrograms = new ArrayList<ProgramBean>();
	Context mContext;
	LayoutInflater mInflater;
	HashMap<Integer, Drawable> mBitmapMap = new HashMap<Integer, Drawable>();

	public MoreAdapter(Context context, ArrayList<ProgramBean> morePrograms) {
		this.mMorePrograms = morePrograms;
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		// TODO 自动生成的方法存根
		return mMorePrograms.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO 自动生成的方法存根
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO 自动生成的方法存根
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.more_item, null);
			holder = new Holder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.movie_image);
			holder.textView = (TextView) convertView.findViewById(R.id.movie_text);

			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.imageView.setImageDrawable(mMorePrograms.get(position).drawable);

		if (mMorePrograms.get(position).getName().length() > 13) {
			holder.textView.setText(mMorePrograms.get(position).getName().substring(0, 8) + "..." + mMorePrograms.get(position).getName().substring(mMorePrograms.get(position).getName().length() - 4));
		} else {
			holder.textView.setText(mMorePrograms.get(position).getName());
		}
		return convertView;
	}

	class Holder {
		ImageView imageView;
		TextView textView;
	}
}
