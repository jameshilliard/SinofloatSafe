package com.sinofloat.mobilesafe.map.entity;

/**
 * Created by oyk on 2016/7/31.
 * 带有坐标的相机组
 */
public class RemoteGroup {
    /**
     * 相机名称（管理员取得名字）
     */
    public String displayName;
    /**
     * 相机Id （相机在数据库中的编号 也可能是 相机组Id）
     */
    public String camearID;
    /**
     * 相机组的经度
     */
    public Double longitude;
    /**
     * 相机组的纬度
     */
    public Double latitude;
    /**
     * 相机类型  是否摇头 形状 组 判断是否是相机组 通过这个判断
     */
    public String camearType;

    /**
     * 是否被选中
     */
    public boolean selected = false;
}
