package com.sinofloat.mobilesafe.entity;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 连接设置信息
 * @author staid
 *
 */
public class ConnectionSettings {
	
	
	/**
	 * 登陆管理中心的用户组名
	 */
	public String UserGroupName = "";
	public String UserGroupID ;
	
	/**
	 * 配置项存储名�?
	 */
	public static final String SETTING_CONFIG = "CONNECTION_SETTINGS";
	
	/**
	 * 连接信息配置对象
	 * @param context	上下�?
	 * @param loadSettings	是否从存储中加载已保存的配置信息
	 */
	public ConnectionSettings(Context context, boolean loadSettings)
	{
		SettingContext = context;
		
		//如果�?��加载配置信息的话，则直接加载
		if (loadSettings)
			Load();
	}
	
	/**
	 * 上下文对�?
	 */
	public Context SettingContext = null;
	
	/**
	 * 是否记住密码
	 */
	public boolean isRememberPass;
	
	/**
	 * 服务器的地址
	 */
	public String ServiceAddress = "";
	
	/**
	 * 移动安防服务端口
	 */
	public int ServicePort = 4523;
	
	/**
	 * 地理位置报告端口
	 */
	public int LocationReportPort = 4527;
	
	/**
	 * 服务�?地理位置发�?端口
	 */
	public int LocationServicePort = 4525;
	
	/**
	 * 在线发送数据端口（音视频 拍照上传）
	 */
	public int OnLIneUploadServicePort = 4520;
	
	/**
	 * 发送状态端口
	 */
	public int StateServicePort = 4521;
	
	/**
	 * 移动安防端口
	 */
	public int SafeServicePort = 4523;
	
	/**
	 * 离线文件发送端口（离线存储的音视频图片文件）
	 */
	public int OffLineUploadPort = 4524;
	
	/**
	 * 端口 4528 ，负责获取最新升级包的信息
	 */
	public int AppUpdatePort = 4528;
	
	
	/**
	 * 当前登录账号的ID
	 */
	public String CurrentUserID = "";
	
	/**
	 * 当前登录账号的登录名
	 */
	public String CurrentUserLoginName = "";
	
	/**
	 * 当前登录账号的显示名�?
	 */
	public String CurrentUserDisplayName = "";
	
	/**
	 * 当前登录账号对应的密码（MD5加密后）
	 */
	public String CurrentUserLoginPass = "";
	
	/**
	 * 当前登录账号对应的密码（MD5加密后）
	 */
	public String CurrentUserPWD = "";
	
	/**
	 * 当前的SessionID
	 */
	public short CurrentSessionID = 0;
	
	/**
	 * 当前发�?udp心跳端口
	 */
	public int CurrentUdpPort;
	
	
	/**
	 * 重新加载系统设置
	 */
	public void Load()
	{
		SharedPreferences sp = SettingContext.getSharedPreferences(SETTING_CONFIG,0);
		
		isRememberPass = sp.getBoolean("isRememberPass", isRememberPass);
		ServiceAddress = sp.getString("ServiceAddress", ServiceAddress);
		ServicePort = sp.getInt("ServicePort", ServicePort);
		CurrentUserID = sp.getString("CurrentUserID", CurrentUserID);
		CurrentUserLoginName = sp.getString("CurrentUserLoginName", CurrentUserLoginName);
		CurrentUserDisplayName = sp.getString("CurrentUserDisplayName", CurrentUserDisplayName);
		CurrentUserLoginPass = sp.getString("CurrentUserLoginPass", CurrentUserLoginPass);
		CurrentUserPWD = sp.getString("CurrentUserPWD", CurrentUserPWD);
		UserGroupName = sp.getString("UserGroupName", UserGroupName);
	}
	
	/**
	 * 保存当前设置 
	 */
	public void Save()
	{
		SharedPreferences sp = SettingContext.getSharedPreferences(SETTING_CONFIG,0);
		SharedPreferences.Editor editor = sp.edit();
		
		editor.putBoolean("isRememberPass", isRememberPass);
		editor.putString("ServiceAddress", ServiceAddress);
		editor.putInt("ServicePort", ServicePort);
		editor.putString("CurrentUserID", CurrentUserID);
		editor.putString("CurrentUserLoginName", CurrentUserLoginName);
		editor.putString("CurrentUserDisplayName", CurrentUserDisplayName);
		editor.putString("UserGroupName", UserGroupName);
		
		if(isRememberPass){
			editor.putString("CurrentUserLoginPass", CurrentUserLoginPass);
		}
		editor.putString("CurrentUserPWD", CurrentUserPWD);
		
		editor.commit();
	}
	
}
