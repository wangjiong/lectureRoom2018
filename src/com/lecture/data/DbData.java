package com.lecture.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.widget.Toast;

import com.lecture.item.view.KeywordsView;

public class DbData {
	public static SQLiteDatabase db;// ��MainActivity�г�ʼ��;
	private static Context mContext;

	public static File fileCache, textHistory, fileDownload;

	public DbData(Context context) {
		// ��ʼ����Ŀ���ݿ�
		mContext = context;
		DataBaseHelper myDbHelper = new DataBaseHelper(mContext);
		try {
			myDbHelper.createDataBase();
			myDbHelper.openDataBase();
			DbData.db = myDbHelper.db;
		} catch (IOException e) {
			e.printStackTrace();
		}
		// �����ļ�
		if (!createFile()) {
			Toast.makeText(context, "��Ǹ����SD����", Toast.LENGTH_SHORT).show();
		}
	}

	// ͨ��ID���ProgramBean
	@SuppressWarnings("deprecation")
	public static ProgramBean getProgramBeanById(String id) {
		Cursor cursor = db.rawQuery("select * from program where id=" + id, null);
		if (cursor.moveToNext()) {
			ProgramBean programBean = new ProgramBean();
			programBean.setId(cursor.getString(0));
			programBean.setName(cursor.getString(1));
			programBean.setAuthor(cursor.getString(2));
			programBean.setPlayTime(cursor.getInt(3));
			programBean.setNum((cursor.getInt(4)));
			programBean.image = new BitmapDrawable(getImageFromAssetsFile("img" + programBean.getId() + ".jpg"));
			return programBean;
		}
		return null;
	}

	// ͨ��TITLE�ͼ������UnitBean
	public static UnitBean getUnitBeanByTitleAndEpisode(String title, int episode) {
		Cursor cursor = db.rawQuery("select * from unit where TITLE=? and EPISODE=?", new String[] { title, episode + "" });
		if (cursor.moveToNext()) {
			UnitBean unitBean = new UnitBean();
			unitBean.setTitle(cursor.getString(0));
			unitBean.setEpisode(cursor.getInt(1));
			unitBean.setName(cursor.getString(2));
			unitBean.setUrl(cursor.getString(3));
			unitBean.setSegment((cursor.getInt(4)));
			return unitBean;
		}
		return null;
	}

	// ͨ��UnitBeanTitle���ProgramBeanId
	public static String getProgramBeanIdByUnitBeanTitle(String title) {
		Cursor cursor = db.rawQuery("select ID from program where NAME=?", new String[] { title });
		if (cursor.moveToNext()) {
			return cursor.getString(0);
		}
		return null;
	}

	// �����Ȳ�
	@SuppressWarnings("deprecation")
	public static ArrayList<ProgramBean> getProgramBeansToday() {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = db.rawQuery("select * from program where PlayTime=2011", null);
		while (cursor.moveToNext()) {
			ProgramBean programBean = new ProgramBean();
			programBean.setId(cursor.getString(0));
			programBean.setName(cursor.getString(1));
			programBean.setAuthor(cursor.getString(2));
			programBean.setPlayTime(cursor.getInt(3));
			programBean.setNum((cursor.getInt(4)));
			programBean.image = new BitmapDrawable(getImageFromAssetsFile("img" + programBean.getId() + ".jpg"));
			programBeans.add(programBean);
		}
		return programBeans;
	}

	// ����Ȳ�
	@SuppressWarnings("deprecation")
	public static ArrayList<ProgramBean> getProgramBeansHot() {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = db.rawQuery("select * from program where id=20110213 or id=20110217 or id=20110221 or id=20110224 or id=20110410 or id=20110420", null);
		while (cursor.moveToNext()) {
			ProgramBean programBean = new ProgramBean();
			programBean.setId(cursor.getString(0));
			programBean.setName(cursor.getString(1));
			programBean.setAuthor(cursor.getString(2));
			programBean.setPlayTime(cursor.getInt(3));
			programBean.setNum((cursor.getInt(4)));
			programBean.image = new BitmapDrawable(getImageFromAssetsFile("img" + programBean.getId() + ".jpg"));
			programBeans.add(programBean);
		}
		return programBeans;
	}

	// �����Ȳ�
	@SuppressWarnings("deprecation")
	public static ArrayList<ProgramBean> getProgramBeansAgo() {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = db.rawQuery("select * from program where PlayTime=2011", null);
		while (cursor.moveToNext()) {
			ProgramBean programBean = new ProgramBean();
			programBean.setId(cursor.getString(0));
			programBean.setName(cursor.getString(1));
			programBean.setAuthor(cursor.getString(2));
			programBean.setPlayTime(cursor.getInt(3));
			programBean.setNum((cursor.getInt(4)));
			programBean.image = new BitmapDrawable(getImageFromAssetsFile("img" + programBean.getId() + ".jpg"));
			programBeans.add(programBean);
		}
		return programBeans;
	}

