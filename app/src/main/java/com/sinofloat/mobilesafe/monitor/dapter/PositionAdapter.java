package com.sinofloat.mobilesafe.monitor.dapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.monitor.entity.CameraPosition;

import java.util.List;

/**
 * Created by oyk on 2016/7/15.
 *
 */
public class PositionAdapter extends BaseQuickAdapter<CameraPosition> {
    public PositionAdapter(int layoutResId, List<CameraPosition> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, CameraPosition cameraPosition) {
        baseViewHolder.setText(R.id.position_item_value,cameraPosition.getPositionValue());
    }
}
