package com.sinofloat.mobilesafe.setting;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.BaseActivity;

public class AboutActivity extends BaseActivity {
    private ImageView goBack;
    private TextView version;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        goBack  = (ImageView) findViewById(R.id.about_iv_back);
        version = (TextView) findViewById(R.id.about_version);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        version.setText("当前版本:"+getResources().getString(R.string.version));
    }
}
