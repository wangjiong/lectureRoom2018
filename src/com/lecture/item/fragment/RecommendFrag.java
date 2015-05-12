package com.lecture.item.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lecture.data.DbData;
import com.lecture.data.ProgramBean;
import com.lecture.item.activity.MoreAct;
import com.lecture.item.activity.MovieIntroAct;
import com.lecture.media.R;
import com.lecture.util.Param;

public class RecommendFrag extends Fragment {
	// 数据
	ArrayList<ProgramBean> programTodays;// 今日热播
	ArrayList<ProgramBean> programHots;// 金典热播
	ArrayList<ProgramBean> programAgos;// 以往热播
	ArrayList<ProgramBean> programBeans;// 响应点击事件
	// 布局
	private ArrayList<View> pageViews;// viewPager
	private ViewPager viewPager;
	private LinearLayout viewGroup;//point
	private int[] viewPagerImageId = new int[] { R.drawable.r01, R.drawable.r02, R.drawable.r03, R.drawable.r04 };
	private ImageView[] viewPagerPoints;
	private ImageView viewPagerPoint;
	private LinearLayout line; // 横线
	private TextView label;
	private String[] labels = { "最近热播", "经典热播", "以往热播" };
	private TextView more_movie_lines[]; // 更多按钮
	private int[] lines = { R.id.line1, R.id.line2, R.id.line3 };
	private int[] recommends = { R.id.recommend1, R.id.recommend2, R.id.recommend3 };
	private LinearLayout recommend, content;
	private TextView movie_text;
	private int[] contents = { R.id.content1, R.id.content2, R.id.content3, R.id.content4, R.id.content5, R.id.content6 };
	private ImageView movie_image, movie_images[];// 图片及图片响应
	
	private Handler handler = new Handler() {  
        @Override  
        public void handleMessage(Message msg) {  
            super.handleMessage(msg);  
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);  
        }  
    };
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
			System.out.println("ItemRecommend initData error");
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.item_recommend, null);
		// head
		pageViews = new ArrayList<View>();// viewPager
		for (int i = 0; i < viewPagerImageId.length; i++) {
			LinearLayout layout = new LinearLayout(getActivity());
			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			final ImageView imageView = new ImageView(getActivity());
			imageView.setScaleType(ScaleType.FIT_XY);
			imageView.setImageResource(viewPagerImageId[i]);
			layout.addView(imageView, lp);
			pageViews.add(layout);
		}
		viewPager = (ViewPager) view.findViewById(R.id.viewPager);
		viewGroup = (LinearLayout) view.findViewById(R.id.viewGroup);
		viewPagerPoints = new ImageView[viewPagerImageId.length];

		for (int i = 0; i < viewPagerImageId.length; i++) { // 小圆点
			viewPagerPoint = new ImageView(getActivity());
			viewPagerPoint.setLayoutParams(new LayoutParams(50, 50));
			viewPagerPoint.setPadding(20, 0, 20, 0);
			viewPagerPoints[i] = viewPagerPoint;
			if (i == 0) {
				// 默认选中第一张图片
				viewPagerPoints[i].setBackgroundResource(R.drawable.page_indicator_focused1);
			} else {
				viewPagerPoints[i].setBackgroundResource(R.drawable.page_indicator1);
			}
			viewGroup.addView(viewPagerPoints[i]);
			viewGroup.setAlpha(0.6f);
		}
		viewPager.setAdapter(new GuidePageAdapter());
		viewPager.setOnPageChangeListener(new GuidePageChangeListener());
		viewPager.setCurrentItem(viewPagerImageId.length*100);
		// 自动切换页面功能  
        new Thread(new Runnable() {  
            @Override  
            public void run() {  
                while (true) {  
                    SystemClock.sleep(5000);  
                    handler.sendEmptyMessage(0);  
                }  
            }  
        }).start();
		// body
		more_movie_lines = new TextView[lines.length];// 横线
		for (int i = 0; i < lines.length; ++i) {
			line = (LinearLayout) view.findViewById(lines[i]);
			label = (TextView) line.findViewById(R.id.label);
			label.setText(labels[i]);
			TextView more_movie_line = (TextView) line.findViewById(R.id.more_movie_line);
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
					programBean = (programTodays.get(programTodays.size() - j - 1));
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
					Intent intent = new Intent(getActivity(), MovieIntroAct.class);
					intent.putExtra(Param.MOVIE_KEY, programBeans.get(j).getId());
					startActivity(intent);
				}
			});
		}
		for (int i = 0; i < more_movie_lines.length; ++i) {
			final int j = i;
			more_movie_lines[i].setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					Intent intent = new Intent(getActivity(), MoreAct.class);
					intent.putExtra(Param.MORE_TYPE, j);
					startActivity(intent);
				}
			});
		}
	}

	// 指引页面数据适配器
	class GuidePageAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return Integer.MAX_VALUE;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(pageViews.get(arg1 % pageViews.size()));
		}

		@Override
		public Object instantiateItem(View arg0,final int arg1) {
			((ViewPager) arg0).addView(pageViews.get(arg1 % pageViews.size()));
			pageViews.get(arg1 % pageViews.size()).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), MoreAct.class);
					intent.putExtra(Param.RECOMMEND_TYPE, arg1 % pageViews.size());
					intent.putExtra(Param.MORE_TYPE, -1);
					startActivity(intent);
				}
			});
			
			return pageViews.get(arg1 % pageViews.size());
		}
	}

	// 指引页面更改事件监听器
	class GuidePageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int arg0) {
			for (int i = 0; i < viewPagerPoints.length; i++) {
				viewPagerPoints[arg0 % viewPagerImageId.length].setBackgroundResource(R.drawable.page_indicator_focused1);
				if (arg0 % viewPagerImageId.length != i) {
					viewPagerPoints[i].setBackgroundResource(R.drawable.page_indicator1);
				}
			}
		}
	}
}
