package com.lecture.data;

import android.graphics.drawable.Drawable;

public class HistoryBean {
	/** ��ĿΨһID */
	String Id;
	/** ��Ŀ���� */
	String title;
	/** ��Ŀ���� */
	String Num;
	/** ��Ŀ���� */
	String Author;
	/** ��Ŀ���� */
	String Episode;
	/** ��Ŀÿ������ */
	String Name;
	/** ����ʱ�� */
	String time;
	public Drawable image;

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getNum() {
		return Num;
	}

	public void setNum(String num) {
		Num = num;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return Author;
	}

	public void setAuthor(String author) {
		Author = author;
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

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return Id + " " + title + " " + Num + " " + Author + " " + Episode
				+ " " + Name + " " + time;
	}

}
