package com.sinofloat.mobilesafe.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class MyComponentManager {

	/**
	 * 键
	 */
	public static final String STRING_KEY = "dataString";
	public static final String STRING_KEY1 = "dataString1";
	
	/**
	 * 数据从一个activity传递到另一个activity
	 */
	public static Object mTransferedData;
	
	/**
	 * 设置需要传过去的数据
	 * @param transferedData
	 */
	public static void setTransferedData(Object transferedData){
		mTransferedData = transferedData;
	}
	
	
	/**
	 * 获取传递过来的对象
	 * @return
	 */
	public static Object getTransferedData(){
		return mTransferedData;
	}
	
	public static void startActivity(boolean forResult,Activity activity,Class<?> cls){
		Intent intent = new Intent(activity, cls);
		if(forResult){
			activity.startActivityForResult(intent, 0);
		}else{
			activity.startActivity(intent);
		}
	}

	/**
	 * 带一个flag值 指定activity在栈里的位置
	 * @param forResult
	 * @param flag
	 * @param activity
     * @param cls
     */
	public static void startActivity(boolean forResult,int flag,Activity activity,Class<?> cls){
		Intent intent = new Intent(activity, cls);
		intent.addFlags(flag);
		if(forResult){
			activity.startActivityForResult(intent, 0);
		}else{
			activity.startActivity(intent);
		}
	}
	
	
	public static void startActivity(boolean forResult,Activity activity,Class<?> cls,String args){
		Intent intent = new Intent(activity, cls);
		if(args != null){
			intent.putExtra(STRING_KEY, args);
		}
		if(forResult){
			activity.startActivityForResult(intent, 0);
		}else{
			activity.startActivity(intent);
		}
	}
	
	/**
	 * action 启动activity。
	 * @param forResult
	 * @param activity
	 * @param action
	 */
	public static void startActivityWithAction(boolean forResult,Activity activity,String action){
		Intent intent = new Intent();
		intent.setAction(action);
		
		if(forResult){
			activity.startActivityForResult(intent, 0);
		}else{
			activity.startActivity(intent);
		}
	}
	
	/**
	 * 停止service
	 * 
	 * @param context
	 */
	public static void stopService(Context context,Class<?> cls) {
		Intent stop = new Intent(context, cls);
		context.stopService(stop);
	}

	/**
	 * 启动service
	 * 
	 * @param context
	 */
	public static void startService(Context context,Class<?> cls) {
		Intent start = new Intent(context, cls);
		context.startService(start);
	}
	
	/**
	 * 启动service
	 * 
	 * @param context
	 */
	public static void startServiceWithCommand(Context context,Class<?> cls,int command,String extra) {
		
		Intent start = new Intent(context, cls);
		
		start.putExtra("command", command);
		
		if(extra != null && extra.length() > 0){
			start.putExtra("extra", extra);
		}
		
		context.startService(start);
	}
	
}
