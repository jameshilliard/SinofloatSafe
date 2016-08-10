package com.sinofloat.mobilesafe.wvp;

import android.graphics.Bitmap;

/**
 * Created by oyk on 2016/7/14.
 * 摄像头数据解码
 */
public interface sq_tool_h264_interface {

    public void DecodeSuccess(Bitmap bitmap);

    public void UpdateSize(int width, int height);
}
