package com.sinofloat.mobilesafe.wvp.core;

import com.sinofloat.mobilesafe.base.C;
import com.sinofloat.mobilesafe.wvp.OnWorkStateListener;
import com.sinofloat.mobilesafe.wvp.tools.MyLinkedBlockingQueue;
import java.io.FileNotFoundException;
import java.io.IOException;
import sinofloat.wvp.core.WPFFileWriter;


/**
 * 本地存储 对象
 * 
 * 操作： 从队列取出数据 写入到SD卡 专供拍照模式调用 不管拍多少张照片 音频文件只有一个。
 * 
 * @author staid
 * 
 */
public class LocalStorageWorker extends Thread {

	/**
	 * 是否写入时出现异常
	 */
	private boolean writeError = false;

	/**
	 * 是否在工作
	 */
	private boolean isWorking = true;

	/**
	 * 文件目录（图片目录还是视频目录）
	 */
	private String mediaFileDir;
	
	/**
	 * 图片存储路径
	 */
	private String pictureFileDir;

	/**
	 * 当前登录用户
	 */
	private String loginName;
	
	private short videoWidth, videoHeight;

	/**
	 * 媒体字节数组队列
	 */
	private MyLinkedBlockingQueue dataQueue;

	/**
	 * 私有文件格式音频写入对象
	 */
	private WPFFileWriter wvpfWriter;

	private OnWorkStateListener localStorageWorkListener;

	
	/**
	 * 图片和声音 （拍照上传模式调用）
	 * @param dataQueue
	 * @param localStorageWorkListener
	 * @param mediaFileDir
	 * @param pictureDir
	 * @param loginName
	 */
	public LocalStorageWorker(MyLinkedBlockingQueue dataQueue,
			OnWorkStateListener localStorageWorkListener, String mediaFileDir,
			String pictureDir, String loginName,short videoWidth,short videoHeight) {

		this.dataQueue = dataQueue;
		this.localStorageWorkListener = localStorageWorkListener;
		this.mediaFileDir = mediaFileDir;
		this.pictureFileDir = pictureDir;
		this.loginName = loginName;
		this.videoWidth = videoWidth;
		this.videoHeight = videoHeight;
	}

	/**
	 * 状态回调到worker worker再回调到UI；
	 * 
	 * @param state
	 *            状态
	 * @param extra
	 *            文字描述
	 */
	private void stateCallbackToUI(int state, String extra) {

		if (localStorageWorkListener != null) {
			localStorageWorkListener.onWorkState(state, extra);
		}
	}

	/**
	 * 线程是否在执行
	 * 
	 * @return
	 */
	public boolean isWorking() {
		return isWorking;
	}

	/**
	 * 
	 * 写文件头和体
	 * 
	 * @param data
	 *            文件路径 创建文件
	 */
	private void writeMediaSample(byte[] data) {

		// 当前是图片上传 需要创建一个音频写入对象和若干个图片写入对象（每个图片存为一个文件）
		try {
			if (wvpfWriter == null) {

				wvpfWriter = new WPFFileWriter(mediaFileDir, pictureFileDir);
				wvpfWriter.WriteHeader(loginName, videoWidth, videoHeight);
			}
			// 不需要写入结束标记
			if (wvpfWriter != null) {
				wvpfWriter.writeMediaSampleByteData(data);
			}
		} catch (FileNotFoundException e) {

			isWorking = false;
			e.printStackTrace();
			stateCallbackToUI(C.STATE_ERROR_FILE_WRITE,
					"文件写入异常" + e.getMessage());
		} catch (IOException e) {

			isWorking = false;
			e.printStackTrace();
			stateCallbackToUI(C.STATE_ERROR_FILE_WRITE,
					"文件写入异常" + e.getMessage());
		}

	}

	/**
	 * 获取当前队列元素个数
	 * 
	 * @return
	 */
	public int getCurQueueSize() {
		if (dataQueue != null) {
			return dataQueue.size();
		}
		return 0;
	}

	/**
	 * 停止线程
	 */
	public void stopMe() {

		isWorking = false;
	}

	@Override
	public void run() {

		try {
			while (isWorking && !writeError) {

				if (dataQueue != null && dataQueue.size() > 0) {

					byte[] data = null;

					data = dataQueue.take();
					if (data == null || data.length == 0) {
						return;
					}

					writeMediaSample(data);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			writeError = true;
			stateCallbackToUI(C.STATE_ERROR_FILE_WRITE, "数据写入异常");
		} finally {
			try {
				if (writeError) {
					// FIXME 写入异常 怎么处理 删除文件还是？
				} else {
					if (wvpfWriter != null) {
						wvpfWriter.endRecord();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			wvpfWriter = null;
		}
	}
}
