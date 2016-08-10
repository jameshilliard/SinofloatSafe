package com.sinofloat.mobilesafe.replay.adapter;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.replay.entity.MediaEntity;
import com.sinofloat.mobilesafe.utils.ImageUtil;
import com.sinofloat.mobilesafe.utils.Tools;

import java.util.List;

/**
 * Created by oyk on 2016/7/14.
 *
 */
public class PictureAdapter extends BaseQuickAdapter<MediaEntity> {
    private boolean isShowCheckBox = false;
    private boolean isAllSelect = false;
    private CheckBox allCheckBox;
    public PictureAdapter(int layoutResId, List<MediaEntity> data,CheckBox allCheckBox) {
        super(layoutResId, data);
        this.allCheckBox = allCheckBox;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, final MediaEntity mediaEntity) {
        baseViewHolder.setText(R.id.replay_picture_item_tv_camera_group,mediaEntity.mediaCreatUserNm);
       // baseViewHolder.setText(R.id.replay_picture_item_tv_camera,mediaEntity.mediaId);
        baseViewHolder.setText(R.id.replay_picture_item_tv_timestamp, Tools.getDate(mediaEntity.mediaCreatTime));
        baseViewHolder.setChecked(R.id.replay_picture_item_cb,mediaEntity.selectState);
        baseViewHolder.setOnCheckedChangeListener(R.id.replay_picture_item_cb, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mediaEntity.selectState = isChecked;
            }
        });
        ImageUtil.loadLocalImage(mContext,mediaEntity.MediaStoreLocation, (ImageView) baseViewHolder.getView(R.id.replay_picture_item_iv));
        baseViewHolder.setVisible(R.id.replay_picture_item_cb,isShowCheckBox);
    }

    /**
     * 是否显示每个item的选中框
     */
    public void showCheckBox(boolean isShowCheckBox){
        this.isShowCheckBox = isShowCheckBox;
        notifyDataSetChanged();
    }

    /**
     * 是否全选
     */
    public void allSelect(boolean isAllSelect){
        this.isAllSelect = isAllSelect;
        for (MediaEntity mediaEntity: mData){
              mediaEntity.selectState = isAllSelect;
             notifyDataSetChanged();
        }
    }
    /**
     * 检查是否处于全选状态
     */
    public boolean inAllSelect(){
        for (MediaEntity mediaEntity: mData){
             if (!mediaEntity.selectState){
                 return false;
             }
        }
        return true;
    }
}
