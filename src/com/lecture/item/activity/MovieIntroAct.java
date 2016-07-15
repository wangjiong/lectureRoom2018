package com.lecture.item.activity;

import android.app.Activity;
import android.content.Intent;
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
import com.lecture.data.UnitBean;
import com.lecture.media.R;
import com.lecture.util.DownLoad;
import com.lecture.util.Param;
import com.lecture.util.Util;

public class MovieIntroAct extends Activity {
	// ����
	ProgramBean mProgramBean;
	// ����
	ImageView movie_image; // ��ĿͼƬ
	TextView description;// ��Ŀ��ϸ��Ϣ
	TextView mDownload;// ���ఴť
	GridLayout gridLayout;

	Button[] bns = new Button[10];
	Button[] mBnsProgram;
	boolean hasDown = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		initView();
	}

	private void initData() {
		String id = getIntent().getStringExtra(Param.MOVIE_KEY);
		mProgramBean = DbData.getProgramBeanById(id);
	}

	private void initView() {
		this.setContentView(R.layout.movie_intro);
		movie_image = (ImageView) findViewById(R.id.image_intro);// ͼƬ
		description = (TextView) findViewById(R.id.text_intro);// ����
		movie_image.setImageDrawable(DbData.getDrawableById(mProgramBean.getId()));
		description.setTextSize(17);
		description.setText("���ƣ���" + mProgramBean.getName() + "��\n������" + mProgramBean.getNum() + "\n���ߣ�" + mProgramBean.getAuthor() + "\n�ײ�ʱ�䣺" + mProgramBean.getId().substring(0, 4) + "-" + mProgramBean.getId().substring(4, 6) + "-" + mProgramBean.getId().substring(6) + "\n��Դ���ټҽ�̳" + "\n���أ��й�");
		gridLayout = (GridLayout) findViewById(R.id.gridLayout);
		gridLayout.setRowCount(2);
		gridLayout.setColumnCount(5);
		mBnsProgram = new Button[mProgramBean.getNum()];
		for (int i = 0; i < 10; ++i) {
			bns[i] = new Button(this);
			bns[i].setTextSize(20);
			bns[i].setWidth(Util.dip2px(MovieIntroAct.this, 68));
			bns[i].setHeight(Util.dip2px(MovieIntroAct.this, 68));
			if (i == 8 && mProgramBean.getNum() > 10) {
				bns[i].setText("...");
			} else if (i == 9 && mProgramBean.getNum() > 10) {
				bns[i].setText(1 + "");
			} else {
				bns[i].setText(mProgramBean.getNum() - i + "");
			}
			if (i >= mBnsProgram.length) {
				bns[i].setVisibility(View.INVISIBLE);
			}
			GridLayout.Spec rowSpec = GridLayout.spec(i / 5);
			GridLayout.Spec columnSpec = GridLayout.spec(i % 5);
			GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
			params.setGravity(Gravity.FILL);
			gridLayout.addView(bns[i], params);
		}
		for (int i = 0; i < mBnsProgram.length; ++i) {
			mBnsProgram[i] = new Button(this);
		}
		for (int i = 0; i < 10; ++i) {
			final String s = bns[i].getText().toString();
			bns[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (s.equals("...")) {
						gridLayout.removeAllViews();
						gridLayout.setRowCount(mProgramBean.getNum() % 5 == 0 ? mProgramBean.getNum() / 5 : mProgramBean.getNum() / 5 + 1);
						gridLayout.setColumnCount(5);
						for (int j = 0; j < mProgramBean.getNum(); ++j) {
							mBnsProgram[j].setTextSize(20);
							mBnsProgram[j].setWidth(Util.dip2px(MovieIntroAct.this, 68));
							mBnsProgram[j].setHeight(Util.dip2px(MovieIntroAct.this, 68));
							mBnsProgram[j].setText(mProgramBean.getNum() - j + "");
							GridLayout.Spec rowSpec = GridLayout.spec(j / 5);
							GridLayout.Spec columnSpec = GridLayout.spec(j % 5);
							GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
							params.setGravity(Gravity.FILL);
							gridLayout.addView(mBnsProgram[j], params);
						}
						for (int i = 0; i < mProgramBean.getNum(); ++i) {
							final String s = mBnsProgram[i].getText().toString();
							mBnsProgram[i].setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View arg0) {
									if (!DbData.isNetworkConnected(MovieIntroAct.this)) {
										Toast.makeText(MovieIntroAct.this, "��ǰ���粻����", Toast.LENGTH_SHORT).show();
										return;
									}
									// ������Ϣ����
									Intent intent = new Intent(MovieIntroAct.this, MovieAct.class);
									intent.putExtra(Param.FROM_TYPE, 0);
									intent.putExtra(Param.TITLE_KEY, mProgramBean.getName());
									intent.putExtra(Param.EPISODE_KEY, s);
									startActivity(intent);
								}
							});
						}
					} else {
						if (!DbData.isNetworkConnected(MovieIntroAct.this)) {
							Toast.makeText(MovieIntroAct.this, "��ǰ���粻����", Toast.LENGTH_SHORT).show();
							return;
						}
						// ������Ϣ����
						Intent intent = new Intent(MovieIntroAct.this, MovieAct.class);
						intent.putExtra(Param.FROM_TYPE, 0);
						intent.putExtra(Param.TITLE_KEY, mProgramBean.getName());
						intent.putExtra(Param.EPISODE_KEY, s);
						startActivity(intent);
					}
				}
			});
		}
		mDownload = (TextView) findViewById(R.id.more_movie_intro);
		mDownload.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mDownload.getText().toString().equals("����")) {
					mDownload.setText("����");
					return;
				}
				Toast.makeText(MovieIntroAct.this, "���������ť��ʼ����", Toast.LENGTH_SHORT).show();
				mDownload.setText("����");
				if (hasDown) {
					return;
				}
				mDownload.setEnabled(false);
				gridLayout.removeAllViews();
				gridLayout.setRowCount(mProgramBean.getNum() % 5 == 0 ? mProgramBean.getNum() / 5 : mProgramBean.getNum() / 5 + 1);
				gridLayout.setColumnCount(5);
				for (int j = 0; j < mProgramBean.getNum(); ++j) {
					mBnsProgram[j].setTextSize(20);
					mBnsProgram[j].setWidth(Util.dip2px(MovieIntroAct.this, 68));
					mBnsProgram[j].setHeight(Util.dip2px(MovieIntroAct.this, 68));
					mBnsProgram[j].setText(mProgramBean.getNum() - j + "");
					GridLayout.Spec rowSpec = GridLayout.spec(j / 5);
					GridLayout.Spec columnSpec = GridLayout.spec(j % 5);
					GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
					params.setGravity(Gravity.FILL);
					gridLayout.addView(mBnsProgram[j], params);
				}

				if (mProgramBean.getNum() < 5) {
					for (int j = mProgramBean.getNum(); j < 5; j++) {
						Button bn = new Button(MovieIntroAct.this);
						bn.setTextSize(20);
						bn.setWidth(Util.dip2px(MovieIntroAct.this, 68));
						bn.setHeight(Util.dip2px(MovieIntroAct.this, 68));
						bn.setVisibility(View.INVISIBLE);
						GridLayout.Spec rowSpec = GridLayout.spec(j / 5);
						GridLayout.Spec columnSpec = GridLayout.spec(j % 5);
						GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
						params.setGravity(Gravity.FILL);
						gridLayout.addView(bn, params);
					}
				}

				for (int i = 0; i < mProgramBean.getNum(); ++i) {
					final int episode = Integer.parseInt(mBnsProgram[i].getText().toString());
					final UnitBean unitBean = DbData.getUnitBeanByTitleAndEpisode(mProgramBean.getName(), episode);
					final String s = mBnsProgram[i].getText().toString();
					mBnsProgram[i].setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							if (mDownload.getText().toString().equals("����")) {
								if (!DbData.isNetworkConnected(MovieIntroAct.this)) {
									Toast.makeText(MovieIntroAct.this, "��ǰ���粻����", Toast.LENGTH_SHORT).show();
									return;
								}
								// ������Ϣ����
								Intent intent = new Intent(MovieIntroAct.this, MovieAct.class);
								intent.putExtra(Param.FROM_TYPE, 0);
								intent.putExtra(Param.TITLE_KEY, mProgramBean.getName());
								intent.putExtra(Param.EPISODE_KEY, s);
								startActivity(intent);
							} else {
								if (Util.isFastDoubleClick()) {
									Toast.makeText(MovieIntroAct.this, "��ҪƵ���������Ŷ~", Toast.LENGTH_SHORT).show();
									return;
								}
								new DownLoad(unitBean).downLoad();
							}
						}
					});
				}
				hasDown = true;
				mDownload.setEnabled(true);
			}
		});
	}
}
