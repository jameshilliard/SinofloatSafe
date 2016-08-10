package com.sinofloat.mobilesafe.wvp.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.util.Log;

import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.base.C;
import com.sinofloat.mobilesafe.map.entity.RemoteGroup;
import com.sinofloat.mobilesafe.monitor.entity.CameraPosition;
import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;
import com.sinofloat.mobilesafe.replay.entity.CameraRecord;
import com.sinofloat.mobilesafe.widget.opengl.DisplayView;
import com.sinofloat.mobilesafe.wvp.OnVideoFrameDecodedCallback;
import com.sinofloat.mobilesafe.wvp.OnWorkStateListener;
import com.sinofloat.mobilesafe.wvp.SwitchWorkListener;
import com.sinofloat.mobilesafe.wvp.decode.HardVideoDecoder;
import com.sinofloat.mobilesafe.wvp.tools.MyLinkedBlockingQueue;
import com.sinofloat.mobilesafe.wvp.tools.SocketReceiver;
import com.sinofloat.mobilesafe.wvp.tools.SocketSender;
import com.sinofloat.mobilesafe.wvp.tools.WvpAudioPlayer;
import com.sinofloat.mobilesafe.wvp.tools.WvpAudioRecoder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import sinofloat.wvp.core.SCCodec;
import sinofloat.wvp.core.VideoDecoder;
import sinofloat.wvp.core.interfaces.ISocketEventListener;
import sinofloat.wvp.messages.WvpControlMove;
import sinofloat.wvp.messages.WvpDataKeyValue;
import sinofloat.wvp.messages.WvpDataMediaSample;
import sinofloat.wvp.messages.WvpDataSampleDemo;
import sinofloat.wvp.messages.WvpDataText;
import sinofloat.wvp.messages.WvpGetResourceRequest;
import sinofloat.wvp.messages.WvpGetResourceResponse;
import sinofloat.wvp.messages.WvpMessage;
import sinofloat.wvp.messages.WvpMessageHelper;
import sinofloat.wvp.messages.WvpNodeConnectRequest;
import sinofloat.wvp.messages.WvpNodeConnectResponse;
import sinofloat.wvp.messages._ResourceTypes;
import sinofloat.wvp.tools.Util;

/**
 * 单例模式 移动安防的工具类 获取相机列表（获取后关闭socket） 获取相机图像（持续获取服务器音视频数据）。
 * //如果摄像头有云台，则使用“CamHasPtz|”前缀，如果没有云台则使用“CamNoPtz|”前缀
 * //如果是摄像头组，则使用“CamGroup|”前缀 //如果是移动设备，则使用“Mobile|”前缀
 *
 * @author staid
 */
public class RemoteCameraWorker {

    private static final String TAG = "RemoteCameraWorker";

    private static RemoteCameraWorker cameraWorker;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            Locale.CHINA);
    /**
     * 是否开启硬编码
     */
    private static final boolean isHardDecode = false;
    /***********************************
     * 软编
     *******************************************************/
    private VideoDecoder h264Decoder;
    /********************************
     * 硬编编
     *******************************************************/
    private HardVideoDecoder hardDecode;
    /**
     * 远程相机对象Bean
     */
    private RemoteCamera curRemoteCamera;

    /**
     * 地图数据 （需要用bitmapFactory 解码一下）
     */
    public byte[] mapImgArray;
    /**
     * 相机的预置位
     */
    public ArrayList<CameraPosition> cameraPositions = new ArrayList<CameraPosition>();
    /**
     * 相机的录像列表
     */
    public ArrayList<CameraRecord> cameraRecords = new ArrayList<CameraRecord>();

    /**
     * 具体相机设备列表数据
     */
    public ArrayList<RemoteCamera> cameraList = new ArrayList<RemoteCamera>();
    /**
     * 地图界面相机设备列表数据
     */
    public ArrayList<RemoteCamera> mapCameraList = new ArrayList<RemoteCamera>();
    /**
     * 回放界面视频的相机设备列表数据
     */
    public ArrayList<RemoteCamera> videoCameraList = new ArrayList<RemoteCamera>();

    /**
     * 地图界面带坐标的相机组列表数据
     */
    public ArrayList<RemoteGroup> remoteGroups = new ArrayList<RemoteGroup>();

    /**
     * 相机在地图上的位置 地图列表 点击列表条目 请求具体的地图图片。
     */
    public ArrayList<WvpDataKeyValue> cameraMapList = new ArrayList<WvpDataKeyValue>();

    /**
     * 相机在地图上的位置信息 根据每个摄像头的位置 最后画在地图上一个摄像头图片。点击摄像头图片进入预览画面
     */
    public ArrayList<WvpDataKeyValue> cameraLocationInfoList = new ArrayList<WvpDataKeyValue>();

    /**
     * 是否正在工作 (发送数据 和 接收数据都会抛出异常 接收数据抛出的异常会慢 所以 监听socket连接状态的listener可能会被调用俩次 所以
     * 设置这个flag socket只要出现异常 就设置false 防止调用俩次)
     */
    private boolean isWorking;

    /**
     * 流对象
     */
    private Socket socket;
    /**
     * 是否开启录音
     */
    private boolean isAudioRecordOpen;

    /**
     * 拍照数据队列
     */
    private MyLinkedBlockingQueue sendQueue;

    /**
     * 接收数据队列
     */
    private LinkedBlockingQueue<byte[]> receiveQueue;

    /**
     * 接收音频的队列
     */
    public LinkedBlockingQueue<WvpDataMediaSample> receiveAudioQueue;

    /**
     * 接收视频的队列
     */
    public LinkedBlockingQueue<WvpDataMediaSample> receiveVideoQueue;

    /**
     * 接收文本的队列
     */
    public LinkedBlockingQueue<WvpDataText> receiveTextQueue;

    /**
     * 数据发送对象
     */
    private SocketSender socketSender;

    /**
     * 用于接收数据对象
     */
    private SocketReceiver socketReceiver;

    /**
     * 用于拆分接收数据对象
     */
    private QueueSwitcher queueSwitcher;

    /**
     * 回调给UI的（发送数据过程出现异常 回调给UI UI相关元素设置为初始状态）
     */
    private OnWorkStateListener workStateListener;

    /**
     * 文本数据回调接口
     */
    private SwitchWorkListener textListener;

    /**
     * 录制音频的对象
     */
    private WvpAudioRecoder audioRecord;

    /**
     * 音频播放对象
     */
    private WvpAudioPlayer audioPlayer;

    /**
     * 获取每秒流量的timer。
     */
    private Timer netTraficTimer;
    /**
     * 获取流量的runnable 对象。
     */
    private TimerTask netTraficTask;
    /**
     * 每秒钟流量
     */
    private long lastTotalTraficLength = 0;

    private StringBuilder builder = new StringBuilder();
    private String netType;
    private static String TRAFIC_UNIT = "KB/S";

    /**
     * 检查解码超时 时间间隔为3秒。
     */
    private static final int CHECK_TIME_INTERVAL = 3000;

