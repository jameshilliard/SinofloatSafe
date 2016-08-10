package com.sinofloat.mobilesafe.wvp.tools;

import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.widget.BackgroundWroker.WorkerEventArgs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import sinofloat.wvp.messages.WvpGetResourceRequest;
import sinofloat.wvp.messages.WvpGetResourceResponse;
import sinofloat.wvp.messages.WvpMessageHelper;
import sinofloat.wvp.messages.WvpNodeConnectRequest;
import sinofloat.wvp.messages.WvpNodeConnectResponse;
import sinofloat.wvp.messages._ResourceTypes;
import sinofloat.wvp.tools.Util;

public class UserConnectionUtil {

	private static final String TAG = "UserConnectionUtil";

	private static final int TIME_OUT = 5 * 1000;


	// private int loginTimes = 0;
	/**
	 * 登录成功返回null 否则返回错误描述
	 * 
	 * @param serviceAddress
	 *            服务地址
	 * @param servicePort
	 *            服务端口
	 * @param userId
	 *            登录Id
	 * @param passwordMD5
	 *            登录密码（MD5加密后）
	 * 
	 *            synchronized 防止 后台service和 设置里用户手动登录 同时操作这个方法。
	 * @return
	 */
	public String Login(String serviceAddress, int servicePort,
			String userId, String userName, String passwordMD5, String groupName) {
		
		Socket socket = null;
		try {
			
			socket = new Socket(serviceAddress, servicePort);
			socket.setSoTimeout(TIME_OUT);
			DataInputStream reader = new DataInputStream(
					socket.getInputStream());
			DataOutputStream writer = new DataOutputStream(
					socket.getOutputStream());
			
			WvpNodeConnectResponse res = nodeConnection(reader, writer, userId, passwordMD5, groupName);
			
			if(res != null && res.isSuccessful){
				// 保存配置信息
				App.ConnectionSet.ServiceAddress = serviceAddress;
				App.ConnectionSet.ServicePort = servicePort;
				App.ConnectionSet.CurrentUserID = userId;
				App.ConnectionSet.CurrentUserLoginName = userName;
				App.ConnectionSet.CurrentUserPWD = passwordMD5;
				App.ConnectionSet.UserGroupName = groupName;
				App.ConnectionSet.Save();
			}else{
				return "登录失败。";
			}
		
		} catch (Exception e) {
			return "登录失败。" + e.getMessage();
		} finally {
			if (socket != null) {
				try {
					socket.shutdownInput();
					socket.shutdownOutput();
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	
	
	/**
	 * 检查获取apk升级的地址（）
	 * @param serviceAddress
	 * @param servicePort
	 * @param appName 要获取的应用名称（速觅直播 移动安防 等等）
	 * @param curAppVersion 当前应用版本号
	 * @param args
	 * @return
	 */
	public void getAppUpdateUrl(String serviceAddress, int servicePort,
			String appName, String curAppVersion, WorkerEventArgs args) {

		String result = null;
		Socket socket = null;
		try {
			socket = new Socket(serviceAddress, servicePort);
			DataInputStream reader = new DataInputStream(
					socket.getInputStream());
			DataOutputStream writer = new DataOutputStream(
					socket.getOutputStream());
			WvpGetResourceRequest groupRequest = new WvpGetResourceRequest();
			groupRequest.resourceType = _ResourceTypes.STATE;
			
			groupRequest.param = "GetAppUpdate:" + appName + ":" + curAppVersion;

			// 发送请求
			writer.write(groupRequest.toFullMessageBytes());

			// 获取回馈
			WvpGetResourceResponse response = (WvpGetResourceResponse) WvpMessageHelper
					.ReadMessage(reader);

			args.result = response;
		} catch (Exception e) {
			result = "连接异常。" + e.getMessage();
			args.result = result;
		} finally {
			
			if (socket != null) {
				try {
					socket.shutdownInput();
					socket.shutdownOutput();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
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

}
