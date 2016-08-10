package com.sinofloat.mobilesafe.map.adapter;

import android.graphics.Color;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;

import java.util.List;

/**
 * Created by oyk on 2016/8/1.
 */
public class MapAdapter extends BaseMultiItemQuickAdapter<RemoteCamera> {
    public static final int GRIDLAYOUT = 1;
    public static final int LINEARLAYOUT = 0;

    public MapAdapter(List data) {
        super(data);
        addItemType(LINEARLAYOUT, R.layout.item_map_remote_camera_list);
        addItemType(GRIDLAYOUT, R.layout.item_map_remote_camera_grid);

    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, RemoteCamera remoteCamera) {
        switch (baseViewHolder.getItemViewType()) {
            case GRIDLAYOUT:
                baseViewHolder.setText(R.id.map_rlv_item_cameraDisplayNameText_grid, remoteCamera.displayName);
                // baseViewHolder.setText(R.id.map_rlv_item_cameraTimeStamp_grid, remoteCamera.displayName);
                //设置默认占位图片,反正图片被复用到其他view中
                if (remoteCamera.bitmap == null) {
                    baseViewHolder.setImageResource(R.id.map_rlv_item_cameraPreview_grid, R.drawable.ic_item_defalult);
                } else {
                    ImageView imageView = baseViewHolder.getView(R.id.map_rlv_item_cameraPreview_grid);
                    imageView.setImageBitmap(remoteCamera.bitmap);
                }
                break;
            case LINEARLAYOUT:
                baseViewHolder.setText(R.id.map_rlv_item_cameraDisplayNameText, remoteCamera.displayName);
                baseViewHolder.setBackgroundColor(R.id.map_rlv_item, Color.parseColor("#25262a"));
                if (remoteCamera.selected) {
                    baseViewHolder.setBackgroundColor(R.id.map_rlv_item, Color.parseColor("#2c495c"));
                }
                if (remoteCamera.camearType.equals(RemoteCamera.CAMEAR_TYPE_CAMGROUP)) {
                    baseViewHolder.setImageResource(R.id.map_rlv_item_cameraTypeImg, R.drawable.ic_top_group);
                } else if (remoteCamera.camearType.equals(RemoteCamera.CAMEAR_TYPE_MOBILE)) {
                    baseViewHolder.setImageResource(R.id.map_rlv_item_cameraTypeImg, R.drawable.ic_top_mobile);
                } else if (remoteCamera.camearType.equals(RemoteCamera.CAMEAR_TYPE_PTZ)) {
                    baseViewHolder.setImageResource(R.id.map_rlv_item_cameraTypeImg, R.drawable.ic_top_ptz_camera);
                } else if (remoteCamera.camearType.equals(RemoteCamera.CAMEAR_TYPE_PTZ)) {
                    baseViewHolder.setImageResource(R.id.map_rlv_item_cameraTypeImg, R.drawable.ic_top_camera);
                }
                break;
        }
    }

}
