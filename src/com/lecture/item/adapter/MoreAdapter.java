package com.lecture.item.adapter;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.lecture.data.ProgramBean;
import com.lecture.media.R;

public class MoreAdapter extends BaseAdapter {
	ArrayList<ProgramBean> morePrograms = new ArrayList<ProgramBean>();
	private LayoutInflater inflater;

	public MoreAdapter(Context context, ArrayList<ProgramBean> morePrograms) {
		this.morePrograms = morePrograms;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO 自动生成的方法存根
		return morePrograms.size();
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
			convertView = inflater.inflate(R.layout.more_item, null);
			holder = new Holder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.movie_image);
			holder.textView = (TextView) convertView.findViewById(R.id.movie_text);

			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.imageView.setImageDrawable(morePrograms.get(position).image);
		holder.textView.setText(morePrograms.get(position).getName());
		return convertView;
	}

	class Holder {
		ImageView imageView;
		TextView textView;
	}
}
