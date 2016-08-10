package com.sinofloat.mobilesafe.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kyleduo.switchbutton.SwitchButton;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.base.BaseActivity;
import com.sinofloat.mobilesafe.base.C;
import com.sinofloat.mobilesafe.main.activity.LoginActivity;
import com.sinofloat.mobilesafe.utils.Tools;
import com.sinofloat.mobilesafe.widget.CustomDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.setting_iv_back)
    ImageView back;
    @BindView(R.id.setting_tv_login)
    TextView tvlogin;
    @BindView(R.id.setting_login)
    RelativeLayout login;
    @BindView(R.id.setting_tv_voice)
    TextView tvVoice;
    @BindView(R.id.setting_sb_voice)
    SwitchButton sbVoice;
    @BindView(R.id.setting_tv_defend)
    TextView tvDefend;
    @BindView(R.id.setting_sb_defend)
    SwitchButton sbDefend;
    @BindView(R.id.setting_tv_video)
    TextView tvVideo;
    @BindView(R.id.setting_video)
    RelativeLayout video;
    @BindView(R.id.setting_tv_version)
    TextView tvVersion;
    @BindView(R.id.setting_version)
    RelativeLayout version;
    @BindView(R.id.setting_about)
    TextView about;
    @BindView(R.id.setting_exit)
    TextView exit;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        ButterKnife.bind(this);

        back.setOnClickListener(this);
        login.setOnClickListener(this);
        video.setOnClickListener(this);
        version.setOnClickListener(this);
        about.setOnClickListener(this);
        exit.setOnClickListener(this);
        tvVersion.setText(Tools.getAppVersionName(this));
        sharedPreferences = getSharedPreferences(C.SETTING, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        tvlogin.setText(App.ConnectionSet.CurrentUserLoginName + "(" + App.ConnectionSet.ServiceAddress + ")");
        sbVoice.setChecked(sharedPreferences.getBoolean("isVoice", false));
        sbDefend.setChecked(sharedPreferences.getBoolean("isDefend", false));
        sbVoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tvVoice.setText("开启");
                    editor.putBoolean("isVoice", true);
                    editor.commit();
                } else {
                    tvVoice.setText("关闭");
                    editor.putBoolean("isVoice", false);
                    editor.commit();
                }
            }
        });
        sbDefend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tvDefend.setText("开启");
                    editor.putBoolean("isDefend", true);
                    editor.commit();
                } else {
                    tvDefend.setText("关闭");
                    editor.putBoolean("isDefend", false);
                    editor.commit();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == login) {

        }
        if (v == video) {
            showVideoDialog(this);
        }
        if (v == version) {
            Toast.makeText(this, "当前已经是最新版本了", Toast.LENGTH_SHORT).show();
        }
        if (v == about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }
        if (v == back) {
            onBackPressed();
        }
        if (v == exit) {
            Intent intent = new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    /**
     * 创建一个自定义的dialog
     *
     * @param context
     */
    private void showVideoDialog(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View videoDialogView = layoutInflater.inflate(R.layout.dialog_setting_video, null);
        final CustomDialog videoDialog = new CustomDialog(this, R.style.customDialog, videoDialogView);

        RadioGroup group = (RadioGroup) videoDialogView.findViewById(R.id.setting_rg);
        int quality = sharedPreferences.getInt("quality", 1);
        switch (quality) {
            case 0:
                group.check(R.id.setting_rb_fd);
                break;
            case 1:
                group.check(R.id.setting_rb_sd);
                break;
            case 2:
                group.check(R.id.setting_rb_hd);
                break;
        }
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.setting_rb_fd:
                        tvVideo.setText("流畅");
                        editor.putInt("quality", 0);
                        editor.commit();
                        break;
                    case R.id.setting_rb_sd:
                        tvVideo.setText("标清");
                        editor.putInt("quality", 1);
                        editor.commit();
                        break;
                    case R.id.setting_rb_hd:
                        tvVideo.setText("高清");
                        editor.putInt("quality", 2);
                        editor.commit();
                        break;
                }
                videoDialog.dismiss();
            }
        });
        videoDialog.show();

    }
}
