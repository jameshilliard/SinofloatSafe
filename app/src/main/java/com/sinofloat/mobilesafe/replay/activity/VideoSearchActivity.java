package com.sinofloat.mobilesafe.replay.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.replay.VideoListManager;
import com.sinofloat.mobilesafe.replay.fragment.VideoFragment;

public class VideoSearchActivity extends Activity {
    private ImageView back;
    private TextView title,titlePath;
    private RecyclerView recyclerView;
    private VideoListManager videoListManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_search);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        back = (ImageView) findViewById(R.id.search_iv_back);
        title = (TextView) findViewById(R.id.search_title);
        titlePath = (TextView) findViewById(R.id.search_title_path);
        recyclerView = (RecyclerView) findViewById(R.id.search_rlv);
        videoListManager = new VideoListManager(this);
        videoListManager.initView( title,titlePath,back,recyclerView);
        videoListManager.getCameraList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoListManager.clearCameraSelectState();
    }

    public void setResult(String cameraID,String cameraName){
        Intent intent = new Intent();
        intent.putExtra("cameraID",cameraID);
        intent.putExtra("cameraName",cameraName);
       setResult(VideoFragment.RESULT,intent);
        finish();
    }
}
