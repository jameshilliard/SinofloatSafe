package com.sinofloat.mobilesafe.wvp.tools;

//startRecording() called on an uninitialized AudioRecord.
import java.util.concurrent.TimeoutException;

import sinofloat.wvp.core.AmrCodec;
import sinofloat.wvp.core.G711;
import sinofloat.wvp.core.speex.Speex;
import sinofloat.wvp.messages.WvpDataMediaSample;
import sinofloat.wvp.messages._WvpMediaMessageTypes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * 音频发�?线程
 * 
 * @author 章晓�?
 * @version 1.1
 * 
 */
public class WvpAudioRecoder extends Thread {

	private static final String TAG = "WvpAudioRecoder";

	private int compressMode;
	/**
	 * 标记是否正在工作
	 */
	public boolean isWorking = false;

	// 录制声音
	private AudioRecord m_audioRecord;

	// 采样�?
	private int m_frequency = 8000;

	// 声道，默认是单声�?
	private int m_channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;

	// 录音样式
	private int m_audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

	private MyLinkedBlockingQueue m_RecordQueue;

	public boolean isEncoding = false;

	public static int speexPackagesize = 160;

	/**
	 * 构�?�?
	 * 
	 * @param recordQueue
	 *            录音时保存的队列(录音数据将以WvpDataMediaSample格式存储)
	 */
	public WvpAudioRecoder(MyLinkedBlockingQueue recordQueue) {
		m_RecordQueue = recordQueue;
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
	 * @throws TimeoutException
	 */
	public void Init(int frequency, int channelConfiguration, int audioEncoding)
			throws TimeoutException {

		m_frequency = frequency;
		m_channelConfiguration = channelConfiguration;
		m_audioEncoding = audioEncoding;

		// 获取�?��buffer size�?
		int minBufSize = AudioRecord.getMinBufferSize(m_frequency,
				m_channelConfiguration, m_audioEncoding);

		// 为什么这里确定是320�?因为有的手机就是返回320的�?数�?
		// 1600是自己定义的320的整数�? 和获取系统的�?��buffer size 比较
		minBufSize = minBufSize > 1600 ? minBufSize : 1600;
		// 如果获取的系统的buffer size 不是320的整数�? 那就补足它�?效果是声音好(注释还需要补充，声音编解码的原理还不�?
		minBufSize = minBufSize + (320 - (minBufSize % 320));

		// 不管 蓝牙耳机 还是手机 都用mic 没有问题�?
		if (m_audioRecord == null) {
			m_audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
					m_frequency, m_channelConfiguration, m_audioEncoding,
					minBufSize);
			int timeOut = 500;
			while (AudioRecord.STATE_INITIALIZED != m_audioRecord.getState()) {

				try {
					Thread.sleep(5);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				timeOut -= 5;

				if (timeOut <= 0) {
					releaseAudioRecorder();
					throw new TimeoutException("recorder初始化超时，请重试！");
				}

			}
		}
	}

	public void setAudioCompressMode(int compressMode) {
		this.compressMode = compressMode;
	}

	/**
	 * FIXME 这里释放recorder 专门做的处理 福州动车的破壁手太操蛋。
	 */
	private void releaseAudioRecorder() {

		int tryTimes = 20;
		if (m_audioRecord != null) {
			AudioRecord recorder = m_audioRecord;
			m_audioRecord = null;
			while (tryTimes > 0) {
				tryTimes--;
				try {
					Thread.sleep(100);
					recorder.stop();
					// Log.e("releaseAudioRecorder",
					// "releaseAudioRecorder while()");
					break;
				} catch (Exception e) {
					// Log.e("releaseAudioRecorder", e.getMessage());
					continue;
				}
			}

			recorder.release();
			recorder = null;
		}

		m_RecordQueue = null;
	}

	/**
	 * 启动接收线程
	 */
	public void run() {

		if (m_audioRecord == null) {
			return;
		}

		try {
			isWorking = true;

			m_audioRecord.startRecording();

			while (isWorking) {

				// 读取音频数据 - 每次读取1600字节�?00 short）的音频数据
				short[] pcmBuffer = new short[800];
				// 从录音硬件上读取音频数据到pcmBuffer。返回为800个short。（1600字节�?
				int pcmLength = m_audioRecord.read(pcmBuffer, 0,
						pcmBuffer.length);
				// 计算读到的帧数量 800short/160short（也就是320字节也就是amr每次要压缩的字节�?
				// 这个很关键）
				int pcmFrameCount = pcmLength / 160;

				if (!isEncoding || pcmLength <= 0 || pcmFrameCount == 0) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}

				// 判断 必须是正在上传视�?并且不是静音状�?
				WvpDataMediaSample mediaSample = null;

				// 根据音频压缩设置选择不同压缩比率
				switch (compressMode) {
				case _WvpMediaMessageTypes.AUDIOG711:// 低压�?G711

					mediaSample = new WvpDataMediaSample();
					mediaSample.data = new byte[pcmLength];
					mediaSample.mediaMessageTypes = _WvpMediaMessageTypes.AUDIOG711;
					G711.linear2alaw(pcmBuffer, 0, mediaSample.data, pcmLength);
					break;
				case _WvpMediaMessageTypes.AudioAMR122:// 中压�?使用amr122

					mediaSample = new WvpDataMediaSample();
					mediaSample.mediaMessageTypes = _WvpMediaMessageTypes.AudioAMR122;
					// amr122压缩后是32字节 �?��这里new�?��5段压缩后32字节的数�?
					// 也就�?600字节的pcm压成�?60的amr122�?��再把压缩好的数据发到服务�?
					mediaSample.data = new byte[32 * pcmFrameCount]; // AMR122

					for (int i = 0; i < pcmFrameCount; i++) {
						// new 32字节的数组是 amr�?20字节的pcm压缩后的字节长度
						byte[] amrBuffer = new byte[32];
						AmrCodec.getInstance().AmrEncode(AmrCodec.MR122,
								pcmBuffer, i * 160, 160, amrBuffer);
						System.arraycopy(amrBuffer, 0, mediaSample.data,
								i * 32, 32);
					}

					break;
//				case _WvpMediaMessageTypes.AudioAMR475:// 高压�?使用amr475
//
//					mediaSample = new WvpDataMediaSample();
//					mediaSample.mediaMessageTypes = _WvpMediaMessageTypes.AudioAMR475;
//					mediaSample.data = new byte[13 * pcmFrameCount]; // AMR475
//																		// 压缩后为13个字�?
//					for (int i = 0; i < pcmFrameCount; i++) {
//						byte[] amrBuffer = new byte[13];
//						AmrCodec.getInstance().AmrEncode(AmrCodec.MR475,
//								pcmBuffer, i * 160, 160, amrBuffer);
//						System.arraycopy(amrBuffer, 0, mediaSample.data,
//								i * 13, 13);
//					}
//					break;
				case _WvpMediaMessageTypes.AudioSpeex8:

					mediaSample = new WvpDataMediaSample();
					mediaSample.mediaMessageTypes = _WvpMediaMessageTypes.AudioSpeex8;
					mediaSample.data = new byte[20 * pcmFrameCount]; 
					
					for (int i = 0; i < pcmFrameCount; i++) {

						byte[] speexBuffer = new byte[20];

						// AudioSpeex8 编码出 20字节
						Speex.getSpeexInstance(Speex.AudioSpeex8).encode(pcmBuffer, i * 160,
								speexBuffer, 160);
						System.arraycopy(speexBuffer, 0, mediaSample.data,
								i * 20, 20);
					}

					break;
				case _WvpMediaMessageTypes.AudioSpeex4:

					mediaSample = new WvpDataMediaSample();
					mediaSample.mediaMessageTypes = _WvpMediaMessageTypes.AudioSpeex4;
					mediaSample.data = new byte[10 * pcmFrameCount]; 
					
					for (int i = 0; i < pcmFrameCount; i++) {

						byte[] speexBuffer = new byte[10];

						// AudioSpeex4 编码出 10字节
						Speex.getSpeexInstance(Speex.AudioSpeex4).encode(pcmBuffer, i * 160,
								speexBuffer, 160);
						System.arraycopy(speexBuffer, 0, mediaSample.data,
								i * 10, 10);
					}

					break;
				default:
					break;
				}
				mediaSample.timestamp = System.currentTimeMillis();
				byte[] sendData = mediaSample.toFullMessageBytes();
				if (m_RecordQueue != null) {
					m_RecordQueue.offer(sendData);
				}

			}
		} catch (Exception e) {
			Log.e(TAG, "出异常了" + e.getMessage());
		}
	}

	/**
	 * 设置声音停止录音
	 */
	public void stopMe() {
		isEncoding = false;
		isWorking = false;
		releaseAudioRecorder();
	}

}
