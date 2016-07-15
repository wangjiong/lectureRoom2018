package com.lecture.data;

public class HistoryBean {
	int id;
	/** 节目首播的时间 */
	String idTime;
	/** 节目名称 */
	String title;
	/** 节目集数 */
	String num;
	/** 节目作者 */
	String author;
	/** 节目集数 */
	String episode;
	/** 节目每集名字 */
	String name;
	/** 播放时间 */
	long playTime;

	public HistoryBean() {

	}

	public HistoryBean(String idTime, String title, String num, String author, String episode, String name, long playTime) {
		this.idTime = idTime;
		this.title = title;
		this.num = num;
		this.author = author;
		this.episode = episode;
		this.name = name;
		this.playTime = playTime;
	}
	
	public HistoryBean(HistoryBean h) {
		this.idTime = h.getIdTime();
		this.title = h.getTitle();
		this.num = h.getNum();
		this.author = h.getAuthor();
		this.episode = h.getEpisode();
		this.name = h.getName();
		this.playTime = h.getPlayTime();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIdTime() {
		return idTime;
	}

	public void setIdTime(String idTime) {
		this.idTime = idTime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNum() {
		return num;
	}

	public void setNum(String num) {
		this.num = num;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getEpisode() {
		return episode;
	}

	public void setEpisode(String episode) {
		this.episode = episode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getPlayTime() {
		return playTime;
	}

	public void setPlayTime(long playTime) {
		this.playTime = playTime;
	}

	@Override
	public String toString() {
		return "HistoryBean [id=" + id + ", idTime=" + idTime + ", title=" + title + ", num=" + num + ", author=" + author + ", episode=" + episode + ", name=" + name + ", playTime=" + playTime + "]";
	}

}
