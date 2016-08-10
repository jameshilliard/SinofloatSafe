package com.sinofloat.mobilesafe.replay.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;

import java.util.List;

/**
 * Created by oyk on 2016/7/14.
 */
public class VideoAdapter extends BaseQuickAdapter<RemoteCamera> {
    public VideoAdapter(int layoutResId, List<RemoteCamera> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, RemoteCamera remoteCamera) {
        baseViewHolder.setText(R.id.monitor_rlv_item_cameraDisplayNameText,remoteCamera.displayName);
    }
}
