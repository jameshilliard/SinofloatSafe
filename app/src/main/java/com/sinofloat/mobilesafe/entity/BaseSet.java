package com.sinofloat.mobilesafe.entity;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera.Size;
import android.util.DisplayMetrics;
import android.util.Log;
import com.sinofloat.mobilesafe.R;

/**
 * 软件基本信息
 * @author staid
 */
public class BaseSet {
	
	private Context context;
	
	private static final String FILE_NAME = "BaseSet";
	
	/**
	 * 软件版本号
	 */
	public String VersionCode;
	
	/**
	 * 程序包名
	 */
	public static final String APP_PACKAGE_NAME = "sinofloat.wvp";
	
	/**
	 * 程序是否升级过。（生命周期只在彻底退出程序前有效）
	 */
	public boolean isAppUpdated;
	
	/**
	 * 主副相机图片支持的分辨率字符串 键
	 */
	public static final String MAIN_CAMERA_SUPPORT_PICTURE_DATA_FILE_NAME = "main_pic_data_string";
	public static final String ASSIST_CAMERA_SUPPORT_PICTURE_DATA_FILE_NAME = "assist_pic_data_string";

	/**
	 *  主副相机预览支持的分辨率字符串 存入file 键
	 */
	public static final String MAIN_CAMERA_SUPPORT_PREVIEW_FILE_NAME = "main_preview_data_string";
	public static final String ASSIST_CAMERA_SUPPORT_PREVIEW_DATA_FILE_NAME = "assist_preview_data_string";

	
	/**
	 * 相机类型 default is mainCarema.
	 */
	public int nowUsingCameraId = 0;
	public static final int MAIN_CAMERA = 0;
	public static final int ASSIST_CAMERA = 1;
	public static final int USB_CAMERA = 2;
	public boolean hasAssistCamera = true;
	
	/**
	 * 设备是否支持录像中持续对焦
	 * 默认就是持续对焦，有些手机不支持这个功能 需要提前获取相机参数 是否支持录像中持续对焦。
	 * 持续对焦 默认开启 api 9以上有这个功能,但是14+
	 * 可以在此模式下调用autoFocus（）。但是调用后要在focus回调后调用cancellAutoFocus。
	 */
	public boolean isSupportContiniousFocus;
	
	
	/**
	 * 默认支持的拍照 编码分辨率 "1280*720"(不能编码), "1920*1080" 
	 */
	public static final String[] DEFAULT_RESOLUTION = new String[] { "320*240",
			"640*480" , "1280*720"};
	
	/**
	 * 支持的相机分辨率列表
	 */
	public String[] mainCameraPictureResolutionArray,
			assistCameraPictureResolutionArray;
	
	/**
	 * 手机支持的预览的分辨率数组
	 */
	public String[] mainCameraPreviewResolutionArray,
			assistCameraPreviewResolutionArray;
	
	// 后置相机最大调焦
	public int maxZoom;
	
	
	
	/**
	 * 状态栏高度 屏幕宽高（横屏的情况下）
	 */
	public int statusBarHeight, screenWidth, screenHeight;
	
	/**
	 * 屏幕密度
	 */
	public float density;
	
	
	
	
	public BaseSet(Context context,boolean isLoad){
		this.context = context;
		density = context.getResources().getDisplayMetrics().density;
		if(isLoad){
			load();
		}
	}

	
	public void Save(){
		SharedPreferences.Editor editor = context.getSharedPreferences(FILE_NAME, 0).edit();
		editor.putBoolean("hasAssistCamera", hasAssistCamera);
		editor.putBoolean("isSupportContiniousFocus", isSupportContiniousFocus);
		editor.commit();
	}
	
	/**
	 * 加载读取文件获取主副相机支持的图片和预览分辨率
	 */
	public void load(){
		SharedPreferences sp = context.getSharedPreferences(FILE_NAME, 0);
		hasAssistCamera = sp.getBoolean("hasAssistCamera", hasAssistCamera);
		isSupportContiniousFocus = sp.getBoolean("isSupportContiniousFocus", isSupportContiniousFocus);
		
		VersionCode = context.getString(R.string.version);
		
		//获取主相机最大调焦
		getMainCameraMaxZoom();
		//读取文件获取预览分辨率
		getPreviewInfoFromFile();
		//读取文件获取图片分辨率
		getPictureInfoFromFile();
	}
	
	/**
	 * 初始化获取屏幕参数
	 * @param activity
	 */
	public void initScreenParam(Activity activity) {
		DisplayMetrics metric = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		screenWidth = metric.widthPixels; // 屏幕宽度（像素）
		screenHeight = metric.heightPixels; // 屏幕高度（像素）
		density = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
	}
	
