package com.sinofloat.mobilesafe.replay.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 创建数据库
 * @author staid
 *
 */
public abstract class DBHelper extends SQLiteOpenHelper {
	
	/**
	 * 数据库版本
	 */
	private static final int VERSION = 1;
	
	/**
	 * 数据库名称
	 */
	private static final String DB_NAME = "SINOFLOAT_DB";

	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	/**
	 * 创建数据库
	 * @param context
	 */
	public DBHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
		initDB(paramSQLiteDatabase);
	}

	public abstract void initDB(SQLiteDatabase paramSQLiteDatabase);

	@Override
	public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int oldversion,
			int newversion) {
	}

}
