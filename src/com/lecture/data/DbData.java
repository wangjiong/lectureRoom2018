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
	public static SQLiteDatabase sDb;// 在MainActivity中初始化;
	public static Context mContext;
	public static volatile boolean sPrepare;

	public static File fileCache, fileMovieCache, fileDownload, fileImage;
	// 下载相关
	public static List<DownloadBean> sDownloadBeans = new ArrayList<DownloadBean>();// 已经下载完成的文件
	public static List<DownloadBean> sDownloadingBeans = new ArrayList<DownloadBean>();// 未完成的下载文件

	public DbData(Context context) {
		// 初始化节目数据库
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
		// 创建文件
		if (!createFile()) {
			Toast.makeText(context, "抱歉，无SD卡！", Toast.LENGTH_SHORT).show();
		}
		// 下载相关
		List<DownloadBean> downloadBeans = DbData.sFinalDb.findAll(DownloadBean.class);
		if (downloadBeans == null || downloadBeans.size() == 0) {// download
			File[] allDownFiles = DbData.fileDownload.listFiles();// allDownFiles中的为“易中天品三国
																	// 50
																	// 易中天
																	// 8”、“王立群读史记
																	// 32
																	// 宠信讲充
																	// 8”
			if (allDownFiles == null || allDownFiles.length == 0) {
			} else {
				// 如果数据库中没有下载文件而文件夹中有，那么把文件夹中的文件导入数据库
				for (int i = 0; i < allDownFiles.length; i++) {
					File[] movies = allDownFiles[i].listFiles();// 每一集的文件夹,movies中为"movie01.mp4"，"movie02.mp4","success","state 95 8"
					if (movies != null && movies.length > 0) {
						for (int j = movies.length - 1; j >= 0; j--) {
							// 下载完成
							if (movies[j].getName().contains("success")) {
								String[] s1 = allDownFiles[i].getName().split(" ");// ex:王立群读史记32宠信讲充8
								DbData.sFinalDb.save(new DownloadBean(s1[0], s1[1], s1[2], Integer.parseInt(s1[3]), Integer.parseInt(s1[3]), 100, true));// segment为1
								break;
								// 下载未完成
							} else if (movies[j].getName().contains("state")) {
								String[] s1 = allDownFiles[i].getName().split(" ");// ex:王立群读史记32宠信讲充8
								String[] s2 = movies[j].getName().split(" "); // ex:state958
								DbData.sFinalDb.save(new DownloadBean(s1[0], s1[1], s1[2], Integer.parseInt(s1[3]), Integer.parseInt(s2[2]), Integer.parseInt(s2[1]), false));// segment为1
								break;
							}
						}
					}
				}
				// 相当于初始化一下数据库
				downloadBeans = DbData.sFinalDb.findAll(DownloadBean.class);
			}
		}
		sDownloadBeans.clear();// 防止savedInstanceState保留数据
		sDownloadingBeans.clear();
		Collections.reverse(downloadBeans);
		for (DownloadBean d : downloadBeans) {
			if (d.isFinish()) {
				// 下载完成
				sDownloadBeans.add(d);
			} else {
				// 正在下载
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

	// 获取ProgramBean最后一条数据
	public static ProgramBean getProgramBeanLast() {
		Cursor cursor = sDb.rawQuery("select * from program ORDER BY id DESC limit 1", null);
		if (cursor.moveToNext()) {
			return getProgramBeanByCursor(cursor);
		}
		return null;
	}

	// 获取UnitBean最后一条数据
	public static UnitBean getUnitBeanLast() {
		Cursor cursor = sDb.rawQuery("select * from unit ORDER BY Title DESC limit 1", null);
		if (cursor.moveToNext()) {
			return getUnitBeanByCursor(cursor);
		}
		return null;
	}

	// 通过ID获得ProgramBean
	public static ProgramBean getProgramBeanById(String id) {
		Cursor cursor = sDb.rawQuery("select * from program where Id=" + id, null);
		if (cursor.moveToNext()) {
			return getProgramBeanByCursor(cursor);
		}
		return null;
	}

	// 通过Title获得ProgramBean
	public static ProgramBean getProgramBeanByTitle(String title) {
		Cursor cursor = sDb.rawQuery("select * from program where Name=?", new String[] { title });
		if (cursor.moveToNext()) {
			return getProgramBeanByCursor(cursor);
		}
		return null;
	}

	// 通过TITLE和集数获得UnitBean
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

	// 通过UnitBeanTitle获得ProgramBeanId
	public static String getProgramBeanIdByUnitBeanTitle(String title) {
		Cursor cursor = sDb.rawQuery("select ID from program where NAME= ?", new String[] { title });
		if (cursor.moveToNext()) {
			return cursor.getString(0);
		}
		return null;
	}

	// 推荐
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
		// 秦皇汉武唐宗宋祖
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

	// 更新upDateProgramBean
	public static void upDateProgramBean(ProgramBean programBeans) {
		sDb.execSQL("update program set num = ? where id = ?", new Object[] { programBeans.getNum(), programBeans.getId() });
	}

	// 插入ProgramBean
	public static void insertProgramBean(List<Data.ProgramBean> programBeans) {
		for (int i = 0; i < programBeans.size(); i++) {
			sDb.execSQL("insert into program values(?,?,?,?,?,?)", new Object[] { programBeans.get(i).getId(), programBeans.get(i).getName(), programBeans.get(i).getDynasty(), programBeans.get(i).getAuthor(), programBeans.get(i).getPlayTime(), programBeans.get(i).getNum() });
		}

	}

	// 插入ProgramBean
	public static void insertUnitBean(List<UnitBean> unitBeans) {
		for (int i = 0; i < unitBeans.size(); i++) {
			sDb.execSQL("insert into unit values(?,?,?,?,?)", new Object[] { unitBeans.get(i).getTitle(), unitBeans.get(i).getEpisode(), unitBeans.get(i).getName(), unitBeans.get(i).getUrl(), unitBeans.get(i).getSegment() });
		}
	}

	// 今日热播
	public static ArrayList<ProgramBean> getProgramBeansToday() {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = sDb.rawQuery("select * from program ORDER BY id DESC limit 6", null);
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		Collections.reverse(programBeans);
		return programBeans;
	}

	// 经典热播
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

	// 以往热播
	static int playTime = (2006 + (int) (Math.random() * 9));

	public static ArrayList<ProgramBean> getProgramBeansAgo() {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = sDb.rawQuery("select * from program where PlayTime = " + playTime, null);
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		return programBeans;
	}

	// 更多
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

	// 分类
	public static ArrayList<ProgramBean> getProgramBeansClassify(int classifyType) {
		// classifyType -= 1;// 与数据库数据相一致
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

	// 定向搜索
	// 名称
	public static ArrayList<ProgramBean> getProgramBeanIdByTitle(String title) {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = sDb.rawQuery("select * from program where NAME like '%" + title + "%'", null);
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		return programBeans;
	}

	// 作者
	public static ArrayList<ProgramBean> getProgramBeansByAuthor(String author) {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = sDb.rawQuery("select * from program where Author like '%" + author + "%'", null);
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		return programBeans;
	}

	// 时间
	public static ArrayList<ProgramBean> getProgramBeansByTime(String time) {
		ArrayList<ProgramBean> programBeans = new ArrayList<ProgramBean>();
		Cursor cursor = sDb.rawQuery("select * from program where PlayTime = ?", new String[] { time });
		while (cursor.moveToNext()) {
			programBeans.add(getProgramBeanByCursor(cursor));
		}
		return programBeans;
	}

	// 检查名称是否存在
	public static boolean searchByTitle(String title) {
		Cursor cursor = sDb.rawQuery("select * from program where NAME like '%" + title + "%'", null);
		while (cursor.moveToNext()) {
			return true;
		}
		return false;
	}

	// 检查作者是否存在
	public static boolean searchByAuthor(String author) {
		Cursor cursor = sDb.rawQuery("select * from program where Author like '%" + author + "%'", null);
		while (cursor.moveToNext()) {
			return true;
		}
		return false;
	}

	// 检查时间是否存在
	public static boolean searchByTime(String time) {
		Cursor cursor = sDb.rawQuery("select * from program where PlayTime = ?", new String[] { time });
		while (cursor.moveToNext()) {
			return true;
		}
		return false;
	}

	// 随机搜索
	public static String[] getSearchStrings() {
		List<String> searchStrings = new ArrayList<String>();
		Cursor cursor = sDb.rawQuery("select NAME from program where PlayTime= ?", new String[] { (2006 + (int) (Math.random() * 10)) + " " });
		while (cursor.moveToNext()) {
			searchStrings.add("《" + cursor.getString(0) + "》");
		}
		String[] searchStringsTemp = new String[KeywordsView.MAX];
		for (int i = 0; i < searchStringsTemp.length; i++) {
			int k = (int) (searchStrings.size() * Math.random());
			searchStringsTemp[i] = searchStrings.remove(k);
		}
		return searchStringsTemp;
	}

	// 获取Drawable
	@SuppressWarnings("deprecation")
	public static Drawable getDrawableById(String id) {
		Bitmap image = DbData.getImageFromAssetsFile("img" + id + ".jpg");
		return new BitmapDrawable(image);
	}

	// 读取图片
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

	// 读取图片
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

	// 创建文件夹
	public static boolean createFile() {
		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			// 创建节目缓冲文件目录
			if (fileCache == null || !fileCache.exists()) {
				fileCache = new File(Environment.getExternalStorageDirectory().toString() + "/lecture");
				fileCache.mkdir();
			}
			// 创建观看记录文本文件
			if (fileMovieCache == null || !fileMovieCache.exists()) {
				fileMovieCache = new File(fileCache + "/cache");
				fileMovieCache.mkdir();
			}
			// 创建下载文件目录
			if (fileDownload == null || !fileDownload.exists()) {
				fileDownload = new File(fileCache + "/download");
				fileDownload.mkdir();
			}
			// 创建下载文件目录
			if (fileImage == null || !fileImage.exists()) {
				fileImage = new File(fileCache + "/image");
				fileImage.mkdir();
			}
			return true;
		}
		return false;
	}

	// 创建下载文件夹
	public static File createDownload(UnitBean unitBean) {
		File file = null;
		if (file == null || !file.exists()) {
			file = new File(DbData.fileDownload + "/" + unitBean.getTitle() + " " + unitBean.getEpisode() + " " + unitBean.getName() + " " + unitBean.getSegment() + "/");
			file.mkdir();
		}
		return file;
	}

	// 创建下载完成文件标志文件
	public static void createDownloadSuccess(final File file) {
		try {
			new File(file + "/success").createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// download表示进度，segment表示下载了几段
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

	// 创建下载完成记录文件

	// 删除文件夹
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
		// 目录此时为空，可以删除
		return dir.delete();
	}

	// 网络是否可用
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

	// 推荐数据图片
	public static String[] recommendUrl = new String[] { "http://192.168.68.149:8080/lecture/r01.jpg", "http://192.168.68.149:8080/lecture/r02.jpg", "http://192.168.68.149:8080/lecture/r03.jpg", "http://192.168.68.149:8080/lecture/r04.jpg", };
	// 请求数据
	public static String responseString;
	// 推荐数据Id
	public static String[] recommendId;

	// 请求推荐数据Id
	public static void getNetData() {
		FinalHttp http = new FinalHttp();
		http.get("http://192.168.68.149:8080/lecture/getNetData.jsp", new AjaxCallBack<String>() {
			// 当我们请求失败的时候会被调用，errorNo是请求失败之后，服务器的错误码,StrMsg则是错误信息
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				System.out.println(strMsg);
			}

			// 如果请求成功，则调用这个回调函数，t就是服务器返回的字符串信息
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				responseString = t;
				System.out.println(responseString);
				recommendId = responseString.split("\\|");// |需要转义，否则无效
			}
		});
	}
}
