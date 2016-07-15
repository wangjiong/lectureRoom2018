package com.lecture.item.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.lecture.data.DbData;
import com.lecture.item.activity.MoreAct;
import com.lecture.item.view.KeywordsView;
import com.lecture.media.R;
import com.lecture.util.Param;

public class SearchFrag extends Fragment implements View.OnClickListener {
	// 搜索类型
	private int searchType = 0;// 0代表按名字 1代表按作者 2代表按年份
	private String[] key_words = new String[18];
	private String[] movie_texts;
	private KeywordsView showKeywords = null;
	private GestureDetector gestureDetector;

	EditText search_text = null;
	Button search_button = null;
	TextView searchTypeTextView = null;

	@SuppressWarnings("deprecation")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.search, container, false);
		search_text = (EditText) view.findViewById(R.id.search_text);
		search_text.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					String text = (search_text.getText()).toString().trim();
					search(text);
				}
				return false;
			}
		});
		search_button = (Button) view.findViewById(R.id.search_button);
		search_button.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				String text = (search_text.getText()).toString().trim();
				search(text);
			}
		});
		searchTypeTextView = (TextView) view.findViewById(R.id.search_back);
		view.findViewById(R.id.search_back_fl).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (searchType == 0) {
					searchTypeTextView.setText("作者");
					searchType = 1;
					search_text.setHint("易中天");
					search_text.setInputType(InputType.TYPE_CLASS_TEXT);
					return;
				}
				if (searchType == 1) {
					searchTypeTextView.setText("时间");
					searchType = 2;
					search_text.setHint("2006");
					search_text.setInputType(InputType.TYPE_CLASS_NUMBER);
					return;
				}
				if (searchType == 2) {
					searchTypeTextView.setText("名称");
					searchType = 0;
					search_text.setHint("易中天品三国");
					search_text.setInputType(InputType.TYPE_CLASS_TEXT);
					return;
				}
			}
		});
		showKeywords = (KeywordsView) view.findViewById(R.id.word);
		showKeywords.setDuration(2000l);
		showKeywords.setOnClickListener(this);
		this.gestureDetector = new GestureDetector(new Mygdlinseter());
		showKeywords.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event); // 注册点击事件
			}
		});
		handler.sendEmptyMessage(Msg_Start_Load);
		return view;
	}

	private void search(String text) {
		if (text.equals("")) {
			Toast.makeText(getActivity(), "输入内容不能为空哦！", Toast.LENGTH_SHORT).show();
			return;
		}
		if (searchType == 0) {// 名字
			if (DbData.searchByTitle(text)) {
				Intent intent = new Intent(getActivity(), MoreAct.class);
				intent.putExtra(Param.TITLE_TYPE, text);
				intent.putExtra(Param.RECOMMEND_TYPE, -1);
				intent.putExtra(Param.MORE_TYPE, -1);
				startActivity(intent);
			} else {
				Toast.makeText(getActivity(), "抱歉！没有找到", Toast.LENGTH_SHORT).show();
			}
		} else if (searchType == 1) {// 作者
			if (DbData.searchByAuthor(text)) {
				Intent intent = new Intent(getActivity(), MoreAct.class);
				intent.putExtra(Param.AUTHOR_TYPE, text);
				intent.putExtra(Param.RECOMMEND_TYPE, -1);
				intent.putExtra(Param.MORE_TYPE, -1);
				startActivity(intent);
			} else {
				Toast.makeText(getActivity(), "抱歉！没有找到", Toast.LENGTH_SHORT).show();
			}
		} else if (searchType == 2) {// 年份
			if (DbData.searchByTime(text)) {
				Intent intent = new Intent(getActivity(), MoreAct.class);
				intent.putExtra(Param.TIME_TYPE, text);
				intent.putExtra(Param.RECOMMEND_TYPE, -1);
				intent.putExtra(Param.MORE_TYPE, -1);
				startActivity(intent);
			} else {
				Toast.makeText(getActivity(), "抱歉！没有找到", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private String[] getRandomArray() {
		movie_texts = DbData.getSearchStrings();
		return movie_texts;
	}

	private static final int Msg_Start_Load = 0x0102;
	private static final int Msg_Load_End = 0x0203;

	private LoadKeywordsTask task = null;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Msg_Start_Load:
				task = new LoadKeywordsTask();
				new Thread(task).start();
				break;
			case Msg_Load_End:
				showKeywords.rubKeywords();
				feedKeywordsFlow(showKeywords, key_words);
				showKeywords.go2Shwo(KeywordsView.ANIMATION_IN);
				break;
			}

		}
	};

	private class LoadKeywordsTask implements Runnable {
		@Override
		public void run() {
			try {
				key_words = getRandomArray();
				if (key_words.length > 0)
					handler.sendEmptyMessage(Msg_Load_End);
			} catch (Exception e) {
			}
		}
	}

	private void feedKeywordsFlow(KeywordsView keyworldFlow, String[] arr) {
		for (int i = 0; i < KeywordsView.MAX; i++) {
			String tmp = arr[i];
			keyworldFlow.feedKeyword(tmp);
		}
	}

	class Mygdlinseter implements OnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e2.getX() - e1.getX() > 100) { // 右滑
				key_words = getRandomArray();
				showKeywords.rubKeywords();
				feedKeywordsFlow(showKeywords, key_words);
				showKeywords.go2Shwo(KeywordsView.ANIMATION_OUT);
				return true;
			}
			if (e2.getX() - e1.getX() < -100) {// 左滑
				key_words = getRandomArray();
				showKeywords.rubKeywords();
				feedKeywordsFlow(showKeywords, key_words);
				showKeywords.go2Shwo(KeywordsView.ANIMATION_IN);
				return true;
			}
			if (e2.getY() - e1.getY() < -100) {// 上滑
				key_words = getRandomArray();
				showKeywords.rubKeywords();
				feedKeywordsFlow(showKeywords, key_words);
				showKeywords.go2Shwo(KeywordsView.ANIMATION_IN);
				return true;
			}
			if (e2.getY() - e1.getY() > 100) {// 下滑
				key_words = getRandomArray();
				showKeywords.rubKeywords();
				feedKeywordsFlow(showKeywords, key_words);
				showKeywords.go2Shwo(KeywordsView.ANIMATION_OUT);
				return true;
			}
			return false;
		}
	}

	@Override
	public void onClick(View v) {
		if (searchType != 0) {
			searchTypeTextView.setText("名称");
			searchType = 0;
			search_text.setHint("易中天品三国");
			search_text.setInputType(InputType.TYPE_CLASS_TEXT);
		}
		String kw = ((TextView) v).getText().toString();
		String text = (search_text.getText()).toString().trim();
		if (text.equals(kw.substring(1, kw.length() - 1))) {
			search(text);
		} else {
			search_text.setText(kw.substring(1, kw.length() - 1));
			search_text.setSelection(kw.substring(1, kw.length() - 1).length());
			search_text.requestFocus();
		}
	}
}
