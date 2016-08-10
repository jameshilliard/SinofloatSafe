package com.sinofloat.mobilesafe.wvp.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import sinofloat.wvp.messages.WvpMessageHelper;
import sinofloat.wvp.messages.WvpNodeConnectRequest;
import sinofloat.wvp.messages.WvpNodeConnectResponse;
import sinofloat.wvp.messages.WvpSetResourceRequest;
import sinofloat.wvp.messages.WvpSetResourceResponse;
import sinofloat.wvp.messages._ResourceTypes;

/**
 * 发送报平安消息
 * @author staid
 *
 */
public class ReportSafeWorker {
	
	private Socket socket;
	
	/**
	 * 连接 服务器
	 * @param 服务器ip
	 * @param 服务器端口号
	 * @param 连接超时
	 *            (毫秒)
	 * @return
	 */
	private boolean startConnection(String serviceAddress, int servicePort,
			String userID, String passwordMd5) {

		boolean isConnected = true;
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

			if (!connResponse.isSuccessful)
				return false;

			// 设置资源
			WvpSetResourceResponse setResResponse = setResource(input, output,
					"Mobile:" + userID, _ResourceTypes.CHANNEL);

			if (!setResResponse.isSuccessful)
				return false;

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
			isConnected = false;
		}
		return isConnected;
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
			DataOutputStream output, String userID, String passwordMd5) throws Exception {

		WvpNodeConnectResponse response = null;
		WvpNodeConnectRequest request = new WvpNodeConnectRequest();
		request.userID = userID;
		request.password = passwordMd5;
		request.groupID = "";

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


}
