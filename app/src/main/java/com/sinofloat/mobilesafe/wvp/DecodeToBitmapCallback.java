package com.sinofloat.mobilesafe.wvp;

/**
 * Created by oyk on 2016/7/14.
 * h264解码对象生成bitmap，回调到主线程
 */
public interface DecodeToBitmapCallback {
    public void onDecoderresult(boolean success, Object obj, int width,
                                int height);

    public void onUpdateSize(int width, int height);

    // 是否超时（超时原因 ：1，socket 在一定时间内没有读取到数据。2，在一定时间内解码持续错误）
    public void onDecodeTimeOut();
}
