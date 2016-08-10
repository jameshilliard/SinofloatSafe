package com.sinofloat.mobilesafe.message.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.replay.entity.MediaEntity;

import java.util.List;

/**
 * Created by oyk on 2016/7/14.
 * 报警列表的适配器
 */
public class MessageAdapter extends BaseQuickAdapter<MediaEntity>{
    public MessageAdapter(int layoutResId, List<MediaEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, MediaEntity mediaEntity) {
        baseViewHolder.setText(R.id.message_item_eventType,mediaEntity.mediaId);
    }
}
