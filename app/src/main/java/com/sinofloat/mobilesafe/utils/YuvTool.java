package com.sinofloat.mobilesafe.utils;

import android.graphics.Point;

/**
 * Created by oyk on 2016/7/30.
 * YUV转换工具
 */
public class YuvTool {
    public static void YV12Resize(byte[] pSrc, Point szSrc, byte[] pDst, Point szDst) {
        int srcPitchY = szSrc.x, srcPitchUV = szSrc.x / 2, dstPitchY = szDst.x, dstPitchUV = szDst.x / 2;

        int rateX = (szSrc.x << 16) / szDst.x;
        int rateY = (szSrc.y << 16) / szDst.y;
        for (int i = 0; i < szDst.y; i++) {
            int srcY = i * rateY >> 16;

            for (int j = 0; j < szDst.x; j++) {
                int srcX = j * rateX >> 16;
                pDst[dstPitchY * i + j] = pSrc[srcY * srcPitchY + srcX];//*(pSrcYLine+srcX);
            }
        }
        for (int i = 0; i < szDst.y / 2; i++) {
            int srcY = i * rateY >> 16;

            for (int j = 0; j < szDst.x / 2; j++) {
                int srcX = j * rateX >> 16;

                pDst[dstPitchY * szDst.y + i * dstPitchUV + j] = pSrc[srcPitchY * szSrc.y + srcY * srcPitchUV + srcX];//*(pSrcVLine+srcX);
                pDst[dstPitchY * szDst.y + i * dstPitchUV + dstPitchUV * szDst.y / 2 + j] = pSrc[srcPitchY * szSrc.y + srcY * srcPitchUV + srcPitchUV * szSrc.y / 2 + srcX];

            }
        }
    }

    /**
     * YV12 To NV21
     * @param input
     * @param output
     * @param width
     * @param height
     */
    public static void YV12toNV21(final byte[] input, final byte[] output, final int width, final int height) {
        long startMs = System.currentTimeMillis();
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;
        final int tempFrameSize = frameSize * 5 / 4;

        System.arraycopy(input, 0, output, 0, frameSize); // Y

        for (int i = 0; i < qFrameSize; i++) {
            output[frameSize + i * 2] = input[frameSize + i]; // Cb (U)
            output[frameSize + i * 2 + 1] = input[tempFrameSize + i]; // Cr (V)
        }
    }

    /**
     * I420 To NV21
     * @param input
     * @param output
     * @param width
     * @param height
     */
    public static void I420ToNV21(final byte[] input, final byte[] output, final int width, final int height) {
        //long startMs = System.currentTimeMillis();
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;
        final int tempFrameSize = frameSize * 5 / 4;

        System.arraycopy(input, 0, output, 0, frameSize); // Y

        for (int i = 0; i < qFrameSize; i++) {
            output[frameSize + i * 2] = input[tempFrameSize + i]; // Cb (U)
            output[frameSize + i * 2 + 1] = input[frameSize + i]; // Cr (V)
        }
    }
}
