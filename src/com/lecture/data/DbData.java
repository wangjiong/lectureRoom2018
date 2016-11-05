package com.lecture.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.tsz.afinal.FinalDb;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.GetChars;
import android.widget.Toast;

import com.lecture.item.view.KeywordsView;
import com.lecture.media.R;

public class DbData {
	public static FinalDb sFinalDb;
	public static SQLiteDatabase sDb;// ��MainActivity�г�ʼ��;
	public static Context mContext;
	public static volatile boolean sPrepare;

	public static File fileCache, fileMovieCache, fileDownload, fileImage;
	// �������
	public static List<DownloadBean> sDownloadBeans = new ArrayList<DownloadBean>();// �Ѿ�������ɵ��ļ�
	public static List<DownloadBean> sDownloadingBeans = new ArrayList<DownloadBean>();// δ��ɵ������ļ�

	public DbData(Context context) {
		// ��ʼ����Ŀ���ݿ�
		mContext = context;
		DataBaseHelper myDbHelper = new DataBaseHelper(mContext);
		DbData.sFinalDb = FinalDb.create(mContext, true);
		try {
			myDbHelper.createDataBase();
			myDbHelper.openDataBase();
			DbData.sDb = myDbHelper.db;
		} catch (IOException e) {
			e.printStackTrace();
		}
		sPrepare = true;
		// �����ļ�
		if (!createFile()) {
			Toast.makeText(context, "��Ǹ����SD����", Toast.LENGTH_SHORT).show();
		}
		// �������
		List<DownloadBean> downloadBeans = DbData.sFinalDb.findAll(DownloadBean.class);
		if (downloadBeans == null || downloadBeans.size() == 0) {// download
			File[] allDownFiles = DbData.fileDownload.listFiles();// allDownFiles�е�Ϊ��������Ʒ����
																	// 50
																	// ������
																	// 8����������Ⱥ��ʷ��
																	// 32
																	// ���Ž���
																	// 8��
			if (allDownFiles == null || allDownFiles.length == 0) {
			} else {
				// ������ݿ���û�������ļ����ļ������У���ô���ļ����е��ļ��������ݿ�
				for (int i = 0; i < allDownFiles.length; i++) {
					File[] movies = allDownFiles[i].listFiles();// ÿһ�����ļ���,movies��Ϊ"movie01.mp4"��"movie02.mp4","success","state 95 8"
					if (movies != null && movies.length > 0) {
						for (int j = movies.length - 1; j >= 0; j--) {
							// �������
							if (movies[j].getName().contains("success")) {
								String[] s1 = allDownFiles[i].getName().split(" ");// ex:����Ⱥ��ʷ��32���Ž���8
								DbData.sFinalDb.save(new DownloadBean(s1[0], s1[1], s1[2], Integer.parseInt(s1[3]), Integer.parseInt(s1[3]), 100, true));// segmentΪ1
								break;
								// ����δ���
							} else if (movies[j].getName().contains("state")) {
								String[] s1 = allDownFiles[i].getName().split(" ");// ex:����Ⱥ��ʷ��32���Ž���8
								String[] s2 = movies[j].getName().split(" "); // ex:state958
								DbData.sFinalDb.save(new DownloadBean(s1[0], s1[1], s1[2], Integer.parseInt(s1[3]), Integer.parseInt(s2[2]), Integer.parseInt(s2[1]), false));// segmentΪ1
								break;
							}
						}
					}
				}
				// �൱�ڳ�ʼ��һ�����ݿ�
				downloadBeans = DbData.sFinalDb.findAll(DownloadBean.class);
			}
		}
		sDownloadBeans.clear();// ��ֹsavedInstanceState��������
		sDownloadingBeans.clear();
		Collections.reverse(downloadBeans);
		for (DownloadBean d : downloadBeans) {
			if (d.isFinish()) {
				// �������
				sDownloadBeans.add(d);
			} else {
				// ��������
				sDownloadingBeans.add(d);
			}
		}
	}

	private static ProgramBean getProgramBeanByCursor(Cursor cursor) {
		ProgramBean programBean = new ProgramBean();
		programBean.setId(cursor.getString(0));
		programBean.setName(cursor.getString(1));
		programBean.setDynasty(cursor.getInt(2));
		programBean.setAuthor(cursor.getString(3));
		programBean.setPlayTime(cursor.getInt(4));
		programBean.setNum((cursor.getInt(5)));
		return programBean;
	}

