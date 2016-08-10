package com.sinofloat.mobilesafe.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.WifiManager;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.entity.AudioSetting;
import com.sinofloat.mobilesafe.entity.BaseSet;
import com.sinofloat.mobilesafe.entity.ConnectionSettings;
import com.sinofloat.mobilesafe.entity.FileSetting;
import com.sinofloat.mobilesafe.entity.LocationSetting;
import com.sinofloat.mobilesafe.entity.NotifySetting;
import com.sinofloat.mobilesafe.wvp.tools.UserConnectionUtil;

import java.util.List;

/**
 * Created by oyk on 2016/7/12.
 */
public class App extends Application{
    public static final String TAG = "oyk";
    /**
     * 最新的坐标（最后一次获取的坐标）
     */
    public static Location lastLocation = new Location(
            LocationManager.GPS_PROVIDER);
    /**
     * 资源文件音频播放Id
     */
    public static int SoundErrorId;
    public static int SoundOkId;
    public static int SoundButtonDownId;
    public static int SoundButtonUpId;

    // 提醒soundId
    public static int SoundNotifyId;
    /**
     * 资源文件音频播放对象
     */
    private static SoundPool m_SoundPool = null;

    /**
     * 提醒功能资源文件音频播放对象
     */
    private static SoundPool m_NotifySoundPool = null;

    /**
     * 震动对象
     */
    private static Vibrator m_Vibrator = null;

    /**
     * 正常震动提示
     */
    public static long[] VibratorPatternOk = { 200, 100 };

    /**
     * 错误震动提示
     */
    public static long[] VibratorPatternError = { 100, 200, 100, 200 };

    /**
     * 提醒提示
     */
    public static long[] VibratorPatternNotify = { 500, 300, 500, 300 };

    /**
     * 保持Wifi连接的锁
     */
    private static WifiManager.WifiLock m_WifiLock = null;

    /**
     * 定位设置对象
     */
    public static LocationSetting LocationSetting;

    /**
     * 连接信息配置对象
     */
    public static ConnectionSettings ConnectionSet = null;

    /**
     * 总的设置 所有共同的设置属性
     */
    public static BaseSet baseSet;

    /**
     * 提醒设置
     */
    public static NotifySetting notifySetting;

    /**
     * 音频设置
     */
    public static AudioSetting audioSetting;

    /**
     * 文件按管理设置
     */
    public static FileSetting fileSetting;

    /**
     * 本程序的广播发�?对象
     */
    public static LocalBroadcastManager broadcastManager;

    /**
     * 定时启动service的alarm对象
     */
    // private AlarmReceiver alarmReceiver;

//	public static BlueToothScoManager scoManager;

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

    public static UserConnectionUtil userUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化Logger日志工具类
        Logger.init(TAG)
                .methodCount(3)
                .hideThreadInfo()
                .logLevel(LogLevel.FULL)
                .methodOffset(0);

        userUtil = new UserConnectionUtil();

        // 监听电话程序
        telephoneManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephoneManager.listen(new MyPhoneStateListener(),
                PhoneStateListener.LISTEN_CALL_STATE);

        // 初始化配置信息对�?
        LocationSetting = new LocationSetting(this, true);
        ConnectionSet = new ConnectionSettings(this, true);
        baseSet = new BaseSet(this, true);
        notifySetting = new NotifySetting(this, true);
        audioSetting = new AudioSetting(this, true);
        fileSetting = new FileSetting(this, true);

        m_Vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // 默认使用music
        initSoundPool(this);

        // 获取广播实例
        broadcastManager = LocalBroadcastManager.getInstance(this);

