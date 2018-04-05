package com.lecture.item.activity;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lecture.data.DbData;
import com.lecture.data.DownloadBean;
import com.lecture.data.HistoryBean;
import com.lecture.data.ProgramBean;
import com.lecture.data.UnitBean;
import com.lecture.item.view.MyMediaControllerView;
import com.lecture.media.R;
import com.lecture.util.DownLoad;
import com.lecture.util.Param;

public class MovieAct extends Activity implements OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback, MediaPlayerControl, OnErrorListener, OnInfoListener {
	String TAG = "MovieAct==";
	// 数据
	private ProgramBean mProgramBean;
	private UnitBean mUnitBean;
	private HistoryBean mHistoryBean;
	private String[] mUrls;
	private long mPlayTime;
	private int mFromType;// 0:MovieIntroAct 1:PersonDownloadAct
	// 2:PersonHistoryAct 3:PersonDownloadingAct
	private Timer mTimer;
	private boolean mFirstHistory = true;
	// 布局
	private int mVideoWidth;
	private int mVideoHeight;
	private int mDisplayWidth;
	private int mDisplayHeight;
	private MediaPlayer mMediaPlayer;
	private SurfaceView mSurfaceView;
	private SurfaceHolder holder;
	private boolean mIsVideoSizeKnown = false;
	private boolean mIsVideoReadyToBePlayed = false;

	private Display currentDisplay;
	private MyMediaControllerView controller;
	private ProgressDialog progressDialog;
	private boolean mIsVideoError = false;
	private boolean mIsVideoFirst = true;
	private boolean mIsDialogFirst = true;
	private boolean mIsCompletion = false;
	private boolean mIsVideoPlay = false;
	private boolean mIsVideoPlayFirstStop = false; // 当视频第一次加载锁屏时，屏幕大小不能正确显示
	private boolean mIsFullScreen = false;
	private long mPosition;

	private RelativeLayout gesture_volume_layout;// 音量控制布局
	private TextView geture_tv_volume_percentage;// 音量百分比
	private ImageView gesture_iv_player_volume;// 音量图标
	private AudioManager audiomanager;
	private int maxVolume, currentVolume;
	WakeLock wakeLock;

