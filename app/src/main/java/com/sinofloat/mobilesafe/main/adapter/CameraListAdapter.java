package com.sinofloat.mobilesafe.main.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;

import java.util.List;

/**
 * Created by oyk on 2016/7/14.
 * 摄像头或组列表的适配器
 */
public class CameraListAdapter extends BaseMultiItemQuickAdapter<RemoteCamera> {
    public static final int GRIDLAYOUT = 1;
    public static final int LINEARLAYOUT = 0;
    public CameraListAdapter(List data) {
        super(data);

        addItemType(LINEARLAYOUT, R.layout.item_monitor_remote_camera_list);
        addItemType(GRIDLAYOUT, R.layout.item_monitor_remote_camera_grid);
    }


    @Override
    protected void convert(BaseViewHolder baseViewHolder, RemoteCamera remoteCamera) {
        switch (baseViewHolder.getItemViewType()) {
            case GRIDLAYOUT:
                baseViewHolder.setText(R.id.monitor_rlv_item_cameraDisplayNameText_grid, remoteCamera.displayName);
                // baseViewHolder.setText(R.id.monitor_rlv_item_cameraTimeStamp_grid, remoteCamera.displayName);
                //设置默认占位图片,反正图片被复用到其他view中
                if (remoteCamera.bitmap == null) {
                    baseViewHolder.setImageResource(R.id.monitor_rlv_item_cameraPreview_grid, R.drawable.ic_item_defalult);
                } else {
                    ImageView imageView = baseViewHolder.getView(R.id.monitor_rlv_item_cameraPreview_grid);
                    imageView.setImageBitmap(remoteCamera.bitmap);
                }
                break;
            case LINEARLAYOUT:
                baseViewHolder.setText(R.id.monitor_rlv_item_cameraDisplayNameText, remoteCamera.displayName);
                baseViewHolder.setBackgroundColor(R.id.monitor_rlv_item, Color.parseColor("#25262a"));
                if (remoteCamera.selected) {
                    baseViewHolder.setBackgroundColor(R.id.monitor_rlv_item, Color.parseColor("#2c495c"));
                } else {

                }
                ImageView rightView = baseViewHolder.getView(R.id.monitor_rlv_item_rightImg);
                if (remoteCamera.camearType.equals(RemoteCamera.CAMEAR_TYPE_CAMGROUP)) {
                    baseViewHolder.setImageResource(R.id.monitor_rlv_item_cameraTypeImg, R.drawable.ic_top_group);
                    rightView.setVisibility(View.VISIBLE);
                } else if (remoteCamera.camearType.equals(RemoteCamera.CAMEAR_TYPE_MOBILE)) {
                    baseViewHolder.setImageResource(R.id.monitor_rlv_item_cameraTypeImg, R.drawable.ic_top_mobile);
                    rightView.setVisibility(View.GONE);
                } else if (remoteCamera.camearType.equals(RemoteCamera.CAMEAR_TYPE_PTZ)) {
                    baseViewHolder.setImageResource(R.id.monitor_rlv_item_cameraTypeImg, R.drawable.ic_top_ptz_camera);
                    rightView.setVisibility(View.GONE);
                } else if (remoteCamera.camearType.equals(RemoteCamera.CAMEAR_TYPE_NO_PTZ)) {
                    baseViewHolder.setImageResource(R.id.monitor_rlv_item_cameraTypeImg, R.drawable.ic_top_camera);
                    rightView.setVisibility(View.GONE);
                }
                break;
        }
    }


}

