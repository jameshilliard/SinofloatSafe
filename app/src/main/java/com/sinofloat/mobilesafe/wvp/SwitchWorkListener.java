package com.sinofloat.mobilesafe.wvp;

import sinofloat.wvp.messages.WvpMessage;

/**
 * Created by oyk on 2016/7/14.
 * 拆分消息接口
 */
public interface SwitchWorkListener {
    public void OnFindMessage(WvpMessage message, short sessionId);
}
