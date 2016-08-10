package com.sinofloat.mobilesafe.main.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.base.C;

/**
 * 监听电话状态service 这个类的设计需求就是 设置监听所有我们想监听的系统广播 或者系统设置改变 例如 按下home键 来电话
 * 等影响程序生命周期（用户可能无意退出 不再回到程序 不能费流量 及时关闭应用） 捕获到手机状态后发出程序内广播
 * 需要这些消息的activity页面可以注册广播接收器 做出相应处理。
 * 
 * 注 如果是home键按下 我们只需要在根activity oncreat 注册广播 destroy取消广播 就可以监听整个应用程序（所有页面）的home键按下事件。
 * @author staid
 * 
 */
public class MonitorPhoneStateService extends Service {

	/**
	 * 系统广播
	 */
	private SystemBroadcastRCVR systemRCVR;

	/**
	 * 电话服务
	 */
	private TelephonyManager telephoneManager;

	/**
	 * 标记电话是否正在通话中
	 */
	private boolean isPhoneCalling = false;

	/**
	 * 标记电话状态（idle ringing offHook）
	 */
	private int lastPhoneState = TelephonyManager.CALL_STATE_IDLE;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		initPhoneState();
		registerRCVR();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterRCVR();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return START_STICKY;
	}

	private void initPhoneState() {
		// 监听电话程序
		telephoneManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		telephoneManager.listen(new MyPhoneStateListener(),
				PhoneStateListener.LISTEN_CALL_STATE);
	}

	// 初始化广播 注册广播
	private void registerRCVR() {

		systemRCVR = new SystemBroadcastRCVR();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(systemRCVR, intentFilter);
	}

	// 取消注册
	private void unregisterRCVR() {
		if (systemRCVR != null) {
			unregisterReceiver(systemRCVR);
		}
	}

	/**
	 * 广播接收系统消息。home键 电量 各种外设设备插拔事件等
	 */
	private class SystemBroadcastRCVR extends BroadcastReceiver {

		final String SYSTEM_DIALOG_REASON_KEY = "reason";
		final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// ToastUtil.showSimpleToast(context, "onReceive hoome", true);
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				if (reason != null
						&& reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {

					Log.e("MonitorPhoneStateService", "SYSTEM_DIALOG_REASON_HOME_KEY");
//					ToastUtil.showSimpleToast(context, "捕获到home键按下", true);
					App.sendBroadcast(C.BROADCAST_HOME_KEY_PRESSED, "home键按下");
				}
			} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			} else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
//				ToastUtil.showSimpleToast(context, "捕获到ACTION_BATTERY_CHANGED", true);
			}
		}
	}

	/**
	 * 监听电话程序状态
	 * @author staid
	 */
	private class MyPhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);

			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				lastPhoneState = state;
				break;
			case TelephonyManager.CALL_STATE_IDLE:// 手机空闲
				if (isPhoneCalling) {
					// 挂电话了
//					scoManager.checkBluetooth(AppComm.this);
					// ToastUtil.showSimpleToast(AppComm.this, "挂电话了", true);
					isPhoneCalling = false;
				}

				lastPhoneState = state;
				// ToastUtil.showSimpleToast(AppComm.this, "CALL_STATE_IDLE",
				// true);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				isPhoneCalling = (lastPhoneState == TelephonyManager.CALL_STATE_RINGING);
				lastPhoneState = state;
				 if(isPhoneCalling){
					 // 接电话了
					 App.sendBroadcast(C.BROADCAST_PICK_UP_PHONE_CALL, "接起电话");
//				 ToastUtil.showSimpleToast(MonitorPhoneStateService.this, "接起电话了", true);
				 }
				break;
			default:
				break;
			}
		};
	}

}
