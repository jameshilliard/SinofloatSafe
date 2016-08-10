package com.sinofloat.mobilesafe.monitor.entity;

/**
 * Created by oyk on 2016/8/1.
 *
 */
public class CameraPosition {
    /**
     * 真实值
     */
    public String positionKey;

    public String getPositionValue() {
        return positionValue;
    }

    public void setPositionValue(String positionValue) {
        this.positionValue = positionValue;
    }

    /**
     * 显示值
     */
    public String positionValue;

    public String getPositionKey() {
        return positionKey;
    }

    public void setPositionKey(String positionKey) {
        this.positionKey = positionKey;
    }
}