	/**
	 * 设置最大焦距
	 * @param maxZoom
	 */
	public void setCameraMaxZoom(int maxZoom) {

		SharedPreferences.Editor editor = context.getSharedPreferences(
				FILE_NAME, 0).edit();
		// 设置视频质量
		editor.putInt("maxZoom", maxZoom);
		editor.commit();
	}
	
	/**
	 * 获取主相机最大焦距
	 */
	private void getMainCameraMaxZoom(){
		SharedPreferences sp = context.getSharedPreferences(FILE_NAME, 0);
		maxZoom = sp.getInt("maxZoom", maxZoom);
	}
	
	/**
	 * 写入相机支持的拍照分辨率。获取相机实例的时�?调用此方法�?
	 * 
	 * @param cameraType
	 * @param fileName
	 * @param supportPictureData
	 */
	public void writePictureInfoToFile(int cameraType,
			List<Size> supportPictureData) {

//		ToastUtil.showSimpleToast(context, "cameraType:"+cameraType+" --------support size: "+supportPictureData.size(), false);
		
		SharedPreferences sp = context.getSharedPreferences(FILE_NAME, 0);
		SharedPreferences.Editor editor = sp.edit();
		StringBuilder sb = new StringBuilder();
		// 将list转化成String 存入到文�?
		switch (cameraType) {
		case MAIN_CAMERA:

			// 写入主相机拍照图片分辨率�?
			int mainPiclength = supportPictureData.size();
			for (int i = 0; i < mainPiclength; i++) {
				Size size = supportPictureData.get(i);
				if (i == mainPiclength - 1) {
					sb.append(size.width).append("*").append(size.height);
				} else {
					sb.append(size.width).append("*").append(size.height)
							.append("_");// 每俩个size之间�?_"分割
				}
			}

			mainCameraPictureResolutionArray = sb.toString().split("_");

			editor.putString(MAIN_CAMERA_SUPPORT_PICTURE_DATA_FILE_NAME,
					sb.toString());

//			getPic640_480_resolutionPosition(MAIN_CAMERA);
//
//			editor.putInt(MAIN_CAMERA_PICTURE_RESOLUTION_POSITION_FILE_NAME,
//					mainCameraPictureResolutionPosition);

			break;
		case ASSIST_CAMERA:

			// 写入副相机拍照图片分辨率�?
			int assistPiclength = supportPictureData.size();
			for (int i = 0; i < assistPiclength; i++) {
				Size size = supportPictureData.get(i);
				if (i == assistPiclength - 1) {
					sb.append(size.width).append("*").append(size.height);
				} else {
					sb.append(size.width).append("*").append(size.height)
							.append("_");// 每俩个size之间�?_"分割
				}
			}

			assistCameraPictureResolutionArray = sb.toString().split("_");

			editor.putString(ASSIST_CAMERA_SUPPORT_PICTURE_DATA_FILE_NAME,
					sb.toString());

//			getPic640_480_resolutionPosition(ASSIST_CAMERA);
//			editor.putInt(ASSIST_CAMERA_PICTURE_RESOLUTION_POSITION_FILE_NAME,
//					assistCameraPictureResolutionPosition);

			break;
		}
		Log.e("BaseSet.java", sb.toString());
		editor.commit();
	}
	
	
	/**
	 * 写入相机支持的预览（视频）分辨率。获取相机实例的时�?调用此方法�?
	 * @param cameraType
	 * @param fileName
	 */
	public void writePreviewInfoToFile(int cameraType, 
			List<Size> previewSupportData) {

//		ToastUtil.showSimpleToast(context, "cameraType:"+cameraType+" --------support size: "+previewSupportData.size(), false);
		
		SharedPreferences sp = context.getSharedPreferences(FILE_NAME, 0);
		SharedPreferences.Editor editor = sp.edit();
		StringBuilder sb = new StringBuilder();
		// 将list转化成String 存入到文�?
		switch (cameraType) {
		case MAIN_CAMERA:
			
			// 写入主相机预览分辨率�?
			int mainPreLenglth = previewSupportData.size();
			for (int i = 0; i < mainPreLenglth; i++) {
				
				Size size = previewSupportData.get(i);
				if (i == mainPreLenglth - 1) {
					sb.append(size.width).append("*").append(size.height);
				} else {
					sb.append(size.width).append("*").append(size.height).append(
							"_");// 每俩个size之间�?_"分割
				}
			}
			
			mainCameraPreviewResolutionArray = sb.toString().split("_");
					
			editor.putString(MAIN_CAMERA_SUPPORT_PREVIEW_FILE_NAME,
					sb.toString());
			
//			getPreview640_480_resolutionPosition(MAIN_CAMERA);
//			editor.putInt(MAIN_CAMERA_PREVIEW_RESOLUTION_POSITION_FILE_NAME, mainCameraPreviewResolutionPosition);
			
			break;
		case ASSIST_CAMERA:
			
			// 写入副相机预览分辨率�?
			int assistPreLenglth = previewSupportData.size();
			for (int i = 0; i < assistPreLenglth; i++) {
				
				Size size = previewSupportData.get(i);
				if (i == assistPreLenglth - 1) {
					sb.append(size.width).append("*").append(size.height);
				} else {
					sb.append(size.width).append("*").append(size.height).append(
							"_");// 每俩个size之间�?_"分割
				}
			}
			
			assistCameraPreviewResolutionArray = sb.toString().split("_");
			
			editor.putString(ASSIST_CAMERA_SUPPORT_PREVIEW_DATA_FILE_NAME,
					sb.toString());
			
//			getPreview640_480_resolutionPosition(ASSIST_CAMERA);
//			editor.putInt(ASSIST_CAMERA_PREVIEW_RESOLUTION_POSITION_FILE_NAME, mainCameraPreviewResolutionPosition);
			
			break;
		}
		Log.e("BaseSet.java", sb.toString());
		editor.commit();
	}
	
	
	// 从file里获取主副相机预�?拍照图片支持的分辨率�?
		private void getPreviewInfoFromFile() {
			SharedPreferences sp = context.getSharedPreferences(FILE_NAME, 0);

			String mainCameraPreviewDataString = sp.getString(
					MAIN_CAMERA_SUPPORT_PREVIEW_FILE_NAME, null);
			if (mainCameraPreviewDataString != null) {
				mainCameraPreviewResolutionArray = mainCameraPreviewDataString
						.split("_");
			}

			String assistCameraPreviewDataString = sp.getString(
					ASSIST_CAMERA_SUPPORT_PREVIEW_DATA_FILE_NAME, null);
			if (assistCameraPreviewDataString != null) {
				assistCameraPreviewResolutionArray = assistCameraPreviewDataString
						.split("_");
			}
			
//			Log.e("BaseSet.java", "货"+mainCameraPreviewResolutionArray.toString());
//			Log.e("BaseSet.java", "货"+assistCameraPreviewResolutionArray.toString());
		}
		
		
		/**
		 * 从文件里获取主副相机拍照图片支持的分辨率
		 */
		private void getPictureInfoFromFile() {
			SharedPreferences sp = context.getSharedPreferences(FILE_NAME, 0);

			String mainCameraPicDataString = sp.getString(
					MAIN_CAMERA_SUPPORT_PICTURE_DATA_FILE_NAME, null);
			if (mainCameraPicDataString != null) {
				mainCameraPictureResolutionArray = mainCameraPicDataString
						.split("_");
			}

			String assistCameraPicDataString = sp.getString(
					ASSIST_CAMERA_SUPPORT_PICTURE_DATA_FILE_NAME, null);
			if (assistCameraPicDataString != null) {
				assistCameraPictureResolutionArray = assistCameraPicDataString
						.split("_");
			}
			
//			Log.e("BaseSet.java", "货"+mainCameraPictureResolutionArray.toString());
//			Log.e("BaseSet.java", "货"+assistCameraPictureResolutionArray.toString());
		}
		
		
		// 获取预览 640*480分辨率的位置�?
//		private void getPreview640_480_resolutionPosition(int whichCamera) {
//
//			int position = 0;
//			String[] tempvariable = null;
//			switch (whichCamera) {
//			case MAIN_CAMERA:
//				tempvariable = mainCameraPreviewResolutionArray;
//				if (tempvariable != null) {
//					for (int i = 0; i < tempvariable.length; i++) {
//						if (tempvariable[i].equals("640*480")) {
//							position = i;
//						}
//					}
//				}
//				mainCameraPreviewResolutionPosition = position;
//				break;
//			case ASSIST_CAMERA:
//				tempvariable = assistCameraPreviewResolutionArray;
//				if (tempvariable != null) {
//					for (int i = 0; i < tempvariable.length; i++) {
//						if (tempvariable[i].equals("640*480")) {
//							position = i;
//						}
//					}
//				}
//				assistCameraPreviewResolutionPosition = position;
//				break;
//			case USB_CAMERA:
//
//				break;
//			}
//			getCurCameraPreviewResolution(whichCamera);
//		}

}
