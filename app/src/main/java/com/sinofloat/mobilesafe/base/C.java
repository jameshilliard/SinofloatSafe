package com.sinofloat.mobilesafe.base;

import com.amap.api.maps.model.LatLng;

/**
 * Created by oyk on 2016/7/13.
 * 用于存放所有常量的类
 */
public class C {
    /**
     * 应用程序的 名称
     */
    public static final String WVP_SAFE = "移动安防";
    /**
     * service给activity发送的广播 命令 和命令描述
     */
    public static final String COMMAND_KEY = "command";
    public static final String COMMAND_EXTRA_KEY = "extra";

    /**
     * 摄像头 不能预览时 统一显示无信号 （无网络 无资源 等各种情况）
     */
    public static final String CAMERA_PREVIEW_ERROR_SHOW_MSG = "无信号";

    /**
     * 摄像头 预览数据加载中
     */
    public static final String CAMERA_PREVIEW_LOADING = "连接中";

    /**
     * 程序内发广播的action
     */
    public static final String ACTION_BROADCAST = "ACTION_BROADCAST";
    // home键 按下
    public static final int BROADCAST_HOME_KEY_PRESSED = 0;
    // 接起电话
    public static final int BROADCAST_PICK_UP_PHONE_CALL = 1;
    // 流量广播
    public static final int BROADCAST_UPLOAD_TRAFIC = 7;

    // app升级广播（可以升级）
    public static final int BROADCAST_APP_UPDATE = 11;

    public static final int TEST_TOAST = 18;

    /**
     * 这几个消息是定义给 视频拍照上传用的。根据不同情况才不不同处理。
     */
    public static final int HOME_PRESSED = 58;

    public static final int EXIT_ACTIVITY = 59;

    // 工作正常(在线上传 本地存储都可以)
    public static final int STATE_ERROR_NONE = 60;

    // 相机错误
    public static final int STATE_ERROR_CAMERA = 61;

    // SD卡错误（sd卡 没有挂载上）
    public static final int STATE_ERROR_SD_CARD = 62;

    // 网络连接错误（俩种情况 1，捕获到网络异常;2，队列未发送数据超过上限定为为网络异常）
    public static final int STATE_ERROR_CONNECTION = 63;

    // 录音错误 （录音对象未实例化）
    public static final int STATE_ERROR_RECORDER = 64;

    // 写入文件错误
    public static final int STATE_ERROR_FILE_WRITE = 65;

    // 结束写文件 主要是UI弹出一个对话框 防止用户其他操作 队列数据写尽后 消失对话框
    public static final int STATE_FINISH_FILE_WRITE = 66;

    /**
     * 解码视频是否超时标记
     */
    public static final int STATE_TIME_OUT_DECODE_BITMAP = 68;

    /**
     * 图像解码失败
     */
    public static final int STATE_DECODE_FAIL = 69;

    /**
     * 正在连接中 连接服务器进行预览
     */
    public static final int STATE_CONNECTING = 70;

    /**
     * 正在连接中 连接服务器进行预览
     */
    public static final int STATE_DECODE_SUCCESS = 71;

    /**
     * 正在连接中 连接服务器进行预览
     */
    public static final int STATE_START_CONNECT = 72;

    /**
     * 正在连接中 连接服务器进行预览
     */
    public static final int STATE_END_CONNECT = 72;

    //----------用户登录验证--------------------------------------------------------------------------------------------------------------------
    // app key
    public static final String APP_KEY = "19c74939-d336-4a08-83a4-abc49f49286e";
    // app secret
    public static final String APP_SECRET = "4ffd2ebf-88b2-4a2d-a612-67985783e281";

    public static final String OAUTH2_BASE_URL = "http://218.4.136.114:124/OAuthAuthorizationServer/oauth/Authorize";
    // 回调地址
    public static final String REDIRECT_URL = "http://218.4.136.114:124/OAuthAuthorizationServer/default.html";

    // 通过code请求toke的URL
    public static final String OAUTH2_ACCESS_TOKEN_URL = "http://218.4.136.114:124/OAuthAuthorizationServer/oauth/Token";

    // 获得基础数据的url
    public static final String EPOINT_BASE_DATA_URL = "http://218.4.136.114:124/EpointBaseDataServer/";

    // 获得用户信息的url
    public static final String EPOINT_USER_DATA_URL = "http://218.4.136.114:124/OAuthAuthorizationServer/Data/";

    // 存储平台url
    public static final String EPOINT_ATTACH_SERVER_URL = "http://218.4.136.114:124/EpointAttachServer/";

    // 授权范围
    public static final String SCOPE = "";

    // 结束====================结束==========================================结束

    /*****************************************高德地图**********************************************/
    public static final LatLng BEIJING = new LatLng(39.90403, 116.407525);// 北京市经纬度
    public static final LatLng ZHONGGUANCUN = new LatLng(39.983456, 116.3154950);// 北京市中关村经纬度
    public static final LatLng SHANGHAI = new LatLng(31.238068, 121.501654);// 上海市经纬度
    public static final LatLng FANGHENG = new LatLng(39.989614, 116.481763);// 方恒国际中心经纬度
    public static final LatLng CHENGDU = new LatLng(30.679879, 104.064855);// 成都市经纬度
    public static final LatLng XIAN = new LatLng(34.341568, 108.940174);// 西安市经纬度
    public static final LatLng ZHENGZHOU = new LatLng(34.7466, 113.625367);// 郑州市经纬度

    public static final String SETTING ="setting";

}
