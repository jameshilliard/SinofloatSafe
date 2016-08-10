package com.sinofloat.mobilesafe.main.adapter;

import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;

import java.util.ArrayList;

/**
 * Created by oyk on 2016/8/4.
 * 相机列表下载完成回调
 */
public interface OnListGetCompleteListener {
    void OnComplete(ArrayList<RemoteCamera> cameraItem);
}
