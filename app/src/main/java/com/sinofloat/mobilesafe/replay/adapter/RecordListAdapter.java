package com.sinofloat.mobilesafe.replay.adapter;

import android.graphics.Color;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.replay.entity.CameraRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by oyk on 2016/8/3.
 *
 */
public class RecordListAdapter extends BaseQuickAdapter<CameraRecord> {
    public RecordListAdapter(int layoutResId, List<CameraRecord> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, CameraRecord cameraRecord) {
        if (cameraRecord.selected) {
            baseViewHolder.setBackgroundColor(R.id.record_rlv_item, Color.parseColor("#2c495c"));
        }else {
            baseViewHolder.setBackgroundColor(R.id.record_rlv_item, Color.parseColor("#25262a"));
        }
        String day = cameraRecord.getBeginTime();
        String date_year = day.substring(0,4);
        String date_month = day.substring(4,6);
        String date_day = day.substring(6,8);
        String date = date_year+"-"+date_month+"-"+date_day+":";
        baseViewHolder.setText(R.id.record_item_date,date);
        long durationTime = Str2TimeStamp(cameraRecord.getEndTime())-Str2TimeStamp(cameraRecord.getBeginTime());
        long time = (int)(durationTime/1000L);
        long ss = time % 60;
        long mm = (time/60) % 60;
        long HH = time/(60*60) % 60;
        String strHH = HH < 10 ? "0"+HH : ""+HH;
        String strmm = mm < 10 ? "0"+mm : ""+mm;
        String strss = ss < 10 ? "0"+ss : ""+ss;
        String duration = strHH+":"+strmm+":"+strss;
        baseViewHolder.setText(R.id.record_item_duration,duration);
        String beginTime_HH = cameraRecord.getBeginTime().substring(8,10);
        String beginTime_mm = cameraRecord.getBeginTime().substring(10,12);
        String beginTime_ss = cameraRecord.getBeginTime().substring(12);
        String beginTime = beginTime_HH+":"+beginTime_mm+":"+beginTime_ss;
        String endTime_HH = cameraRecord.getEndTime().substring(8,10);
        String endTime_mm = cameraRecord.getEndTime().substring(10,12);
        String endTime_ss = cameraRecord.getEndTime().substring(12);
        String endTime = endTime_HH+":"+endTime_mm+":"+endTime_ss;
        baseViewHolder.setText(R.id.record_item_description,cameraRecord.getDescription());
        baseViewHolder.setText(R.id.record_item_begin_time,beginTime);
        baseViewHolder.setText(R.id.record_item_end_time,endTime);
    }

    public long Str2TimeStamp(String str){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        long time = 0;
        try {
            Date date = sdf.parse(str);
            time = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }
}
