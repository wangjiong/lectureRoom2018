package com.lecture.item.activity;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.Toast;

import com.lecture.data.DbData;
import com.lecture.data.DownloadBean;
import com.lecture.data.UnitBean;
import com.lecture.item.view.MyMediaControllerView;
import com.lecture.media.R;

public class MovieDownloadAct extends Activity implements OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback, MediaPlayerControl, OnErrorListener, OnInfoListener {
	// 数据
	private DownloadBean downloadBean;
	private UnitBean unitBean;
	private String[] Urls; // 网络存放的位置
	private String[] localUrls;// 本地存放的位置
	// 布局
	private int mVideoWidth;
	private int mVideoHeight;
	private int mVideoWidthTemp;
	private int mVideoHeightTemp;
	private MediaPlayer mMediaPlayer;
	private SurfaceView mPreview;
	private SurfaceHolder holder;
	private boolean mIsVideoSizeKnown = false;
	private boolean mIsVideoReadyToBePlayed = false;

	private Display currentDisplay;
	private MyMediaControllerView controller;
	private ProgressDialog progressDialog;
	private boolean mIsVideoError = false;
	private boolean mIsVideofirst = true;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		initData();
		initView();
	}

	private void initData() {
		downloadBean = PersonDownloadAct.downloadBean;
		unitBean = DbData.getUnitBeanByTitleAndEpisode(downloadBean.getTitle(), Integer.parseInt(downloadBean.getEpisode()));
		Urls = new String[unitBean.getSegment()];
		String s = unitBean.getUrl();
		if (s.charAt(s.length() - 1) == '_') {// 视频url的两种形式'_'和'-'
			for (int i = 0; i < Urls.length; i++) {
				if (i < 9) {
					Urls[i] = s + "00" + (i + 1) + ".mp4";
				} else {
					Urls[i] = s + "0" + (i + 1) + ".mp4";
				}
			}
		} else if (s.charAt(s.length() - 1) == '-') {
			for (int i = 0; i < Urls.length; i++) {
				Urls[i] = s + (i + 1) + ".mp4";
			}
		}
		localUrls = PersonDownloadAct.Urls;
	}

	private void initView() {
		setContentView(R.layout.media);
		currentDisplay = getWindowManager().getDefaultDisplay();
		mPreview = (SurfaceView) findViewById(R.id.surface);
		holder = mPreview.getHolder();
		holder.addCallback(this);
		holder.setFormat(PixelFormat.RGBA_8888);
		controller = new MyMediaControllerView(this, unitBean, Urls);
		// 对话框
		progressDialog = new ProgressDialog(MovieDownloadAct.this);
		progressDialog.setTitle(null);
		progressDialog.setMessage("视频正在拼命加载中...");
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
	}

	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		if (mIsVideofirst) {
			playVideo();
		} else {
			mMediaPlayer.setDisplay(holder);
			startVideoPlayback();
		}
	}

	private void playVideo() {
		doCleanUp();
		try {
			mMediaPlayer = new MediaPlayer(this);
			mMediaPlayer.setDataSegments(localUrls, DbData.fileCache.toString());
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
			System.out.println("playVideo error");
		}
	}

	public void onPrepared(MediaPlayer mediaplayer) {
		mIsVideoReadyToBePlayed = true;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	public void onBufferingUpdate(MediaPlayer arg0, int percent) {
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

	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		switch (what) {
		case MediaPlayer.MEDIA_ERROR_IO:
		case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
		case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
		case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
		case MediaPlayer.MEDIA_ERROR_MALFORMED:
		case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
			error();
			break;
		}
		return true;
	}

	public void onCompletion(MediaPlayer arg0) {
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

	@SuppressWarnings("deprecation")
	private void startVideoPlayback() {
		mVideoWidth = mMediaPlayer.getVideoWidth();
		mVideoHeight = mMediaPlayer.getVideoHeight();
		if (mVideoWidth > currentDisplay.getWidth() || mVideoHeight > currentDisplay.getHeight()) {
			float heightRatio = (float) mVideoHeight / (float) currentDisplay.getHeight();
			float widthRatio = (float) mVideoWidth / (float) currentDisplay.getWidth();
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
			float heightRatio = (float) mVideoHeight / (float) currentDisplay.getHeight();
			float widthRatio = (float) mVideoWidth / (float) currentDisplay.getWidth();
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
		mVideoWidthTemp = mVideoWidth;
		mVideoHeightTemp = mVideoHeight;
		holder.setFixedSize(mVideoWidth, mVideoHeight);
		progressDialog.dismiss();
		mMediaPlayer.start();
		controller.setMediaPlayer(this);
		controller.setAnchorView(this.findViewById(R.id.MainView));
		controller.setEnabled(true);
		controller.show();
	}

	private void error() {
		Toast.makeText(MovieDownloadAct.this, "视频加载错误,请检查网络", Toast.LENGTH_LONG).show();
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		mIsVideoError = true;
		controller.show(Integer.MAX_VALUE);
	}

	@Override
	public boolean canPause() {
		// TODO 自动生成的方法存根
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		// TODO 自动生成的方法存根
		return true;
	}

	@Override
	public boolean canSeekForward() {
		// TODO 自动生成的方法存根
		return true;
	}

	@Override
	public int getAudioSessionId() {
		// TODO 自动生成的方法存根
		return mMediaPlayer.getAudioSessionId();
	}

	@Override
	public int getBufferPercentage() {
		// TODO 自动生成的方法存根
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		// TODO 自动生成的方法存根
		return (int) mMediaPlayer.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		// TODO 自动生成的方法存根
		return (int) mMediaPlayer.getDuration();
	}

	@Override
	public boolean isPlaying() {
		// TODO 自动生成的方法存根
		return mMediaPlayer.isPlaying();
	}

	@Override
	public void pause() {
		// TODO 自动生成的方法存根
		if (mMediaPlayer.isPlaying())
			mMediaPlayer.pause();
	}

	@Override
	public void seekTo(int pos) {
		// TODO 自动生成的方法存根
		mMediaPlayer.seekTo(pos);
	}

	@Override
	public void start() {
		// TODO 自动生成的方法存根
		mMediaPlayer.start();
	}

	long last,x,y;

	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - last) < 300) {
				System.out.println("onTouchEvent");
				if (mVideoWidth == mVideoWidthTemp) {
					mVideoWidth = currentDisplay.getWidth();
					mVideoHeight = currentDisplay.getHeight();
					holder.setFixedSize(mVideoWidth, mVideoHeight);
				} else {
					mVideoWidth = mVideoWidthTemp;
					mVideoHeight = mVideoHeightTemp;
					holder.setFixedSize(mVideoWidth, mVideoHeight);
				}

			}
			last = System.currentTimeMillis();
			if (controller.isShowing()) {
				controller.hide();
			} else {
				controller.show();
			}
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mIsVideofirst) {
			controller.show(Integer.MAX_VALUE);
		} else {
			controller.show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
		if (controller != null && controller.isShowing()) {
			controller.hide();
		}
		if (mIsVideoError) {
			finish();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		mIsVideofirst = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseMediaPlayer();
		doCleanUp();
	}
}
