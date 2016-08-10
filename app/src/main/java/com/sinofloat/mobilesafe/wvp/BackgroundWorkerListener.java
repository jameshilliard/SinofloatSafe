package com.sinofloat.mobilesafe.wvp;

import com.sinofloat.mobilesafe.widget.BackgroundWroker.WorkerEventArgs;

/**
 * Created by oyk on 2016/7/14.
 * 工作线程获取服务器数据的接口
 */
public interface BackgroundWorkerListener {
    public void onWorking(Object sender, WorkerEventArgs args)
            throws InterruptedException;

    public void onComplete(Object sender, WorkerEventArgs args);
}
