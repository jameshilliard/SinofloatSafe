package com.sinofloat.mobilesafe.entity;

import android.content.Context;
import android.content.SharedPreferences;

public class LocationSetting {
	
	private static final String FILE_NAME = "LocationSetting";
	/**
	 * 是否发送位置信息 （默认是打开）
	 */
	public boolean isEnableLocationSend = true;

	/**
	 * 定位时间间隔在数组中的下标位置
	 */
	public int timeIntervalPosition = 2;

	/**
	 * 预设的定位间隔具体时间 单位：秒
	 */
	public int[] locationTimeIntervalArray = { 1 * 1000, 3 * 1000, 5 * 1000,
			10 * 1000, 30 * 1000, 60 * 1000, 180 * 1000, 300 * 1000,
			600 * 1000, 1800 * 1000, 3600 * 1000 };

	/**
	 * 用户设置的定位间隔具体�? 单位：毫秒， 默认�?秒�?
	 */
	public int locationTimeInterval = locationTimeIntervalArray[timeIntervalPosition];

	
	/**
	 * 默认使用高德的定位信息
	 */
	public static final int LOCATION_USAGE_GAODE = 0;
	
	/**
	 * 系统的定位信息
	 */
	public static final int LOCATION_USAGE_SYSTEM = 1;
	
	/**
	 * 当前使用的定位信息 在数组中的位置。
	 */
	public int curLocationUsagePosition = 0;
	
	
	
	
	/**
	 * 构�?方法 加载数据
	 * 
	 * @param context
	 */
	public LocationSetting(Context context, boolean isLoad) {
		if (isLoad) {
			load(context);
		}
	}

	/**
	 * 加载读取数据
	 * 
	 * @param context
	 */
	public void load(Context context) {

		SharedPreferences sp = context.getSharedPreferences(
				FILE_NAME, 0);
		timeIntervalPosition = sp.getInt("timeIntervalPosition",
				timeIntervalPosition);
		locationTimeInterval = sp.getInt("locationTimeInterval",
				locationTimeInterval);
		isEnableLocationSend = sp.getBoolean("isEnableLocationSend",
				isEnableLocationSend);
		curLocationUsagePosition = sp.getInt("curLocationUsagePosition",
				curLocationUsagePosition);
	}

	/**
	 * 保存数据
	 * 
	 * @param context
	 */
	public void save(Context context) {
		SharedPreferences sp = context.getSharedPreferences(
				FILE_NAME, 0);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt("timeIntervalPosition", timeIntervalPosition);
		editor.putInt("locationTimeInterval",locationTimeInterval);
		editor.putInt("curLocationUsagePosition",curLocationUsagePosition);
		
		editor.putBoolean("isEnableLocationSend", isEnableLocationSend);
		editor.commit();
	}
	
	
	/**
	 * 设置定位时间间隔
	 * @param timeIntervalPosition
	 */
	public void setLocationTimeIntervalPosition(int timeIntervalPosition){
		this.timeIntervalPosition = timeIntervalPosition;
		locationTimeInterval = locationTimeIntervalArray[timeIntervalPosition];
	}
}