        // // 实例化SCO监听
        // scoManager = new BlueToothScoManager(this);
        // scoManager.checkBluetooth(this);

    }

    /**
     * 获取最后一次收到的location对象
     *
     * @return
     */
    public static Location getLastLocation() {
        return lastLocation;
    }

    /**
     * 设置最新的location
     *
     * @param location
     */
    public static void setLastLocation(Location location) {
        lastLocation = location;
    }

    /**
     * 初始化资源播放对象
     *
     * @param context
     */
    public void initSoundPool(Context context) {

        // 初始化音频播放对�? 加载音频文件
        int streamType = AudioManager.STREAM_MUSIC;
        if (audioSetting.IsUseVoicePhone) {
            streamType = AudioManager.STREAM_VOICE_CALL;
        }
        m_SoundPool = new SoundPool(1, streamType, 0);

        SoundOkId = m_SoundPool.load(context, R.raw.connect_ok, 1);
        SoundButtonDownId = m_SoundPool.load(context, R.raw.button_down, 1);
        SoundButtonUpId = m_SoundPool.load(context, R.raw.button_up, 1);
        SoundErrorId = m_SoundPool.load(context, R.raw.error, 1);

        // 提醒 sound 对象
        m_NotifySoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        SoundNotifyId = m_NotifySoundPool.load(context, R.raw.notify, 1);
    }

    /**
     * 重置soundPool
     *
     * @param context
     */
    public static void resetSoundPool(Context context) {

        SoundPool sp = m_SoundPool;
        m_SoundPool = null;
        sp.release();
        sp = null;

        // 初始化音频播放对�? 加载音频文件
        int streamType = AudioManager.STREAM_MUSIC;
        if (audioSetting.IsUseVoicePhone) {
            streamType = AudioManager.STREAM_VOICE_CALL;
        }
        sp = new SoundPool(1, streamType, 0);

        SoundOkId = sp.load(context, R.raw.connect_ok, 1);
        SoundButtonDownId = sp.load(context, R.raw.button_down, 1);
        SoundButtonUpId = sp.load(context, R.raw.button_up, 1);
        SoundErrorId = sp.load(context, R.raw.error, 1);

        m_SoundPool = sp;
    }

    /**
     * service 发送结果给activity
     *
     * @param command
     *            命令
     * @param extra
     *            命令描述
     */
    public static void sendBroadcast(int command, String extra) {
        Intent intent = new Intent(C.ACTION_BROADCAST);
        intent.putExtra(C.COMMAND_KEY, command);
        intent.putExtra(C.COMMAND_EXTRA_KEY, extra);
        broadcastManager.sendBroadcast(intent);
    }

    /**
     * 设置定时启动service
     */
    // public void setAlarmWork(int repeatTimeInterval) {
    // if (alarmReceiver == null) {
    // alarmReceiver = new AlarmReceiver();
    // }
    // alarmReceiver.setAlarm(this,repeatTimeInterval);
    // }

    /**
     * 播放提醒3种模式（震动+铃音，铃音，震动）
     */
    public static void playNotifySound(int soundId) {

        float volume = notifySetting.alertVolume / 10.0f;

        switch (notifySetting.notifyModePosition) {
            case 0:// 震动+铃音
                m_NotifySoundPool.play(soundId, volume, volume, 1, 0, 1.0f);
                m_Vibrator.vibrate(VibratorPatternNotify, -1);
                break;
            case 1:// 铃音
                m_NotifySoundPool.play(soundId, volume, volume, 1, 0, 1.0f);
                break;
            case 2:// 震动
                m_Vibrator.vibrate(VibratorPatternNotify, -1);
                break;
        }
    }

    /**
     * 播放系统资源中的声音
     *
     * @param resId
     *            声音文件的资源ID
     */
    public static void PlaySound(int resId, long[] viberate) {

        float volume = notifySetting.alertVolume / 10.0f;

        try {
            if (m_SoundPool != null) {

                switch (notifySetting.notifyModePosition) {
                    case 0:// 震动+铃音
                        m_SoundPool.play(resId, volume, volume, 1, 0, 1.0f);
                        m_Vibrator.vibrate(viberate, -1);
                        break;
                    case 1:// 铃音
                        m_SoundPool.play(resId, volume, volume, 1, 0, 1.0f);
                        break;
                    case 2:// 震动
                        m_Vibrator.vibrate(viberate, -1);
                        break;
                }
            }
        } catch (Exception e) {

        }

        // try {
        // if (m_SoundPool != null) {
        //
        // m_SoundPool.play(resId, volume,
        // volume / 10f, 1, 0, 1.0f);
        // }
        // if (vibrate != null && notifySetting.IsVibrate) {
        // // /*
        // // * 想设置震动大小可以�?过改变pattern来设定，如果�?��时间太短，震动效果可能感觉不�?
        // // * */
        // // 重复两次上面的pattern 如果只想震动�?��，index设为-1
        // m_Vibrator.vibrate(vibrate, -1);
        // }
        // } catch (Exception e) {
        // }
    }

    /**
     * 启用Wifi网络连接保持操作
     */
    private void WifiLockAcquire() {

        if (m_WifiLock == null) {
            WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            m_WifiLock = manager.createWifiLock("SINOFLOAT_TALKIE");
        }

        if (!m_WifiLock.isHeld()) {
            m_WifiLock.acquire();
        }
    }

    /**
     * 关闭Wifi网络保持操作
     */
    private void WifiLockRelease() {
        if (m_WifiLock == null) {
            return;
        }

        if (m_WifiLock.isHeld()) {
            m_WifiLock.release();
        }
    }

    /**
     * 判断当前activity是不是在栈最下边
     *
     * @param act
     * @return
     */
    public static boolean isBaseActivity(Activity act) {
        ActivityManager manager = (ActivityManager) act
                .getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

        if (runningTaskInfos != null)
            return runningTaskInfos.get(0).baseActivity.equals(act
                    .getComponentName());
        else
            return false;
    }

    /**
     * 监听电话程序状态
     *
     * @author staid
     *
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
                    // if(isCalling){
                    // ToastUtil.showSimpleToast(AppComm.this, "接起电话了", true);
                    // }
                    break;
                default:
                    break;
            }
        };
    }

    /**
     * 弹出一个最简单的 程序退出alertDialog 退出程序
     *
     * @param context
     * @param title
     * @param message
     */
    public static void showSimpleAlertDialog(Context context, String title,
                                             String message, DialogInterface.OnClickListener listener) {
        AlertDialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != null && title.length() > 0) {
            builder.setTitle(title);
        }
        if (message != null && message.length() > 0) {
            builder.setMessage(message);
        }

        builder.setPositiveButton("确 定", listener);
        builder.setNegativeButton("取 消", listener);
        dialog = builder.create();
        dialog.show();
    }

    public void releaseApp() {

        broadcastManager = null;
        // scoManager.unregisterConnectionRcvr(this);
        // scoManager.unregisterScoRcvr(this);
        // scoManager = null;

        m_SoundPool.release();
        m_SoundPool = null;
        m_Vibrator = null;
        m_WifiLock = null;
        ConnectionSet = null;
        LocationSetting = null;
    }
}
