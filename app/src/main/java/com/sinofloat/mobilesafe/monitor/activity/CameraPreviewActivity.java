package com.sinofloat.mobilesafe.monitor.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.base.BaseActivity;
import com.sinofloat.mobilesafe.base.C;
import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;
import com.sinofloat.mobilesafe.monitor.fragment.ControlFragment;
import com.sinofloat.mobilesafe.monitor.fragment.ListFragment;
import com.sinofloat.mobilesafe.monitor.fragment.PositionFragment;
import com.sinofloat.mobilesafe.replay.db.MediaDB;
import com.sinofloat.mobilesafe.replay.entity.MediaEntity;
import com.sinofloat.mobilesafe.utils.YuvTool;
import com.sinofloat.mobilesafe.widget.BackgroundWroker;
import com.sinofloat.mobilesafe.widget.BackgroundWroker.WorkerEventArgs;
import com.sinofloat.mobilesafe.widget.opengl.DisplayManagerView;
import com.sinofloat.mobilesafe.widget.opengl.DisplayView;
import com.sinofloat.mobilesafe.wvp.BackgroundWorkerListener;
import com.sinofloat.mobilesafe.wvp.OnVideoFrameDecodedCallback;
import com.sinofloat.mobilesafe.wvp.OnWorkStateListener;
import com.sinofloat.mobilesafe.wvp.core.RemoteCameraWorker;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sinofloat.wvp.messages._WvpMediaMessageTypes;
import sinofloat.wvp.tools.ToastUtil;

public class CameraPreviewActivity extends BaseActivity {
    @BindView(R.id.camera_preview_statebar)
    ImageView stateBar;
    @BindView(R.id.camera_preview_title)
    TextView title;
    @BindView(R.id.camera_preview_titlebar)
    RelativeLayout titleBar;
    @BindView(R.id.camera_preview_rb_list)
    RadioButton list;
    @BindView(R.id.camera_preview_rb_control)
    RadioButton control;
    @BindView(R.id.camera_preview_rb_position)
    RadioButton position;
    @BindView(R.id.camera_preview_rg)
    RadioGroup radioGroup;
    @BindView(R.id.camera_preview_rb_full_screen)
    RadioButton fullScreen;
    @BindView(R.id.camera_preview_rl_bottom_container)
    LinearLayout bottomContainer;
    /**
     * 分屏管理
     */
    @BindView(R.id.camera_preview_displayManagerView)
    DisplayManagerView _displayManager;
    @BindView(R.id.camera_preview_loading_progressbar)
    ProgressBar progressbar;
    @BindView(R.id.camera_preview_loading_StatusText)
    TextView previewStatusText;
    @BindView(R.id.camera_preview_ll_loading)
    LinearLayout loadinglayout;

    @OnClick(R.id.camera_preview_iv_back)
    void goBack() {
        onBackPressed();
    }

    private ListFragment listFragment;
    private ControlFragment controlFragment;
    private PositionFragment positionFragment;

    /**
     * 显示画面个数
     */
    private int _displayNum = 1;

    int testCount = 0;
    /**
     * 是否正在预览
     */
    private boolean isPreview;

    /**
     * 当前预览的一帧YUV420数据
     */
    private byte[] i420;
    int frameWidth,frameHeight;
    private boolean isSaveImage = true;

