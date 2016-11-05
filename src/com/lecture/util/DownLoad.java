package com.lecture.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import net.tsz.afinal.http.HttpHandler;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.lecture.data.DbData;
import com.lecture.data.DownloadBean;
import com.lecture.data.UnitBean;
import com.lecture.item.activity.PersonDownloadAct;
import com.lecture.item.activity.PersonDownloadingAct;

public class DownLoad {
	// 正在下载的文件，最多3个
	public static List<String> sDownloading = new ArrayList<String>();
	@SuppressWarnings("rawtypes")
	public static HashMap<String, HttpHandler> sHandlerMap = new HashMap<String, HttpHandler>();
	// 每个3秒刷新一次，上次刷新时间
	public static long sLastTime;
	public static final long REFRESHTIME = 3000;
	public static final long DOWNLOADFAILCOUNT = 15;

	// 需要下载的节目信息
	Context mContext;
	UnitBean mUnitBean;
	String mDownloadString;
	DownloadBean mDownloadBean;
	String[] mUrls;
	File mFile;// 下载的文件夹
	int mSegment;// 已经下载的节目分段数目
	int mDownloadFailCount = 0;

	public DownLoad(UnitBean unitBean) {
		mContext = DbData.mContext;
		mUnitBean = unitBean;
	}

	// 下载文件的文件夹
	public boolean downLoad() {
		// 判断是否正在下载
		mDownloadString = mUnitBean.getTitle() + mUnitBean.getEpisode();
		if (sDownloading.contains(mDownloadString)) {
			Toast.makeText(mContext, "正在下载", Toast.LENGTH_SHORT).show();
			return false;
		}
		// 判断正在下载数目超过3个
		if (sDownloading.size() >= 3) {
			Toast.makeText(mContext, "正在下载的文件不能超过3个哦~", Toast.LENGTH_SHORT).show();
			return false;
		}
		List<DownloadBean> downloadBeans = DbData.sFinalDb.findAllByWhere(DownloadBean.class, "title='" + mUnitBean.getTitle() + "' and Episode='" + mUnitBean.getEpisode() + "'");
		if (downloadBeans == null || downloadBeans.size() == 0) {
			// 以前从未下载
			if (!DbData.isNetworkConnected(mContext)) {
				Toast.makeText(mContext, "当前网络不可用", Toast.LENGTH_SHORT).show();
				return false;
			}
			DbData.sFinalDb.save(new DownloadBean(mUnitBean.getTitle(), mUnitBean.getEpisode() + "", mUnitBean.getName(), mUnitBean.getSegment(), 0, 0, false));// segment为1
			mDownloadBean = DbData.sFinalDb.findAllByWhere(DownloadBean.class, "title='" + mUnitBean.getTitle() + "' and Episode='" + mUnitBean.getEpisode() + "'").get(0);
			DbData.sDownloadingBeans.add(0, mDownloadBean);
		} else {
			mDownloadBean = DbData.sFinalDb.findAllByWhere(DownloadBean.class, "title='" + mUnitBean.getTitle() + "' and Episode='" + mUnitBean.getEpisode() + "'").get(0);
		}
		if (mDownloadBean.getSegment() == mDownloadBean.getSegmentTotal() && mDownloadBean.isFinish()) {
			Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!DbData.isNetworkConnected(mContext)) {
			Toast.makeText(mContext, "当前网络不可用", Toast.LENGTH_SHORT).show();
			return false;
		}
		// 开始下载
		if (mDownloadBean.getSegment() > 0) {
			// 以前有下载过
			Toast.makeText(mContext, "继续下载", Toast.LENGTH_SHORT).show();
			for (DownloadBean d : DbData.sDownloadingBeans) {// 将mDownloadBean换为未完成下载中的
				if (d.getId() == mDownloadBean.getId()) {
					mDownloadBean = d;
					break;
				}
			}
		} else {
			// 以前从未下载过
			Toast.makeText(mContext, "开始下载", Toast.LENGTH_SHORT).show();
			mDownloadBean.setSegment(1);
			DbData.sFinalDb.update(mDownloadBean);
		}
		sDownloading.add(mUnitBean.getTitle() + mUnitBean.getEpisode());
		initData();
		download();
		return true;
	}

