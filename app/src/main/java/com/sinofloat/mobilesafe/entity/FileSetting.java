package com.sinofloat.mobilesafe.entity;

import java.io.File;
import java.io.IOException;

import sinofloat.wvp.tools.FileUtil;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * 文件设置 程序启动时就需要创建好文件夹 或者当时SD异常没能成功创建 那么在需要使用文件的时候再次调用创建文件夹的方法
 * app文件夹根目录（SINOFLOAT）下设VIDEOS PICTURES FTP_FILE三个子目录，程序apk升级 文件夹目录
 * 
 * @author staid
 * 
 */
public class FileSetting {

	private static final String TAG = "FileSetting";
	/**
	 * 设定的可用空间
	 */
	private static final int AVAIABLE_SPACE = 10;

	/**
	 * sharedPreference 配置文件名
	 */
	private static final String FILE_SETTING_NAME = "FILE_SETTING";

	/**
	 * ftp文件同步 用
	 */
	private static final String APP_HOME_DIR_CHINESE = "根目录";

	/**
	 * 程序环境
	 */
	private Context context;

	/**
	 * 定义的本程序文件夹主目录
	 */
	public static final String APP_FILE_HOME_SHORT_DIR = "/SINOFLOAT/";

	/**
	 * ftp目录
	 */
	private String FTP_SHORT_DIR = "FTP/";

	/**
	 * 离线存储视频目录
	 */
	private String VIDEO_SHORT_DIR = "VIDEOS/";

	/**
	 * 离线存储视频目录
	 */
	private String PICTURE_SHORT_DIR = "PICTURES/";

	/**
	 * 拍照图片保存路径（石景山项目需求）
	 */
	private static final String IMG_SHORT_DIR = "IMAGES/";
	
	/**
	 * 移动安防截屏图片目录
	 */
	private String SAFE_SCREENSHOT_DIR = "SAFE_SCREENSHOT/";

	/**
	 * apk检测更新目录（下载的apk文件存储在此文件夹，在程序启动时确保这个文件夹是创建好的
	 * 否则升级apk的activity会报错，同时确保删掉目录下已经有的apk）
	 */
	private static final String APP_UPDATE_SHORT_DIR = "APK_UPDATE/";

	/**
	 * 移动安防摄像头地点分布 图 路径
	 */
	private static final String SAFE_MAP_DIR = "SAFE_MAP/";

	/**
	 * SD卡根路径
	 */
	private static String SDCardRoot;

	/**
	 * 程序文件夹在SD卡上的路径(目录)
	 */
	public String appFileHomeRootDir;

	/**
	 * ftp文件同步在SD卡上的目录路径
	 */
	public String ftpDirFullPath;

	/**
	 * 保存图片的文件夹名称（石景山项目需求）
	 */
	public String imageDirFullPath;

	/**
	 * 保存图片的文件夹名称（图片离线存储）
	 */
	public String picturesDirFullPath;

	/**
	 * 保存视频的文件夹名称（视频离线存储）
	 */
	public String videoDirFullPath;

	/**
	 * 程序升级目录文件夹全路径
	 */
	public String apkUpdateDirFullPath;

	/**
	 * 程序升级目录文件夹相对路径 系统downloadmanager需要
	 */
	public String apkUpdateDirRelativePath;

	/**
	 * 移动安防 摄像头分布图 存储文件路径
	 */
	public String safeMapDataDir;
	
	/**
	 * 移动安防 截屏图片 存储文件路径
	 */
	public String safeScreenshotDir;

	/**
	 * 同步模式 （默认使用主动模式）
	 */
	public boolean ftpSyncMode = true;

	/**
	 * 是否删除所有文件(ftp同步) FIXME
	 */
	public boolean deleteAllFIle = false;

	/**
	 * 是否成功创建出程序目录（ftp文件管理 目录）
	 */
	public boolean isAppFileHomeDirCreated;

	public FileSetting(Context context, boolean isLoad) {
		this.context = context;
		if (isLoad) {
			load();
		}
		creatAppFileHomeDir();
	}

	/**
	 * 加载保存的数据
	 */
	public void load() {
		SharedPreferences sp = context.getSharedPreferences(FILE_SETTING_NAME,
				0);
		isAppFileHomeDirCreated = sp.getBoolean("isAppFileHomeDirCreated",
				isAppFileHomeDirCreated);
		ftpSyncMode = sp.getBoolean("ftpSyncMode", ftpSyncMode);
		deleteAllFIle = sp.getBoolean("deleteAllFIle", deleteAllFIle);
	}

