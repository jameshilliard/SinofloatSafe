package com.sinofloat.mobilesafe.replay.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sinofloat.mobilesafe.replay.entity.MediaEntity;

import java.util.ArrayList;

import sinofloat.wvp.messages._WvpMediaMessageTypes;

/**
 * 创建媒体数据表 数据库操作 20140717 这里主要是解决视频离线存储 时 离线视频的缩略图与离线视频文件的关联问题 ，怕以后有扩展
 * 所以类名取得有点大。
 * 
 * @author staid
 * 
 */
public class MediaDB extends DBHelper {

	private static final String MEDIA_TABLE = "media";

	private static final String MEDIA_ID = "media_id";
	private static final String MEDIA_CREAT_USER_NAME = "user_name";
	private static final String MEDIA_CREAT_USER_ID = "user_id";
	private static final String MEDIA_CREAT_TIME = "media_time";
	private static final String MEDIA_STORE_LOCATION = "media_store_location";
	private static final String MEDIA_THUMBNAIL_STORE_LOCATION = "media_thumbnail_store_location";

	private static final String MEDIA_TYPE = "media_type";
	private static final String MEDIA_READ_STATE = "media_read_state";

	// MEDIA_ID MEDIA_CREAT_USER_NAME MEDIA_CREAT_USER_ID MEDIA_CREAT_TIME
	// MEDIA_STORE_LOCATION
	// MEDIA_THUMBNAIL_STORE_LOCATION MEDIA_TYPE MEDIA_READ_STATE
	/**
	 * 创建消息表的 sql语句
	 */
	private final static String CREAT_MEDIA_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ MEDIA_TABLE
			+ "("
			+ MEDIA_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, "
			+ MEDIA_CREAT_USER_NAME
			+ " VARCHAR(20), "
			+ MEDIA_CREAT_USER_ID
			+ " VARCHAR(40), "
			+ MEDIA_CREAT_TIME
			+ " LONG, "
			+ MEDIA_STORE_LOCATION
			+ " VARCHAR(500), "
			+ MEDIA_THUMBNAIL_STORE_LOCATION
			+ " VARCHAR(500), "
			+ MEDIA_TYPE
			+ " INTEGER, " + MEDIA_READ_STATE + " INTEGER)";

	@Override
	public void initDB(SQLiteDatabase paramSQLiteDatabase) {
	}

