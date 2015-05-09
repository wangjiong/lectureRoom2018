package com.lecture.item.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lecture.data.DbData;
import com.lecture.item.view.KeywordsView;
import com.lecture.media.R;

public class SearchFrag extends Fragment implements View.OnClickListener {
	private String[] totalKeys = null;
	private String[] key_words = new String[18];
	
	private String[] movie_texts=(String[]) DbData.getSearchStrings().toArray(new String[0]);
	private KeywordsView showKeywords = null;
	private LinearLayout searchLayout = null;
	private GestureDetector gestureDetector;
	private boolean isOutter;
	
	EditText search_text=null;
	Button search_button=null;
	TextView search_back=null;
	@SuppressWarnings("deprecation")
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.search, container, false);
		search_text=(EditText)view.findViewById(R.id.search_text);
		search_button=(Button)view.findViewById(R.id.search_button);
		search_button.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				String text=(search_text.getText()).toString().trim();
				int j=0;
				for(int i=0;i<movie_texts.length;++i){
					if(movie_texts[i].equals(text)){
						j=i;
						break;
					}
				}
//				if(j>=6&&j<=11){
//					Intent intent = new Intent(getActivity(), Movie_intro.class);
//					Integer id=Movie_res.movie_images[j];
//					intent.putExtra("image_id",id.toString());
//					getActivity().startActivity(intent);
//				}else{
//					Toast.makeText(getActivity(),"抱歉！暂时不能播放",Toast.LENGTH_SHORT).show();
//				}
			}
		});
		search_back=(TextView)view.findViewById(R.id.search_back);
		search_back.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				search_text.setText("");
//				getFragmentManager().beginTransaction().replace(R.id.content,new ItemSearch()).commit();
			}
		});
		
		
		
		
		searchLayout = (LinearLayout) view.findViewById(R.id.searchContent);
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
		isOutter = true;

		handler.sendEmptyMessage(Msg_Start_Load);

		return view;
	}

	private String[] getRandomArray() {
		if (totalKeys != null && totalKeys.length > 0) {
			String[] keys = new String[15];
			List<String> ks = new ArrayList<String>();
			for (int i = 0; i < totalKeys.length; i++) {
				ks.add(totalKeys[i]);
			}
			for (int i = 0; i < keys.length; i++) {
				int k = (int) (ks.size() * Math.random());
				keys[i] = ks.remove(k);
				if (keys[i] == null)
					System.out.println("nulnulnulnulnul");
			}
			System.out.println("result's length = " + keys.length);
			return keys;
		}
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
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
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
		if (isOutter) {
			isOutter = false;
			String kw = ((TextView) v).getText().toString();
			if (!kw.trim().equals("")) {
				searchLayout.removeAllViews();
			}
			search_text.setText(kw);
		}
		
	}
}

