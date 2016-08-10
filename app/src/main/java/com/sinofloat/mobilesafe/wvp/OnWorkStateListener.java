package com.sinofloat.mobilesafe.wvp;

/**
 * Created by oyk on 2016/7/14.
 * 工作状态接口（上传 离线存储）
 */
public interface OnWorkStateListener {
    /**
     * @param result
     *            工作状态（正常，各种异常）
     * @param result
     *            具体情况
     * @param extra
     *            toast的值
     */
    public void onWorkState(int result, String extra);
}