	/**
	 * 创建表
	 */
	@Override
	public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int oldversion,
			int newversion) {
		paramSQLiteDatabase.execSQL(CREAT_MEDIA_TABLE);
		super.onUpgrade(paramSQLiteDatabase, oldversion, newversion);
	}

	/**
	 * 数据库对象
	 */
	private SQLiteDatabase mDB;

	private static MediaDB db;

	public MediaDB(Context context) {
		super(context);
		mDB = getWritableDatabase();
		onUpgrade(mDB, 0, 0);
		mDB.close();
		mDB = null;
	}
	
	/**
	 * 单例模式
	 * @param context
	 * @return
	 */
	public static MediaDB getMediaDBInstance(Context context){
		
		if(db == null){
			db = new MediaDB(context);
		}
		return db;
	}

	/**
	 * 删除表
	 */
	public void dropTable() {
		mDB = this.getWritableDatabase();
		mDB.execSQL("DROP TABLE " + MEDIA_TABLE);
		mDB.close();
		mDB = null;
	}

	/**
	 * 添加一条记录
	 * 
	 * @param msgEntity
	 * @return
	 */
	public boolean add(MediaEntity mediaEntity) {

		mDB = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(MEDIA_CREAT_USER_NAME, mediaEntity.mediaCreatUserNm);
		values.put(MEDIA_CREAT_USER_ID, mediaEntity.mediaCreatUserId);
		values.put(MEDIA_CREAT_TIME, mediaEntity.mediaCreatTime);
		values.put(MEDIA_STORE_LOCATION, mediaEntity.MediaStoreLocation);
		values.put(MEDIA_THUMBNAIL_STORE_LOCATION,
				mediaEntity.MediaThumbnailStoreLocation);
		values.put(MEDIA_TYPE, mediaEntity.mediaType);
		values.put(MEDIA_READ_STATE, mediaEntity.mediaReadState);

		long rowId = mDB.insert(MEDIA_TABLE, null, values);

		mDB.close();
		mDB = null;

		if (rowId > -1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 删除一条记录(发送信息人姓名 和 信息时间)
	 * 
	 * @param msgSenderName
	 * @param msgTime
	 * @return
	 */
	public boolean deleteByMsgId(String msgId) {

		mDB = this.getWritableDatabase();

		int result = mDB.delete(MEDIA_TABLE, MEDIA_ID + "=?",
				new String[] { msgId });

		mDB.close();
		mDB = null;

		return result == 0;
	}

	
	/**
	 * FIXME 需要测试 看看是不是能删除多条记录 删除指定字段的信息 需要指定字段
	 * 
	 * @param msgSenderName
	 * @return
	 */
	public boolean deleteAllByField(String fieldName) {

		mDB = this.getWritableDatabase();

		int result = mDB.delete(MEDIA_TABLE, fieldName + "=?",
				new String[] { fieldName });

		mDB.close();
		mDB = null;

		if (result == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 修改一条记录
	 * 
	 * @param msgEntity
	 * @return
	 */
	public boolean update(MediaEntity mediaEntity, String mediaId) {

		mDB = this.getWritableDatabase();

		if (mediaEntity != null) {

			ContentValues values = new ContentValues();

			values.put(MEDIA_CREAT_USER_NAME, mediaEntity.mediaCreatUserNm);
			values.put(MEDIA_CREAT_USER_ID, mediaEntity.mediaCreatUserId);
			values.put(MEDIA_CREAT_TIME, mediaEntity.mediaCreatTime);
			values.put(MEDIA_STORE_LOCATION, mediaEntity.MediaStoreLocation);
			values.put(MEDIA_THUMBNAIL_STORE_LOCATION,
					mediaEntity.MediaThumbnailStoreLocation);
			values.put(MEDIA_TYPE, mediaEntity.mediaType);
			values.put(MEDIA_READ_STATE, mediaEntity.mediaReadState);

			long rowId = mDB.update(MEDIA_TABLE, values, MEDIA_ID + "=?",
					new String[] { mediaId });

			mDB.close();
			mDB = null;

			if (rowId > 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

	/**
	 * 查询所有类型的消息数据 按照发消息人的姓名
	 * 
	 * @param name
	 * @return
	 */
	public ArrayList<MediaEntity> quieryByCreatUserName(String whoCreatTheMedia) {

		mDB = this.getWritableDatabase();
		ArrayList<MediaEntity> msgList = null;

		String sql = "SELECT * FROM " + MEDIA_TABLE + " WHERE "
				+ MEDIA_CREAT_USER_NAME + "=? " + " ORDER BY " + MEDIA_ID
				+ " DESC LIMIT ?,?";//
		String[] argus = new String[] { whoCreatTheMedia };

		Cursor cursor = mDB.rawQuery(sql, argus);
		if (cursor != null && cursor.getCount() > 0) {

			msgList = new ArrayList<MediaEntity>();

			if (cursor.moveToFirst()) {

				do {

					MediaEntity entity = new MediaEntity();

					entity.mediaId = cursor.getString(cursor
							.getColumnIndex(MEDIA_ID));

					entity.mediaCreatUserNm = cursor.getString(cursor
							.getColumnIndex(MEDIA_CREAT_USER_NAME));

					entity.mediaCreatUserId = cursor.getString(cursor
							.getColumnIndex(MEDIA_CREAT_USER_ID));

					entity.mediaCreatTime = cursor.getLong(cursor
							.getColumnIndex(MEDIA_CREAT_TIME));

					entity.MediaStoreLocation = cursor.getString(cursor
							.getColumnIndex(MEDIA_STORE_LOCATION));

					entity.MediaThumbnailStoreLocation = cursor
							.getString(cursor
									.getColumnIndex(MEDIA_THUMBNAIL_STORE_LOCATION));

					entity.mediaType = cursor.getInt(cursor
							.getColumnIndex(MEDIA_TYPE));

					entity.mediaReadState = cursor.getInt(cursor
							.getColumnIndex(MEDIA_READ_STATE));

					msgList.add(entity);

				} while (cursor.moveToNext());
			}
			// 关闭游标
			cursor.close();
		}

		mDB.close();
		mDB = null;

		return msgList;
	}

	/**
	 * 查询某个月的所有数据
	 */
	public ArrayList<MediaEntity> quieryPictureByMonth(long startTimeStamp, long endTimeStamp) {

		mDB = this.getWritableDatabase();

		ArrayList<MediaEntity> msgList = null;

		String sql = "SELECT * FROM " + MEDIA_TABLE + " WHERE " + MEDIA_TYPE
				+ "=" + _WvpMediaMessageTypes.PICTUREJPEG + " AND "+MEDIA_CREAT_TIME+">= ?"+" AND "+MEDIA_CREAT_TIME+" < ? "+" ORDER BY " + MEDIA_ID
				+ " DESC ";
		String[] args = new String[] {
				String.valueOf(startTimeStamp),
				String.valueOf(endTimeStamp) };

		Cursor cursor = mDB.rawQuery(sql, args);

		msgList = queryMediaData(cursor);

		mDB.close();
		mDB = null;
		return msgList;
	}

	/**
	 * 查询 第一次插入的一条数据
	 *
	 * @return
	 */
	public MediaEntity queryTheFirstesMedia() {

		MediaEntity entity = null;

		String sql = "SELECT * FROM " + MEDIA_TABLE + " ORDER BY " + MEDIA_ID
				+ " LIMIT ?,?";//
		String[] argus = new String[] { String.valueOf(0), String.valueOf(1) };

		mDB = getReadableDatabase();
		Cursor cursor = mDB.rawQuery(sql, argus);

		if (cursor != null && cursor.getCount() > 0) {
			if (cursor.moveToFirst()) {
				do {

					entity = new MediaEntity();

					entity.mediaId = cursor.getString(cursor
							.getColumnIndex(MEDIA_ID));

					entity.mediaCreatUserNm = cursor.getString(cursor
							.getColumnIndex(MEDIA_CREAT_USER_NAME));

					entity.mediaCreatUserId = cursor.getString(cursor
							.getColumnIndex(MEDIA_CREAT_USER_ID));

					entity.mediaCreatTime = cursor.getLong(cursor
							.getColumnIndex(MEDIA_CREAT_TIME));

					entity.MediaStoreLocation = cursor.getString(cursor
							.getColumnIndex(MEDIA_STORE_LOCATION));

					entity.MediaThumbnailStoreLocation = cursor
							.getString(cursor
									.getColumnIndex(MEDIA_THUMBNAIL_STORE_LOCATION));

					entity.mediaType = cursor.getInt(cursor
							.getColumnIndex(MEDIA_TYPE));

					entity.mediaReadState = cursor.getInt(cursor
							.getColumnIndex(MEDIA_READ_STATE));

				} while (cursor.moveToNext());
			}
			// 关闭游标
			cursor.close();
		}

		// 关闭数据库
		mDB.close();
		mDB = null;

		return entity;
	}

	/**
	 * 查询 最后一次插入的一条数据
	 * 
	 * @return
	 */
	public MediaEntity queryTheLatestMedia() {

		MediaEntity entity = null;

		String sql = "SELECT * FROM " + MEDIA_TABLE + " ORDER BY " + MEDIA_ID
				+ " DESC LIMIT ?,?";//
		String[] argus = new String[] { String.valueOf(0), String.valueOf(1) };

		mDB = getReadableDatabase();
		Cursor cursor = mDB.rawQuery(sql, argus);

		if (cursor != null && cursor.getCount() > 0) {
			if (cursor.moveToFirst()) {
				do {

					entity = new MediaEntity();

					entity.mediaId = cursor.getString(cursor
							.getColumnIndex(MEDIA_ID));

					entity.mediaCreatUserNm = cursor.getString(cursor
							.getColumnIndex(MEDIA_CREAT_USER_NAME));

					entity.mediaCreatUserId = cursor.getString(cursor
							.getColumnIndex(MEDIA_CREAT_USER_ID));

					entity.mediaCreatTime = cursor.getLong(cursor
							.getColumnIndex(MEDIA_CREAT_TIME));

					entity.MediaStoreLocation = cursor.getString(cursor
							.getColumnIndex(MEDIA_STORE_LOCATION));

					entity.MediaThumbnailStoreLocation = cursor
							.getString(cursor
									.getColumnIndex(MEDIA_THUMBNAIL_STORE_LOCATION));

					entity.mediaType = cursor.getInt(cursor
							.getColumnIndex(MEDIA_TYPE));

					entity.mediaReadState = cursor.getInt(cursor
							.getColumnIndex(MEDIA_READ_STATE));

				} while (cursor.moveToNext());
			}
			// 关闭游标
			cursor.close();
		}

		// 关闭数据库
		mDB.close();
		mDB = null;

		return entity;
	}

	/**
	 * 分页查询
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * 
	 *         String sql = "SELECT id,name,birthday FROM " + TABLENAME +
	 *         " WHERE (name LIKE ? OR birthday LIKE ?)" + " LIMIT ?,? " ;
	 */
	public ArrayList<MediaEntity> quieryPictureByPageNumber(int pageNumber,
			int pageSize) {

		mDB = this.getWritableDatabase();

		ArrayList<MediaEntity> msgList = null;

		String sql = "SELECT * FROM " + MEDIA_TABLE + " WHERE " + MEDIA_TYPE
				+ "=" + _WvpMediaMessageTypes.PICTUREJPEG + " ORDER BY " + MEDIA_ID
				+ " DESC LIMIT ?,? ";
		String[] args = new String[] {
				String.valueOf((pageNumber - 1) * pageSize),
				String.valueOf(pageNumber * pageSize) };

		Cursor cursor = mDB.rawQuery(sql, args);

		msgList = queryMediaData(cursor);

		mDB.close();
		mDB = null;
		return msgList;
	}

	/**
	 * 分页查询
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * 
	 *         String sql = "SELECT id,name,birthday FROM " + TABLENAME +
	 *         " WHERE (name LIKE ? OR birthday LIKE ?)" + " LIMIT ?,? " ;
	 */
	public ArrayList<MediaEntity> quieryVideoAudioByPageNumber(int pageNumber,
			int pageSize) {

		mDB = this.getWritableDatabase();

		ArrayList<MediaEntity> msgList = null;

		String sql = "SELECT * FROM " + MEDIA_TABLE + " WHERE " + MEDIA_TYPE
				+ "=" + _WvpMediaMessageTypes.VIDEOH264 + " ORDER BY "
				+ MEDIA_ID + " DESC LIMIT ?,? ";
		String[] args = new String[] {
				String.valueOf((pageNumber - 1) * pageSize),
				String.valueOf(pageNumber * pageSize) };

		Cursor cursor = mDB.rawQuery(sql, args);

		msgList = queryMediaData(cursor);

		mDB.close();
		mDB = null;
		return msgList;
	}

	/**
	 * 查询
	 * 
	 * @param cursor
	 * @return
	 */
	private ArrayList<MediaEntity> queryMediaData(Cursor cursor) {

		if (cursor == null) {
			return null;
		}

		ArrayList<MediaEntity> msgList = new ArrayList<MediaEntity>();

		if (cursor.moveToFirst()) {
			do {

				MediaEntity entity = new MediaEntity();

				entity.mediaId = cursor.getString(cursor
						.getColumnIndex(MEDIA_ID));

				entity.mediaCreatUserNm = cursor.getString(cursor
						.getColumnIndex(MEDIA_CREAT_USER_NAME));

				entity.mediaCreatUserId = cursor.getString(cursor
						.getColumnIndex(MEDIA_CREAT_USER_ID));

				entity.mediaCreatTime = cursor.getLong(cursor
						.getColumnIndex(MEDIA_CREAT_TIME));

				entity.MediaStoreLocation = cursor.getString(cursor
						.getColumnIndex(MEDIA_STORE_LOCATION));

				entity.MediaThumbnailStoreLocation = cursor.getString(cursor
						.getColumnIndex(MEDIA_THUMBNAIL_STORE_LOCATION));

				entity.mediaType = cursor.getInt(cursor
						.getColumnIndex(MEDIA_TYPE));

				entity.mediaReadState = cursor.getInt(cursor
						.getColumnIndex(MEDIA_READ_STATE));

				msgList.add(entity);

			} while (cursor.moveToNext());
		}
		// 关闭游标
		cursor.close();

		return msgList;
	}

}