	// 下载
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void download() {
		if (mSegment > mUrls.length) {
			return;
		}
		FinalHttp fh = new FinalHttp();
		HttpHandler handler = fh.download(mUrls[mSegment - 1], new AjaxParams(), mFile + "/movie" + (mSegment > 9 ? mSegment : ("0" + mSegment)) + ".mp4", true, new AjaxCallBack() {
			@Override
			public void onLoading(long count, long current) {
				if (System.currentTimeMillis() - sLastTime > REFRESHTIME) {
					Log.i("onLoading", (int) (100 * (1.0f * current / count + mSegment - 1) / mDownloadBean.getSegmentTotal()) + " ");
					mDownloadBean.setDownload((int) (100 * (1.0f * current / count + mSegment - 1) / mDownloadBean.getSegmentTotal()));
					// 刷新正在下载列表和数据库
					refreshDownloading();
					DbData.sFinalDb.update(mDownloadBean);
					sLastTime = System.currentTimeMillis();
				}
			}

			@Override
			public void onSuccess(Object t) {
				mDownloadBean.setSegment(mSegment);
				Log.i("onSuccess", "mSegment:" + mSegment + " SegmentTotal:" + mDownloadBean.getSegmentTotal());
				if (mSegment != mDownloadBean.getSegmentTotal()) {
					mSegment++;
					mDownloadFailCount = 0;// 下载错误次数清0
					download();
				} else {
					Log.i("onSuccess", "下载完成 " + mDownloadBean.getDownload());
					Toast.makeText(mContext, mUnitBean.getName() + " 下载完成", Toast.LENGTH_SHORT).show();
					mDownloadBean.setFinish(true);
					mDownloadBean.setDownload(100);
					DbData.sDownloadBeans.add(0, mDownloadBean);
					DbData.sDownloadingBeans.remove(mDownloadBean);
					sDownloading.remove(mUnitBean.getTitle() + mUnitBean.getEpisode());
					refreshDownloading();
					DbData.sFinalDb.update(mDownloadBean);
					refreshDownload();
					refreshDownloading();
					// 创建记录下载完成的文件
					DbData.createDownloadSuccess(mFile);
				}
			}

			// 0 user stop download thread || null
			// 416 response status error code:416
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				Log.i("onFailure", mUnitBean.getName() + " 下载失败  errorNo:" + errorNo + " strMsg:" + strMsg);
				if (errorNo == 0) {// 手动停止下载
					if (sDownloading.contains(mDownloadBean.getTitle() + mDownloadBean.getEpisode())) {
						DownLoad.sDownloading.remove(mDownloadBean.getTitle() + mDownloadBean.getEpisode());
					}
					// 创建记录下载进度的文件
					if (sDownloading.contains(mDownloadString)) {
						DbData.createDownloadState(mFile, mDownloadBean.getDownload(), mSegment);
					}
					if (strMsg == null) { // 网络不好时，下载失败
						Toast.makeText(mContext, mUnitBean.getName() + " 下载失败 ", Toast.LENGTH_SHORT).show();
						refreshDownloading();
					}
					return;
				}
				if (mDownloadFailCount++ < DOWNLOADFAILCOUNT) {
					if (errorNo == 416) {// 416代表已经下载了该文件
						mSegment++;
					}
					download();
				} else { // 经过DOWNLOADFAILCOUNT次的下载还是失败了
					Toast.makeText(mContext, mUnitBean.getName() + " 下载失败 ", Toast.LENGTH_SHORT).show();
					if (sDownloading.contains(mDownloadBean.getTitle() + mDownloadBean.getEpisode())) {
						DownLoad.sDownloading.remove(mDownloadBean.getTitle() + mDownloadBean.getEpisode());
					}
					refreshDownloading();
					// 创建记录下载进度的文件
					DbData.createDownloadState(mFile, mDownloadBean.getDownload(), mSegment);
				}
			}
		});
		sHandlerMap.put(mDownloadString, handler);
	}

	private void refreshDownloading() {
		if (PersonDownloadingAct.sAdapter != null) {
			PersonDownloadingAct.sAdapter.notifyDataSetChanged();
		}
	}

	private void refreshDownload() {
		if (PersonDownloadAct.sAdapter != null) {
			PersonDownloadAct.sAdapter.notifyDataSetChanged();
		}
	}

	private void initData() {
		mUrls = new String[mUnitBean.getSegment()];
		String s = mUnitBean.getUrl();
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
		mFile = DbData.createDownload(mUnitBean);
		mSegment = mDownloadBean.getSegment();
	}
}
