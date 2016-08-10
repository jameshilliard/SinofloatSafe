package com.sinofloat.mobilesafe.wvp;

import com.sinofloat.mobilesafe.widget.opengl.DisplayView;

/**
 * Created by oyk on 2016/7/14.
 *  解码回调接口
 */
public interface OnVideoFrameDecodedCallback {

    public void onDecodeTimeOut(DisplayView displayView);// 解码超时（解码一定时间都没有解码出一帧图像）

    public void onDecodedResult(DisplayView displayView, boolean isSuccess,
                                byte[] frameBuffer, int width, int height);
}