	private static UnitBean getUnitBeanByCursor(Cursor cursor) {
		UnitBean unitBean = new UnitBean();
		unitBean.setTitle(cursor.getString(0));
		unitBean.setEpisode(cursor.getInt(1));
		unitBean.setName(cursor.getString(2));
		unitBean.setUrl(cursor.getString(3));
		unitBean.setSegment((cursor.getInt(4)));
		return unitBean;
	}

	// ��ȡProgramBean���һ������
	public static ProgramBean getProgramBeanLast() {
		Cursor cursor = sDb.rawQuery("select * from program ORDER BY id DESC limit 1", null);
		if (cursor.moveToNext()) {
			return getProgramBeanByCursor(cursor);
		}
		return null;
	}

	// ��ȡUnitBean���һ������
	public static UnitBean getUnitBeanLast() {
		Cursor cursor = sDb.rawQuery("select * from unit ORDER BY Title DESC limit 1", null);
		if (cursor.moveToNext()) {
			return getUnitBeanByCursor(cursor);
		}
		return null;
	}

	// ͨ��ID���ProgramBean
	public static ProgramBean getProgramBeanById(String id) {
		Cursor cursor = sDb.rawQuery("select * from program where Id=" + id, null);
		if (cursor.moveToNext()) {
			return getProgramBeanByCursor(cursor);
		}
		return null;
	}

	// ͨ��Title���ProgramBean
	public static ProgramBean getProgramBeanByTitle(String title) {
		Cursor cursor = sDb.rawQuery("select * from program where Name=?", new String[] { title });
		if (cursor.moveToNext()) {
			return getProgramBeanByCursor(cursor);
		}
		return null;
	}

	// ͨ��TITLE�ͼ������UnitBean
	public static UnitBean getUnitBeanByTitleAndEpisode(String title, int episode) {
		Cursor cursor = sDb.rawQuery("select * from unit where TITLE=? and EPISODE=?", new String[] { title, episode + "" });
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
		Cursor cursor = sDb.rawQuery("select ID from program where NAME= ?", new String[] { title });
		if (cursor.moveToNext()) {
			return cursor.getString(0);
		}
		return null;
	}

	// �Ƽ�
	public static ArrayList<ProgramBean> getProgramBeansRecommend(int type) {
		String IdString = null;
		// IdString=recommendId[type];
		switch (type) {
		case 0:
			IdString = "Id=20111115 or Id=20070106 or Id=20140620 or Id=20120824";
			break;
		case 1:
			IdString = "Id=20130127 or Id=20140124 or Id=20150212 or Id=20160208";
			break;
		case 2:
			IdString = "Id=20081229 or Id=20090325 or Id=20090622 or Id=20090628 or Id=20100420";
			break;
		case 3:
			IdString = "Id=20110614 or Id=20101001 or Id=20110628";
			break;
		}
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = sDb.rawQuery("select * from program where " + IdString, null);
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		// �ػʺ�����������
		if (type == 0) {
			ArrayList<ProgramBean> programBeansTemp = new ArrayList<ProgramBean>();
			programBeansTemp.add(programBeans.get(1));
			programBeansTemp.add(programBeans.get(0));
			programBeansTemp.add(programBeans.get(3));
			programBeansTemp.add(programBeans.get(2));
			return programBeansTemp;
		}
		if (type == 3) {
			ArrayList<ProgramBean> programBeansTemp = new ArrayList<ProgramBean>();
			programBeansTemp.add(programBeans.get(1));
			programBeansTemp.add(programBeans.get(0));
			programBeansTemp.add(programBeans.get(2));
			return programBeansTemp;
		}
		return programBeans;
	}

	// ����upDateProgramBean
	public static void upDateProgramBean(ProgramBean programBeans) {
		sDb.execSQL("update program set num = ? where id = ?", new Object[] { programBeans.getNum(), programBeans.getId() });
	}

