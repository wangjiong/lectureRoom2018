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
	// ����
	ArrayList<ProgramBean> programTodays;// �����Ȳ�
	ArrayList<ProgramBean> programHots;// ����Ȳ�
	ArrayList<ProgramBean> programAgos;// �����Ȳ�
	ArrayList<ProgramBean> programBeans;// ��Ӧ����¼�
	// ����
	private LinearLayout line; // ����
	private TextView label;
	private String[] labels = { "����Ȳ�", "�����Ȳ�", "�����Ȳ�" };
	private TextView more_movie_lines[];
	private int[] lines = { R.id.line1, R.id.line2, R.id.line3 };
	private int[] recommends = { R.id.recommend1, R.id.recommend2,
			R.id.recommend3 };
	private LinearLayout recommend, content;
	private TextView movie_text;
	private int[] contents = { R.id.content1, R.id.content2, R.id.content3,
			R.id.content4, R.id.content5, R.id.content6 };
	private ImageView movie_image, movie_images[];// ͼƬ��ͼƬ��Ӧ

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
			System.out.println("initData error");
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.item_recommend, null);
		// body
		more_movie_lines = new TextView[lines.length];// ����
		for (int i = 0; i < lines.length; ++i) {
			line = (LinearLayout) view.findViewById(lines[i]);
			label = (TextView) line.findViewById(R.id.label);
			label.setText(labels[i]);
			TextView more_movie_line = (TextView) line
					.findViewById(R.id.more_movie_line);
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
					programBean = (programTodays.get(programTodays.size() - j
							- 1));
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
					// TODO �Զ����ɵķ������
					Intent intent = new Intent(getActivity(), MovieIntro.class);
					intent.putExtra(MOVIE_KEY, programBeans.get(j).getId());
					startActivity(intent);
				}
			});
		}
	}
}
