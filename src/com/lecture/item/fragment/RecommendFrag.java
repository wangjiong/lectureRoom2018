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
	// ����
	ArrayList<ProgramBean> programTodays;// �����Ȳ�
	ArrayList<ProgramBean> programHots;// ����Ȳ�
	ArrayList<ProgramBean> programAgos;// �����Ȳ�
	ArrayList<ProgramBean> programBeans;// ��Ӧ����¼�
	// ����
	private ArrayList<View> pageViews;// viewPager
	private ViewPager viewPager;
	private LinearLayout viewGroup;//point
	private int[] viewPagerImageId = new int[] { R.drawable.r01, R.drawable.r02, R.drawable.r03, R.drawable.r04 };
	private ImageView[] viewPagerPoints;
	private ImageView viewPagerPoint;
	private LinearLayout line; // ����
	private TextView label;
	private String[] labels = { "����Ȳ�", "�����Ȳ�", "�����Ȳ�" };
	private TextView more_movie_lines[]; // ���ఴť
	private int[] lines = { R.id.line1, R.id.line2, R.id.line3 };
	private int[] recommends = { R.id.recommend1, R.id.recommend2, R.id.recommend3 };
	private LinearLayout recommend, content;
	private TextView movie_text;
	private int[] contents = { R.id.content1, R.id.content2, R.id.content3, R.id.content4, R.id.content5, R.id.content6 };
	private ImageView movie_image, movie_images[];// ͼƬ��ͼƬ��Ӧ
	
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
			programTodays = DbData.getProgramBeansToday();// �����Ȳ�
			programHots = DbData.getProgramBeansHot();// ����Ȳ�
			programAgos = DbData.getProgramBeansAgo();// �����Ȳ�
			programBeans = new ArrayList<ProgramBean>();// ��Ӧ
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

		for (int i = 0; i < viewPagerImageId.length; i++) { // СԲ��
			viewPagerPoint = new ImageView(getActivity());
			viewPagerPoint.setLayoutParams(new LayoutParams(50, 50));
			viewPagerPoint.setPadding(20, 0, 20, 0);
			viewPagerPoints[i] = viewPagerPoint;
			if (i == 0) {
				// Ĭ��ѡ�е�һ��ͼƬ
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
		// �Զ��л�ҳ�湦��  
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
		more_movie_lines = new TextView[lines.length];// ����
		for (int i = 0; i < lines.length; ++i) {
			line = (LinearLayout) view.findViewById(lines[i]);
			label = (TextView) line.findViewById(R.id.label);
			label.setText(labels[i]);
			TextView more_movie_line = (TextView) line.findViewById(R.id.more_movie_line);
			more_movie_lines[i] = more_movie_line;
		}
		movie_images = new ImageView[recommends.length * contents.length];
		for (int i = 0; i < recommends.length; ++i) {// 3���Ƽ�ģ��
			recommend = (LinearLayout) view.findViewById(recommends[i]);
			for (int j = 0; j < contents.length; ++j) {// 6�����ݽ�Ŀ
				content = (LinearLayout) recommend.findViewById(contents[j]);
				movie_image = (ImageView) content// ��ĿͼƬ
						.findViewById(R.id.movie_image);
				movie_text = (TextView) content.findViewById(R.id.movie_text);// ��Ŀ����
				ProgramBean programBean = new ProgramBean();
				switch (i) {
				case 0:// �����Ȳ�
					programBean = (programTodays.get(programTodays.size() - j - 1));
					break;
				case 1:// ����Ȳ�
					programBean = programHots.get(j);
					break;
				case 2:// �����Ȳ�
					programBean = programAgos.get(j);
					break;
				}
				movie_image.setTag(programBean);
				movie_image.setImageDrawable((programBean.image));
				movie_text.setText("��" + programBean.getName() + "��");
				programBeans.add(programBean);
				movie_images[i * 6 + j] = movie_image;
			}
		}
		return view;
	}

	// ���ͼƬ��Ӧ�¼�
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

	// ָ��ҳ������������
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

	// ָ��ҳ������¼�������
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