	// ����ProgramBean
	public static void insertProgramBean(List<Data.ProgramBean> programBeans) {
		for (int i = 0; i < programBeans.size(); i++) {
			sDb.execSQL("insert into program values(?,?,?,?,?,?)", new Object[] { programBeans.get(i).getId(), programBeans.get(i).getName(), programBeans.get(i).getDynasty(), programBeans.get(i).getAuthor(), programBeans.get(i).getPlayTime(), programBeans.get(i).getNum() });
		}

	}

	// ����ProgramBean
	public static void insertUnitBean(List<UnitBean> unitBeans) {
		for (int i = 0; i < unitBeans.size(); i++) {
			sDb.execSQL("insert into unit values(?,?,?,?,?)", new Object[] { unitBeans.get(i).getTitle(), unitBeans.get(i).getEpisode(), unitBeans.get(i).getName(), unitBeans.get(i).getUrl(), unitBeans.get(i).getSegment() });
		}
	}

	// �����Ȳ�
	public static ArrayList<ProgramBean> getProgramBeansToday() {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = sDb.rawQuery("select * from program ORDER BY id DESC limit 6", null);
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		Collections.reverse(programBeans);
		return programBeans;
	}

	// �����Ȳ�
	public static ArrayList<ProgramBean> getProgramBeansHot() {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = sDb.rawQuery("select * from program where id=20111115 or id=20101023 or id=20060807 or id=20140417 or id=20120506 or id=20100730", null);
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		ArrayList<ProgramBean> programBeansTemp = new ArrayList<ProgramBean>();
		programBeansTemp.add(programBeans.get(3));
		programBeansTemp.add(programBeans.get(2));
		programBeansTemp.add(programBeans.get(0));
		programBeansTemp.add(programBeans.get(5));
		programBeansTemp.add(programBeans.get(4));
		programBeansTemp.add(programBeans.get(1));
		return programBeansTemp;
	}

	// �����Ȳ�
	static int playTime = (2006 + (int) (Math.random() * 9));

	public static ArrayList<ProgramBean> getProgramBeansAgo() {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = sDb.rawQuery("select * from program where PlayTime = " + playTime, null);
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		return programBeans;
	}

	// ����
	public static ArrayList<ProgramBean> getProgramBeansMore(int type) {
		int moreType = 2010;
		switch (type) {
		case 0:
			moreType = 2015;
			break;
		default:
			moreType = playTime;
		}
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = sDb.rawQuery("select * from program where PlayTime = ? ", new String[] { moreType + "" });
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		return programBeans;
	}

	// ����
	public static ArrayList<ProgramBean> getProgramBeansClassify(int classifyType) {
		// classifyType -= 1;// �����ݿ�������һ��
		// if (classifyType == -1) {
		// classifyType = 14;
		// } else if (classifyType == 0) {
		// classifyType = 15;
		// }
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = null;
		if (classifyType == 14) {
			cursor = sDb.rawQuery("select * from program where Id=20060807 or Id = 20111115 or Id=20060101 or Id=20061001 or Id=20110514 or Id=20131226 or Id=20150315 or Id=20091109 or Id=20090713 or Id=20081229 or Id=20130127 or Id=20130328", null);
		} else {
			cursor = sDb.rawQuery("select * from program where Dynasty = ? ", new String[] { classifyType + "" });
		}
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		return programBeans;
	}

	// ��������
	// ����
	public static ArrayList<ProgramBean> getProgramBeanIdByTitle(String title) {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = sDb.rawQuery("select * from program where NAME like '%" + title + "%'", null);
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		return programBeans;
	}

	// ����
	public static ArrayList<ProgramBean> getProgramBeansByAuthor(String author) {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = sDb.rawQuery("select * from program where Author like '%" + author + "%'", null);
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		return programBeans;
	}

	// ʱ��
	public static ArrayList<ProgramBean> getProgramBeansByTime(String time) {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = sDb.rawQuery("select * from program where PlayTime = ?", new String[] { time });
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		return programBeans;
	}

	// ��������Ƿ����
	public static boolean searchByTitle(String title) {
		Cursor cursor = sDb.rawQuery("select * from program where NAME like '%" + title + "%'", null);
		while (cursor.moveToNext()) {
			return true;
		}
		return false;
	}

