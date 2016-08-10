package com.sinofloat.mobilesafe.wvp.core;

import com.sinofloat.mobilesafe.base.App;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;

import sinofloat.wvp.messages.WvpMessageHelper;
import sinofloat.wvp.messages.WvpNodeConnectRequest;
import sinofloat.wvp.messages.WvpNodeConnectResponse;
import sinofloat.wvp.messages.WvpSetResourceRequest;
import sinofloat.wvp.messages.WvpSetResourceResponse;
import sinofloat.wvp.messages._ResourceTypes;

/**
 * 文件上传对象 
 * 需要读取文件流再socket上传
 * @author staid
 *
 */
public class UploadFileWorker extends Thread {

	/**
	 * 线程工作flag
	 */
	private boolean isWorking;
	
	private File file;
	private Socket socket;
	private String serviceAddress;
	private int servicePort; 
	private String userID;
	private String passwordMd5;
	
	/**
	 * 文件大小
	 */
	private long fileSize;
	/**
	 * 文件名
	 */
	private String fileName;
	
	/**
	 * 服务器需要的文件名格式（截取8个字节的文件名作为上传的文件名）
	 */
	private String dataName;
	
	/**
	 * 服务器返回的需要跳过的字节
	 */
	private int skipByteCount;
	
	private DataOutputStream dataOutputStream;
	
	
	/**
	 * 构造方法 
	 * @param serviceAddress
	 * @param servicePort
	 * @param userID
	 * @param passwordMd5
	 */
	public UploadFileWorker(File file, String serviceAddress, int servicePort, String userID, String passwordMd5) {
		
		this.file = file;
		this.fileName = file.getName();
		this.dataName = fileName.substring(0, 8);
		this.fileSize = file.length();
		
		this.serviceAddress = serviceAddress;
		this.servicePort = servicePort;
		this.userID = userID;
		this.passwordMd5 = passwordMd5;
	}
	
	
	@Override
	public void run() {
		
		String result = startConnection(serviceAddress, servicePort, userID, passwordMd5);
		if(result != null && result.equals("success")){
			isWorking = true;
			readFileAndUpload();
		}
	}
	
	
	public String  startWork(){
		
		String result = startConnection(serviceAddress, servicePort, userID, passwordMd5);
		if(result != null && result.equals("success")){
			isWorking = true;
			result = readFileAndUpload();
		}
		return result;
	}
	
	/**
	 * 停止线程
	 */
	public void stopMe(){
		
		isWorking = false;
	}
	
	/**
	 * 读取文件并上传
	 */
	private String readFileAndUpload(){
		
		String writeSuccess = "success";
		
		if(file != null && file.exists()){
			
			long fileRead = 0;
			FileInputStream fileInput = null;
			
			try {
				fileInput = new FileInputStream(file);
				fileInput.skip(skipByteCount);
				byte[] msgbody = new byte[2 * 1024];
				
				while (fileRead < fileSize && isWorking) {
					int length = fileInput.read(msgbody, 0, 2 * 1024);
					sendMessage(msgbody, length);

					fileRead += length;
					//FIXME 上传进度
//					listener.notifyProgress(fileRead * 100
//							/ fileSize);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				writeSuccess = "文件未找到";
			} catch (IOException e) {
				e.printStackTrace();
				writeSuccess = "文件IO异常";
			}finally{
				
				stopConnection();
				
				if(fileInput != null){
					try {
						fileInput.close();
						fileInput = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(dataOutputStream != null){
					try {
						dataOutputStream.close();
						dataOutputStream = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				file = null;
//				if(writeSuccess.equals("success")){
//					file.delete();
//				}
			}
		}
		return writeSuccess;
	}
	
	/**
	 * 连接 服务器
	 * 
	 * @param 服务器ip
	 * @param 服务器端口号
	 * @param 连接超时
	 *            (毫秒)
	 * @return
	 */
	private String startConnection(String serviceAddress, int servicePort,
			String userID, String passwordMd5) {

		String result = "success";
		//
		try {
			// 建立socket连接
			socket = new Socket(serviceAddress, servicePort);
			socket.setSoTimeout(5000);
			socket.setTcpNoDelay(true);
			socket.setKeepAlive(true);

			// 获取输入输出流
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(
					socket.getOutputStream());

			// 节点连接
			WvpNodeConnectResponse connResponse = nodeConnection(input, output,
					userID, passwordMd5);

			if (!connResponse.isSuccessful){
				result = "连接失败";
				return result;
			}

			// 设置资源
			String param = "Storage:UploadFile:"
					+ userID + ":"
					+ dataName + ":" + fileName + ":" + fileSize;
			WvpSetResourceResponse setResResponse = setResource(input, output,
					param, _ResourceTypes.CHANNEL);

			if (!setResResponse.isSuccessful){
				result = "设置资源失败" + setResResponse.result;
				return result;
			}
			
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			skipByteCount = Integer.parseInt(setResResponse.result);

		} catch (Exception e) {
			e.printStackTrace();
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				socket = null;
			}
			result = "网络异常" + e.getMessage();
		}
		return result;
	}

	/**
	 * 停止网络连接
	 */
	private void stopConnection() {
		if (socket != null) {
			try {
				socket.shutdownOutput();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				socket = null;
			}
		}
	}

	/**
	 * 发送真正的登录请求,必须在用户信息验证之后,重新连接socket
	 * 
	 * @param userName
	 *            用户名
	 * @param passwordMd5
	 *            密码
	 * @param group
	 *            组
	 * @return 是否登录成功
	 * @throws Exception
	 */
	private WvpNodeConnectResponse nodeConnection(DataInputStream input,
			DataOutputStream output, String userID, String passwordMd5)
			throws Exception {

		WvpNodeConnectResponse response = null;
		WvpNodeConnectRequest request = new WvpNodeConnectRequest();
		request.userID = userID;
		request.password = passwordMd5;
		request.groupID = App.ConnectionSet.UserGroupID;

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
	private WvpSetResourceResponse setResource(DataInputStream input,
			DataOutputStream output, String param, String resourceType)
			throws Exception {

		WvpSetResourceResponse response = null;

		WvpSetResourceRequest request = new WvpSetResourceRequest();
		request.param = param;
		request.resourceType = resourceType; // _ResourceTypes.CHANNEL;

		// 发送请求
		output.write(request.toFullMessageBytes());

		// 接收回馈
		response = (WvpSetResourceResponse) WvpMessageHelper.ReadMessage(input);

		return response;
	}
	
	/**
	 * 发送消息
	 * @param message
	 * @param len
	 * @throws IOException
	 */
	public void sendMessage(byte[] message,int len) throws IOException{
		
		dataOutputStream.write(message,0,len);
		dataOutputStream.flush();
	}
	
}
