package com.sinofloat.mobilesafe.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.sinofloat.mobilesafe.base.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import sinofloat.wvp.tools.ToastUtil;

/**
 * Created by oyk on 2016/7/15.
 * 一些常用的小工具
 */
public class Tools {

    /**
     * 双击退出函数
     */
    private static Boolean isExit = false;

    public static void exitBy2Click(Activity activity) {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(activity, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            activity.finish();
            System.exit(0);
        }
    }

    /**
     * 获取当前Activity的名字
     * @param context
     * @return runningActivityName
     */
    public static String getRunningActivityName(Context context) {
        String contextString = context.toString();
        return contextString.substring(contextString.lastIndexOf(".") + 1, contextString.indexOf("@"));
    }

    /**
     * 检查SD卡是否可用
     *
     */
    public static boolean SDCardReady(Context context){

        if (App.fileSetting.isSDCardReady()) {
            if (!App.fileSetting.isAppFileHomeDirCreated) {
                App.fileSetting.creatAppFileHomeDir();
            }
        } else {
                ToastUtil.showSimpleToast(context, "SD卡不可用，请检查", true);
            return false;
        }
        return true;
    }

    /**
     *
     * @param time 时间
     * @return 返回一个日期
     */
    public static String getDate(long time) {

        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);

    }

    /**
     * 获取年
     */
    public static int getYear(long time) {

        return Integer.parseInt(new SimpleDateFormat("yyyy").format(time));

    }
    /**
     * 获取月
     */
    public static int getMonth(long time) {

        return Integer.parseInt(new SimpleDateFormat("MM").format(time));

    }
    /**
     * 字符串转日期
     */
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static Date str2Date(String str, String format) {
        if (str == null || str.length() == 0) {
            return null;
        }
        if (format == null || format.length() == 0) {
            format = FORMAT;
        }
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            date = sdf.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 功能描述：返回毫
     *
     * @param date 日期
     * @return 返回毫
     */
    public static Calendar calendar = null;
    public static long getMillis(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getTimeInMillis();
    }

    /**
     * 在日期上增加数个整月
     * @param date 日期
     * @param n 要增加的月数
     * @return   增加数个整月
     */
    public static Date addMonth(Date date, int n) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, n);
        return cal.getTime();

    }

    /**
       * 获取版本号
       * @return 当前应用的版本号
       */
    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        int versioncode = 1;
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    /**
     * 复制单个文件
     * @param oldfile
     * @param newfile
     * @return boolean
     */
    public static boolean copyFile(File oldfile, File newfile) {
        boolean isok = true;
        try {
            int bytesum = 0;
            int byteread = 0;
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldfile); //读入原文件
                FileOutputStream fs = new FileOutputStream(newfile);
                byte[] buffer = new byte[1024];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    //System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                fs.flush();
                fs.close();
                inStream.close();
            }
            else
            {
                isok = false;
            }
        }
        catch (Exception e) {
            // System.out.println("复制单个文件操作出错");
            // e.printStackTrace();
            isok = false;
        }
        return isok;

    }

    //把日期转为字符串
        public static String ConverToString(Date date)
        {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                return df.format(date);
            }
        //把字符串转为日期
                public static Date ConverToDate(String strDate) throws Exception
       {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                return df.parse(strDate);
            }

    //获得当天0点时间
    public static int getTimesmorning(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis()/1000);
    }
    //获得当天24点时间
    public static int getTimesnight(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) (cal.getTimeInMillis()/1000);
    }

    /**
     *
     * @param time 时间
     * @return 返回一个日期
     */
    public static String getStrToday(long time) {

        return new SimpleDateFormat("yyyyMMdd").format(time);

    }
    //把日期转为字符串
    public static String toDayToString(Date date)
    {
        DateFormat df = new SimpleDateFormat("yyyyMMdd");

        return df.format(date);
    }
}