	/**
	 * 保存设置数据。
	 */
	public void save() {
		SharedPreferences.Editor editor = context.getSharedPreferences(
				FILE_SETTING_NAME, 0).edit();
		editor.putBoolean("isAppFileHomeDirCreated", isAppFileHomeDirCreated);
		editor.putBoolean("ftpSyncMode", ftpSyncMode);
		editor.putBoolean("deleteAllFIle", deleteAllFIle);

		editor.commit();
	}

	/**
	 * SD卡是否处于可用状态 如果SD卡不可用那么就不能进入文件管理页面
	 * 
	 * @return
	 */
	public boolean isSDCardReady() {
		boolean isready = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		return isready;
	}

	public boolean getSDAvailableSpace() {
		boolean ishasSpace = false;
		StatFs statFs = new StatFs(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		// sd卡分区数
		int blockCounts = statFs.getBlockCount();
		Log.d(TAG, "blockCounts=" + blockCounts);

		// sd卡可用分区数
		int avCounts = statFs.getAvailableBlocks();
		Log.d(TAG, "avCounts=" + avCounts);

		// 一个分区数的大小
		long blockSize = statFs.getBlockSize();
		Log.d(TAG, "blockSize=" + blockSize);

		// sd卡可用空间
		long spaceLeft = (avCounts * blockSize) / (1024 * 1024);
		Log.d(TAG, "spaceLeft=" + spaceLeft);

		if (spaceLeft >= AVAIABLE_SPACE) {
			ishasSpace = true;
		}
		return ishasSpace;
	}

	/**
	 * 创建程序文件主目录及离线存储（图片 视频）
	 * 
	 * @return
	 */
	public void creatAppFileHomeDir() {

		if (isSDCardReady()) {

			SDCardRoot = Environment.getExternalStorageDirectory()
					.getAbsolutePath();

			appFileHomeRootDir = SDCardRoot + APP_FILE_HOME_SHORT_DIR;

			File file = null;
			try {
				file = FileUtil.createDirs(appFileHomeRootDir);
				if (file != null) {
					isAppFileHomeDirCreated = true;
				}

				file = FileUtil
						.createDirs(appFileHomeRootDir + VIDEO_SHORT_DIR);
				if (file != null) {
					videoDirFullPath = file.getPath() + "/";
				}

				file = FileUtil.createDirs(appFileHomeRootDir
						+ PICTURE_SHORT_DIR);
				if (file != null) {
					picturesDirFullPath = file.getPath() + "/";
				}

				file = FileUtil.createDirs(appFileHomeRootDir + FTP_SHORT_DIR);
				if (file != null) {
					ftpDirFullPath = file.getPath() + "/";
				}

				file = FileUtil.createDirs(appFileHomeRootDir + IMG_SHORT_DIR);
				if (file != null) {
					imageDirFullPath = file.getPath() + "/";
				}

				file = FileUtil.createDirs(appFileHomeRootDir
						+ APP_UPDATE_SHORT_DIR);

				apkUpdateDirRelativePath = APP_FILE_HOME_SHORT_DIR
						+ APP_UPDATE_SHORT_DIR;

				if (file != null) {
					apkUpdateDirFullPath = file.getPath() + "/";
					// 重启程序后删除升级的文件
					FileUtil.deleteFile(apkUpdateDirFullPath);
				}

				file = FileUtil.createDirs(appFileHomeRootDir + SAFE_MAP_DIR);

				if (file != null) {
					safeMapDataDir = file.getPath() + "/";
				}
				
				file = FileUtil.createDirs(appFileHomeRootDir + SAFE_SCREENSHOT_DIR);

				if (file != null) {
					safeScreenshotDir = file.getPath() + "/";
				}

			} catch (IOException e) {
				e.printStackTrace();
				isAppFileHomeDirCreated = false;
			}

			save();
		}

	}

	/**
	 * 获取本程序文件夹路径(SD卡根路径+程序文件夹路径)裁剪一下 ftp文件同步调用
	 * 
	 * @return
	 */
	public String getStrimedFileDir(String fullDir) {
		String str = fullDir.substring(ftpDirFullPath.length(),
				fullDir.length());
		return APP_HOME_DIR_CHINESE + str;
	}
}
