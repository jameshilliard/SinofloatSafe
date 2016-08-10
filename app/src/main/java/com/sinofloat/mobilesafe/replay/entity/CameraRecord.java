package com.sinofloat.mobilesafe.replay.entity;

/**
 * Created by oyk on 2016/8/3.
 *
 */
public class CameraRecord {
    private String recordKey;
    private String beginTime;
    private String endTime;
    private String description;
    public boolean selected = false;

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecordKey() {
        return recordKey;
    }

    public void setRecordKey(String recordKey) {
        this.recordKey = recordKey;
    }



}
