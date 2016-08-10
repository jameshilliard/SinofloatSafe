package com.sinofloat.mobilesafe.monitor.fragment;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.base.BaseFragment;
import com.sinofloat.mobilesafe.base.C;
import com.sinofloat.mobilesafe.monitor.activity.CameraPreviewActivity;
import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;
import com.sinofloat.mobilesafe.widget.BackgroundWroker;
import com.sinofloat.mobilesafe.widget.BackgroundWroker.WorkerEventArgs;
import com.sinofloat.mobilesafe.wvp.BackgroundWorkerListener;
import com.sinofloat.mobilesafe.wvp.core.RemoteCameraWorker;

import butterknife.BindView;
import butterknife.ButterKnife;
import sinofloat.wvp.messages._MoveTypes;
import sinofloat.wvp.tools.ToastUtil;

/**
 * Created by oyk on 2016/7/13.
 */
public class ControlFragment extends BaseFragment implements View.OnClickListener, View.OnTouchListener {
    @BindView(R.id.monitor_control_bitrate)
    CheckBox bitRate;
    /*@BindView(R.id.monitor_control_up_left)
    ImageView upLeft;*/
    @BindView(R.id.monitor_control_up)
    ImageView up;
    @BindView(R.id.monitor_control_shot)
    ImageView shot;
  /*  @BindView(R.id.monitor_control_up_right)
    ImageView upRight;*/
    @BindView(R.id.monitor_control_defend)
    CheckBox defend;
    @BindView(R.id.monitor_control_left)
    ImageView left;
    @BindView(R.id.monitor_control_zoom_in)
    ImageView zoomIn;
    @BindView(R.id.monitor_control_right)
    ImageView right;
    @BindView(R.id.monitor_control_mute)
    CheckBox mute;
    /*@BindView(R.id.monitor_control_down_left)
    ImageView downLeft;*/
    @BindView(R.id.monitor_control_down)
    ImageView down;
    @BindView(R.id.monitor_control_zoom_out)
    ImageView zoomOut;
   /* @BindView(R.id.monitor_control_down_right)
    ImageView downRight;*/
    @BindView(R.id.monitor_control_definition_rg)
    RadioGroup bitRateRadioGroup;
    @BindView(R.id.monitor_control_definition)
    LinearLayout definition;
    @BindView(R.id.monitor_control_talk)
    ImageView talk;
    @BindView(R.id.monitor_control_bitrate_tv)
    TextView monitorControlBitrateTv;
    @BindView(R.id.monitor_control_shot_tv)
    TextView monitorControlShotTv;
    @BindView(R.id.monitor_control_defend_tv)
    TextView monitorControlDefendTv;
    @BindView(R.id.monitor_control_zoom_in_tv)
    TextView monitorControlZoomInTv;
    @BindView(R.id.monitor_control_mute_tv)
    TextView monitorControlMuteTv;
    @BindView(R.id.monitor_control_zoom_out_tv)
    TextView monitorControlZoomOutTv;
    /**
     * 摄像头状态 （云台还是不是 ）
     */
    @BindView(R.id.monitor_control_cameraStateTextV)
    TextView cameraStateTextV;

    /**
     * 是否在预览
     */
    private boolean isPreview;

    /**
     * 按下对讲按钮是否显示 如果当前是移动设备 默认是显示对讲按钮 隐藏文本编辑 发送
     */
    private boolean istalkShow = true;
    /**
     * 等待加载框
     */
    private BackgroundWroker loadingDialog;

    private BackgroundWroker SavingScreenShotDialog;

    private RemoteCameraWorker cameraWorker;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraWorker = RemoteCameraWorker.getInstance();
        sharedPreferences = getActivity().getSharedPreferences(C.SETTING, getActivity().MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitor_control, null);
        ButterKnife.bind(this, view);

        talk.setOnTouchListener(this);
        zoomIn.setOnClickListener(this);
        shot.setOnClickListener(this);
        zoomOut.setOnClickListener(this);

