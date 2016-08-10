package com.sinofloat.mobilesafe.wvp.tools;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import com.sinofloat.mobilesafe.base.C;
import sinofloat.wvp.core.interfaces.ISocketEventListener;
import sinofloat.wvp.messages.WvpMessageHead;

/**
 * Socket 接收对象
 * 
 * @author 章晓宇
 * @version 1.1
 * 
 */
public class SocketReceiver extends Thread {

	private static final String TAG = "SocketReceiver";
	/**
	 * 使用的是哪个功能模块。（拍照 视频 遥控 安防）
	 */
	private String modualName;
	
	/**
	 * 当前时间毫秒数（执行while循环的次数）
	 */
	private int timeCount;
	/**
	 * 超时5秒
	 */
	private int TIME_OUT = 10000;
	/**
	 * Socket事件监听器
	 */
	private ISocketEventListener m_SocketEventListener = null;

	/**
	 * 标记是否正在工作
	 */
	public boolean isWorking = false;

	// 接收数据使用的流
	private DataInputStream m_DataInputStream = null;

	/**
	 * 接收队列的存储上限
	 */
	private int m_receiveQueueMaxSize = 2000;

	/**
	 * 接收队列
	 */
	public LinkedBlockingQueue<byte[]> m_ReceiveQueue = null;

	/**
	 * 接收对象
	 * 
	 * @param inputStream
	 * @param receiveMaxSize
	 *            缓存最大值
	 * @param receiveQueue
	 */
	public SocketReceiver(String modualName,DataInputStream inputStream, int receiveMaxSize,
			LinkedBlockingQueue<byte[]> receiveQueue) {
		this.modualName = modualName;
		m_DataInputStream = inputStream;
		m_receiveQueueMaxSize = receiveMaxSize;
		m_ReceiveQueue = receiveQueue;
	}

	/**
	 * 设置事件监听
	 */
	public void setSocketEventListener(ISocketEventListener listener) {
		m_SocketEventListener = listener;
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
	}

	/**
	 * 启动接收线程
	 */
	public void run() {

		isWorking = true;

		try {

			while (isWorking) {

				Thread.sleep(1);

				byte[] receiveBuffer = readMessage();
				// FIXME 这句话原来有 逻辑不合理
				// if (receiveBuffer == null)
				// break;

				// 加入缓冲队列
				try {

					// 判断队列是否已经达到最大上线,如果达到最大值,则删除第一个
					if (m_ReceiveQueue.size() > m_receiveQueueMaxSize) {
						m_ReceiveQueue.take();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (receiveBuffer != null) {
					m_ReceiveQueue.offer(receiveBuffer);
//					Log.e(TAG, "接收队列长度============" + m_ReceiveQueue.size());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (m_SocketEventListener != null){
				m_SocketEventListener.OnDisconnect(this, "receiver异常");
			}
		}

		m_ReceiveQueue.clear();

		isWorking = false;
	}

	/**
	 * 读取一个消息
	 * 
	 * @return
	 */
	private byte[] readMessage() {
		byte[] result = null;

		try {
			// 读取消息头
			byte[] headBuffer = new byte[WvpMessageHead.WVP_MSG_HEAD_LENGTH];
			m_DataInputStream.readFully(headBuffer);

			// 获取正文长度
			int bodyLength = WvpMessageHead.getMessageBodyLength(headBuffer);

			// 完整消息
			result = new byte[headBuffer.length + bodyLength];

			// 读取消息体
			m_DataInputStream.readFully(result, headBuffer.length, bodyLength);

			// 重新合并消息头和消息体
			System.arraycopy(headBuffer, 0, result, 0, headBuffer.length);

		} catch (IOException e) {
			//java.io.EOFException 是正常的读取 没有读到数据的异常 项目认为它是正常的异常。
			//但是 如果服务器断开连接 也是走这个异常 所以 这个异常需要特别判断下
			//如果是移动安防模块 那就抛出异常
			if(modualName != null && modualName.equals(C.WVP_SAFE)){
				timeCount++;
				if(timeCount >= TIME_OUT){
					if (m_SocketEventListener != null){
						m_SocketEventListener.OnDisconnect(this, "无信号");
					}
				}
			}
			
		}

		return result;
	}
}
