package com.sinofloat.mobilesafe.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
/**
 * Created by oyk on 2016/7/15.
 * 屏幕截图工具
 */
public class ScreenShot {
	/**
	 * 截取当前整个屏幕
	 * @param activity
	 * @return bitmap
     */
	public static Bitmap takeScreenShot(Activity activity) {
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();

		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;
		System.out.println(statusBarHeight);
		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		int height = activity.getWindowManager().getDefaultDisplay().getHeight();
		Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
		view.destroyDrawingCache();
		return b;

	}

	/**
	 * 截取指定区域图片
	 * @param activity
	 * @param x
	 * @param y
	 * @param width
	 * @param height
     * @return bitmap
     */
	public static Bitmap takeScreenShotClip(Activity activity,int x,int y,int width,int height) {
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();

		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;
		System.out.println(statusBarHeight);
//		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
//		int height = activity.getWindowManager().getDefaultDisplay().getHeight();
		 Bitmap b = Bitmap.createBitmap(b1, x, y, width, height);
		view.destroyDrawingCache();
		return b;

	}

	/**
	 * 保存图片
	 * @param b
	 * @param strFileName
     * @return
     */
	public static boolean savePic(Bitmap b, String strFileName) {
		boolean sucess = false;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(strFileName);
			if (null != fos)
			{
				b.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.flush();
				fos.close();
				sucess = true;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		}
		return sucess;
	}
}