        bitRate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    definition.setVisibility(View.VISIBLE);
                } else {
                    definition.setVisibility(View.GONE);
                }
            }
        });

        int quality = sharedPreferences.getInt("quality", 1);
        switch (quality) {
            case 0:
                bitRateRadioGroup.check(R.id.monitor_control_definition_rb_fd);
                bitRate.setBackgroundResource(R.drawable.ic_control_fd_select);
                break;
            case 1:
                bitRateRadioGroup.check(R.id.monitor_control_definition_rb_sd);
                bitRate.setBackgroundResource(R.drawable.ic_control_bitrate_select);
                break;
            case 2:
                bitRateRadioGroup.check(R.id.monitor_control_definition_rb_hd);
                bitRate.setBackgroundResource(R.drawable.ic_control_hd_select);
                break;
        }
        bitRate.postInvalidate();
        bitRateRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.monitor_control_definition_rb_fd:
                        bitRate.setBackgroundResource(R.drawable.ic_control_fd_select);
                        editor.putInt("quality", 0);
                        editor.commit();
                        break;
                    case R.id.monitor_control_definition_rb_sd:
                        bitRate.setBackgroundResource(R.drawable.ic_control_bitrate_select);
                        editor.putInt("quality", 1);
                        editor.commit();
                        break;
                    case R.id.monitor_control_definition_rb_hd:
                        bitRate.setBackgroundResource(R.drawable.ic_control_hd_select);
                        editor.putInt("quality", 2);
                        editor.commit();
                        break;
                }
                bitRate.setChecked(false);
                definition.setVisibility(View.GONE);
            }
        });
        defend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("isDefend", isChecked);
                editor.commit();
            }
        });
        mute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("isVoice", isChecked);
                editor.commit();
            }
        });

        up.setOnClickListener(this);
        down.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);

      /*  upLeft.setOnClickListener(this);
        upRight.setOnClickListener(this);
        downLeft.setOnClickListener(this);
        downRight.setOnClickListener(this);*/

        mute.setChecked(sharedPreferences.getBoolean("isVoice", false));
        defend.setChecked(sharedPreferences.getBoolean("isDefend", false));

        return view;
    }

    public void setPreviewState(boolean isPreview) {

        this.isPreview = isPreview;
        setCurUI();
    }

    private void setCurUI() {

        if (cameraWorker == null) {
            return;
        }

        setNoPreviewUI();

        if (!isPreview) {

            if (loadingDialog == null) {
//				showLoadingDialog();FIXME
            }
            return;
        }

        RemoteCamera curRomoteCamera = cameraWorker.getCurRemoteCamera();
        if (curRomoteCamera == null) {
            return;
        }

        if (curRomoteCamera.camearType.equals(RemoteCamera.CAMEAR_TYPE_PTZ)) {

            setPtzUI();
        } else if (curRomoteCamera.camearType
                .equals(RemoteCamera.CAMEAR_TYPE_NO_PTZ)) {

            setNoPtzUI(false);
        } else if (curRomoteCamera.camearType
                .equals(RemoteCamera.CAMEAR_TYPE_MOBILE)) {

            // 移动设备
            setNoPtzUI(true);
        }
    }


    /**
     * 什么也咩有
     */
    private void setNoPreviewUI() {
        setOrientationCtr(false);
    }

    /**
     * 有云台的UI
     */
    private void setPtzUI() {
        // 没有对讲按钮 和 发短信 其他都有
        setOrientationCtr(true);
        talk.setEnabled(false);
        cameraStateTextV.setText("固定摄像头-可以云台控制");
    }

    /**
     * 没有云台的UI
     */
    private void setNoPtzUI(boolean isMobile) {

        setOrientationCtr(false);
        if (isMobile) {
            // 有对讲按钮 有发短信
            talk.setEnabled(true);
            cameraStateTextV.setText("手机摄像头");
        } else {
            // 只有截屏（保存图片 功能）
            shot.setEnabled(true);
            cameraStateTextV.setText("固定摄像头-无云台控制");
        }
    }

    /**
     * 设置所有按钮按钮时否能点击
     *
     * @param isEnable
     */
    private void setOrientationCtr(boolean isEnable) {
        // bitRate.setEnabled(isEnable);
        shot.setEnabled(isEnable);
        //defend.setEnabled(isEnable);
        zoomIn.setEnabled(isEnable);
        // mute.setEnabled(isEnable);
        zoomOut.setEnabled(isEnable);
        talk.setEnabled(isEnable);
        up.setEnabled(isEnable);
        down.setEnabled(isEnable);
        left.setEnabled(isEnable);
        right.setEnabled(isEnable);
        zoomIn.setEnabled(isEnable);
        zoomOut.setEnabled(isEnable);
      /*  upLeft.setEnabled(isEnable);
        upRight.setEnabled(isEnable);
        downLeft.setEnabled(isEnable);
        downRight.setEnabled(isEnable);*/
        if (isEnable){
            monitorControlBitrateTv.setTextColor(Color.WHITE);
            monitorControlShotTv.setTextColor(Color.WHITE);
            monitorControlDefendTv.setTextColor(Color.WHITE);
            monitorControlMuteTv.setTextColor(Color.WHITE);
            monitorControlZoomInTv.setTextColor(Color.WHITE);
            monitorControlZoomOutTv.setTextColor(Color.WHITE);
        }else {
            monitorControlBitrateTv.setTextColor(Color.GRAY);
            monitorControlShotTv.setTextColor(Color.GRAY);
            monitorControlDefendTv.setTextColor(Color.GRAY);
            monitorControlMuteTv.setTextColor(Color.GRAY);
            monitorControlZoomInTv.setTextColor(Color.GRAY);
            monitorControlZoomOutTv.setTextColor(Color.GRAY);

        }

    }


    /**
     * 显示一个dialog 正在保存图片
     *
     * @param showMsg
     */
    public void showSaveScreenShotDialog(String showMsg) {

        if (SavingScreenShotDialog == null) {
            if (getActivity() == null) {
                return;
            }
            SavingScreenShotDialog = new BackgroundWroker(getActivity(), false, null,
                    "正在保存截屏", BackgroundWroker.BUTTON_STYLE_NONE,
                    saveScreenShotListener);
            SavingScreenShotDialog.startWork(null);
        }

    }


    public void dismissSaveScreenShotDialog() {

        if (SavingScreenShotDialog != null) {
            SavingScreenShotDialog.stopWork();
        }
        SavingScreenShotDialog = null;
    }

    /**
     * 保存截屏图片SD卡本地写入监听
     */
    private BackgroundWorkerListener saveScreenShotListener = new BackgroundWorkerListener() {

        @Override
        public void onWorking(Object sender, WorkerEventArgs args)
                throws InterruptedException {

            while (true) {

                Thread.sleep(500);
            }
        }

        @Override
        public void onComplete(Object sender, WorkerEventArgs args) {

        }
    };


    @Override
    public void onClick(View v) {

        if (!isPreview) {
            ToastUtil.showSimpleToast(getActivity(), "未连接到服务，请检查", true);
            return;
        }

        if (v == up) {
            // 上
            cameraWorker.sendCommand(_MoveTypes.Up, "");
        } else if (v == down) {
            // 下
            cameraWorker.sendCommand(_MoveTypes.Down, "");
        } else if (v == left) {
            // 左
            cameraWorker.sendCommand(_MoveTypes.Left, "");
        } else if (v == right) {
            // 右
            cameraWorker.sendCommand(_MoveTypes.Right, "");
        } /*else if (v == upLeft) {
            // 左上
            cameraWorker.sendCommand(_MoveTypes.Left, "");
            cameraWorker.sendCommand(_MoveTypes.Up, "");
        } else if (v == upRight) {
            // 右上
            cameraWorker.sendCommand(_MoveTypes.Right, "");
            cameraWorker.sendCommand(_MoveTypes.Up, "");
        } else if (v == downLeft) {
            // 左下
            cameraWorker.sendCommand(_MoveTypes.Left, "");
            cameraWorker.sendCommand(_MoveTypes.Down, "");
        } else if (v == downRight) {
            // 右下
            cameraWorker.sendCommand(_MoveTypes.Right, "");
            cameraWorker.sendCommand(_MoveTypes.Down, "");
        } */else if (v == zoomIn) {
            // 放大
            cameraWorker.sendCommand(_MoveTypes.Forward, "");
        } else if (v == zoomOut) {
            // 缩小
            cameraWorker.sendCommand(_MoveTypes.Back, "");
        } else if (v == shot) {
            // 截屏 保存图片
            CameraPreviewActivity cameraPreviewActivity = (CameraPreviewActivity) getActivity();
            cameraPreviewActivity.saveCameraPreviewScreenShot();

        } /*else if (v == bitRate) {
            // 切换码率

        } */ /*else if (v == defend) {
            // 布防

        }*/  /*else if (v == send) {

            String text = msgEdit.getText().toString().trim();
            cameraWorker.sendTextMessage(null, null, text);
            msgEdit.setText(null);
        } else if (v == changeMode) {

            istalkShow = !istalkShow;

            setBottomMenuUI();
        }*/

    }

    /**
     * 设置录音状态
     *
     * @param isRecord
     */
    private void setRecordState(int action, boolean isRecord) {
        if (cameraWorker != null) {

            cameraWorker.setRecorderOpen(isRecord);

            if (isRecord && cameraWorker.isWorking()) {
                // 提示可以说话 正在录音
                resetUI(action, R.drawable.hint_pressed, "正在对讲中...");
            } else if (isRecord && !cameraWorker.isWorking()) {
                ToastUtil.showSimpleToast(getActivity(), "无信号，对讲呼叫失败", true);
            } else if (!isRecord) {

                // 网络连接失败 不能讲话
                resetUI(action, R.drawable.hint_normal, "按下开始对讲");
            }
        }
    }

    // 改变对讲按钮等图标。专门处理对讲按钮按下都的一切操作。
    private void resetUI(int buttonState, int lightState, String statustext) {
        switch (buttonState) {

            case MotionEvent.ACTION_UP:
                App.PlaySound(App.SoundButtonUpId, null);
                break;
            case MotionEvent.ACTION_DOWN:

                App.PlaySound(App.SoundButtonDownId, null);

                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == talk) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                talk.setImageResource(R.drawable.ic_control_talk_pressed);
                setRecordState(action, true);
            } else if (action == MotionEvent.ACTION_UP) {
                talk.setImageResource(R.drawable.ic_control_talk);
                setRecordState(action, false);
            }
        }
        return true;
    }
}
