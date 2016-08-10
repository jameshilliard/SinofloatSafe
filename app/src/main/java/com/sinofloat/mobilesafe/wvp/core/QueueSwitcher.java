package com.sinofloat.mobilesafe.wvp.core;

import com.sinofloat.mobilesafe.wvp.SwitchWorkListener;

import java.util.concurrent.LinkedBlockingQueue;

import sinofloat.wvp.messages.WvpDataHeartbeat;
import sinofloat.wvp.messages.WvpDataMediaSample;
import sinofloat.wvp.messages.WvpDataText;
import sinofloat.wvp.messages.WvpMessage;
import sinofloat.wvp.messages.WvpMessageHead;
import sinofloat.wvp.messages.WvpMessageHelper;
import sinofloat.wvp.messages._WvpMediaMessageTypes;
import sinofloat.wvp.messages._WvpMessageTypes;
import sinofloat.wvp.tools.ApplicationException;

/**
 * 拆分数据接收队列
 * 
 * @author 章晓�?
 * @version 1.1
 * 
 */
public class QueueSwitcher extends Thread {

	
	private SwitchWorkListener m_AudioListener = null;
	private SwitchWorkListener m_VideoListener = null;
	private SwitchWorkListener m_PictureListener = null;
	private SwitchWorkListener m_TextListener = null;
	private SwitchWorkListener m_HeartbeatListener = null;
	private SwitchWorkListener m_keyValueListener = null;
	private SwitchWorkListener m_CommandListener = null;
	

	public LinkedBlockingQueue<byte[]> m_ReceiveQueue = null;

	public LinkedBlockingQueue<WvpDataMediaSample> m_ReceiveAudioQueue = null;
	public LinkedBlockingQueue<WvpDataMediaSample> m_ReceiveVideoQueue = null;
	public LinkedBlockingQueue<WvpDataText> m_ReceiveTextQueue = null;
	public LinkedBlockingQueue<WvpDataHeartbeat> m_ReceiveHeartbeatQueue = null;

	/**
	 * 标记是否正在工作
	 */
	public boolean isWorking = false;

	/**
	 * 实例�?
	 * 
	 * @param receiveQueue
	 *            接收数据的队�?
	 * @param audioQueue
	 *            拆分后用于保存音频数据的队列
	 * @param videoQueue
	 *            拆分后用于保存视频数据的队列
	 * @param txtQueue
	 *            拆分后用于保存文字内容的队列
	 */
	public QueueSwitcher(LinkedBlockingQueue<byte[]> receiveQueue,
			LinkedBlockingQueue<WvpDataMediaSample> audioQueue,
			LinkedBlockingQueue<WvpDataMediaSample> videoQueue,
			LinkedBlockingQueue<WvpDataText> txtQueue,
			LinkedBlockingQueue<WvpDataHeartbeat> heartbeatQueue) {
		m_ReceiveQueue = receiveQueue;
		m_ReceiveAudioQueue = audioQueue;
		m_ReceiveVideoQueue = videoQueue;
		m_ReceiveTextQueue = txtQueue;
		m_ReceiveHeartbeatQueue = heartbeatQueue;
	}

	public void setAudioListener(SwitchWorkListener listener) {
		m_AudioListener = listener;
	}

	public void setVideoListener(SwitchWorkListener listener) {
		m_VideoListener = listener;
	}
	
	public void setPictureListener(SwitchWorkListener listener) {
		m_PictureListener = listener;
	}

	public void setTextListener(SwitchWorkListener listener) {
		m_TextListener = listener;
	}

	public void setHeartbeatListener(SwitchWorkListener listener) {
		m_HeartbeatListener = listener;
	}
	
	
	public void setKeyValueListener(SwitchWorkListener listener) {
		m_keyValueListener = listener;
	}
	
