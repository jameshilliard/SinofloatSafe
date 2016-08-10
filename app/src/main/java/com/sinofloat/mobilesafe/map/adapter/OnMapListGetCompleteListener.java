package com.sinofloat.mobilesafe.map.adapter;

import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;

import java.util.ArrayList;

/**
 * Created by oyk on 2016/8/5.
 */
public interface OnMapListGetCompleteListener {
    void OnMapComplete(ArrayList<RemoteCamera> cameraItem);
}
