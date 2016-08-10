package com.sinofloat.mobilesafe.wvp.decode;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.nio.ByteBuffer;

/**
 * Created by oyk on 2016/7/23.
 * 系统硬编码解码操作
 */
public class HardDecode {
    private static final String FILE_PATH = Environment.getExternalStorageDirectory() + "/testHard.h264";
    private static final String OUT_PATH = Environment.getExternalStorageDirectory() + "/testHard.YUV";
    private String mime = "Video/AVC";
    public final static int width = 1280;
    public final static int height = 720;
    //设置解码超时时间
    public final static long TIME_OUT = 10 * 1000;
    private MediaCodec decoder;
    private BufferedInputStream bis;

    public HardDecode() {
    }

    /**************************
     * 初始化解码器
     **********************************/
    public void initDecode() {
        try {
            MediaFormat format = MediaFormat.createVideoFormat(mime, width, height);
           /* byte[] header_sps = { 0, 0, 0, 1, 103, 100, 0, 40, -84, 52,
                    -59, 1, -32, 17, 31, 120, 11, 80, 16, 16, 31, 0, 0, 3,
                    3, -23, 0, 0, -22, 96, -108 };
            byte[] header_pps = { 0, 0, 0, 1, 104, -18, 60, -128 };
            format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));*/
            decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
            decoder.configure(format, null, null, 0);
            decoder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (decoder == null) {
            Logger.e("Can't find video info!");
            return;
        }

    }

    public int decode(byte[] bitstream, int bitstreamLength, byte[] frameBuffer, int bufferSize) {
        ByteBuffer[] inputBuffers = decoder.getInputBuffers();
        ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
        BufferInfo info = new BufferInfo();
        long startMs = System.currentTimeMillis();
        while (true) {
                int inIndex = decoder.dequeueInputBuffer(10000);
                if (inIndex >= 0) {
                    ByteBuffer buffer = inputBuffers[inIndex];
                    buffer.clear();
                    buffer.put(bitstream, 0, bitstreamLength);
                    decoder.queueInputBuffer(inIndex, 0, bitstreamLength, 0, 0);
                }
            int outIndex = decoder.dequeueOutputBuffer(info, 10000);
                if (outIndex>=0){
                    ByteBuffer buffer = outputBuffers[outIndex];
                    byte[] data = new byte[info.size];
                     buffer.get(data);
                    if (data != null){
                        frameBuffer  = data;
                    }

                }
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED");
                    outputBuffers = decoder.getOutputBuffers();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d("DecodeActivity", "New format " + decoder.getOutputFormat());
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
                    break;
                default:
                    ByteBuffer buffer = outputBuffers[outIndex];
                    Log.v("DecodeActivity", "We can't use this buffer but render it due to the API limit, " + buffer);

                    // We use a very simple clock to keep the video FPS, or the video
                    // playback will be too fast
                  /*  while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                        try {
                            sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }*/
                    decoder.releaseOutputBuffer(outIndex, true);
                    break;
            }
            // All decoded frames have been rendered, we can stop playing now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d("DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                return -1;
            }

        }
    }


    public void stopDecode() {
        decoder.stop();
        decoder.release();
    }


}
