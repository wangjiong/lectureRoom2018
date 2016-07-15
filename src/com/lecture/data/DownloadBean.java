package com.lecture.data;

public class DownloadBean {
	/** id �������ݿ�Ψһ��־ */
	int id;
	/** ��Ŀ���� */
	String title;
	/** ��Ŀ���� */
	String Episode;
	/** ��Ŀÿ������ */
	String Name;
	/** ���ؽ��Ȱٷֱ� */
	int download;
	/** �ܵķֶ���Ŀ */
	int segmentTotal;
	/** �Ѿ����صķֶ���Ŀ */
	int segment;
	/** finish ����һ���Ƿ��������� */
	boolean finish;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isFinish() {
		return finish;
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}

	public DownloadBean() {

	}

	public DownloadBean(String title, String episode, String name, int segmentTotal, int segment, int download, boolean finish) {
		this.title = title;
		this.Episode = episode;
		this.Name = name;
		this.segmentTotal = segmentTotal;
		this.segment = segment;
		this.download = download;
		this.finish = finish;
	}

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

	public int getSegmentTotal() {
		return segmentTotal;
	}

	public void setSegmentTotal(int segmentTotal) {
		this.segmentTotal = segmentTotal;
	}

	public int getSegment() {
		return segment;
	}

	public void setSegment(int segment) {
		this.segment = segment;
	}

	@Override
	public String toString() {
		return "id:" + id + " title:" + title + " Episode:" + Episode + " Name:" + Name + " download:" + download + " segment:" + segment + " segmentTotal:" + segmentTotal + " finish:" + finish;
	}

}
