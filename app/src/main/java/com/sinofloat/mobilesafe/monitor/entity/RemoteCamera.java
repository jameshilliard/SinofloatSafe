package com.sinofloat.mobilesafe.monitor.entity;


import android.graphics.Bitmap;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;

/**
 * 远程相机（球型旋转 固定）
 * @author staid
 * //如果摄像头有云台，则使用“CamHasPtz|”前缀，如果没有云台则使用“CamNoPtz|”前缀
                //如果是摄像头组，则使用“CamGroup|”前缀
                //如果是移动设备，则使用“Mobile|”前缀
 *
 */
public class RemoteCamera extends MultiItemEntity implements Serializable{

	/**
	 * 有云台    
	 */
	public static final String CAMEAR_TYPE_PTZ = "CamHasPtz";
	/**
	 * 无云台
	 */
	public static final String CAMEAR_TYPE_NO_PTZ = "CamNoPtz";
	/**
	 * 移动视频采集设备
	 */
	public static final String CAMEAR_TYPE_MOBILE = "Mobile";
	/**
	 * 视频采集设备组（按地理位置 或者功能 用户划分的组）
	 */
	public static final String CAMEAR_TYPE_CAMGROUP = "CamGroup";

	public int getType() {
		return type;
	}

	@Override
	public int getItemType() {
		return getType();
	}

	public void setType(int type) {
		this.type = type;
	}

	/**
	 * 当前摄像头是否是组
	 */
	public int type;

	/**
	 * 相机名称（管理员取得名字）
	 */
	public String displayName;
	/**
	 * 相机Id （相机在数据库中的编号 也可能是 相机组Id）
	 */
	public String camearID;
	
	/**
	 * 相机类型  是否摇头 形状 组 判断是否是相机组 通过这个判断
	 */
	public String camearType;
	
	/**
	 * 是否被选中
	 */
	public boolean selected = false;

	/**
	 * 缩略图
	 */
	public Bitmap bitmap = null;
	
}
