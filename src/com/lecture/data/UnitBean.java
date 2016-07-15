package com.lecture.data;

public class UnitBean {
	/**
	 * 节目名称
	 */
	String Title;
	/**
	 * 节目集数
	 */
	int Episode;
	/**
	 * 节目每集名字
	 */
	String Name;
	/**
	 * 节目Url
	 */
	String Url;
	/**
	 * 节目分段数
	 */
	int Segment;

	public UnitBean() {

	}

	public UnitBean(String Title, int Episode, String Name, int Segment) {
		this.Title = Title;
		this.Episode = Episode;
		this.Name = Name;
		this.Segment = Segment;
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public int getEpisode() {
		return Episode;
	}

	public void setEpisode(int episode) {
		Episode = episode;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getUrl() {
		return Url;
	}

	public void setUrl(String url) {
		Url = url;
	}

	public int getSegment() {
		return Segment;
	}

	public void setSegment(int segment) {
		Segment = segment;
	}

}