	// ��������Ƿ����
	public static boolean searchByAuthor(String author) {
		Cursor cursor = sDb.rawQuery("select * from program where Author like '%" + author + "%'", null);
		while (cursor.moveToNext()) {
			return true;
		}
		return false;
	}

	// ���ʱ���Ƿ����
	public static boolean searchByTime(String time) {
		Cursor cursor = sDb.rawQuery("select * from program where PlayTime = ?", new String[] { time });
		while (cursor.moveToNext()) {
			return true;
		}
		return false;
	}

	// �������
	public static String[] getSearchStrings() {
		List<String> searchStrings = new ArrayList<String>();
		Cursor cursor = sDb.rawQuery("select NAME from program where PlayTime= ?", new String[] { (2006 + (int) (Math.random() * 10)) + " " });
		while (cursor.moveToNext()) {
			searchStrings.add("��" + cursor.getString(0) + "��");
		}
		String[] searchStringsTemp = new String[KeywordsView.MAX];
		for (int i = 0; i < searchStringsTemp.length; i++) {
			int k = (int) (searchStrings.size() * Math.random());
			searchStringsTemp[i] = searchStrings.remove(k);
		}
		return searchStringsTemp;
	}

	// ��ȡDrawable
	@SuppressWarnings("deprecation")
	public static Drawable getDrawableById(String id) {
		Bitmap image = DbData.getImageFromAssetsFile("img" + id + ".jpg");
		return new BitmapDrawable(image);
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
		if (image == null) {
			image = DbData.getImageFromCatchFile(fileName);
		}
		if (image == null) {
			return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
		}
		return image;
	}

	// ��ȡͼƬ
	public static Bitmap getImageFromCatchFile(String img) {
		Bitmap image = null;
		try {
			FileInputStream fis = new FileInputStream(DbData.fileImage + "/" + img);
			image = BitmapFactory.decodeStream(fis);
			fis.close();
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
			if (fileMovieCache == null || !fileMovieCache.exists()) {
				fileMovieCache = new File(fileCache + "/cache");
				fileMovieCache.mkdir();
			}
			// ���������ļ�Ŀ¼
			if (fileDownload == null || !fileDownload.exists()) {
				fileDownload = new File(fileCache + "/download");
				fileDownload.mkdir();
			}
			// ���������ļ�Ŀ¼
			if (fileImage == null || !fileImage.exists()) {
				fileImage = new File(fileCache + "/image");
				fileImage.mkdir();
			}
			return true;
		}
		return false;
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

	// ������������ļ���־�ļ�
	public static void createDownloadSuccess(final File file) {
		try {
			new File(file + "/success").createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// download��ʾ���ȣ�segment��ʾ�����˼���
	public static void createDownloadState(final File file, final int download, final int segment) {
		new Thread() {
			public void run() {
				try {
					for (File f : file.listFiles()) {
						if (f.toString().contains("state")) {
							f.delete();
						}
					}
					new File(file + "/state " + download + " " + segment).createNewFile();
				} catch (IOException e) {
				}
			}
		}.start();
	}

	// ����������ɼ�¼�ļ�

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

	// �����Ƿ����
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	// �Ƽ�����ͼƬ
	public static String[] recommendUrl = new String[] { "http://192.168.68.149:8080/lecture/r01.jpg", "http://192.168.68.149:8080/lecture/r02.jpg", "http://192.168.68.149:8080/lecture/r03.jpg", "http://192.168.68.149:8080/lecture/r04.jpg", };
	// ��������
	public static String responseString;
	// �Ƽ�����Id
	public static String[] recommendId;

	// �����Ƽ�����Id
	public static void getNetData() {
		FinalHttp http = new FinalHttp();
		http.get("http://192.168.68.149:8080/lecture/getNetData.jsp", new AjaxCallBack<String>() {
			// ����������ʧ�ܵ�ʱ��ᱻ���ã�errorNo������ʧ��֮�󣬷������Ĵ�����,StrMsg���Ǵ�����Ϣ
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				System.out.println(strMsg);
			}

			// �������ɹ������������ص�������t���Ƿ��������ص��ַ�����Ϣ
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				responseString = t;
				System.out.println(responseString);
				recommendId = responseString.split("\\|");// |��Ҫת�壬������Ч
			}
		});
	}
}
