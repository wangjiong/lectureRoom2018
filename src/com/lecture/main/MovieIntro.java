package com.lecture.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lecture.data.DbData;
import com.lecture.data.ProgramBean;
import com.lecture.media.R;

public class MovieIntro extends Activity {
	// 数据
	public static ProgramBean programBean;
	public static final String EPISODE_KEY = "com.lecture.media.episode";
	// 布局
	ImageView movie_image; // 节目图片
	TextView description;// 节目详细信息
	TextView more;// 更多按钮
	GridLayout gridLayout;

	Button[] bns = new Button[10];
	Button[] bnsProgram;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		initView();
	}

	private void initData() {
		String id = getIntent().getStringExtra(ItemRecommend.MOVIE_KEY);
		programBean = DbData.getProgramBeanById(id);
	}

	private void initView() {
		this.setContentView(R.layout.movie_intro);
		movie_image = (ImageView) findViewById(R.id.image_intro);// 图片
		description = (TextView) findViewById(R.id.text_intro);// 描述
		movie_image.setImageDrawable(programBean.image);
		description.setTextSize(17);
		description.setText("名称：《" + programBean.getName() + "》\n集数："
				+ programBean.getNum() + "\n作者：" + programBean.getAuthor()
				+ "\n首播时间：" + programBean.getId().substring(0, 4) + "-"
				+ programBean.getId().substring(4, 6) + "-"
				+ programBean.getId().substring(6) + "\n来源：百家讲坛" + "\n产地：中国");
		gridLayout = (GridLayout) findViewById(R.id.gridLayout);
		gridLayout.setRowCount(2);
		gridLayout.setColumnCount(5);
		bnsProgram = new Button[programBean.getNum()];
		for (int i = 0; i < 10; ++i) {
			bns[i] = new Button(this);
			bns[i].setTextSize(20);
			bns[i].setWidth(130);
			bns[i].setHeight(130);
			if (i == 8 && programBean.getNum() > 10) {
				bns[i].setText("...");
			} else if (i == 9 && programBean.getNum() > 10) {
				bns[i].setText(1 + "");
			} else {
				bns[i].setText(programBean.getNum() - i + "");
			}
			if (i >= bnsProgram.length) {
				bns[i].setVisibility(View.INVISIBLE);
			}
			GridLayout.Spec rowSpec = GridLayout.spec(i / 5);
			GridLayout.Spec columnSpec = GridLayout.spec(i % 5);
			GridLayout.LayoutParams params = new GridLayout.LayoutParams(
					rowSpec, columnSpec);
			params.setGravity(Gravity.FILL);
			gridLayout.addView(bns[i], params);
		}
		for (int i = 0; i < bnsProgram.length; ++i) {
			bnsProgram[i] = new Button(this);
		}
		for (int i = 0; i < 10; ++i) {
			final String s = bns[i].getText().toString();
			bns[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (s.equals("...")) {
						gridLayout.removeAllViews();
						gridLayout.setRowCount(programBean.getNum() % 5 == 0 ? programBean
								.getNum() / 5 : programBean.getNum() / 5 + 1);
						gridLayout.setColumnCount(5);
						for (int j = 0; j < programBean.getNum(); ++j) {
							bnsProgram[j].setTextSize(20);
							bnsProgram[j].setWidth(130);
							bnsProgram[j].setHeight(130);
							bnsProgram[j].setText(programBean.getNum() - j + "");
							GridLayout.Spec rowSpec = GridLayout.spec(j / 5);
							GridLayout.Spec columnSpec = GridLayout.spec(j % 5);
							GridLayout.LayoutParams params = new GridLayout.LayoutParams(
									rowSpec, columnSpec);
							params.setGravity(Gravity.FILL);
							gridLayout.addView(bnsProgram[j], params);
						}
						for (int i = 0; i < programBean.getNum(); ++i) {
							final String s = bnsProgram[i].getText().toString();
							bnsProgram[i]
									.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View arg0) {
											if (!isNetworkConnected(MovieIntro.this)) {
												Toast.makeText(MovieIntro.this,
														"当前网络不可用",
														Toast.LENGTH_SHORT)
														.show();
												return;
											}
											// 传递信息集数
											Intent intent = new Intent(
													MovieIntro.this,
													Movie.class);
											intent.putExtra(EPISODE_KEY, s);
											startActivity(intent);
										}
									});
						}
					} else {
						if (!isNetworkConnected(MovieIntro.this)) {
							Toast.makeText(MovieIntro.this, "当前网络不可用",
									Toast.LENGTH_SHORT).show();
							return;
						}
						// 传递信息
						Intent intent = new Intent(MovieIntro.this, Movie.class);
						intent.putExtra(EPISODE_KEY, s);
						startActivity(intent);
					}
				}
			});
		}
		more = (TextView) findViewById(R.id.more_movie_intro);
		more.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				gridLayout.removeAllViews();
				gridLayout.setRowCount(programBean.getNum() % 5 == 0 ? programBean
						.getNum() / 5 : programBean.getNum() / 5 + 1);
				gridLayout.setColumnCount(5);
				for (int j = 0; j < programBean.getNum(); ++j) {
					bnsProgram[j].setTextSize(20);
					bnsProgram[j].setWidth(130);
					bnsProgram[j].setHeight(130);
					bnsProgram[j].setText(programBean.getNum() - j + "");
					GridLayout.Spec rowSpec = GridLayout.spec(j / 5);
					GridLayout.Spec columnSpec = GridLayout.spec(j % 5);
					GridLayout.LayoutParams params = new GridLayout.LayoutParams(
							rowSpec, columnSpec);
					params.setGravity(Gravity.FILL);
					gridLayout.addView(bnsProgram[j], params);
				}
				for (int i = 0; i < programBean.getNum(); ++i) {
					final String s = bnsProgram[i].getText().toString();
					bnsProgram[i].setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							if (!isNetworkConnected(MovieIntro.this)) {
								Toast.makeText(MovieIntro.this, "当前网络不可用" , Toast.LENGTH_SHORT).show();
								return;
							}
							// 传递信息集数
							Intent intent = new Intent(MovieIntro.this,
									Movie.class);
							intent.putExtra(EPISODE_KEY, s);
							startActivity(intent);
						}
					});
				}
			}
		});
	}

	private boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

}
