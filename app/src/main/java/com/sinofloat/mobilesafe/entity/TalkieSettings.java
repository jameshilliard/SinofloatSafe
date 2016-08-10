package com.sinofloat.mobilesafe.entity;

import java.util.ArrayList;

import sinofloat.wvp.messages.WvpDataKeyValue;
import sinofloat.wvp.messages.WvpDataText;
import sinofloat.wvp.messages._WvpMediaMessageTypes;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 对讲机设置对�?
 * 
 * @author staid
 * 
 */
public class TalkieSettings {

	/**
	 * 配置项存储名�?
	 */
	public static final String SETTING_CONFIG = "TALKIE_SETTINGS";

	/**
	 * 对讲机配置信�?
	 * 
	 * @param context
	 *            上下�?
	 * @param loadSettings
	 *            是否从存储中加载已保存的配置信息
	 */
	public TalkieSettings(Context context, boolean loadSettings) {
		SettingContext = context;

		// 如果�?��加载配置信息的话，则直接加载
		if (loadSettings)
			Load();
	}

	/**
	 * 上下文对�?
	 */
	public Context SettingContext = null;
	
	/**
	 * 在拍照上传 视频上传 或者遥控模式下 是否始终开启对讲机
	 */
	public boolean isTalkieOnWhenVideoUpload = true;
	
	/**
	 * 是否播放时使用缓冲
	 */
	public boolean isOpenCache = true;

	/**
	 * 是否使用音量键进行对讲
	 */
	public boolean IsUseVolumeButton= true;
	
	/**
	 * 开机后是否自动进入对讲机
	 */
	public boolean IsAutoRun = true;

	/**
	 * 硬按键的KeyCode�?:表示没有指定任何硬按键）
	 */
	public int ControlKeyCode = 0;
	
	
	/**
	 * 当前对讲组的ID
	 */
	public String CurrentGroupID = "";

	/**
	 * 当前对讲组的名称
	 */
	public String CurrentGroupName = "";

	/**
	 * 当前对讲组的验证码
	 */
	public String CurrentGroupVerifyCode = "";

	/**
	 * 用户组信�?
	 */
	public ArrayList<WvpDataKeyValue> GroupList;

	/**
	 * 重新加载系统设置
	 */
	public void Load() {
		SharedPreferences sp = SettingContext.getSharedPreferences(
				SETTING_CONFIG, 0);

		isTalkieOnWhenVideoUpload = sp.getBoolean("isTalkieOnWhenVideoUpload", isTalkieOnWhenVideoUpload);
		
		isOpenCache = sp.getBoolean("isOpenCache", isOpenCache);
		
		IsAutoRun = sp.getBoolean("IsAutoRun", IsAutoRun);
		
		IsUseVolumeButton = sp.getBoolean("IsUseVolumeButton",
				IsUseVolumeButton);
		
		ControlKeyCode = sp.getInt("ControlKeyCode", ControlKeyCode);
		CurrentGroupID = sp.getString("CurrentGroupID", CurrentGroupID);
		CurrentGroupName = sp.getString("CurrentGroupName", CurrentGroupName);
		CurrentGroupVerifyCode = sp.getString("CurrentGroupVerifyCode",
				CurrentGroupVerifyCode);
		
	}

	/**
	 * 保存当前设置
	 */
	public void Save() {
		SharedPreferences sp = SettingContext.getSharedPreferences(
				SETTING_CONFIG, 0);
		SharedPreferences.Editor editor = sp.edit();

		editor.putBoolean("isTalkieOnWhenVideoUpload", isTalkieOnWhenVideoUpload);
		
		editor.putBoolean("isOpenCache", isOpenCache);
		
		editor.putBoolean("IsAutoRun", IsAutoRun);
		
		editor.putBoolean("IsUseVolumeButton", IsUseVolumeButton);
		
		editor.putInt("ControlKeyCode", ControlKeyCode);
		editor.putString("CurrentGroupID", CurrentGroupID);
		editor.putString("CurrentGroupName", CurrentGroupName);
		editor.putString("CurrentGroupVerifyCode", CurrentGroupVerifyCode);

		editor.commit();
	}

}