	// Flag
	private boolean mIsActivityFlag = true;// Activity状态判断

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		initData();
		initView();
		mTimer = new Timer();
		mTimer.schedule(historyTask, 3000, 10000);
	}

	private void initData() {
		mFromType = getIntent().getIntExtra(Param.FROM_TYPE, 0);
		mPlayTime = getIntent().getLongExtra(Param.PLAY_TIME, 0);
		String title = getIntent().getStringExtra(Param.TITLE_KEY);
		int episode = Integer.parseInt(getIntent().getStringExtra(Param.EPISODE_KEY));
		mProgramBean = DbData.getProgramBeanByTitle(title);
		mUnitBean = DbData.getUnitBeanByTitleAndEpisode(title, episode);
		// 初始化历史记录
		List<HistoryBean> historyBeans = DbData.sFinalDb.findAllByWhere(HistoryBean.class, "title='" + mUnitBean.getTitle() + "' and Episode='" + mUnitBean.getEpisode() + "'");
		if (historyBeans != null && historyBeans.size() > 0) {
			mHistoryBean = historyBeans.get(0);
		}
		initUrl();
	}

	private void initUrl() {
		// 先检查是否下载完成
		List<DownloadBean> downloadBeans = DbData.sFinalDb.findAllByWhere(DownloadBean.class, "title='" + mUnitBean.getTitle() + "' and episode='" + mUnitBean.getEpisode() + "'");
		if (downloadBeans != null && downloadBeans.size() > 0) {
			if (downloadBeans.get(0).isFinish() || mFromType == 3) { // 3代表从未完成下载
				File[] allDownFiles = DbData.fileDownload.listFiles();
				File[] files = null;
				for (File f : allDownFiles) {
					if (f.toString().contains(mUnitBean.getTitle() + " " + mUnitBean.getEpisode() + " " + mUnitBean.getName())) {
						files = f.listFiles();
						break;
					}
				}
				if (files == null) {
					return;
				}
				List<String> temp = new ArrayList<String>();
				for (int i = 0; i < files.length; i++) {
					if (files[i].toString().contains("movie")) {
						temp.add(files[i].toString());
					}
				}
				mUrls = (String[]) temp.toArray(new String[temp.size()]);
				Arrays.sort(mUrls);
				return;
			}
		}
		// 如果下载完成中没有则用网络的
		mUrls = new String[mUnitBean.getSegment()];
		String s = mUnitBean.getUrl().trim();
		if (s.charAt(s.length() - 1) == '_') {// 视频url的两种形式'_'和'-'
			for (int i = 0; i < mUrls.length; i++) {
				if (i < 9) {
					mUrls[i] = s + "00" + (i + 1) + ".mp4";
				} else {
					mUrls[i] = s + "0" + (i + 1) + ".mp4";
				}
			}
		} else if (s.charAt(s.length() - 1) == '-') {
			for (int i = 0; i < mUrls.length; i++) {
				mUrls[i] = s + (i + 1) + ".mp4";
			}
		} else {
			for (int i = 0; i < mUrls.length; i++) {
				mUrls[i] = s;
			}
		}
	}

	private void initView() {
		Log.i(TAG, "initView");
		setContentView(R.layout.media);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		holder = mSurfaceView.getHolder();
		holder.addCallback(this);
		holder.setFormat(PixelFormat.RGBA_8888);
		currentDisplay = getWindowManager().getDefaultDisplay();
		controller = new MyMediaControllerView(this, mUnitBean, mUrls);
		progressDialog = new ProgressDialog(MovieAct.this);
		progressDialog.setTitle(null);
		progressDialog.setMessage("视频正在加载中...");
		progressDialog.setCancelable(false);
		progressDialog.setOnKeyListener(onKeyListener);
		progressDialog.show();
		// 屏幕
		mDisplayWidth = currentDisplay.getWidth();
		mDisplayHeight = currentDisplay.getHeight();
		// 声音
		gesture_volume_layout = (RelativeLayout) findViewById(R.id.gesture_volume_layout);
		gesture_iv_player_volume = (ImageView) findViewById(R.id.gesture_iv_player_volume);
		geture_tv_volume_percentage = (TextView) findViewById(R.id.geture_tv_volume_percentage);
		gesture_iv_player_volume = (ImageView) findViewById(R.id.gesture_iv_player_volume);
		audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
		currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Movie");
	}

	public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
	}

	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");
		if (mIsVideoFirst) {
			Log.i(TAG, "surfaceCreated mIsVideoFirst");
			playVideo();
		} else {
			Log.i(TAG, "surfaceCreated startVideoPlayback");
			mMediaPlayer.setDisplay(holder);
			startVideoPlayback();
		}
	}

	private void playVideo() {
		Log.i(TAG, "playVideo");
		doCleanUp();
		try {
			mMediaPlayer = new MediaPlayer(this);
			if (mUrls == null) {
				return;
			}
			mMediaPlayer.setDataSegments(mUrls, DbData.fileMovieCache.toString());
			mMediaPlayer.setDisplay(holder);
			mMediaPlayer.prepareAsync();
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnInfoListener(this);
			mMediaPlayer.setOnErrorListener(this);
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
		} catch (Exception e) {
		}
	}

	public void onPrepared(MediaPlayer mediaplayer) {
		Log.i(TAG, "onPrepared:" + mMediaPlayer.getDuration());
		if (mMediaPlayer.getDuration() < 1000) {
//			releaseMediaPlayer();
//			progressDialog.setMessage("视频正在加载中..." + 0 + "%");
//			playVideo();
			Toast.makeText(MovieAct.this, "视频加载错误，试试下载后再播放~", Toast.LENGTH_LONG).show();
			new DownLoad(mUnitBean).downLoad();
			finish();
			return;
		}
		if (mPlayTime > 0) {
			mMediaPlayer.seekTo(mPlayTime);
		}
		progressDialog.dismiss();
		mIsVideoReadyToBePlayed = true;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	public void onBufferingUpdate(MediaPlayer arg0, final int percent) {
		progressDialog.setMessage("视频正在加载中..." + percent + "%");
	}

	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		if (width == 0 || height == 0) {
			return;
		}
		mIsVideoSizeKnown = true;
		mVideoWidth = width;
		mVideoHeight = height;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	boolean needResume = false;

	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			Log.i(TAG, "surfaceCreated MEDIA_INFO_BUFFERING_START");
			// 开始缓存，暂停播放
			if (mIsDialogFirst != true && isPlaying()) {// 防止第一次加载视频时对话框总是闪一下
				pause();
				needResume = true;
				progressDialog.show();
				progressDialog.setMessage("视频正在加载中...0%");
			}
			mIsDialogFirst = false;
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			Log.i(TAG, "surfaceCreated MEDIA_INFO_BUFFERING_END");
			// 缓存完成，继续播放
			if (needResume) {
				start();
				needResume = false;
				progressDialog.dismiss();
			}
			break;
		case MediaPlayer.MEDIA_ERROR_IO:
		case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
		case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
		case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
		case MediaPlayer.MEDIA_ERROR_MALFORMED:
		case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
			Log.i(TAG, "surfaceCreated error");
			error();
			break;
		}
		return true;
	}

	public void onCompletion(MediaPlayer arg0) {
		mIsCompletion = true;
		finish();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		error();
		return false;
	}

	private void releaseMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private void doCleanUp() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		mIsVideoReadyToBePlayed = false;
		mIsVideoSizeKnown = false;
	}

	private void startVideoPlayback() {
		Log.i(TAG, "startVideoPlayback");
		mVideoWidth = mMediaPlayer.getVideoWidth();
		mVideoHeight = mMediaPlayer.getVideoHeight();
		if (mVideoWidth > mDisplayWidth || mVideoHeight > mDisplayHeight) {
			float heightRatio = (float) mVideoHeight / (float) mDisplayHeight;
			float widthRatio = (float) mVideoWidth / (float) mDisplayWidth;
			if (heightRatio > 1 || widthRatio > 1) {
				if (heightRatio > widthRatio) {
					mVideoHeight = (int) Math.ceil((float) mVideoHeight / (float) heightRatio);
					mVideoWidth = (int) Math.ceil((float) mVideoWidth / (float) heightRatio);
				} else {
					mVideoHeight = (int) Math.ceil((float) mVideoHeight / (float) widthRatio);
					mVideoWidth = (int) Math.ceil((float) mVideoWidth / (float) widthRatio);
				}
			}
		} else {
			float heightRatio = (float) mVideoHeight / (float) mDisplayHeight;
			float widthRatio = (float) mVideoWidth / (float) mDisplayWidth;
			if (heightRatio < 1 || widthRatio < 1) {
				if (heightRatio > widthRatio) {
					mVideoHeight = (int) Math.ceil((float) mVideoHeight / (float) heightRatio);
					mVideoWidth = (int) Math.ceil((float) mVideoWidth / (float) heightRatio);
				} else {
					mVideoHeight = (int) Math.ceil((float) mVideoHeight / (float) widthRatio);
					mVideoWidth = (int) Math.ceil((float) mVideoWidth / (float) widthRatio);
				}
			}
		}
		if (mIsFullScreen) {
			holder.setFixedSize(mDisplayWidth, mDisplayHeight);
		} else {
			holder.setFixedSize(mVideoWidth, mVideoHeight);
		}
		if (mIsActivityFlag) {
			if (mIsVideoPlayFirstStop) {
				//mMediaPlayer.pause();
			} else {
				mMediaPlayer.start();
			}
			mIsVideoPlayFirstStop = false;
			mIsVideoPlay = true;
		} else {
			//mMediaPlayer.pause();
			if (!mIsVideoPlay) {
				mIsVideoPlayFirstStop = true;
			}
		}
		controller.setMediaPlayer(this);
		controller.setAnchorView(this.findViewById(R.id.MainView));
		controller.setEnabled(true);
		controller.show();
	}

	private void error() {
		Toast.makeText(MovieAct.this, "视频加载错误,请检查网络", Toast.LENGTH_LONG).show();
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		mIsVideoError = true;
		controller.show(Integer.MAX_VALUE);
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getAudioSessionId() {
		return mMediaPlayer.getAudioSessionId();
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		return (int) mMediaPlayer.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return (int) mMediaPlayer.getDuration();
	}

	@Override
	public boolean isPlaying() {
		return mMediaPlayer.isPlaying();
	}

	@Override
	public void pause() {
		if (mMediaPlayer.isPlaying()){
			//mMediaPlayer.pause();
		}
	}

	@Override
	public void seekTo(int pos) {
		mMediaPlayer.seekTo(pos);
	}

	@Override
	public void start() {
		if (mIsActivityFlag) {
			mMediaPlayer.start();
		} else {
			//mMediaPlayer.pause();
		}
	}

	long last;
	float moveY;

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		// 屏幕大小调节
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - last) < 300) {
				if (!mIsFullScreen) {
					holder.setFixedSize(mDisplayWidth, mDisplayHeight);
					mIsFullScreen = true;
				} else {
					holder.setFixedSize(mVideoWidth, mVideoHeight);
					mIsFullScreen = false;
				}

			}
			last = System.currentTimeMillis();
			moveY = e.getY();
			if (controller.isShowing()) {
				controller.hide();
			} else {
				controller.show();
			}
		}
		// 声音调节
		if (e.getAction() == MotionEvent.ACTION_MOVE) {
			if (Math.abs(e.getY() - moveY) < 5) {
				return false;
			}
			gesture_volume_layout.setVisibility(View.VISIBLE);
			currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
			if (e.getY() < moveY - 5) {
				if (currentVolume < maxVolume) {
					currentVolume++;
					moveY = e.getY();
				}
				gesture_iv_player_volume.setImageResource(R.drawable.player_volume);
			} else if (e.getY() > moveY + 5) {
				if (currentVolume > 0) {
					currentVolume--;
					moveY = e.getY();
					if (currentVolume == 0) {// 静音，设定静音独有的图片
						gesture_iv_player_volume.setImageResource(R.drawable.player_silence);
					}
				}
			}
			moveY = e.getY();
			int percentage = (currentVolume * 100) / maxVolume;
			geture_tv_volume_percentage.setText(percentage + "%");
			audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
		}
		if (e.getAction() == MotionEvent.ACTION_UP) {
			gesture_volume_layout.setVisibility(View.INVISIBLE);
		}
		return false;
	}

	private OnKeyListener onKeyListener = new OnKeyListener() {
		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
				progressDialog.dismiss();
				finish();
			}
			return false;
		}

	};

	@Override
	protected void onResume() {
		super.onResume();
		mIsActivityFlag = true;
		if (mIsVideoPlayFirstStop) {
			startVideoPlayback();// 主要是视频的大小显示不正确
		}
		if (mIsVideoFirst) {
			controller.show(Integer.MAX_VALUE);
		} else {
			controller.show();
		}
		wakeLock.acquire();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mIsActivityFlag = false;
		if (mIsVideoPlay) {
			mIsVideoFirst = false;
		}
		if (mMediaPlayer != null && mIsVideoPlay && mMediaPlayer.isPlaying()) {
			//mMediaPlayer.pause();
		}
		if (controller != null && controller.isShowing()) {
			controller.hide();
		}
		if (mIsVideoError) {
			finish();
		}
		wakeLock.release();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mTimer != null) {
			mTimer.cancel();
		}
		releaseMediaPlayer();
		doCleanUp();
	}

	@Override
	public void finish() {
		saveHistory();
		if (mMediaPlayer != null && mIsVideoPlay) {
			if (mFromType == 2) { // 返回到历史界面，主要用于刷新
				if (mIsCompletion) {
					mPosition = -1;// -1代表已经播放完毕
				} else {
					mPosition = mMediaPlayer.getCurrentPosition();
				}
				setResult(Activity.RESULT_OK, new Intent().putExtra(Param.PLAY_TIME, mPosition));
			}
		}
		super.finish();
	}

	private void saveHistory() {
		if (mIsVideoPlay == true && mIsCompletion == false && mMediaPlayer != null) {
			if (mPosition == mMediaPlayer.getCurrentPosition()) {
				return;
			}
			mPosition = mMediaPlayer.getCurrentPosition();
			if (mPosition == 0) {
				return;
			}
			// 如果没有历史记录，那么新生成一条
			if (mHistoryBean == null) {
				DbData.sFinalDb.save(new HistoryBean(mProgramBean.getId(), mProgramBean.getName(), mProgramBean.getNum() + "", mProgramBean.getAuthor(), mUnitBean.getEpisode() + "", mUnitBean.getName(), mPosition));
				mHistoryBean = DbData.sFinalDb.findAllByWhere(HistoryBean.class, "title='" + mUnitBean.getTitle() + "' and episode='" + mUnitBean.getEpisode() + "'").get(0);
			} else {
				// 如果有历史记录，那么先把其删除，然后插入一条到最后
				if (mFirstHistory) {
					mFirstHistory = false;
					DbData.sFinalDb.delete(mHistoryBean);
					DbData.sFinalDb.save(new HistoryBean(mHistoryBean));
					List<HistoryBean> historyBeans = DbData.sFinalDb.findAllByWhere(HistoryBean.class, "title='" + mUnitBean.getTitle() + "' and Episode='" + mUnitBean.getEpisode() + "'");
					mHistoryBean = historyBeans.get(0);
				}
			}
			mHistoryBean.setPlayTime(mPosition);
			DbData.sFinalDb.update(mHistoryBean);
			return;
		}
		if (mIsCompletion == true && !mIsVideoError && mHistoryBean != null) {
			DbData.sFinalDb.delete(mHistoryBean);
		}
	}

	TimerTask historyTask = new TimerTask() {
		public void run() {
			saveHistory();
		}
	};
}