	// ����
	public static List<String> getSearchStrings() {
		List<String> searchStrings=new ArrayList<String>();
		Cursor cursor = db.rawQuery("select NAME from program where PlayTime=2011 LIMIT ?", new String[]{KeywordsView.MAX+""});
		while (cursor.moveToNext()){
			searchStrings.add("��"+cursor.getString(0)+"��");
		}
		return searchStrings;
	}

	// ��ȡͼƬ
	public static Bitmap getImageFromAssetsFile(String fileName) {
		Bitmap image = null;
		AssetManager am = mContext.getResources().getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}

	// �����ļ���
	public static boolean createFile() {
		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			// ������Ŀ�����ļ�Ŀ¼
			if (fileCache == null || !fileCache.exists()) {
				fileCache = new File(Environment.getExternalStorageDirectory().toString() + "/lecture");
				fileCache.mkdir();
			}
			// �����ۿ���¼�ı��ļ�
			if (textHistory == null || !textHistory.exists()) {
				textHistory = new File(fileCache + "/history.txt");
				try {
					textHistory.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// ���������ļ�Ŀ¼
			if (fileDownload == null || !fileDownload.exists()) {
				fileDownload = new File(fileCache + "/download");
				fileDownload.mkdir();
			}
			return true;
		}
		return false;
	}

	// д��ۿ���¼�ļ�
	public static void writeHistory(HistoryBean h) {
		List<HistoryBean> historyBeans = readHistory();
		for (int i = 0; i < historyBeans.size(); i++) {
			HistoryBean historyBean = historyBeans.get(i);
			if (historyBean.getId().equals(h.getId()) && historyBean.getEpisode().equals(h.getEpisode())) {
				historyBeans.remove(i);
			}
		}
		if (historyBeans.size() > 30)// ����30����¼��д��
			historyBeans.remove(0);
		historyBeans.add(h);
		writeHistory(historyBeans);
	}

	// д��ۿ���¼�ļ�
	public static void writeHistory(List<HistoryBean> hs) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(textHistory));
			for (int i = 0; i < hs.size(); i++) {
				bw.write(hs.get(i).toString());
				bw.write("\n");
				bw.flush();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ɾ���ۿ��ļ�
	public static void deleteHistory(HistoryBean h) {
		List<HistoryBean> historyBeans = readHistory();
		for (int i = 0; i < historyBeans.size(); i++) {
			HistoryBean historyBean = historyBeans.get(i);
			if (historyBean.getId().equals(h.getId()) && historyBean.getEpisode().equals(h.getEpisode())) {
				historyBeans.remove(i);
			}
		}
		writeHistory(historyBeans);
	}

	// ��ȡ�ۿ���¼�ļ�
	public static List<HistoryBean> readHistory() {
		List<HistoryBean> historyBeans = new ArrayList<HistoryBean>();
		try {
			String[] s = null;
			BufferedReader br = new BufferedReader(new FileReader(textHistory));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				s = line.split(" ");
				HistoryBean historyBean = new HistoryBean();
				historyBean.setId(s[0]);
				historyBean.setTitle(s[1]);
				historyBean.setNum(s[2]);
				historyBean.setAuthor(s[3]);
				historyBean.setEpisode(s[4]);
				historyBean.setName(s[5]);
				historyBean.setTime(s[6]);
				historyBeans.add(historyBean);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return historyBeans;
	}

	// ���������ļ���
	public static File createDownload(UnitBean unitBean) {
		File file = null;
		if (file == null || !file.exists()) {
			file = new File(DbData.fileDownload + "/" + unitBean.getTitle() + " " + unitBean.getEpisode() + " " + unitBean.getName() + " " + unitBean.getSegment() + "/");
			file.mkdir();
		}
		return file;
	}

	// ��ȡ�����ļ���
	public static List<DownloadBean> readDownload() {
		List<DownloadBean> downloadBeans = new ArrayList<DownloadBean>();
		File[] files = DbData.fileDownload.listFiles();
		for (File file : files) {
			int download = file.listFiles().length;
			String[] sDownloadBeans = file.getName().split(" ");
			DownloadBean downloadBean = new DownloadBean();
			downloadBean.setTitle(sDownloadBeans[0]);
			downloadBean.setEpisode(sDownloadBeans[1]);
			downloadBean.setName(sDownloadBeans[2]);
			downloadBean.setDownload(100 * download / Integer.parseInt(sDownloadBeans[3]));
			downloadBeans.add(downloadBean);
		}
		return downloadBeans;
	}

	// ɾ���ļ���
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// Ŀ¼��ʱΪ�գ�����ɾ��
		return dir.delete();
	}
}
