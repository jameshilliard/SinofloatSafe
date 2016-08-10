package com.sinofloat.mobilesafe.entity;

import android.content.Context;
import android.content.SharedPreferences;

public class NotifySetting {

	private static final String NOTIFY_SETTING = "notify";
	private Context context;
	
	/**
	 * 是否使用震动提示
	 */
	public boolean IsVibrate = false;
	
	/**
	 * 对讲机连接提示音量
	 */
	public int alertVolume = 5;
	
	/**
	 * 提醒模式 (震动，铃音，震动)默认: 震动+铃音
	 */
	public int notifyModePosition = 0;
	
	/**
	 * 对讲机连接提示音量最低1 
	 */
	public static int SEEKBAR_LOW = 1;
	
	/**
	 * 对讲机连接提示音量最高�?
	 */
	public static int SEEKBAR_MAX = 9;

	
	
	public NotifySetting(Context context,boolean isLoad){
		this.context = context;
		if(isLoad){
			load();
		}
	}
	
	
	public void load(){
		
		SharedPreferences sp = context.getSharedPreferences(NOTIFY_SETTING, 0);
		IsVibrate = sp.getBoolean("IsVibrate", IsVibrate);
		alertVolume = sp.getInt("alertVolume",
				alertVolume);
		notifyModePosition = sp.getInt("notifyModePosition",
				notifyModePosition);
	}
	
	public void save(){
		
		SharedPreferences.Editor editor = context.getSharedPreferences(NOTIFY_SETTING, 0).edit();
		editor.putBoolean("IsVibrate", IsVibrate);
		editor.putInt("alertVolume", alertVolume);
		editor.putInt("notifyModePosition", notifyModePosition);
		editor.commit();
	}
}