    /**
     * 数据库对象
     */
    private MediaDB mediaDB;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss",
            Locale.CHINA);

    private BackgroundWroker delayQuickClickDialog;
    private RemoteCameraWorker cameraWorker;
    private RemoteCamera curEntity;
    private String curPath;
    private ArrayList<RemoteCamera> remoteCameras;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        ButterKnife.bind(this);
        cameraWorker = RemoteCameraWorker.getInstance();
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mediaDB = MediaDB.getMediaDBInstance(this);
        curPath = getIntent().getStringExtra("path");
       // remoteCameras = (ArrayList<RemoteCamera>) getIntent().getSerializableExtra("list");
        if (getIntent().getIntExtra("list",-1) == 0){
            remoteCameras = cameraWorker.cameraList;
            curEntity = cameraWorker.cameraList.get(getIntent().getIntExtra("position",-1));
        }else if(getIntent().getIntExtra("list",-1) == 1){
            remoteCameras = cameraWorker.mapCameraList;
            curEntity = cameraWorker.mapCameraList.get(getIntent().getIntExtra("position",-1));
        }
        initView();
        selectFragment(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _displayManager.onResume();
        setUpDisplayViewData();
        startWork(curEntity);
    }

    @Override
    protected void onPause() {
        super.onPause();
        _displayManager.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopWork();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void initView() {
        fullScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setFullScreen(isChecked);
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.camera_preview_rb_list:
                        selectFragment(0);
                        break;
                    case R.id.camera_preview_rb_control:
                        selectFragment(1);
                        break;
                    case R.id.camera_preview_rb_position:
                        selectFragment(2);
                        break;
                }
            }
        });

        //初始化预览配置
        _displayManager.setDisplayNum(_displayNum);// setDisplayNum must be
        // invoked at
        // first
        _displayManager.setLayout(DisplayManagerView.LayoutType_1x1);
        //默认为独占模式
        _displayManager.setExclusive(true);
        // _displayManager.setOnDisplayManagerViewListener(this);
        // test
        for (int i = 0; i < _displayNum; i++) {
            _displayManager.displayAtIndex(i).setUserData("Ch: " + (i + 1));
        }
    }

    /**
     * 设置显示View和预览worker绑定
     */
    private void setUpDisplayViewData() {
        cameraWorker.setVideoDecodedFrameCallback(new OnWorkStateListener() {

            @Override
            public void onWorkState(final int result, final String extra) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        setPreviewState(result, extra);
                    }
                });
            }
        }, new OnVideoFrameDecodedCallback() {

            @Override
            public void onDecodedResult(DisplayView displayview,
                                        boolean isSuccess, byte[] frameBuffer, int width, int height) {

                if (isSaveImage)
                {
                    i420 = frameBuffer;
                    frameWidth = width;
                    frameHeight = height;
                   // yuvImage = new YuvImage(frameBuffer, ImageFormat.YUY2,width,height,null);
                    isSaveImage = false;
                }

                if (!isPreview) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // 通知controlFragment 状态。
                            controlFragment.setPreviewState(true);
                            listFragment.onCameraPreviewResult();
                            dismissDelayQuickClickDialogDialog();
                            setPreviewState(C.STATE_DECODE_SUCCESS, "成功预览");
                        }
                    });
                }

                isPreview = true;
                if (displayview != null) {
                    displayview.displayVideo(frameBuffer, width, height, 0);
                }
            }

            @Override
            public void onDecodeTimeOut(DisplayView displayview) {

                isPreview = false;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        listFragment.onCameraPreviewResult();
                        dismissDelayQuickClickDialogDialog();
                        setPreviewState(C.STATE_DECODE_FAIL, "无信号");
                    }
                });

            }
        },_displayManager.displayAtIndex(0));//

    }


    /**
     * 设置预览状态 断线 解码超时 等状态
     *
     * @param state
     * @param extra
     */
    private void setPreviewState(int state, String extra) {
        switch (state) {
            case C.STATE_DECODE_SUCCESS:
                setLoadingPreviewState(state);
                break;
            case C.STATE_TIME_OUT_DECODE_BITMAP:

                // 提示用户成功
                break;
            case C.STATE_DECODE_FAIL:

                // 提示用户成功
                stopWork();
                setLoadingPreviewState(state);
                break;
            case C.STATE_ERROR_NONE:

                // 提示用户成功
                // ToastUtil.showSimpleToast(PictureActivity.this,
                // extra,
                // true);
                break;
            case C.STATE_ERROR_CONNECTION:

                // dismissPreviewLoadingDialog();
                setLoadingPreviewState(C.STATE_DECODE_FAIL);
                // 出现错误 UI恢复到初始状态
                ToastUtil.showSimpleToast(this, extra, true);
                break;
            case C.STATE_ERROR_RECORDER:
                // 出现错误 UI恢复到初始状态
                // 弹出对话框
                // showErrorDialog(C.ERROR_RECORDER, extra);
                break;
        }
    }


    /**
     * 请求视频数据
     */
    public void startWork(RemoteCamera entity) {

        stopWork();

        // 弹出dialog 在预览页面
        setLoadingPreviewState(C.STATE_CONNECTING);

        // 弹出dialog（屏幕中心）
        showDelayQuickClickDialog();

        this.curEntity = entity;
        title.setText(curEntity.displayName);
        positionFragment.setCameraId(entity.camearID);
        // cameraPreview.setImgBitmapAndDraw(null);
        // cameraPreview.showProgressView();
        //  cameraDisplayName.setText(hostActivity.getCameraGroupDir()
        //         + entity.displayName);


        // 工作线程执行
        cameraWorker.startCameraPreviewWork(true,
                App.ConnectionSet.ServiceAddress,
                App.ConnectionSet.SafeServicePort,
                App.ConnectionSet.CurrentUserID,
                App.ConnectionSet.CurrentUserPWD,
                App.ConnectionSet.UserGroupName, curEntity);
    }

    /**
     * 停止视频数据
     */
    public void stopWork() {

        if (isPreview) {
            controlFragment.setPreviewState(false);
        }

        isPreview = false;
        setLoadingPreviewState(C.STATE_DECODE_FAIL);

        if (cameraWorker != null) {
            cameraWorker.stopWork();
        }
    }


    /**
     * 预览画面里中间的加载预览数据dialog 和一个 textView
     *
     * @param state
     */
    private void setLoadingPreviewState(int state) {

        switch (state) {
            case C.STATE_CONNECTING:
                // 连接中。。。
                loadinglayout.setVisibility(View.VISIBLE);
                progressbar.setVisibility(View.VISIBLE);
                //     toLandPreview.setVisibility(View.INVISIBLE);
                previewStatusText.setText(C.CAMERA_PREVIEW_LOADING);
                break;
            case C.STATE_DECODE_FAIL:
                // FIXME 设置一个黑色的背景图
                loadinglayout.setVisibility(View.VISIBLE);
                progressbar.setVisibility(View.INVISIBLE);
                //     toLandPreview.setVisibility(View.INVISIBLE);
                previewStatusText.setText(C.CAMERA_PREVIEW_ERROR_SHOW_MSG);
                break;
            case C.STATE_DECODE_SUCCESS:

                //   toLandPreview.setVisibility(View.VISIBLE);
                loadinglayout.setVisibility(View.INVISIBLE);
                previewStatusText.setText(null);
                progressbar.setVisibility(View.INVISIBLE);
                break;
        }
    }


    /**
     * 弹出 预览加载dialog
     */
    private void showDelayQuickClickDialog() {
        delayQuickClickDialog = new BackgroundWroker(this, false, null,
                "正在加载视频数据...", BackgroundWroker.BUTTON_STYLE_NONE,
                delayQuickCklickListener);
        delayQuickClickDialog.startWork(null);
    }

    /**
     * 关闭对话框
     */
    private void dismissDelayQuickClickDialogDialog() {
        if (delayQuickClickDialog != null) {
            delayQuickClickDialog.stopWork();
            delayQuickClickDialog = null;
        }
    }


    /**
     * 在用户点击一个预览摄像头时 延迟500毫秒才容许第二次点击。
     */
    private BackgroundWorkerListener delayQuickCklickListener = new BackgroundWorkerListener() {

        @Override
        public void onWorking(Object sender, WorkerEventArgs args)
                throws InterruptedException {

			while (!isPreview) {
            Thread.sleep(500);
			}
        }

        @Override
        public void onComplete(Object sender, WorkerEventArgs args) {

            // 执行完结果通知列表fragment 设置点击事件
            listFragment.onCameraPreviewResult();
            dismissDelayQuickClickDialogDialog();
        }
    };


    //选中一个Fragment
    private void selectFragment(int position) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (listFragment == null) {
            listFragment = new ListFragment();
            ft.add(R.id.camera_preview_fl_bottom_container, listFragment);
        }
        if (controlFragment == null) {
            controlFragment = new ControlFragment();
            ft.add(R.id.camera_preview_fl_bottom_container, controlFragment);
        }
        if (positionFragment == null) {
            positionFragment = new PositionFragment();
            ft.add(R.id.camera_preview_fl_bottom_container, positionFragment);
        }
        hideFragment(ft);
        switch (position) {
            case 0:
                ft.show(listFragment);
                list.setBackgroundResource(R.drawable.ic_monitor_tab_bg);
                break;
            case 1:
                ft.show(controlFragment);
                control.setBackgroundResource(R.drawable.ic_monitor_tab_bg);
                break;
            case 2:
                ft.show(positionFragment);
                this.position.setBackgroundResource(R.drawable.ic_monitor_tab_bg);
                break;
        }
        ft.commit();
    }

    //隐藏所有Fragment
    private void hideFragment(FragmentTransaction transaction) {
        if (listFragment != null) {
            transaction.hide(listFragment);
            list.setBackground(null);
        }
        if (controlFragment != null) {
            transaction.hide(controlFragment);
            control.setBackground(null);
        }
        if (positionFragment != null) {
            transaction.hide(positionFragment);
            position.setBackground(null);
        }
    }

    /**
     * 保存截屏 图像
     */
    public void saveCameraPreviewScreenShot() {
        if (App.fileSetting.isSDCardReady()) {
            if (!App.fileSetting.isAppFileHomeDirCreated) {
                App.fileSetting.creatAppFileHomeDir();
            }
            // FIXME 2.3的有些手机 插上数据线后SD卡不可用 拔掉线 没有SD卡的手机也会说SD卡已经挂载上。是系统的bug
            // motorola就是这样。
            // ToastUtil.showSimpleToast(getActivity(), "SD ready", true);
        } else {

            ToastUtil.showSimpleToast(this, "SD卡不可用，请检查", true);
            return;
        }
         isSaveImage = true;
        if (i420 != null) {

            // 弹出dialog

            //      controlFragment.showSaveScreenShotDialog("正在保存");
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // 转换时间戳为年月日时分秒毫秒
                    long timeStamp = System.currentTimeMillis();
                    String fileName = sdf.format(new Date(timeStamp));
                    String jpgFileFath = App.fileSetting.safeScreenshotDir
                            + curEntity.displayName+"_"+fileName + ".jpg";
                    try {
                        FileOutputStream picsWriter = new FileOutputStream(
                                jpgFileFath);
                        //将I420数据转换成NV21
                        byte[] nv21 = new byte[i420.length];
                        YuvTool.I420ToNV21(i420,nv21,frameWidth,frameHeight);
                        //YuvImage只支持NV21和YVY2格式
                        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21,frameWidth,frameHeight,null);
                        yuvImage.compressToJpeg(
                                new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()),
                                80, picsWriter);
                        i420 = null;
                        picsWriter.flush();
                        picsWriter.close();

                        MediaEntity entity = new MediaEntity();
                        entity.mediaCreatUserNm = curEntity.displayName;
                        entity.mediaCreatTime = timeStamp;
                        entity.mediaType = _WvpMediaMessageTypes.PICTUREJPEG;
                        entity.MediaStoreLocation = jpgFileFath;
                        mediaDB.add(entity);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(CameraPreviewActivity.this,"保存成功",Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                              //  ToastUtil.showSimpleToast(CameraPreviewActivity.this, "保存成功", true);
                            }
                        });
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        //                     controlFragment.dismissSaveScreenShotDialog();
                    }
                }
            }).start();
        }
    }

    /**
     * 切换全屏模式
     */
    public void setFullScreen(boolean isFullScreen) {
        if (isFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            fullScreen.setChecked(false);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        //  Logger.d(isFullScreen);
    }

    /**
     * 设置切换横竖屏不走生命周期后可以通过这个函数来改变布局
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            stateBar.setVisibility(View.GONE);
            titleBar.setVisibility(View.GONE);
            bottomContainer.setVisibility(View.GONE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //Logger.e("我是横屏");

        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            stateBar.setVisibility(View.VISIBLE);
            titleBar.setVisibility(View.VISIBLE);
            bottomContainer.setVisibility(View.VISIBLE);
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //  Logger.e("我是竖屏");
        }
    }

    @Override
    public void onBackPressed() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            super.onBackPressed();
        }

    }

    /**
     * 获取当前摄像头路径
     */
    public String getCurCameraPath() {
        return curPath;
    }

    public ArrayList<RemoteCamera> getRemoteCameras(){
        return remoteCameras;
    }
}
