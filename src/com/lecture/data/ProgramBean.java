package com.lecture.data;

import android.graphics.drawable.Drawable;

public class ProgramBean {
	/**
	 * ��ĿΨһID
	 */
	String Id;
	/**
	 * ��Ŀ����
	 */
	String Name;
	/**
	 * ����
	 */
	int Dynasty;
	/**
	 * ��Ŀ����
	 */
	String Author;
	/**
	 * ��Ŀ������ʼʱ��
	 */
	int PlayTime;
	/**
	 * ��Ŀ����
	 */
	int Num;

	public Drawable drawable;

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public int getDynasty() {
		return Dynasty;
	}

	public void setDynasty(int dynasty) {
		Dynasty = dynasty;
	}

	public String getAuthor() {
		return Author;
	}

	public void setAuthor(String author) {
		Author = author;
	}

	public int getPlayTime() {
		return PlayTime;
	}

	public void setPlayTime(int time) {
		PlayTime = time;
	}

	public int getNum() {
		return Num;
	}

	public void setNum(int num) {
		Num = num;
	}
}
