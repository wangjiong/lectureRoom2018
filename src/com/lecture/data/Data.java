package com.lecture.data;

import java.util.List;

public class Data {
	public List<ProgramBean> programBeans;
	public List<UnitBean> unitBeans;
	
	public static class ProgramBean {
		/**
		 * 节目唯一ID
		 */
		String Id;
		/**
		 * 节目名称
		 */
		String Name;
		/**
		 * 朝代
		 */
		int Dynasty;
		/**
		 * 节目作者
		 */
		String Author;
		/**
		 * 节目播放起始时间
		 */
		int PlayTime;
		/**
		 * 节目集数
		 */
		int Num;

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
}
