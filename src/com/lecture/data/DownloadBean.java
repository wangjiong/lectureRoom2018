package com.lecture.data;

import android.graphics.drawable.Drawable;

public class DownloadBean {
	/** ��Ŀ���� */
	String title;
	/** ��Ŀ���� */
	String Episode;
	/** ��Ŀÿ������ */
	String Name;
	/** ���ؽ��Ȱٷֱ� */
	int download;
	public Drawable image;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getEpisode() {
		return Episode;
	}

	public void setEpisode(String episode) {
		Episode = episode;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public int getDownload() {
		return download;
	}

	public void setDownload(int download) {
		this.download = download;
	}

	@Override
	public String toString() {
		return title + " " + Episode + " " + Name + " " + download;
	}

}
