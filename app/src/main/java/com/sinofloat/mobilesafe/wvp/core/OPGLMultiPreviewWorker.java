package com.sinofloat.mobilesafe.wvp.core;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.util.Log;

import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.base.C;
import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;
import com.sinofloat.mobilesafe.widget.opengl.DisplayView;
import com.sinofloat.mobilesafe.wvp.OnVideoFrameDecodedCallback;
import com.sinofloat.mobilesafe.wvp.OnWorkStateListener;
import com.sinofloat.mobilesafe.wvp.SwitchWorkListener;
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
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import sinofloat.wvp.core.VideoDecoder;
import sinofloat.wvp.core.interfaces.ISocketEventListener;
import sinofloat.wvp.messages.WvpControlMove;
import sinofloat.wvp.messages.WvpDataMediaSample;
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
 * 多线程 使用openGl 渲染多画面
 * 
 * 需要把 NUMBER 路视频解码数据返回到UI UI中使用OPENGL的View进行渲染
 * 
 * @author staid
 * 
 */
public class OPGLMultiPreviewWorker {

	private static final String TAG = "NEW_VIDEO_DECODER";

	/**
	 * 检查解码超时 时间间隔为3秒。
	 */
	private static final int CHECK_TIME_INTERVAL = 3000;

	/**
	 * 超时时间最大值 9秒
	 */
	private static int TIME_OUT_DECODE_BITMAP = 9 * 1000;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
			Locale.CHINA);

	private VideoDecoder h264Decoder;

	/**
	 * 远程相机对象Bean
	 */
	private RemoteCamera curRemoteCamera;

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
	 * 当前超时计时。
	 */
	private int nowTimeCount = 0;
	
	/**
	 * 回调给UI的（发送数据过程出现异常 回调给UI UI相关元素设置为初始状态）
	 */
	private OnWorkStateListener workStateListener;

	/**
	 * 视频解码回调接口
	 */
	private OnVideoFrameDecodedCallback videoDecodedFrameCallback;

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

	private DisplayView displayView;

	/**
	 * 构造方法
	 */
	public OPGLMultiPreviewWorker() {
		InitH264Decode();
	}

	/**
	 * 设置视频解码回调
	 * 
	 * @param videoDecodedFrameCallback
	 */
	public void setVideoDecodedFrameCallback(
			OnWorkStateListener workstateListener,
			OnVideoFrameDecodedCallback videoDecodedFrameCallback,
			DisplayView displayView) {
		this.displayView = displayView;
		this.workStateListener = workstateListener;
		this.videoDecodedFrameCallback = videoDecodedFrameCallback;
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
	 * 远程监控视频数据回调接口
	 */
	public SwitchWorkListener videoListener = new SwitchWorkListener() {

		@Override
		public void OnFindMessage(WvpMessage message, short sessionId) {

			WvpDataMediaSample frame = (WvpDataMediaSample) message;

			if (frame.data != null && frame.data.length > 0) {

				// long last = System.currentTimeMillis();

				if (h264Decoder.decode(frame.data, frame.data.length,
						frameBuffer, frameBuffer.length) != -1) {

					nowTimeCount = 0;
					// long now = System.currentTimeMillis();

					// Log.e(Thread.currentThread().getName()
					// + "新解码库decode time $$$$$$$$$$$$$$",
					// "decode time===" + (now - last));
					if (videoDecodedFrameCallback != null) {
						videoDecodedFrameCallback.onDecodedResult(displayView,
								true, frameBuffer, h264Decoder.frameWidth(),
								h264Decoder.frameHeight());
					}
				}
			}
		}
	};

	/**
	 * 图片数据回调接口（手机与手机视频对讲）
	 */
	public SwitchWorkListener pictureListener = new SwitchWorkListener() {

		@Override
		public void OnFindMessage(WvpMessage message, short sessionId) {

			WvpDataMediaSample frame = (WvpDataMediaSample) message;

			if (h264Decoder.decode(frame.data, frame.data.length, frameBuffer,
					frameBuffer.length) != -1) {
				
				nowTimeCount = 0;

				if (videoDecodedFrameCallback != null) {
					videoDecodedFrameCallback.onDecodedResult(displayView,
							true, frameBuffer, h264Decoder.frameWidth(),
							h264Decoder.frameHeight());
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

				if (nowTimeCount >= TIME_OUT_DECODE_BITMAP) {
					// 如果超时 停止预览 停止一切工作
					// stateCallbackToUI(C.STATE_TIME_OUT_DECODE_BITMAP,
					// "无信号");
					if (videoDecodedFrameCallback != null) {
						videoDecodedFrameCallback.onDecodeTimeOut(displayView);
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

		// 设置正在正常工作
		isWorking = true;

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
	public void InitH264Decode() {

		if (h264Decoder == null) {
			h264Decoder = new VideoDecoder();
			h264Decoder.init(VideoDecoder.DecoderTypeH264,
					VideoDecoder.OutputFormatYUV420P, 0, 0);
		}

		// h264Decoder.setBitmapCalback(bitmapCallbackListener);
		// h264Decoder.initH264Decoder();
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
	 * 释放解码器
	 */
	public void releaseH264Decoder() {

		if (h264Decoder != null) {
			h264Decoder.destroy();
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
	 * @param start
	 *            构�?成功后是否直接启动录音对�?
	 * @param recorderQueue
	 *            录音数据保存的队�?
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
	 * @param clear
	 *            是否进行缓存清理
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
	 *是否进行缓存清理
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

		releaseH264Decoder();

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
		if (h264Decoder != null) {
			h264Decoder.destroy();
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
	 * @param userID
	 *            用户名
	 * @param passwordMd5
	 *            密码
	 * @param groupNameLowerCase
	 *            组
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
	 * @param param
	 *            参数(例如"Mobile:userid")
	 * @param resourceType
	 *            资源类型(参考_ResourceTypes内的定义)
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

}
