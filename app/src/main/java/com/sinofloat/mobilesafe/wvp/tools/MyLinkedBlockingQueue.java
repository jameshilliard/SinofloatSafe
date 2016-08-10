package com.sinofloat.mobilesafe.wvp.tools;

import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

import com.sinofloat.mobilesafe.base.C;
import com.sinofloat.mobilesafe.wvp.OnWorkStateListener;

import sinofloat.wvp.messages.WvpDataMediaSample;
import sinofloat.wvp.messages.WvpMessageHead;
import sinofloat.wvp.messages._MessageTypes;
import sinofloat.wvp.messages._WvpMediaMessageTypes;
import sinofloat.wvp.messages._WvpMessageTypes;

/**
 * 发送数据到服务器 专门添加byte[]数组 作为发送队列使用。 项目中接收队列 元素是MediaDataSample对象 添加 取出 队列里元素时
 * 计算队列里元素数据总长度
 * 
 * 只要是数据超过设定的长度 listener回调一次后就设为null（数据超过预定长度 监听值调用一次）
 * 
 * @author staid
 * 
 */
public class MyLinkedBlockingQueue {

	private static final String TAG = "MyLinkedBlockingQueue";
	
	/**
	 * 入队的音频数据包个数
	 */
	private int audioPackageDatacount;
	/**
	 * 入队的视频数据包个数
	 */
	private int videoPackageDatacount;
	/**
	 * 入队的文本数据包个数
	 */
	private int textPackageDatacount;
	/**
	 * 入队的JPEG图片数据包个数
	 */
	private int picPackageDatacount;
	
	
	/**
	 * 自定义 队列里存放最大数据长度 这个值由UI层传递过来
	 */
	private int maxQueueDataLength;//
	/**
	 * 系统队列
	 */
	private LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();

	/**
	 * 队列里所有数据总长度
	 */
	public long queueLength;

	/**
	 * 队列元素个数（长度）
	 */
	public int queueSize;

	/**
	 * 工作状态监听回调（队列里的数据长度是否超过上限 来确定是否网络不好 或者断网 等异常）
	 */
	private OnWorkStateListener workStateListener;

	/**
	 * 
	 * @param workStateListener
	 *            数据状态监听
	 * @param queueMaxLength
	 *            队列容许的最大长度（超过做大长度 算网络异常）
	 */
	public MyLinkedBlockingQueue(OnWorkStateListener workStateListener,
			int queueMaxLength) {
		this.workStateListener = workStateListener;
		this.maxQueueDataLength = queueMaxLength;
		// FIXME 有些地方传过来的最大长度值参数为0 那么就给它赋值为1M 这个值应该是UI那边确定（视频1M 拍照3M 其他都是0）
		if (maxQueueDataLength == 0) {
			maxQueueDataLength = 1024 * 1024;
		}
	}

	/**
	 * 设置监听 可能会传null过来
	 * 
	 * @param workStateListener
	 */
	public void setWorkStateListener(OnWorkStateListener workStateListener) {
		this.workStateListener = workStateListener;
	}

	/**
	 * 调节最大队列长度大小（调大或者调小）
	 */
	public void updateMaxQueueLength(double factor) {

		maxQueueDataLength *= factor;
	}

	/**
	 * 加入队列并且累加数据长度 如果数据超过最大值 那就不再填进队列 需要根据情况
	 * @param data
	 */
	public synchronized void offer(byte[] data) {

		if (data != null && data.length > 0) {

			if (workStateListener == null) {
				queue.add(data);
			} else {
				if (queueLength < maxQueueDataLength) {
					if (queue.add(data)) {

						if (WvpMessageHead.getMessageType(data) == _WvpMessageTypes.MEDIA) {
							int mediaType = WvpDataMediaSample.getMediaSampleType(
									WvpMessageHead.WVP_MSG_BODY_OFFSET, data);

							switch (mediaType) {
							case _WvpMediaMessageTypes.PICTUREJPEG:
								
								picPackageDatacount++;
								break;
							case _WvpMediaMessageTypes.VIDEOH264:
								
								videoPackageDatacount++;
								break;
							default :// 全是音频
								
								audioPackageDatacount++;
								break;
							}
						}
						
						queueSize += 1;
						queueLength += data.length;
						// Log.e(TAG, "队列长度============" + queue.size());
						// 如果当前队列里元素长度大于预设的最大长度 那么我们认为是网络异常 按照断网处理
						if (queueLength >= maxQueueDataLength) {
							workStateListener.onWorkState(
									C.STATE_ERROR_CONNECTION,
									"网络异常，数据超过预设长度");
							// 回调执行后设为null 不再回调。 再次点击录像按钮会重新new这个实例
							workStateListener = null;
						}
					}
				}
			}
		}
	}

	/**
	 * 从队列取出元素 并减去取出的元素数据长度
	 */
	public synchronized byte[] take() {

		byte[] data = null;

		if (queue != null && queue.size() > 0) {

			try {
				data = queue.take();
				if (data != null && data.length > 0) {
					
					if (WvpMessageHead.getMessageType(data) == _WvpMessageTypes.MEDIA) {
						int mediaType = WvpDataMediaSample.getMediaSampleType(
								WvpMessageHead.WVP_MSG_BODY_OFFSET, data);

						switch (mediaType) {
						case _WvpMediaMessageTypes.PICTUREJPEG:
							
							picPackageDatacount--;
							break;
						case _WvpMediaMessageTypes.VIDEOH264:
							
							videoPackageDatacount--;
							break;
						default :// 全是音频
							
							audioPackageDatacount--;
							break;
						}
					}
					queueSize -= 1;
					queueLength -= data.length;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	/**
	 * @return
	 */
	public byte[] peek() {

		byte[] data = null;

		if (queue != null && queue.size() > 0) {
			data = queue.peek();
			if (data != null && data.length > 0) {
				queueLength -= data.length;
			}
		}
		return data;
	}

	public void clear() {
		if (queue != null) {
			queue.clear();
		}
		queueLength = 0;
		queueSize = 0;
		
		audioPackageDatacount = 0;
		videoPackageDatacount = 0;
		textPackageDatacount = 0;
		picPackageDatacount = 0;
	}

	/**
	 * 队列长度
	 * 
	 * @return
	 */
	public int size() {
		if (queue != null) {
			return queue.size();
		}
		return 0;
	}
	
	/**
	 * 已入队的视频数据包
	 * @return
	 */
	public int getVideoPackageDataCount(){
		return videoPackageDatacount;
	}
	
	/**
	 * 已入队的视频消息数据包
	 * @return
	 */
	public int getAudioPackageDataCount(){
		return audioPackageDatacount;
	}
	
	/**
	 * 已入队的图片消息数据包
	 * @return
	 */
	public int getPicPackageDataCount(){
		return picPackageDatacount;
	}
	
	/**
	 * 已入队的文本消息数据包
	 * @return
	 */
	public int getTextPackageDataCount(){
		return textPackageDatacount;
	}

}
