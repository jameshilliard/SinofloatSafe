package com.sinofloat.mobilesafe.replay.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.replay.entity.MediaEntity;

import java.util.List;

/**
 * Created by oyk on 2016/7/31.
 */
public class PreviewListAdapter extends BaseQuickAdapter<MediaEntity> {
    public PreviewListAdapter(int layoutResId, List<MediaEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, MediaEntity mediaEntity) {
        baseViewHolder.setText(R.id.monitor_rlv_item_cameraDisplayNameText,mediaEntity.mediaId);
    }
}