//	/**
//	 * 超时时间最大值 10秒
//	 */
//	private static int TIME_OUT_DECODE_BITMAP = 10 * 1000;

    /**
     * 接收视频流数据超时时间最大值 10秒
     */
    private static int TIME_OUT_RECEIVE_DATA = 10 * 1000;


    /**
     * 解码buffer 正常是 videoWidth * videoHeight * 3 / 2
     */
    private byte[] frameBuffer = new byte[1280 * 720 * 3 / 2];

    /**
     * 从连接网络成功开始到解码视频成功的超时timer
     */
    private Timer decodeBitmapTimer;

    private TimerTask decodeBitmapTask;

    /**
     * 当前超时计时。()
     */
    private int nowTimeCount = 0;


    /**
     * openGL 显示view
     */
    private DisplayView displayView;

    /**
     * 视频解码回调接口
     */
    private OnVideoFrameDecodedCallback videoDecodedFrameCallback;

    /**
     * 执行预览的工作线程
     */
    private Thread previewThread;

    //private FileOutputStream fos;


    public static RemoteCameraWorker getInstance() {
        if (cameraWorker == null) {
            cameraWorker = new RemoteCameraWorker();
            // 获取实例后要及时调用
            SCCodec.init("test_for_billy_20140726");
        }
        return cameraWorker;
    }

    private RemoteCameraWorker() {

    }

    /**
     * 设置回调接口
     *
     * @param videoDecodedFrameCallback
     * @param displayView
     */
    public void setVideoDecodedFrameCallback(
            OnWorkStateListener onWorkStateListener,
            OnVideoFrameDecodedCallback videoDecodedFrameCallback,
            DisplayView displayView) {
        this.displayView = displayView;
        this.workStateListener = onWorkStateListener;
        this.videoDecodedFrameCallback = videoDecodedFrameCallback;
    }

    /**
     * 远程监控视频数据回调接口
     */
    public SwitchWorkListener videoListener = new SwitchWorkListener() {

        @Override
        public void OnFindMessage(WvpMessage message, short sessionId) {
/*********************************软编***********************************************/
            WvpDataMediaSample frame = (WvpDataMediaSample) message;

            if (frame.data != null && frame.data.length > 0) {
                // long last = System.currentTimeMillis();
                nowTimeCount = 0;
/*

				if (hardDecode == null) {
					VideoPacket.StreamSettings streamSettings = VideoPacket.getStreamSettings(frame.data);
					//Logger.e("sps" + streamSettings.sps + "------------------pps" + streamSettings.pps);
					hardDecode = new HardVideoDecoder();
					hardDecode.start();
					hardDecode.configure(null, 720, 576, streamSettings.sps, streamSettings.pps);
				}else {
					hardDecode.decodeSample(frame.data,0,frame.data.length,frameBuffer,frameBuffer.length);
				}
*/

                if (isHardDecode) {
                } else {
                    if (h264Decoder.decode(frame.data, frame.data.length,
                            frameBuffer, frameBuffer.length) != -1) {
                        try {
                            //Logger.e("--------"+frameBuffer.length);
                            //fos.write(frame.data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        nowTimeCount = 0;
                        // long now = System.currentTimeMillis();
                        // Log.e(Thread.currentThread().getName()
                        // + "新解码库decode time $$$$$$$$$$$$$$", "decode time==="
                        // + (now - last));
                        if (videoDecodedFrameCallback != null) {
                            videoDecodedFrameCallback.onDecodedResult(displayView,
                                    true, frameBuffer, h264Decoder.frameWidth(),
                                    h264Decoder.frameHeight());
                        }
                    }

                }
            }

/*********************************硬编***********************************************/


        }
    };

    /**
     * 图片数据回调接口（手机与手机视频对讲）
     */
    public SwitchWorkListener pictureListener = new SwitchWorkListener() {

        @Override
        public void OnFindMessage(WvpMessage message, short sessionId) {

            WvpDataMediaSample frame = (WvpDataMediaSample) message;

            if (isHardDecode) {


            } else {
                if (h264Decoder.decode(frame.data, frame.data.length, frameBuffer,
                        frameBuffer.length) != -1) {

                    stopCheckDecodeBitmapWorker();

                    if (videoDecodedFrameCallback != null) {
                        videoDecodedFrameCallback.onDecodedResult(null, true,
                                frameBuffer, h264Decoder.frameWidth(),
                                h264Decoder.frameHeight());
                    }
                }

            }
        }
    };

    /**
     * 开启timer检查是否有数据回调过来（从服务器读取）
     */
    public void startCheckDecodeBitmapWorker() {

        nowTimeCount = 0;

        if (decodeBitmapTimer == null) {
            decodeBitmapTimer = new Timer();
        }

        decodeBitmapTask = new TimerTask() {

            @Override
            public void run() {

                nowTimeCount += CHECK_TIME_INTERVAL;

                if (nowTimeCount >= TIME_OUT_RECEIVE_DATA /*TIME_OUT_DECODE_BITMAP*/) {
                    // 如果超时 停止预览 停止一切工作
                    // stateCallbackToUI(C.STATE_TIME_OUT_DECODE_BITMAP,
                    // "无信号");
                    if (videoDecodedFrameCallback != null) {
                        videoDecodedFrameCallback.onDecodeTimeOut(null);
                    }
                    stopCheckDecodeBitmapWorker();
                    Log.e(TAG, "ERROR_TIME_OUT_DECODE_BITMAP");
                }
            }
        };
        decodeBitmapTimer.scheduleAtFixedRate(decodeBitmapTask, 0,
                CHECK_TIME_INTERVAL);
    }

    /**
     * 结束timer
     */
    public void stopCheckDecodeBitmapWorker() {

        if (decodeBitmapTimer != null) {
            decodeBitmapTimer.cancel();
            decodeBitmapTimer.purge();
            decodeBitmapTimer = null;
            decodeBitmapTask = null;
        }
    }


    /**
     * 获取当前正在使用的远程相机（固定 和 移动）
     *
     * @return
     */
    public RemoteCamera getCurRemoteCamera() {
        return curRemoteCamera;
    }

    /**
     * 是否socket连接正常
     *
     * @return
     */
    public boolean isWorking() {
        return isWorking;
    }

    /**
     * 启动开始预览线程 这个线程创建及启动放到这里是为了在不同activity都能停止他，因为这个类也是个单例模式。
     */
    public void startCameraPreviewWork(final boolean remoteCommunicate,
                                       final String serviceAddress, final int port, final String userID,
                                       final String passwordMd5, final String groupNameLowerCase,
                                       final RemoteCamera entity) {

        previewThread = new Thread(new Runnable() {

            @Override
            public void run() {

                // 回调UI 弹出对话框
                // stateCallbackToUI(C.STATE_START_CONNECT, "正在连接预览...");
                startWork(remoteCommunicate, serviceAddress, port, userID,
                        passwordMd5, groupNameLowerCase, entity);
                // 回调UI 结束对话框
                stateCallbackToUI(C.STATE_END_CONNECT, "取消加载对话框");
            }
        });
        previewThread.start();
    }

    /**
     * 启动开始预览线程 这个线程创建及启动放到这里是为了在不同activity都能停止他，因为这个类也是个单例模式。
     */
    public void startRecordPreviewWork(final boolean remoteCommunicate,
                                       final String serviceAddress, final int port, final String userID,
                                       final String passwordMd5, final String groupNameLowerCase,
                                       final String cameraID,final String beginTime,final String endTime,
                                       final String rate) {

        previewThread = new Thread(new Runnable() {

            @Override
            public void run() {

                // 回调UI 弹出对话框
                // stateCallbackToUI(C.STATE_START_CONNECT, "正在连接预览...");
                startWork(remoteCommunicate, serviceAddress, port, userID,
                        passwordMd5, groupNameLowerCase, cameraID,beginTime,endTime,rate);
                // 回调UI 结束对话框
                stateCallbackToUI(C.STATE_END_CONNECT, "取消加载对话框");
            }
        });
        previewThread.start();
    }

    /**
     * 工作状态回调给UI
     *
     * @param result
     * @param extra
     */
    private void stateCallbackToUI(int result, String extra) {

        switch (result) {
            case C.STATE_ERROR_CONNECTION:

                stopWork();
                break;
            case C.STATE_TIME_OUT_DECODE_BITMAP:

                stopWork();
                break;
        }

        if (workStateListener != null) {
            workStateListener.onWorkState(result, extra);
        }
    }

    /**
     * 发送数据 socket监听状态
     */
    private ISocketEventListener socketEventListenner = new ISocketEventListener() {

        @Override
        public void OnDisconnect(Object sender, String reason) {

            if (!isWorking) {
                return;
            }
            // 通知UI
            stateCallbackToUI(C.STATE_ERROR_CONNECTION, reason);
        }
    };

    /**
     * 清空摄像头列表
     */
    public void clearCameraList() {
        cameraList.clear();
    }
    /**
     * 清空地图摄像头列表
     */
    public void clearMapCameraList() {
        mapCameraList.clear();
    }
    /**
     * 清空视频摄像头列表
     */
    public void clearVideoCameraList() {
        videoCameraList.clear();
    }

    /**
     * 清空带坐标的摄像组列表
     */
    public void clearRemoteGroups() {
        remoteGroups.clear();
    }
    /**
     * 清空摄像头列表
     */
    public void clearCameraLocationInfoList() {
        cameraLocationInfoList.clear();
    }

    /**
     * 清空摄像头列表
     */
    public void clearCameraMapList() {
        cameraMapList.clear();
    }

    /**
     * 清空摄像头列表
     */
    public void clearCameraMapByteData() {
        mapImgArray = null;
    }

    /**
     * 重置listView item 选中状态
     *
     * @param position
     */
    public void resetListItemSelectState(int position) {

        int size = cameraList.size();

        for (int i = 0; i < size; i++) {

            RemoteCamera entity = cameraList.get(i);
            if (position == i) {
                entity.selected = true;
            } else {
                entity.selected = false;
            }
        }
    }

    /**
     * 重置listView item 选中状态(地图界面)
     *
     * @param position
     */
    public void resetMapListItemSelectState(int position) {

        int size = mapCameraList.size();

        for (int i = 0; i < size; i++) {

            RemoteCamera entity = mapCameraList.get(i);
            if (position == i) {
                entity.selected = true;
            } else {
                entity.selected = false;
            }
        }
    }
    /**
     * 重置listView item 选中状态(视频界面)
     *
     * @param position
     */
    public void resetVideoListItemSelectState(int position) {

        int size = videoCameraList.size();

        for (int i = 0; i < size; i++) {

            RemoteCamera entity = videoCameraList.get(i);
            if (position == i) {
                entity.selected = true;
            } else {
                entity.selected = false;
            }
        }
    }

    /**
     * 获取Monitor远程相机列表数据 建立socket连接 获取数据后关闭。
     *
     * @param serviceAddress
     * @param port
     * @param userID
     * @param passwordMd5
     * @return 错误信息
     */
    public String getRemoteCameraList(String serviceAddress, int port,
                                      String userID, String passwordMd5, String groupNameLowerCase,
                                      String cameraGroupId) {

        String result = null;
        Socket socket = new Socket();
        DataInputStream input = null;
        DataOutputStream output = null;

        try {
            // 连接网络
            socket.connect(new InetSocketAddress(serviceAddress, port), 5000);

            // 获取输入输出流
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            // 建立节点连接
            WvpNodeConnectResponse nodeRes = nodeConnection(input, output,
                    userID, passwordMd5, groupNameLowerCase);

            if (nodeRes == null || !nodeRes.isSuccessful) {
                return "连接失败";
            }

            // 获取相机资源的请求
            // 获取摄像头列表：ResourceType = EnumResourceTypes.ResourceList
            // Param="Safe:CameraList:用户ID"
            // 获取摄像头列表：ResourceType = EnumResourceTypes.ResourceList
            // Param="Safe:CameraList:用户ID:摄像机组ID"
            WvpGetResourceResponse getRes = null;
            if (cameraGroupId == null || cameraGroupId.length() == 0) {
                getRes = getResource(input, output,
                        "Safe:CameraList:" + userID,
                        _ResourceTypes.RESOURCELIST);
            } else {
                getRes = getResource(input, output, "Safe:CameraList:" + userID
                        + ":" + cameraGroupId, _ResourceTypes.RESOURCELIST);
            }

            if (getRes != null) {
                if (!getRes.isSuccessful) {
                    return getRes.result;
                }
            } else {
                return "获取资源失败";
            }

            // 代码走到这里就算成功获取数据了
            int cameraDataSize = Integer.parseInt(getRes.result);

            for (int i = 0; i < cameraDataSize; i++) {

                WvpDataSampleDemo sample = (WvpDataSampleDemo) WvpMessageHelper
                        .ReadMessage(input);
                if (sample != null) {
                    RemoteCamera camera = new RemoteCamera();

                    camera.camearID = new String(sample.id).trim();

                    String displayTitle = new String(sample.displayTitle)
                            .trim();
                    String[] array = displayTitle.split("\\|");
                    String cameraType = array[0];
                    camera.camearType = cameraType;
                    //根据type不同添加int type
                    if(cameraType.equals(RemoteCamera.CAMEAR_TYPE_CAMGROUP)){
                        camera.setType(0);
                    }else {
                        camera.setType(1);
                    }
                    camera.displayName = array[1];
                    cameraList.add(camera);
                }
            }
        } catch (SocketTimeoutException e) {
            result = "网络连接超时";
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            result = "网络连接异常";
        } catch (Exception e) {
            e.printStackTrace();
            result = "网络连接异常";
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 获取Map远程相机列表数据 建立socket连接 获取数据后关闭。
     *
     * @param serviceAddress
     * @param port
     * @param userID
     * @param passwordMd5
     * @return 错误信息
     */
    public String getMapRemoteCameraList(String serviceAddress, int port,
                                      String userID, String passwordMd5, String groupNameLowerCase,
                                      String cameraGroupId) {

        String result = null;
        Socket socket = new Socket();
        DataInputStream input = null;
        DataOutputStream output = null;

        try {
            // 连接网络
            socket.connect(new InetSocketAddress(serviceAddress, port), 5000);

            // 获取输入输出流
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            // 建立节点连接
            WvpNodeConnectResponse nodeRes = nodeConnection(input, output,
                    userID, passwordMd5, groupNameLowerCase);

            if (nodeRes == null || !nodeRes.isSuccessful) {
                return "连接失败";
            }

            // 获取相机资源的请求
            // 获取摄像头列表：ResourceType = EnumResourceTypes.ResourceList
            // Param="Safe:CameraList:用户ID"
            // 获取摄像头列表：ResourceType = EnumResourceTypes.ResourceList
            // Param="Safe:CameraList:用户ID:摄像机组ID"
            WvpGetResourceResponse getRes = null;
            if (cameraGroupId == null || cameraGroupId.length() == 0) {
                getRes = getResource(input, output,
                        "Safe:CameraList:" + userID,
                        _ResourceTypes.RESOURCELIST);
            } else {
                getRes = getResource(input, output, "Safe:CameraList:" + userID
                        + ":" + cameraGroupId, _ResourceTypes.RESOURCELIST);
            }

            if (getRes != null) {
                if (!getRes.isSuccessful) {
                    return getRes.result;
                }
            } else {
                return "获取资源失败";
            }

            // 代码走到这里就算成功获取数据了
            int cameraDataSize = Integer.parseInt(getRes.result);

            for (int i = 0; i < cameraDataSize; i++) {

                WvpDataSampleDemo sample = (WvpDataSampleDemo) WvpMessageHelper
                        .ReadMessage(input);
                if (sample != null) {
                    RemoteCamera camera = new RemoteCamera();

                    camera.camearID = new String(sample.id).trim();

                    String displayTitle = new String(sample.displayTitle)
                            .trim();
                    String[] array = displayTitle.split("\\|");
                    String cameraType = array[0];
                    camera.camearType = cameraType;
                    if (cameraType.equals(RemoteCamera.CAMEAR_TYPE_CAMGROUP)){
                        camera.setType(0);
                    }else {
                        camera.setType(1);
                    }
                    camera.displayName = array[1];
                    //返回的是地图界面的摄像机列表
                    mapCameraList.add(camera);
                }
            }
        } catch (SocketTimeoutException e) {
            result = "网络连接超时";
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            result = "网络连接异常";
        } catch (Exception e) {
            e.printStackTrace();
            result = "网络连接异常";
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 获取video远程相机列表数据 建立socket连接 获取数据后关闭。
     *
     * @param serviceAddress
     * @param port
     * @param userID
     * @param passwordMd5
     * @return 错误信息
     */
    public String getVideoRemoteCameraList(String serviceAddress, int port,
                                         String userID, String passwordMd5, String groupNameLowerCase,
                                         String cameraGroupId) {

        String result = null;
        Socket socket = new Socket();
        DataInputStream input = null;
        DataOutputStream output = null;

        try {
            // 连接网络
            socket.connect(new InetSocketAddress(serviceAddress, port), 5000);

            // 获取输入输出流
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            // 建立节点连接
            WvpNodeConnectResponse nodeRes = nodeConnection(input, output,
                    userID, passwordMd5, groupNameLowerCase);

            if (nodeRes == null || !nodeRes.isSuccessful) {
                return "连接失败";
            }

            // 获取相机资源的请求
            // 获取摄像头列表：ResourceType = EnumResourceTypes.ResourceList
            // Param="Safe:CameraList:用户ID"
            // 获取摄像头列表：ResourceType = EnumResourceTypes.ResourceList
            // Param="Safe:CameraList:用户ID:摄像机组ID"
            WvpGetResourceResponse getRes = null;
            if (cameraGroupId == null || cameraGroupId.length() == 0) {
                getRes = getResource(input, output,
                        "Safe:CameraList:" + userID,
                        _ResourceTypes.RESOURCELIST);
            } else {
                getRes = getResource(input, output, "Safe:CameraList:" + userID
                        + ":" + cameraGroupId, _ResourceTypes.RESOURCELIST);
            }

            if (getRes != null) {
                if (!getRes.isSuccessful) {
                    return getRes.result;
                }
            } else {
                return "获取资源失败";
            }

            // 代码走到这里就算成功获取数据了
            int cameraDataSize = Integer.parseInt(getRes.result);

            for (int i = 0; i < cameraDataSize; i++) {

                WvpDataSampleDemo sample = (WvpDataSampleDemo) WvpMessageHelper
                        .ReadMessage(input);
                if (sample != null) {
                    RemoteCamera camera = new RemoteCamera();

                    camera.camearID = new String(sample.id).trim();

                    String displayTitle = new String(sample.displayTitle)
                            .trim();
                    String[] array = displayTitle.split("\\|");
                    String cameraType = array[0];
                    camera.camearType = cameraType;
                    if (cameraType.equals(RemoteCamera.CAMEAR_TYPE_CAMGROUP)){
                        camera.setType(0);
                    }else {
                        camera.setType(1);
                    }
                    camera.displayName = array[1];
                    //返回的是地图界面的摄像机列表
                    videoCameraList.add(camera);
                }
            }
        } catch (SocketTimeoutException e) {
            result = "网络连接超时";
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            result = "网络连接异常";
        } catch (Exception e) {
            e.printStackTrace();
            result = "网络连接异常";
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


    /**
     * 获取远程相机视频数据
     *
     * @param serviceAddress
     * @param port
     * @param userID
     * @param passwordMd5
     * @param entity
     * @return
     */
    public String startWork(boolean remoteCommunicate, String serviceAddress,
                            int port, String userID, String passwordMd5,
                            String groupNameLowerCase, RemoteCamera entity) {

        this.curRemoteCamera = entity;

        String result = null;
        socket = new Socket();
        DataInputStream input = null;
        DataOutputStream output = null;

        try {
            // 连接网络
            socket.connect(new InetSocketAddress(serviceAddress, port), 5000);

            // 获取输入输出流
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            // 建立节点连接
            WvpNodeConnectResponse nodeRes = nodeConnection(input, output,
                    userID, passwordMd5, groupNameLowerCase);
            if (nodeRes == null || !nodeRes.isSuccessful) {
                result = "节点连接失败";
                stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
                return result;
            }

            WvpGetResourceResponse getRes = null;
            // 创建请求实例（手机视频数据 固定摄像头视频数据）
            if (entity.camearType.equals(RemoteCamera.CAMEAR_TYPE_MOBILE)) {
                // 获取相机资源的请求
                getRes = getResource(input, output, "Safe:Mobile:"
                        + entity.camearID, _ResourceTypes.CHANNEL);
                if (getRes == null || !getRes.isSuccessful) {
                    result = "获取资源失败";
                    stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
                    return result;
                }
            } else if (entity.camearType
                    .equals(RemoteCamera.CAMEAR_TYPE_NO_PTZ)
                    || entity.camearType.equals(RemoteCamera.CAMEAR_TYPE_PTZ)) {
                // 获取相机资源的请求
                getRes = getResource(input, output, "Safe:Live:"
                        + entity.camearID, _ResourceTypes.CHANNEL);
                if (getRes == null || !getRes.isSuccessful) {
                    result = "获取资源失败";
                    stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
                    return result;
                }
            }

            InitH264Decode();
            //  fos  = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/testHard.h264"));
            // 启动线程上传下载
            transferStart(true, output, input);
            // 开启发送gps坐标的timer

            // 根据具体需求 是否开启远程音频 文字消息发送
            if (remoteCommunicate) {
                // 实例化播放声音对象
                initAudioPlayer();

                // 实例化录音对象
                audioRecord = GetNewAudioRecorder(true, sendQueue);

                audioRecord.isEncoding = isAudioRecordOpen;
            }

            // 设置正在正常工作
            isWorking = true;

            // 开始流量计算
            // startNetTraficWorker();

            // 开始检查解码视频流超时
            startCheckDecodeBitmapWorker();

            // 全部执行完成 通知UI
            stateCallbackToUI(C.STATE_ERROR_NONE, "初始化完成");

            Log.e("RemoteCameraWorker",
                    "startWork-----------------------------");

        } catch (SocketTimeoutException e) {
            result = "网络连接超时";
            stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            result = "网络连接异常";
            stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
        } catch (Exception e) {
            e.printStackTrace();
            result = "网络连接异常";
            stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
        }
        return result;
    }

    /**
     * 获取远程相机视频数据
     *
     * @param serviceAddress
     * @param port
     * @param userID
     * @param passwordMd5
     * @param cameraId
     * @return
     */
    public String startWork(boolean remoteCommunicate, String serviceAddress,
                            int port, String userID, String passwordMd5,
                            String groupNameLowerCase, String cameraId) {

        String result = null;
        socket = new Socket();
        DataInputStream input = null;
        DataOutputStream output = null;

        try {
            // 连接网络
            socket.connect(new InetSocketAddress(serviceAddress, port), 5000);

            // 获取输入输出流
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            // 建立节点连接
            WvpNodeConnectResponse nodeRes = nodeConnection(input, output,
                    userID, passwordMd5, groupNameLowerCase);
            if (nodeRes == null || !nodeRes.isSuccessful) {
                result = "连接失败";
                stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
                return result;
            }

            WvpGetResourceResponse getRes = null;
            // 创建请求实例（手机视频数据 固定摄像头视频数据）

            // 获取相机资源的请求
            getRes = getResource(input, output, "Csv:Live:" + cameraId,
                    _ResourceTypes.CHANNEL);
            if (getRes == null || !getRes.isSuccessful) {
                result = "获取资源失败";
                stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
                return result;
            }

            InitH264Decode();
            // 启动线程上传下载
            transferStart(true, output, input);
            // 开启发送gps坐标的timer

            // 根据具体需求 是否开启远程音频 文字消息发送
            if (remoteCommunicate) {
                // 实例化播放声音对象
                initAudioPlayer();

                // 实例化录音对象
                audioRecord = GetNewAudioRecorder(true, sendQueue);

                audioRecord.isEncoding = isAudioRecordOpen;
            }

            // 设置正在正常工作
            isWorking = true;

            // 开始流量计算
            // startNetTraficWorker();

            // 开始检查解码视频流超时
            startCheckDecodeBitmapWorker();

            // 全部执行完成 通知UI
            stateCallbackToUI(C.STATE_ERROR_NONE, "初始化完成");

            Log.e("RemoteCameraWorker",
                    "startWork-----------------------------");


        } catch (SocketTimeoutException e) {
            result = "网络连接超时";
            stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            result = "网络连接异常";
            stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
        } catch (Exception e) {
            e.printStackTrace();
            result = "网络连接异常";
            stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
        }
        return result;
    }

    /**
     * 获取远程相机的录像视频数据
     *
     * @param serviceAddress
     * @param port
     * @param userID
     * @param passwordMd5
     * @param
     * @return
     */
    public String startWork(boolean remoteCommunicate, String serviceAddress,
                            int port, String userID, String passwordMd5,
                            String groupNameLowerCase, String cameraId,String beginTime,String endTime,String rate) {

        String result = null;
        socket = new Socket();
        DataInputStream input = null;
        DataOutputStream output = null;

        try {
            // 连接网络
            socket.connect(new InetSocketAddress(serviceAddress, port), 5000);

            // 获取输入输出流
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            // 建立节点连接
            WvpNodeConnectResponse nodeRes = nodeConnection(input, output,
                    userID, passwordMd5, groupNameLowerCase);
            if (nodeRes == null || !nodeRes.isSuccessful) {
                result = "连接失败";
                stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
                return result;
            }

            WvpGetResourceResponse getRes = null;
            // 创建请求实例（手机视频数据 固定摄像头视频数据）

            // 获取相机资源的请求
            getRes = getResource(input, output, "Safe:RecordStream:" + cameraId+":"+beginTime+":"+endTime+":"+rate,
                    _ResourceTypes.CHANNEL);
            if (getRes == null || !getRes.isSuccessful) {
                result = "获取资源失败";
                stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
                return result;
            }

            InitH264Decode();
            // 启动线程上传下载
            transferStart(true, output, input);
            // 开启发送gps坐标的timer

            // 根据具体需求 是否开启远程音频 文字消息发送
            if (remoteCommunicate) {
                // 实例化播放声音对象
                initAudioPlayer();

                // 实例化录音对象
                audioRecord = GetNewAudioRecorder(true, sendQueue);

                audioRecord.isEncoding = isAudioRecordOpen;
            }

            // 设置正在正常工作
            isWorking = true;

            // 开始流量计算
            // startNetTraficWorker();

            // 开始检查解码视频流超时
            startCheckDecodeBitmapWorker();

            // 全部执行完成 通知UI
            stateCallbackToUI(C.STATE_ERROR_NONE, "初始化完成");

            Log.e("RemoteCameraWorker",
                    "startWork-----------------------------");


        } catch (SocketTimeoutException e) {
            result = "网络连接超时";
            stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            result = "网络连接异常";
            stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
        } catch (Exception e) {
            e.printStackTrace();
            result = "网络连接异常";
            stateCallbackToUI(C.STATE_ERROR_CONNECTION, result);
        }
        return result;
    }

    /**
     * 获取当前发送数据量并广播出去
     *
     * @return
     */
    private void getTraficLengthPerSecond() {

        if (socketSender != null) {
            long totalLength = socketSender.getTotalSendLength();
            long traficLengthSecond = (totalLength - lastTotalTraficLength) / 1024;
            lastTotalTraficLength = totalLength;
            builder.delete(0, builder.length());
            String msg = builder.append(netType).append(traficLengthSecond)
                    .append(TRAFIC_UNIT).toString();
            // 发送每秒流量到UI
            App.sendBroadcast(C.BROADCAST_UPLOAD_TRAFIC, msg);
        }
    }

    /**
     * 设置当前使用网络类型
     *
     * @param netType
     */
    public void setNetType(String netType) {
        this.netType = netType;
    }

    /**
     * 开启timer检查流量
     */
    private void startNetTraficWorker() {

        if (netTraficTimer == null) {
            netTraficTimer = new Timer();
        }

        netTraficTask = new TimerTask() {

            @Override
            public void run() {
                getTraficLengthPerSecond();
            }
        };
        netTraficTimer.scheduleAtFixedRate(netTraficTask, 0, 1000);
    }

    /**
     * 结束timer
     */
    private void stopNetTraficWorker() {

        if (netTraficTimer != null) {
            netTraficTimer.cancel();
            netTraficTimer.purge();
            netTraficTimer = null;
            netTraficTask = null;
        }
    }

    /**
     * 给云台相机发送命令
     *
     * @param moveTypes
     * @param param
     */
    public void sendCommand(String moveTypes, String param) {
        if (curRemoteCamera == null) {
            return;
        }
        String cameraType = curRemoteCamera.camearType;
        if (cameraType.equals(RemoteCamera.CAMEAR_TYPE_PTZ)) {

            WvpControlMove wcm = new WvpControlMove();
            wcm.moveType = moveTypes;
            wcm.param = param;
            wcm.targetID = curRemoteCamera.camearID;
            wcm.tag = "";
            sendQueue.offer(wcm.toFullMessageBytes());
        }
    }

    /**
     * 给拍摄视频的移动设备发送文本消息
     *
     * @param writerId
     * @param writerNm
     * @param text
     */
    public void sendTextMessage(String writerId, String writerNm, String text) {

        WvpDataText textData = new WvpDataText();
        textData.writerID = "";
        textData.writerDisplayName = "";
        textData.targetID = "Mobile:" + curRemoteCamera.camearID;
        textData.text = text;

        String createTime = sdf.format(new Date());
        textData.createTime = "" + createTime.replace(' ', 'T');
        textData.tag = "";
        sendQueue.offer(textData.toFullMessageBytes());
    }

    /**
     * 重置audioPlayer。
     */
    public void resetAudioPlayer() {
        int streamType = AudioManager.STREAM_MUSIC;
        if (App.audioSetting.IsUseVoicePhone) {
            streamType = AudioManager.STREAM_VOICE_CALL;
        }
        audioPlayer.reset(App.audioSetting.audioStreamType,
                App.audioSetting.volumeRiseUpPosition);
    }

    /**
     * 初始化player
     */
    private void initAudioPlayer() {

        audioPlayer = new WvpAudioPlayer(receiveAudioQueue,
                App.audioSetting.volumeRiseUpPosition);
        audioPlayer.Init(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                App.audioSetting.audioStreamType);
        audioPlayer.start();
    }

    /**
     * 初始化视频解码
     */
    private void InitH264Decode() {
        if (isHardDecode) {


        } else {
            if (h264Decoder == null) {
                h264Decoder = new VideoDecoder();
                h264Decoder.init(VideoDecoder.DecoderTypeH264,
                        VideoDecoder.OutputFormatYUV420P, 1280, 720);
            }
        }
    }

    /**
     * 释放player
     */
    public void releaseAudioPlayer() {
        // 释放录音和播�?socket等所有资源�?
        if (audioPlayer != null) {
            audioPlayer.releaseAudioPlayer();
            audioPlayer = null;
        }
    }

    private void releaseAudioRecorder() {
        if (audioRecord != null) {
            try {
                audioRecord.stopMe();
            } catch (Exception e) {
                // 调用stop会抛异常
            }
            audioRecord = null;
        }
    }

    /**
     * 释放解码器 在UI界面调用   只要不退出当期界面 decoder一直不释放
     */
    public void releaseH264Decoder() {
        if (isHardDecode) {


        } else {
            if (h264Decoder != null) {
                h264Decoder.destroy();
            }
        }
    }

    /**
     * 设置是否录音
     *
     * @param record
     */
    public void setRecorderOpen(boolean record) {
        isAudioRecordOpen = record;
        if (audioRecord != null) {
            audioRecord.isEncoding = isAudioRecordOpen;
        }
    }

    /**
     * 构�?新的录音对象
     *
     * @param start         构�?成功后是否直接启动录音对�?
     * @param recorderQueue 录音数据保存的队�?
     * @return
     * @throws TimeoutException
     */
    private WvpAudioRecoder GetNewAudioRecorder(boolean start,
                                                MyLinkedBlockingQueue recorderQueue) throws TimeoutException {

        WvpAudioRecoder result = null;

        result = new WvpAudioRecoder(recorderQueue);
        result.Init(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        result.setAudioCompressMode(App.audioSetting.EncodingFormat);

        if (start) {
            result.start();
        }

        return result;
    }

    /**
     * 启动后台线程进行上传和下载工作
     *
     * @param clear 是否进行缓存清理
     */
    public void transferStart(boolean clear, DataOutputStream output,
                              DataInputStream input) {

        sendQueue = new MyLinkedBlockingQueue(null, 0);
        receiveQueue = new LinkedBlockingQueue<byte[]>();
        receiveAudioQueue = new LinkedBlockingQueue<WvpDataMediaSample>();
        receiveTextQueue = new LinkedBlockingQueue<WvpDataText>();

        // 启动发送线程
        socketSender = new SocketSender(output, sendQueue);
        socketSender.setSocketEventListener(socketEventListenner);
        socketSender.start();

        // 启动接收
        socketReceiver = new SocketReceiver(C.WVP_SAFE, input,
                2000, receiveQueue);
        socketReceiver.setSocketEventListener(socketEventListenner);
        socketReceiver.start();

        // 启动接收数据拆分
        queueSwitcher = new QueueSwitcher(receiveQueue, receiveAudioQueue,
                receiveVideoQueue, receiveTextQueue, null);

        queueSwitcher.setTextListener(textListener);
        queueSwitcher.setVideoListener(videoListener);
        queueSwitcher.setPictureListener(pictureListener);

        queueSwitcher.start();
    }

    /**
     * 停止后台线程进行上传和下载工作
     *
     *  是否进行缓存清理
     */
    public void transferStop() {

        if (socketSender != null) {
            socketSender.stopMe();

        }
        if (socketReceiver != null) {
            socketReceiver.stopMe();
        }
        if (queueSwitcher != null) {
            queueSwitcher.stopMe();
        }

        socketSender = null;
        socketReceiver = null;
        queueSwitcher = null;
    }

    /**
     * 停止工作（1，断开连接,2，停止数据上传 socketSender）
     */
    public void stopWork() {

        isWorking = false;

        if (previewThread != null) {
            previewThread.interrupt();
            previewThread = null;
        }

        stopCheckDecodeBitmapWorker();

        if (queueSwitcher != null) {
            queueSwitcher.setVideoListener(null);
        }
        // 断开录音
        releaseAudioRecorder();
        // 释放播放
        releaseAudioPlayer();
        // 停止发送流量
        stopNetTraficWorker();

        // 先停止发送对象
        transferStop();
        // 断开socket连接
        stopConnection();
        Log.e("RemoteCameraWorker", "stopWork===============================");
    }

    /**
     * 释放解码 在activity退出时调用
     */
    public void releaseDecoder() {
        if (isHardDecode) {

        } else {
            if (h264Decoder != null) {
                h264Decoder.destroy();
            }
        }
    }

    /**
     * 停止网络连接
     */
    private void stopConnection() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket = null;
            }
        }
    }

    /**
     * 发送真正的登录请求,必须在用户信息验证之后,重新连接socket
     *
     * @param userID    用户名
     * @param passwordMd5 密码
     * @param groupNameLowerCase       组
     * @return 是否登录成功
     * @throws Exception
     */
    private WvpNodeConnectResponse nodeConnection(DataInputStream input,
                                                  DataOutputStream output, String userID, String passwordMd5,
                                                  String groupNameLowerCase) throws Exception {

        WvpNodeConnectResponse response = null;
        WvpNodeConnectRequest request = new WvpNodeConnectRequest();
        request.userID = userID;
        request.password = passwordMd5;
        request.groupID = Util.getMd5(groupNameLowerCase);

        // 发送请求
        output.write(request.toFullMessageBytes());

        // 获取反馈
        response = (WvpNodeConnectResponse) WvpMessageHelper.ReadMessage(input);

        return response;
    }

    /**
     * 请求设置资源
     *
     * @param param        参数(例如"Mobile:userid")
     * @param resourceType 资源类型(参考_ResourceTypes内的定义)
     * @return
     * @throws Exception
     */
    private WvpGetResourceResponse getResource(DataInputStream input,
                                               DataOutputStream output, String param, String resourceType)
            throws Exception {

        WvpGetResourceResponse response = null;

        WvpGetResourceRequest request = new WvpGetResourceRequest();
        request.param = param;
        request.resourceType = resourceType; // _ResourceTypes.CHANNEL;

        // 发送请求
        output.write(request.toFullMessageBytes());

        // 接收回馈
        response = (WvpGetResourceResponse) WvpMessageHelper.ReadMessage(input);

        return response;
    }


    /**
     * 获取带定位信息的远程相机组列表 建立socket连接 获取数据后关闭。
     *
     * @param serviceAddress
     * @param port
     * @param userID
     * @param passwordMd5
     * @return 错误信息
     */
    public String getRemoteCameraGroupForLocationList(String serviceAddress, int port,
                                                      String userID, String passwordMd5, String groupNameLowerCase,
                                                      String cameraGroupId) {

        String result = null;
        Socket socket = new Socket();
        DataInputStream input = null;
        DataOutputStream output = null;

        try {
            // 连接网络
            socket.connect(new InetSocketAddress(serviceAddress, port), 5000);

            // 获取输入输出流
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            // 建立节点连接
            WvpNodeConnectResponse nodeRes = nodeConnection(input, output,
                    userID, passwordMd5, groupNameLowerCase);

            if (nodeRes == null || !nodeRes.isSuccessful) {
                return "连接失败";
            }
            WvpGetResourceResponse getRes = null;
            if (cameraGroupId == null || cameraGroupId.length() == 0) {
                getRes = getResource(input, output,
                        "Safe:CameraList:" + userID,
                        _ResourceTypes.RESOURCELIST);
            } else {
                getRes = getResource(input, output, "Safe:CameraList:" + userID
                        + ":" + cameraGroupId, _ResourceTypes.RESOURCELIST);
            }

            if (getRes != null) {
                if (!getRes.isSuccessful) {
                    return getRes.result;
                }
            } else {
                return "获取资源失败";
            }

            // 代码走到这里就算成功获取数据了
            int cameraDataSize = Integer.parseInt(getRes.result);

            for (int i = 0; i < cameraDataSize; i++) {

                WvpDataSampleDemo sample = (WvpDataSampleDemo) WvpMessageHelper
                        .ReadMessage(input);
                if (sample != null) {
                    String displayTitle = new String(sample.displayTitle)
                            .trim();
                    String[] array = displayTitle.split("\\|");
                    String cameraType = array[0];
                    String latLon = new String(sample.recordUserLoginName).trim();
                    //只有相机组并且经纬度不为空才获取
                    if (cameraType.equals(RemoteCamera.CAMEAR_TYPE_CAMGROUP) && latLon != null) {
                        String[] latLonArray = latLon.split("\\|");
                        double lon = Double.parseDouble(latLonArray[0]);
                        double lat = Double.parseDouble(latLonArray[1]);

                        //只有经纬度不为0才获取
                        if (lat != 0 && lon != 0) {
                            RemoteGroup group = new RemoteGroup();
                            group.camearID = new String(sample.id).trim();
                            group.displayName = array[1];
                            group.latitude = lat;
                            group.longitude = lon;

                            remoteGroups.add(group);
                        }
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            result = "网络连接超时";
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            result = "网络连接异常";
        } catch (Exception e) {
            e.printStackTrace();
            result = "网络连接异常";
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 获取远程相机略缩图
     *
     * @param serviceAddress
     * @param port
     * @param userID
     * @param passwordMd5
     * @param cameraId
     * @return
     */
    public HashMap<String,Bitmap> subImages = new HashMap<String, Bitmap>();
    public void clearSubImages(){
        subImages.clear();
    }

    public synchronized String getCameraSubImage(String serviceAddress, int port,
                                              String userID, String passwordMd5, String groupNameLowerCase,
                                              String cameraId) {
        String result = null;
        DataInputStream input = null;
        DataOutputStream output = null;
        Socket subImageSocket = new Socket();
        try {
               // 连接网络
                subImageSocket.connect(new InetSocketAddress(serviceAddress, port), 5000);

                // 获取输入输出流
                output = new DataOutputStream(subImageSocket.getOutputStream());
                input = new DataInputStream(subImageSocket.getInputStream());
            // 建立节点连接
            WvpNodeConnectResponse nodeRes = nodeConnection(input, output,
                    userID, passwordMd5, cameraId);

            if (nodeRes == null || !nodeRes.isSuccessful) {
                return "连接失败";
            }



            WvpGetResourceResponse getRes = null;
            getRes = getResource(input, output, "Safe:SubCamImg:" + cameraId, _ResourceTypes.FILE);

            if (getRes != null) {
                if (getRes.isSuccessful) {
                    WvpDataMediaSample mediaSample = (WvpDataMediaSample) WvpMessageHelper.ReadMessage(input);
                    //subImageArray = mediaSample.data;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(mediaSample.data,0,mediaSample.data.length);
                    subImages.put(cameraId,bitmap);
                }else {
                    return getRes.result;
                }
            } else {
                return "获取资源失败";
            }

        } catch (SocketTimeoutException e) {
            result = "网络连接超时";
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            result = "网络连接异常";
        } catch (Exception e) {
            e.printStackTrace();
            result = "网络连接异常";
        } finally {
           if (subImageSocket != null){
               try {
                   subImageSocket.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
        }
        return result;
    }


    /**
     * 获取远程摄像头的预位置列表 建立socket连接 获取数据后关闭。
     *
     * @param serviceAddress
     * @param port
     * @param userID
     * @param passwordMd5
     * @return 错误信息
     */
    public String getRemoteCameraPositionList(String serviceAddress, int port,
                                                      String userID, String passwordMd5, String groupNameLowerCase,
                                                      String cameraId) {

        String result = null;
        Socket socket = new Socket();
        DataInputStream input = null;
        DataOutputStream output = null;

        try {
            // 连接网络
            socket.connect(new InetSocketAddress(serviceAddress, port), 5000);

            // 获取输入输出流
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());


            // 建立节点连接
            WvpNodeConnectResponse nodeRes = nodeConnection(input, output,
                    userID, passwordMd5, groupNameLowerCase);

            if (nodeRes == null || !nodeRes.isSuccessful) {
                return "连接失败";
            }
            WvpGetResourceResponse getRes = null;
            getRes = getResource(input, output, "Safe:PtzPresetList:" + cameraId, _ResourceTypes.RESOURCELIST);

            if (getRes != null) {
                // 代码走到这里就算成功获取数据了
                int count = Integer.parseInt(getRes.result);

                for (int i = 0; i < count; i++) {

                    WvpDataKeyValue  data = (WvpDataKeyValue) WvpMessageHelper.ReadMessage(input);
                    if (data != null) {
                        CameraPosition cameraPosition = new CameraPosition();
                        cameraPosition.setPositionKey(data.Key);
                        cameraPosition.setPositionValue(data.Value);
                        cameraPositions.add(cameraPosition);
                    }
                }
              /*  if (!getRes.isSuccessful) {
                    return getRes.result;
                }*/
            } else {
                return "获取资源失败";
            }


        } catch (SocketTimeoutException e) {
            result = "网络连接超时";
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            result = "网络连接异常";
        } catch (Exception e) {
            e.printStackTrace();
            result = "网络连接异常";
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    /**
     * 清空预位置列表
     */
    public void clearPresetList() {
        cameraPositions.clear();
    }

    /**
     * 获取远程摄像头的录像列表 建立socket连接 获取数据后关闭。
     *
     * @param serviceAddress
     * @param port
     * @param userID
     * @param passwordMd5
     * @return 错误信息
     */
    public String getCameraRecordList(String serviceAddress, int port,
                                              String userID, String passwordMd5, String groupNameLowerCase,
                                              String cameraId,String beginTime,String endTime) {

        String result = null;
        Socket socket = new Socket();
        DataInputStream input = null;
        DataOutputStream output = null;

        try {
            // 连接网络
            socket.connect(new InetSocketAddress(serviceAddress, port), 5000);

            // 获取输入输出流
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());


            // 建立节点连接
            WvpNodeConnectResponse nodeRes = nodeConnection(input, output,
                    userID, passwordMd5, groupNameLowerCase);

            if (nodeRes == null || !nodeRes.isSuccessful) {
                return "连接失败";
            }
            WvpGetResourceResponse getRes = null;
            getRes = getResource(input, output, "Safe:RecordList:" + cameraId+":"+beginTime+":"+endTime,
                    _ResourceTypes.RESOURCELIST);

            if (getRes != null) {
                // 代码走到这里就算成功获取数据了
                int count = Integer.parseInt(getRes.result);

                for (int i = 0; i < count; i++) {

                    WvpDataKeyValue  data = (WvpDataKeyValue) WvpMessageHelper.ReadMessage(input);
                    if (data != null) {
                        CameraRecord cameraRecord = new CameraRecord();
                        cameraRecord.setRecordKey(data.Key);
                        String[] valueArray = data.Value.split("\\|");
                        cameraRecord.setBeginTime(valueArray[0]);
                        cameraRecord.setEndTime(valueArray[1]);
                        if (valueArray.length>2) {
                            cameraRecord.setDescription(valueArray[2]);
                        }
                        cameraRecords.add(cameraRecord);
                    }
                }
              /*  if (!getRes.isSuccessful) {
                    return getRes.result;
                }*/
            } else {
                return "获取资源失败";
            }


        } catch (SocketTimeoutException e) {
            result = "网络连接超时";
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            result = "网络连接异常";
        } catch (Exception e) {
            e.printStackTrace();
            result = "网络连接异常";
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    /**
     * 清空录像列表
     */
    public void clearCameraRecordList() {
        cameraRecords.clear();
    }


}
