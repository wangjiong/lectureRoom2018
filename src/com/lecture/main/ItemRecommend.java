package com.lecture.main;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lecture.data.DbData;
import com.lecture.data.ProgramBean;
import com.lecture.media.R;

public class ItemRecommend extends Fragment {
	public static final String MOVIE_KEY = "com.lecture.media.movie";
	// 数据
	ArrayList<ProgramBean> programTodays;// 今日热播
	ArrayList<ProgramBean> programHots;// 金典热播
	ArrayList<ProgramBean> programAgos;// 以往热播
	ArrayList<ProgramBean> programBeans;// 响应点击事件
	// 布局
	private LinearLayout line; // 横线
	private TextView label;
	private String[] labels = { "最近热播", "经典热播", "以往热播" };
	private TextView more_movie_lines[];
	private int[] lines = { R.id.line1, R.id.line2, R.id.line3 };
	private int[] recommends = { R.id.recommend1, R.id.recommend2,
			R.id.recommend3 };
	private LinearLayout recommend, content;
	private TextView movie_text;
	private int[] contents = { R.id.content1, R.id.content2, R.id.content3,
			R.id.content4, R.id.content5, R.id.content6 };
	private ImageView movie_image, movie_images[];// 图片及图片响应

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
	}

	private void initData() {
		try {
			programTodays = DbData.getProgramBeansToday();// 今日热播
			programHots = DbData.getProgramBeansHot();// 金典热播
			programAgos = DbData.getProgramBeansAgo();// 以往热播
			programBeans = new ArrayList<ProgramBean>();// 响应
		} catch (Exception e) {
			System.out.println("initData error");
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.item_recommend, null);
		// body
		more_movie_lines = new TextView[lines.length];// 横线
		for (int i = 0; i < lines.length; ++i) {
			line = (LinearLayout) view.findViewById(lines[i]);
			label = (TextView) line.findViewById(R.id.label);
			label.setText(labels[i]);
			TextView more_movie_line = (TextView) line
					.findViewById(R.id.more_movie_line);
			more_movie_lines[i] = more_movie_line;
		}
		movie_images = new ImageView[recommends.length * contents.length];
		for (int i = 0; i < recommends.length; ++i) {// 3个推荐模块
			recommend = (LinearLayout) view.findViewById(recommends[i]);
			for (int j = 0; j < contents.length; ++j) {// 6个内容节目
				content = (LinearLayout) recommend.findViewById(contents[j]);
				movie_image = (ImageView) content// 节目图片
						.findViewById(R.id.movie_image);
				movie_text = (TextView) content.findViewById(R.id.movie_text);// 节目名字
				ProgramBean programBean = new ProgramBean();
				switch (i) {
				case 0:// 今日热播
					programBean = (programTodays.get(programTodays.size() - j
							- 1));
					break;
				case 1:// 金典热播
					programBean = programHots.get(j);
					break;
				case 2:// 以往热播
					programBean = programAgos.get(j);
					break;
				}
				movie_image.setTag(programBean);
				movie_image.setImageDrawable((programBean.image));
				movie_text.setText("《" + programBean.getName() + "》");
				programBeans.add(programBean);
				movie_images[i * 6 + j] = movie_image;
			}
		}
		return view;
	}

	// 点击图片响应事件
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		for (int i = 0; i < movie_images.length; ++i) {
			final int j = i;
			movie_images[i].setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					// TODO 自动生成的方法存根
					Intent intent = new Intent(getActivity(), MovieIntro.class);
					intent.putExtra(MOVIE_KEY, programBeans.get(j).getId());
					startActivity(intent);
				}
			});
		}
	}
}
