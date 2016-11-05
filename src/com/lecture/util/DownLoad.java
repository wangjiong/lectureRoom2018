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
	// �������ص��ļ������3��
	public static List<String> sDownloading = new ArrayList<String>();
	@SuppressWarnings("rawtypes")
	public static HashMap<String, HttpHandler> sHandlerMap = new HashMap<String, HttpHandler>();
	// ÿ��3��ˢ��һ�Σ��ϴ�ˢ��ʱ��
	public static long sLastTime;
	public static final long REFRESHTIME = 3000;
	public static final long DOWNLOADFAILCOUNT = 15;

	// ��Ҫ���صĽ�Ŀ��Ϣ
	Context mContext;
	UnitBean mUnitBean;
	String mDownloadString;
	DownloadBean mDownloadBean;
	String[] mUrls;
	File mFile;// ���ص��ļ���
	int mSegment;// �Ѿ����صĽ�Ŀ�ֶ���Ŀ
	int mDownloadFailCount = 0;

	public DownLoad(UnitBean unitBean) {
		mContext = DbData.mContext;
		mUnitBean = unitBean;
	}

	// �����ļ����ļ���
	public boolean downLoad() {
		// �ж��Ƿ���������
		mDownloadString = mUnitBean.getTitle() + mUnitBean.getEpisode();
		if (sDownloading.contains(mDownloadString)) {
			Toast.makeText(mContext, "��������", Toast.LENGTH_SHORT).show();
			return false;
		}
		// �ж�����������Ŀ����3��
		if (sDownloading.size() >= 3) {
			Toast.makeText(mContext, "�������ص��ļ����ܳ���3��Ŷ~", Toast.LENGTH_SHORT).show();
			return false;
		}
		List<DownloadBean> downloadBeans = DbData.sFinalDb.findAllByWhere(DownloadBean.class, "title='" + mUnitBean.getTitle() + "' and Episode='" + mUnitBean.getEpisode() + "'");
		if (downloadBeans == null || downloadBeans.size() == 0) {
			// ��ǰ��δ����
			if (!DbData.isNetworkConnected(mContext)) {
				Toast.makeText(mContext, "��ǰ���粻����", Toast.LENGTH_SHORT).show();
				return false;
			}
			DbData.sFinalDb.save(new DownloadBean(mUnitBean.getTitle(), mUnitBean.getEpisode() + "", mUnitBean.getName(), mUnitBean.getSegment(), 0, 0, false));// segmentΪ1
			mDownloadBean = DbData.sFinalDb.findAllByWhere(DownloadBean.class, "title='" + mUnitBean.getTitle() + "' and Episode='" + mUnitBean.getEpisode() + "'").get(0);
			DbData.sDownloadingBeans.add(0, mDownloadBean);
		} else {
			mDownloadBean = DbData.sFinalDb.findAllByWhere(DownloadBean.class, "title='" + mUnitBean.getTitle() + "' and Episode='" + mUnitBean.getEpisode() + "'").get(0);
		}
		if (mDownloadBean.getSegment() == mDownloadBean.getSegmentTotal() && mDownloadBean.isFinish()) {
			Toast.makeText(mContext, "�������", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!DbData.isNetworkConnected(mContext)) {
			Toast.makeText(mContext, "��ǰ���粻����", Toast.LENGTH_SHORT).show();
			return false;
		}
		// ��ʼ����
		if (mDownloadBean.getSegment() > 0) {
			// ��ǰ�����ع�
			Toast.makeText(mContext, "��������", Toast.LENGTH_SHORT).show();
			for (DownloadBean d : DbData.sDownloadingBeans) {// ��mDownloadBean��Ϊδ��������е�
				if (d.getId() == mDownloadBean.getId()) {
					mDownloadBean = d;
					break;
				}
			}
		} else {
			// ��ǰ��δ���ع�
			Toast.makeText(mContext, "��ʼ����", Toast.LENGTH_SHORT).show();
			mDownloadBean.setSegment(1);
			DbData.sFinalDb.update(mDownloadBean);
		}
		sDownloading.add(mUnitBean.getTitle() + mUnitBean.getEpisode());
		initData();
		download();
		return true;
	}

	// ����
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
					// ˢ�����������б�����ݿ�
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
					mDownloadFailCount = 0;// ���ش��������0
					download();
				} else {
					Log.i("onSuccess", "������� " + mDownloadBean.getDownload());
					Toast.makeText(mContext, mUnitBean.getName() + " �������", Toast.LENGTH_SHORT).show();
					mDownloadBean.setFinish(true);
					mDownloadBean.setDownload(100);
					DbData.sDownloadBeans.add(0, mDownloadBean);
					DbData.sDownloadingBeans.remove(mDownloadBean);
					sDownloading.remove(mUnitBean.getTitle() + mUnitBean.getEpisode());
					refreshDownloading();
					DbData.sFinalDb.update(mDownloadBean);
					refreshDownload();
					refreshDownloading();
					// ������¼������ɵ��ļ�
					DbData.createDownloadSuccess(mFile);
				}
			}

			// 0 user stop download thread || null
			// 416 response status error code:416
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				Log.i("onFailure", mUnitBean.getName() + " ����ʧ��  errorNo:" + errorNo + " strMsg:" + strMsg);
				if (errorNo == 0) {// �ֶ�ֹͣ����
					if (sDownloading.contains(mDownloadBean.getTitle() + mDownloadBean.getEpisode())) {
						DownLoad.sDownloading.remove(mDownloadBean.getTitle() + mDownloadBean.getEpisode());
					}
					// ������¼���ؽ��ȵ��ļ�
					if (sDownloading.contains(mDownloadString)) {
						DbData.createDownloadState(mFile, mDownloadBean.getDownload(), mSegment);
					}
					if (strMsg == null) { // ���粻��ʱ������ʧ��
						Toast.makeText(mContext, mUnitBean.getName() + " ����ʧ�� ", Toast.LENGTH_SHORT).show();
						refreshDownloading();
					}
					return;
				}
				if (mDownloadFailCount++ < DOWNLOADFAILCOUNT) {
					if (errorNo == 416) {// 416�����Ѿ������˸��ļ�
						mSegment++;
					}
					download();
				} else { // ����DOWNLOADFAILCOUNT�ε����ػ���ʧ����
					Toast.makeText(mContext, mUnitBean.getName() + " ����ʧ�� ", Toast.LENGTH_SHORT).show();
					if (sDownloading.contains(mDownloadBean.getTitle() + mDownloadBean.getEpisode())) {
						DownLoad.sDownloading.remove(mDownloadBean.getTitle() + mDownloadBean.getEpisode());
					}
					refreshDownloading();
					// ������¼���ؽ��ȵ��ļ�
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
		if (s.charAt(s.length() - 1) == '_') {// ��Ƶurl��������ʽ'_'��'-'
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
