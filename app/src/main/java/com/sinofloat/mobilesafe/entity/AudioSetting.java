package com.sinofloat.mobilesafe.entity;

import sinofloat.wvp.messages._WvpMediaMessageTypes;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;

public class AudioSetting {

	private static final String AUDIO_SETTING = "audio";
	
	private Context context;
	
	/**
	 * 对讲机音频所使用的编码格式
	 */
	public int EncodingFormat = _WvpMediaMessageTypes.AudioAMR122;

	/**
	 * 录音是否默认打开
	 */
	public boolean isRecorderOpen = true;
	
	/**
	 * 是否使用听筒模式（蓝牙耳机）默认使用外放扬声器
	 */
	public boolean IsUseVoicePhone = false;
	
	/**
	 * 默认使用外放扬声器
	 */
	public int audioStreamType = AudioManager.STREAM_MUSIC;
	
	/**
	 * 音量增益值 对应的控件中的位置（0是无增益，1是放大一倍 以此类推）
	 */
	public int volumeRiseUpPosition;
	
	
	public AudioSetting(Context context,boolean isLoad){
		this.context = context;
		if(isLoad){
			load();
		}
	}
	
	
	public void load(){
		SharedPreferences sp = context.getSharedPreferences(
				AUDIO_SETTING, 0);

		EncodingFormat = sp.getInt("EncodingFormat", EncodingFormat);
		volumeRiseUpPosition = sp.getInt("volumeRiseUpPosition", volumeRiseUpPosition);
		IsUseVoicePhone = sp.getBoolean("IsUseVoicePhone",
				IsUseVoicePhone);
		isRecorderOpen = sp.getBoolean("isRecorderOpen", isRecorderOpen);
	}
	
	public void save(){
		SharedPreferences.Editor editor = context.getSharedPreferences(
				AUDIO_SETTING, 0).edit();
		editor.putInt("EncodingFormat", EncodingFormat);
		editor.putInt("volumeRiseUpPosition", volumeRiseUpPosition);
		editor.putBoolean("IsUseVoicePhone", IsUseVoicePhone);
		editor.putBoolean("isRecorderOpen", isRecorderOpen);
		editor.commit();
	}
}
