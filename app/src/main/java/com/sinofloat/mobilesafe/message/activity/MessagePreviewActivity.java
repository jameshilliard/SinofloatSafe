package com.sinofloat.mobilesafe.message.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.orhanobut.logger.Logger;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.BaseActivity;
import com.sinofloat.mobilesafe.replay.entity.MediaEntity;
import com.sinofloat.mobilesafe.utils.MyComponentManager;
import com.sinofloat.mobilesafe.widget.CustomPopWindow;

public class MessagePreviewActivity extends BaseActivity {
    //预览的对象
    private MediaEntity mediaEntity;
    private ImageView more,back;
    private CustomPopWindow customPopWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_preview);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mediaEntity = (MediaEntity) MyComponentManager.getTransferedData();
        more = (ImageView) findViewById(R.id.message_preview_iv_more);
        back = (ImageView) findViewById(R.id.message_preview_iv_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 showPopWindow();
            }
        });

    }

    /**
     * 弹出一个选项列表
     */
    private void showPopWindow() {
        View customView = LayoutInflater.from(MessagePreviewActivity.this).inflate(R.layout.custom_popwindow,null);
        LinearLayout share = (LinearLayout) customView.findViewById(R.id.pop_share);
        final LinearLayout download = (LinearLayout) customView.findViewById(R.id.pop_download);
        customPopWindow = new CustomPopWindow(MessagePreviewActivity.this,customView);
        customPopWindow.showPopupWindow(more);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customPopWindow.dismiss();
            }
        });
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImage();
                customPopWindow.dismiss();
            }
        });
    }

    /**
     * 保存图片到本地
     */
    private void downloadImage() {
        Logger.e("正在下载图片---------");
    }
}
