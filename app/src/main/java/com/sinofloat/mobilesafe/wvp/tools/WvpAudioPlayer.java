package com.sinofloat.mobilesafe.wvp.tools;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

import sinofloat.wvp.core.AmrCodec;
import sinofloat.wvp.core.G711;
import sinofloat.wvp.core.speex.Speex;
import sinofloat.wvp.messages.WvpDataMediaSample;
import sinofloat.wvp.messages._WvpMediaMessageTypes;

/**
 * 音频播放线程
 * 
 * @author
 * @version 1.1
 * 
 */
public class WvpAudioPlayer extends Thread {

	private static final String TAG = "WvpAudioPlayer";
	/**
	 * 播放缓存时间 默认 单位毫秒 如果>0启动缓冲模式保证音频连续但会增加延时 如果<0 则尽量降低延时，会进行丢包（跳帧）处理 如果 ==0
	 * 则随到随播。
	 */
	public int cacheTime = 0;
	/**
	 * 标记是否正在工作
	 */
	public boolean isWorking = false;

	// 播放声音
	private AudioTrack _audioTrack = null;

	// 采样�?
	private int _frequency = 8000;

	// 声道，默认是单声�?
	private int _channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;

	// 录音样式
	private int _audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

	// 播放缓存（AudioTrack根据参数自动获取�?
	private int _playBufSize = 800;

	/**
	 * 音量增益值 控件里的位置从0开始 实际的值需要+1
	 */
	private int vloumeRiseUpValue;

	// 播放缓冲
	private LinkedBlockingQueue<WvpDataMediaSample> m_PlayQueue = null;

	/**
	 * 
	 * @param m_PlayQueue
	 *            播放队列
	 * @param volumeRiseUp
	 *            音量增益值
	 */
	public WvpAudioPlayer(LinkedBlockingQueue<WvpDataMediaSample> m_PlayQueue,
			int volumeRiseUp) {
		this.m_PlayQueue = m_PlayQueue;
		this.vloumeRiseUpValue = volumeRiseUp;
		vloumeRiseUpValue += 1;
		Log.e(TAG, "--------------vloumeRiseUpValue = " + vloumeRiseUpValue);
	}

