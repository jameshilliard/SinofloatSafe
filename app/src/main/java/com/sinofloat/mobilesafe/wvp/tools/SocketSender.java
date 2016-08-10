package com.sinofloat.mobilesafe.wvp.tools;

import java.io.DataOutputStream;
import java.io.IOException;

import sinofloat.wvp.core.interfaces.ISocketEventListener;

/**
 * Socket 发送数据对象
 * 
 * @author 章晓宇
 * @version 1.1
 */
public class SocketSender extends Thread {

	/**
	 * Socket事件监听器
	 */
	private ISocketEventListener m_SocketEventListener = null;

	/**
	 * 标记是否正在工作
	 */
	public boolean isWorking = false;

	// 数据输出流
	private DataOutputStream m_DataOutputStream = null;

	/**
	 * 发送队列
	 */
	public MyLinkedBlockingQueue m_SendQueue = null;

	/**
	 * 总流量
	 */
	private long m_totalSendLength = 0;

	/**
	 * 获取当前应用启动后总发送流量
	 * 
	 * @return
	 */
	public long getTotalSendLength() {
		return m_totalSendLength;
	}

	/**
	 * 实例化
	 * 
	 * @param outputStream
	 *            发送数据流
	 * @param sendQueue
	 *            发送队列
	 */
	public SocketSender(DataOutputStream outputStream,
			MyLinkedBlockingQueue sendQueue) {
		m_DataOutputStream = outputStream;
		m_SendQueue = sendQueue;
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
	 * 启动发送线程
	 */
	public void run() {

		super.run();

		isWorking = true;

		try {

			while (isWorking) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
				byte[] sendData = m_SendQueue.take();
				if (sendData != null) {

					Exception exception = sendMessage(sendData);
					m_totalSendLength += sendData.length;

					if (exception != null) {

						String exceptionMsg = exception.getMessage();
						if (exceptionMsg != null
								&& exceptionMsg.equals("Socket closed")) {
							return;
						}

						if (m_DataOutputStream != null) {
							try {
								m_DataOutputStream.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						if (m_SocketEventListener != null) {
							m_SocketEventListener
									.OnDisconnect(this, "sender异常");

						}
					}else {
						//回调一个数据包已经发送完毕 FIXME 值班考勤需要这个结果
						
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
//		m_SendQueue.clear();
		isWorking = false;
	}

	/**
	 * 往Socket写数据
	 * 
	 * @param message
	 * @throws Exception
	 *             任富 2013-09-17改过后的方法。在finally里关闭流。
	 */
	public Exception sendMessage(byte[] message) {
		Exception exception = null;
		try {
			m_DataOutputStream.write(message, 0, message.length);
			m_DataOutputStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
			exception = e;
			// System.out.println("sendMessage（） exception-------- "+e.getMessage());
		}
		return exception;
	}

}
