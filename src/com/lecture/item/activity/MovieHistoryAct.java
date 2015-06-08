package com.lecture.item.activity;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnSeekCompleteListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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
import com.lecture.data.HistoryBean;
import com.lecture.data.UnitBean;
import com.lecture.item.view.MyMediaControllerView;
import com.lecture.media.R;

public class MovieHistoryAct extends Activity implements OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback, MediaPlayerControl, OnErrorListener, OnInfoListener, OnSeekCompleteListener {
	// 数据
	private UnitBean unitBean;
	private String[] Urls;
	// 布局
	private int mVideoWidth;
	private int mVideoHeight;
	private MediaPlayer mMediaPlayer;
	private SurfaceView mPreview;
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
	private boolean mIsFullScreen = false;
	private int position;
	//声音
	private RelativeLayout gesture_volume_layout;// 音量控制布局
	private TextView geture_tv_volume_percentage;// 音量百分比
	private ImageView gesture_iv_player_volume;// 音量图标
	private AudioManager audiomanager;
	private int maxVolume, currentVolume;
	WakeLock wakeLock;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		initData();
		initView();
	}

	private void initData() {
		unitBean = DbData.getUnitBeanByTitleAndEpisode(PersonHistoryAct.historyBean.getTitle(), Integer.parseInt(PersonHistoryAct.historyBean.getEpisode()));
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
	}

	@SuppressWarnings("deprecation")
	private void initView() {
		setContentView(R.layout.media);
		mPreview = (SurfaceView) findViewById(R.id.surface);
		holder = mPreview.getHolder();
		holder.addCallback(this);
		holder.setFormat(PixelFormat.RGBA_8888);
		currentDisplay = getWindowManager().getDefaultDisplay();
		controller = new MyMediaControllerView(this, unitBean, Urls);
		// 对话框
		progressDialog = new ProgressDialog(MovieHistoryAct.this);
		progressDialog.setTitle(null);
		progressDialog.setMessage("视频正在拼命加载中...");
		progressDialog.setCancelable(false);
		progressDialog.setOnKeyListener(onKeyListener);
		progressDialog.show();
		// 声音
		gesture_volume_layout = (RelativeLayout) findViewById(R.id.gesture_volume_layout);
		gesture_iv_player_volume = (ImageView) findViewById(R.id.gesture_iv_player_volume);
		geture_tv_volume_percentage = (TextView) findViewById(R.id.geture_tv_volume_percentage);
		gesture_iv_player_volume = (ImageView) findViewById(R.id.gesture_iv_player_volume);
		audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
		currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,"Movie");
	}

	public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
	}

	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		if (mIsVideoFirst) {
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
			mMediaPlayer.setDataSegments(Urls, DbData.fileCache.toString());
			mMediaPlayer.setDisplay(holder);
			mMediaPlayer.prepareAsync();
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnInfoListener(this);
			mMediaPlayer.setOnErrorListener(this);
			mMediaPlayer.setOnSeekCompleteListener(this);
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
		} catch (Exception e) {
			System.out.println("playVideo error");
		}
	}

	public void onPrepared(MediaPlayer mediaplayer) {
		mMediaPlayer.seekTo(Integer.parseInt(PersonHistoryAct.historyBean.getTime()));
		mIsVideoReadyToBePlayed = true;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		progressDialog.dismiss();
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

	boolean needResume=false;
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
            //开始缓存，暂停播放
            if (mIsDialogFirst !=true && isPlaying()) {//防止第一次加载视频时对话框总是闪一下
            	pause();
                needResume = true;
                progressDialog.show();
            }
            mIsDialogFirst=false;
            break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
            //缓存完成，继续播放
            if (needResume){
                start();
                needResume=false;
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
		if (mIsFullScreen) {
			holder.setFixedSize(currentDisplay.getWidth(), currentDisplay.getHeight());
		} else {
			holder.setFixedSize(mVideoWidth, mVideoHeight);
		}
		mMediaPlayer.start();
		controller.setMediaPlayer(this);
		controller.setAnchorView(this.findViewById(R.id.MainView));
		controller.setEnabled(true);
		controller.show();
		mIsVideoPlay = true;
	}

	private void error() {
		Toast.makeText(MovieHistoryAct.this, "视频加载错误,请检查网络", Toast.LENGTH_LONG).show();
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

	long last;
	float moveY;

	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		// 屏幕大小调节
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - last) < 300) {
				if (!mIsFullScreen) {
					holder.setFixedSize(currentDisplay.getWidth(), currentDisplay.getHeight());
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
			if (Math.abs(e.getY() - moveY) < 5 ) {
				return false;
			}
			gesture_volume_layout.setVisibility(View.VISIBLE);
			currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
			if (e.getY() < moveY-5) {
				if (currentVolume < maxVolume) {
					currentVolume++;
					moveY=e.getY();
				}
				gesture_iv_player_volume.setImageResource(R.drawable.player_volume);
			} else if (e.getY() > moveY+5){
				if (currentVolume > 0) {
					currentVolume--;
					moveY=e.getY();
					if (currentVolume == 0) {// 静音，设定静音独有的图片
						gesture_iv_player_volume.setImageResource(R.drawable.player_silence);
					}
				}
			}
			moveY=e.getY();
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
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
		if (controller != null && controller.isShowing()) {
			controller.hide();
		}
		// 存储播放记录
		if (mIsVideoPlay == true && mIsCompletion == false && mMediaPlayer != null) {
			position = (int) mMediaPlayer.getCurrentPosition();
			HistoryBean historyBean = PersonHistoryAct.historyBean;
			historyBean.setTime(position + "");
			DbData.writeHistory(historyBean);
		}
		if (mIsCompletion == true && !mIsVideoError) {
			DbData.deleteHistory(PersonHistoryAct.historyBean);
		}
		
		wakeLock.release();
		
		if (mIsVideoError) {
			finish();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		mIsVideoFirst = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseMediaPlayer();
		doCleanUp();
	}
}