	/**
	 * 初始�?
	 * 
	 * @param frequency
	 *            采样�?电话�?000
	 * @param channelConfiguration
	 *            声道：AudioFormat.CHANNEL_CONFIGURATION_MONO（单声道�?
	 * @param audioEncoding
	 *            AudioFormat.ENCODING_PCM_16BIT（G14只支�?6位）
	 * @param streamType
	 *            AudioManager.STREAM_MUSIC(外置扬声�?,AudioManager.
	 *            STREAM_VOICE_CALL(内置扬声�?
	 */
	public void Init(int frequency, int channelConfiguration,
			int audioEncoding, int streamType) {
		_frequency = frequency;
		_channelConfiguration = channelConfiguration;
		_audioEncoding = audioEncoding;

		_playBufSize = AudioTrack.getMinBufferSize(_frequency,
				_channelConfiguration, _audioEncoding);

		_audioTrack = new AudioTrack(streamType, _frequency,
				_channelConfiguration, _audioEncoding, _playBufSize,
				AudioTrack.MODE_STREAM);

		while (AudioTrack.STATE_INITIALIZED != _audioTrack.getState()) {

			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 重置audioTrack 不停止线程
	 * 
	 * @param streamType
	 */
	public void reset(int streamType, int volumeRiseUp) {

		this.vloumeRiseUpValue = volumeRiseUp;

		AudioTrack at = _audioTrack;
		_audioTrack = null;
		at.release();

		at = new AudioTrack(streamType, _frequency, _channelConfiguration,
				_audioEncoding, _playBufSize, AudioTrack.MODE_STREAM);

		while (AudioTrack.STATE_INITIALIZED != at.getState()) {

			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		at.play();
		_audioTrack = at;
	}

	public void run() {

		isWorking = true;
		_audioTrack.play();// �?��播放
		while (isWorking) {

			WvpDataMediaSample mediaSample = null;

			try {
				mediaSample = m_PlayQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			byte[] mediaBuffer = mediaSample.data;
			short[] playBuffer = null;
			int frameCount = 0;
			// 服务器发送过来声音的类型�?
			switch (mediaSample.mediaMessageTypes) {
			case _WvpMediaMessageTypes.AUDIOG711:// 低压�?解码
				playBuffer = new short[mediaBuffer.length];
				G711.alaw2linear(mediaBuffer, playBuffer, mediaBuffer.length);
				break;
			case _WvpMediaMessageTypes.AudioAMR122:// 中压�?使用amr122 解码
				// �?��服务器返回的压缩视频长度，根据amr122压缩后的长度32 算出有几帧音频数�?
				frameCount = mediaBuffer.length / 32;
				// 这里�?60 * frameCount �?60 的意思是 shor[160] 正好�?20字节
				// 同时320字节正好是amr�?��压缩的每�?��数据长度，压缩成32字节�?
				playBuffer = new short[160 * frameCount];
				// amr122 解码后为320字节�?60 short）new 出几帧音频的总长度�?
				for (int i = 0; i < frameCount; i++) {
					short[] frameBuffer = new short[160];
					AmrCodec.getInstance().AmrDecode(mediaBuffer, i * 32, 32,
							frameBuffer);
					System.arraycopy(frameBuffer, 0, playBuffer, i * 160, 160);
				}
				// Log.d("wvpAudioPlayer",
				// "AudioAMR122 frameCount="+frameCount);
				break;
//			case _WvpMediaMessageTypes.AudioAMR475:// 高压�?使用amr475解码
//				// �?��服务器返回的压缩视频长度，根据amr122压缩后的长度32 算出有几帧音频数�?
//				frameCount = mediaBuffer.length / 13;
//				playBuffer = new short[160 * frameCount];
//				// amr122 解码后为320字节�?60 short）new 出几帧音频的总长度�?
//				for (int i = 0; i < frameCount; i++) {
//					short[] frameBuffer = new short[160];
//					AmrCodec.getInstance().AmrDecode(mediaBuffer, i * 13, 13,
//							frameBuffer);
//					System.arraycopy(frameBuffer, 0, playBuffer, i * 160, 160);
//				}
//				break;
			case _WvpMediaMessageTypes.AudioSpeex8:

				frameCount = mediaBuffer.length / 20;
				playBuffer = new short[160 * frameCount];
				// amr122 解码后为320字节�?60 short）new 出几帧音频的总长度�?
				for (int i = 0; i < frameCount; i++) {
					/* get the amount of decoded data */
					short[] frameBuffer = new short[160];
					byte[] encodedBuffer = new byte[20];
					System.arraycopy(mediaBuffer, i * 20, encodedBuffer, 0, 20);
					int decsize = Speex.getSpeexInstance(Speex.AudioSpeex8)
							.decode(encodedBuffer, frameBuffer, 20);
					System.arraycopy(frameBuffer, 0, playBuffer, i * 160, 160);
				}

				break;
			case _WvpMediaMessageTypes.AudioSpeex4:

				frameCount = mediaBuffer.length / 10;
				playBuffer = new short[160 * frameCount];
				// amr122 解码后为320字节�?60 short）new 出几帧音频的总长度�?
				for (int i = 0; i < frameCount; i++) {
					/* get the amount of decoded data */
					short[] frameBuffer = new short[160];
					byte[] encodedBuffer = new byte[10];
					System.arraycopy(mediaBuffer, i * 10, encodedBuffer, 0, 10);
					Speex.getSpeexInstance(Speex.AudioSpeex4).decode(
							encodedBuffer, frameBuffer, 10);
					System.arraycopy(frameBuffer, 0, playBuffer, i * 160, 160);
				}
				break;
			default:
				break;
			}

			// 写入数据即播放
			if (playBuffer != null && _audioTrack != null) {

				try {

					// 音量增益
					if (vloumeRiseUpValue > 1) {

						for (int i = 0; i < playBuffer.length; i++) {

							int value = (playBuffer[i] * vloumeRiseUpValue);
							if (value > Short.MAX_VALUE) {

								playBuffer[i] = Short.MAX_VALUE;
							} else if (value < Short.MIN_VALUE) {

								playBuffer[i] = Short.MIN_VALUE;
							} else {

								playBuffer[i] = (short) value;
							}
						}
					}
					// _audioTrack
					_audioTrack.write(playBuffer, 0, playBuffer.length);
					_audioTrack.flush();

				} catch (Exception e) {

				}
			}

			// 如果设置了缓冲 则缓冲一定时间的数据
			if (cacheTime > 0 && m_PlayQueue.size() == 0) {
				try {
					Thread.sleep(cacheTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			} else if (cacheTime <= 0 && get_playQueueSize() > 5) {// 如果没有设置缓冲
																	// 则检测接收队列
																	// 如果超长 则跳帧

				m_PlayQueue.clear();
			}

			// Log.d("SET_RESOURCE_SUCCESS",
			// "播放成功!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		// _audioTrack.release();

		isWorking = false;
	}

	/**
	 * 停止播放
	 */
	public void releaseAudioPlayer() {
		this.isWorking = false;
		// 下边俩句话是新加�?.5版本没有�?
		if (_audioTrack != null) {
			_audioTrack.stop();
			_audioTrack.release();
			_audioTrack = null;
		}
		m_PlayQueue = null;
	}

//	private void mixFiles(ArrayList<byte[]> array) {
//		try {
//			InputStream is1 = getResources().openRawResource(R.raw.test1);
//			List<Short> music1 = createMusicArray(is1);
//
//			InputStream is2 = getResources().openRawResource(R.raw.test2);
//			List<Short> music2 = createMusicArray(is2);
//
//			InputStream is3 = getResources().openRawResource(R.raw.test3);
//			List<Short> music3 = createMusicArray(is3);

//			completeStreams(music1, music2, music3);
//			short[] music1Array = buildShortArray(music1);
//			short[] music2Array = buildShortArray(music2);
//			short[] music3Array = buildShortArray(music3);

//			short[] output = new short[music1Array.length];
//			for (int i = 0; i < output.length; i++) {
//
//				float samplef1 = music1Array[i] / 32768.0f;
//				float samplef2 = music2Array[i] / 32768.0f;
//				float samplef3 = music3Array[i] / 32768.0f;
//
//				float mixed = samplef1 + samplef2 + samplef3;
//				// reduce the volume a bit:
//				mixed *= 0.8;
//				// hard clipping
//				if (mixed > 1.0f)
//					mixed = 1.0f;
//				if (mixed < -1.0f)
//					mixed = -1.0f;
//				short outputSample = (short) (mixed * 32768.0f);
//
//				output[i] = outputSample;
//			}
//			saveToFile(output);
//		} catch (NotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	/**
	 * 原有代码有bug,所以我加上了非空保护
	 *
     */
	public int get_playQueueSize(){
		if (m_PlayQueue!=null) {
			//Logger.e("m_PlayQueue.size:"+m_PlayQueue.size());
			return m_PlayQueue.size();
		}else {
			//Logger.e("m_PlayQueue.size:null");
			return 0;
		}
	}

}
