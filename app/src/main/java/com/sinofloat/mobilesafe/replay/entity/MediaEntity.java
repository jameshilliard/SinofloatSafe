package com.sinofloat.mobilesafe.replay.entity;

public class MediaEntity {

	public String mediaId ;
	/**
	 * 媒体创建者名称
	 */
	public String mediaCreatUserNm;
	/**
	 * 媒体创建者用户ID
	 */
	public String mediaCreatUserId;
	/**
	 * 媒体创建时间
	 */
	public long mediaCreatTime;
	/**
	 * 媒体存储位置 路径 地址
	 */
	public String MediaStoreLocation;
	/**
	 * 媒体缩略图位置 路径地址
	 */
	public String MediaThumbnailStoreLocation;
	
	/**
	 * 媒体类型（_WvpMediaMessageTypes.VIDEOH264_THUMBNAIL VIDEOH264 PICTUREJPEG）
	 */
	public int mediaType;
	/**
	 * 媒体 用户阅读状态 是否已读等。
	 */
	public int mediaReadState;
	
	/**
	 * 是否选中（准备删除）
	 */
	public boolean selectState;
}