	public void setCommandListener(SwitchWorkListener listener) {
		m_CommandListener = listener;
	}
	/**
	 * 停止线程
	 */
	public void stopMe() {
		isWorking = false;
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (m_ReceiveQueue != null) {
			m_ReceiveQueue.clear();
		}
		if (m_ReceiveAudioQueue != null) {
			m_ReceiveAudioQueue.clear();
		}
		if (m_ReceiveVideoQueue != null) {
			m_ReceiveVideoQueue.clear();
		}
		if (m_ReceiveTextQueue != null) {
			m_ReceiveTextQueue.clear();
		}

	}

	@Override
	public void run() {

		isWorking = true;
		while (isWorking) {

			byte[] receiveData = null;
			try {
				receiveData = m_ReceiveQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			WvpMessage msg = null;
			try {
				msg = WvpMessageHelper.ConvertToWvpMessage(receiveData);
			} catch (ApplicationException e) {
				e.printStackTrace();
			}
			short sessionId = WvpMessageHead.getSessionId(receiveData);

			if(msg == null){
				continue;
			}
			
			switch (msg.messageType) {
			case _WvpMessageTypes.MEDIA:

				WvpDataMediaSample mediaSample = (WvpDataMediaSample) msg;

				switch (mediaSample.mediaMessageTypes) {
				// 视频 
				case _WvpMediaMessageTypes.VIDEOH264:

//					if (m_ReceiveVideoQueue != null){
//						m_ReceiveVideoQueue.add(mediaSample);
//					}

//					Log.e("QueueSwitcher", "读取到服务器返回的视频数据");
					if (m_VideoListener != null) {
						m_VideoListener.OnFindMessage(mediaSample,sessionId);
					}
					break;
				case _WvpMediaMessageTypes.PICTUREJPEG:
					if(m_PictureListener != null){
						m_PictureListener.OnFindMessage(mediaSample,sessionId);
					}
					break;
				case _WvpMediaMessageTypes.AUDIOPCM8K: // 8K的PCM直接保存
				case _WvpMediaMessageTypes.AUDIOG711: // 如果是G.711则首先转换成PCM音频
				case _WvpMediaMessageTypes.AudioAMR102:
				case _WvpMediaMessageTypes.AudioAMR122:
				case _WvpMediaMessageTypes.AudioAMR475:
				case _WvpMediaMessageTypes.AudioAMR515:
				case _WvpMediaMessageTypes.AudioAMR59:
				case _WvpMediaMessageTypes.AudioAMR67:
				case _WvpMediaMessageTypes.AudioAMR74:
				case _WvpMediaMessageTypes.AudioAMR795:
					mediaSample.data = mediaSample.data; // ****** 等待处理!!
					
					if (m_ReceiveAudioQueue != null){
						m_ReceiveAudioQueue.add(mediaSample);
					}
					
					if (m_AudioListener != null) {
						m_AudioListener.OnFindMessage(mediaSample,sessionId);
					}
					break;
				}

				break;
			case _WvpMessageTypes.TEXT: // 文字

				if (m_ReceiveTextQueue != null){
					m_ReceiveTextQueue.add((WvpDataText) msg);
				}

				if (m_TextListener != null) {
					m_TextListener.OnFindMessage(msg,sessionId);
				}
				break;

			case _WvpMessageTypes.HEARTBEAT: // 心跳
				if (m_ReceiveHeartbeatQueue != null)
					m_ReceiveHeartbeatQueue.add((WvpDataHeartbeat) msg);

				if (m_HeartbeatListener != null)
					m_HeartbeatListener.OnFindMessage(msg,sessionId);

				break;
			case _WvpMessageTypes.KEYVALUE:
				if(m_keyValueListener != null){
					m_keyValueListener.OnFindMessage(msg, sessionId);
				}
				break;
			case _WvpMessageTypes.CONTROLACTION:
				if(m_CommandListener != null){
					m_CommandListener.OnFindMessage(msg, sessionId);
				}
				break;
			}
		}
	}



//	int count = 0;
}
