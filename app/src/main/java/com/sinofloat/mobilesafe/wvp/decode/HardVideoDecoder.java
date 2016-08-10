package com.sinofloat.mobilesafe.wvp.decode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class HardVideoDecoder {
    // video output dimension
    static final int OUTPUT_WIDTH = 640;
    static final int OUTPUT_HEIGHT = 480;
    private byte[] outData;
    private Worker mWorker;
    private int outSize;


    public void decodeSample(byte[] data, int offset, int size,byte[] frameBuffer, int bufferSize) {
        if (mWorker != null) {
            outData = frameBuffer;
            outSize = bufferSize;
            mWorker.decodeSample(data, offset, size);
        }
    }

    public void configure(Surface surface, int width, int height, ByteBuffer csd0, ByteBuffer csd1) {
        if (mWorker != null) {
            mWorker.configure(surface, width, height, csd0, csd1);
        }
    }

    public void start() {
        if (mWorker == null) {
            mWorker = new Worker();
            mWorker.setRunning(true);
            mWorker.start();
        }
    }

    public void stop() {
        if (mWorker != null) {
            mWorker.setRunning(false);
            mWorker = null;
        }
    }

    public byte[] getOutData() {
        return outData;
    }

    private class Worker extends Thread {

        private AtomicBoolean mIsRunning = new AtomicBoolean(false);
        private MediaCodec mCodec;
        private AtomicBoolean mIsConfigured = new AtomicBoolean(false);
        private final long mTimeoutUs;

        Worker() {
            this.mTimeoutUs = 10000L;
        }

        private void setRunning(boolean isRunning) {
            mIsRunning.set(isRunning);
        }

        private void configure(Surface surface, int width, int height, ByteBuffer csd0, ByteBuffer csd1) {
            if (mIsConfigured.get()) {
                // try change pps and sps format
                //mCodec.ge
                return;
            }
            MediaFormat format = MediaFormat.createVideoFormat(VideoCodecConstants.VIDEO_CODEC, width, height);

            format.setByteBuffer("csd-0", csd0);
            format.setByteBuffer("csd-1", csd1);
            try {
                mCodec = MediaCodec.createDecoderByType(VideoCodecConstants.VIDEO_CODEC);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create codec", e);
            }
            mCodec.configure(format, surface, null, 0);
            mCodec.start();
            mIsConfigured.set(true);
        }

        @SuppressWarnings("deprecation")
        public void decodeSample(byte[] data, int offset, int size) {
            if (mIsConfigured.get() && mIsRunning.get()) {
                int index = mCodec.dequeueInputBuffer(mTimeoutUs);
                if (index >= 0) {
                    ByteBuffer buffer;
                    // since API 21 we have new API to use
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        buffer = mCodec.getInputBuffers()[index];
                        buffer.clear();
                    } else {
                        buffer = mCodec.getInputBuffer(index);
                    }
                    if (buffer != null) {
                        buffer.put(data, offset, size);
                        mCodec.queueInputBuffer(index, 0, size, 0, 0);
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                while (mIsRunning.get()) {
                    if (mIsConfigured.get()) {
                        int index = mCodec.dequeueOutputBuffer(info, mTimeoutUs);
                        if (index >= 0) {
                            // setting true is telling system to render frame onto Surface
                            ByteBuffer buffer;
                            outData= new byte[info.size];
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                buffer = mCodec.getOutputBuffers()[index];
                               // buffer.clear();
                            } else {
                                buffer = mCodec.getOutputBuffer(index);
                            }
                            buffer.position(info.offset);
                            buffer.limit(info.offset+info.size);
                            if (buffer != null) {
                                buffer.get(outData,0,info.size);
                            }
                            mCodec.releaseOutputBuffer(index, true);
                            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                                break;
                            }
                        }
                    } else {
                        // just waiting to be configured, then decode and render
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ignore) {
                        }
                    }
                }
            } finally {
                if (mIsConfigured.get()) {
                    mCodec.stop();
                    mCodec.release();
                }
            }
        }
    }
}
