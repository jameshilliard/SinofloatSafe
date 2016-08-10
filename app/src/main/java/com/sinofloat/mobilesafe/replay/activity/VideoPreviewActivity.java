package com.sinofloat.mobilesafe.replay.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.base.C;
import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;
import com.sinofloat.mobilesafe.widget.BackgroundWroker;
import com.sinofloat.mobilesafe.widget.opengl.DisplayManagerView;
import com.sinofloat.mobilesafe.widget.opengl.DisplayView;
import com.sinofloat.mobilesafe.wvp.BackgroundWorkerListener;
import com.sinofloat.mobilesafe.wvp.OnVideoFrameDecodedCallback;
import com.sinofloat.mobilesafe.wvp.OnWorkStateListener;
import com.sinofloat.mobilesafe.wvp.core.RemoteCameraWorker;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sinofloat.wvp.tools.ToastUtil;

public class VideoPreviewActivity extends Activity {
    /**
     * 返回
     */
    @BindView(R.id.video_preview_iv_back)
    ImageView videoPreviewIvBack;

    @OnClick(R.id.video_preview_iv_back)
    void onClick() {
        onBackPressed();
    }

    /**
     * 全屏
     */
    @BindView(R.id.video_preview_titlebar)
    RelativeLayout titleBar;
    /**
     * 标题
     */
    @BindView(R.id.video_preview_title)
    TextView title;

    @BindView(R.id.video_preview_bottom_bar)
    RelativeLayout bottomBar;

    @BindView(R.id.video_preview_sub_rate)
    ImageView subRate;

    @BindView(R.id.video_preview_add_rate)
    ImageView addRate;
    @BindView(R.id.video_preview_rate)
    TextView tvRate;

    @BindView(R.id.video_preview_rl_top_container)
    RelativeLayout container;

    /**
     * 视频预览
     */
    @BindView(R.id.video_preview_displayManagerView)
    DisplayManagerView _displayManager;
    @BindView(R.id.video_preview_loading_progressbar)
    ProgressBar progressbar;
    @BindView(R.id.video_preview_loading_StatusText)
    TextView previewStatusText;
    @BindView(R.id.video_preview_ll_loading)
    LinearLayout loadinglayout;
    @BindView(R.id.video_preview_state)
    ImageView state;
    /**
     * 显示画面个数
     */
    private int _displayNum = 1;
    /**
     * 是否正在预览
     */
    private boolean isPreview;
    private RemoteCameraWorker cameraWorker;
    private BackgroundWroker delayQuickClickDialog;
    private RemoteCamera curEntity;
    private ArrayAdapter<String> rateAdapter;
    /**
     * 播放录像的参数
     */
    private String curCameraID, recordStart, recordEnd, rate;

    private int ratePosition = 0;
    private String[] rateArray;

    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            titleBar.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_preview);
        ButterKnife.bind(this);
        cameraWorker = RemoteCameraWorker.getInstance();
        curCameraID = getIntent().getStringExtra("cameraID");
        recordStart = getIntent().getStringExtra("recordStart");
        recordEnd = getIntent().getStringExtra("recordEnd");
        title.setText(getIntent().getStringExtra("cameraName"));
        initData();
        initView();
        showStateBar();
        subRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStateBar();
                if (ratePosition > 0) {
                    subRate.setImageResource(R.drawable.sub_rate_select);
                    addRate.setImageResource(R.drawable.add_rate_select);
                    ratePosition--;
                    tvRate.setText(rateArray[ratePosition]);
                    rate = rateArray[ratePosition].split("X")[1];
                    if (curCameraID != null && recordStart != null && recordEnd != null & rate != null) {
                        startWork(curCameraID, recordStart, recordEnd, rate);
                    }
                }else if(ratePosition == 0){
                    subRate.setImageResource(R.drawable.sub_rate_disable);
                }

            }
        });
        addRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStateBar();
                if (ratePosition < rateArray.length - 1) {
                    subRate.setImageResource(R.drawable.sub_rate_select);
                    addRate.setImageResource(R.drawable.add_rate_select);
                    ratePosition++;
                    tvRate.setText(rateArray[ratePosition]);
                    rate = rateArray[ratePosition].split("X")[1];
                    if (curCameraID != null && recordStart != null && recordEnd != null & rate != null) {
                        startWork(curCameraID, recordStart, recordEnd, rate);
                    }
                }else if(ratePosition == (rateArray.length-1)){
                    addRate.setImageResource(R.drawable.add_rate_disable);
                }

            }
        });
        _displayManager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showStateBar();
                }
                return true;
            }
        });
        /*if (curCameraID != null && recordStart != null && recordEnd != null & rate != null) {
            startWork(curCameraID, recordStart, recordEnd, rate);
        }*/


        /* spinnerRate.setAdapter(rateAdapter);
        spinnerRate.setSelection(0);
        spinnerRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] rateArray = rateAdapter.getItem(position).split("X");
                rate = rateArray[1];
                if (curCameraID !=null && recordStart != null && recordEnd != null && rate !=null){
                    startWork(curCameraID, recordStart,recordEnd , rate);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/
    }

    public void showStateBar() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer == null && timerTask == null) {
            titleBar.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(1);
                }
            };
            timer.schedule(timerTask, 4000);
        }


    }


    /**
     * 弹出日期选择器
     */
    /*TextView todayData;
    String day;

    private void showDateDialog(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dateDialogView = layoutInflater.inflate(R.layout.custom_times_dialog, null);
        final CustomDialog dataDialog = new CustomDialog(this, R.style.customDialog, dateDialogView);
        Calendar fristDay = Calendar.getInstance();
        fristDay.add(Calendar.YEAR, -5);
        fristDay.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        Calendar lastDay = Calendar.getInstance();
        lastDay.add(Calendar.YEAR, +5);
        lastDay.set(Calendar.DAY_OF_MONTH, lastDay.getActualMaximum(Calendar.DAY_OF_MONTH));
        final CalendarPickerView calendar = (CalendarPickerView) dateDialogView.findViewById(R.id.calendar_view);
        todayData = (TextView) dateDialogView.findViewById(R.id.today);
        TextView saveDate = (TextView) dateDialogView.findViewById(R.id.save_date);
        Date today = new Date();
        todayData.setText(Tools.ConverToString(today));
        calendar.init(fristDay.getTime(), lastDay.getTime()).withSelectedDate(today);
        calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                todayData.setText(Tools.ConverToString(date));
                day = Tools.toDayToString(date);
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });

        saveDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRecordList(day);
                dataDialog.dismiss();
            }
        });


        dataDialog.show();
    }*/
    private void initView() {
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

    protected void initData() {
        // 建立数据源
        rateArray = getResources().getStringArray(R.array.rate);
        tvRate.setText(rateArray[ratePosition]);
        rate = rateArray[ratePosition].split("X")[1];

    }

    @Override
    protected void onResume() {
        super.onResume();
        _displayManager.onResume();
        setUpDisplayViewData();
        if (curCameraID != null && recordStart != null && recordEnd != null && rate != null) {
            startWork(curCameraID, recordStart, recordEnd, rate);
        }

        // startWork(curEntity);
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
                        // Logger.e(result+"------"+extra);
                    }
                });
            }
        }, new OnVideoFrameDecodedCallback() {

            @Override
            public void onDecodedResult(DisplayView displayview,
                                        boolean isSuccess, byte[] frameBuffer, int width, int height) {

                if (!isPreview) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // 通知controlFragment 状态。
                            //  controlFragment.setPreviewState(true);
                            //   listFragment.onCameraPreviewResult();
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
                        //  listFragment.onCameraPreviewResult();
                        dismissDelayQuickClickDialogDialog();
                        setPreviewState(C.STATE_DECODE_FAIL, "无信号");
                        state.setImageResource(R.drawable.record_state_pause);
                        state.setVisibility(View.VISIBLE);
                    }
                });

            }
        }, _displayManager.displayAtIndex(0));//

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
    public void startWork(String cameraID, String beginTime, String endTime, String rate) {
        stopWork();
        state.setVisibility(View.GONE);
        // 弹出dialog 在预览页面
        setLoadingPreviewState(C.STATE_CONNECTING);

        // 弹出dialog（屏幕中心）
        showDelayQuickClickDialog();

        // this.curEntity = entity;
        //  title.setText(curEntity.displayName);
        //  positionFragment.setCameraId(entity.camearID);
        // cameraPreview.setImgBitmapAndDraw(null);
        // cameraPreview.showProgressView();
        //  cameraDisplayName.setText(hostActivity.getCameraGroupDir()
        //         + entity.displayName);


        // 工作线程执行
        cameraWorker.startRecordPreviewWork(true,
                App.ConnectionSet.ServiceAddress,
                App.ConnectionSet.SafeServicePort,
                App.ConnectionSet.CurrentUserID,
                App.ConnectionSet.CurrentUserPWD,
                App.ConnectionSet.UserGroupName, cameraID, beginTime, endTime, rate);
    }

    /**
     * 停止视频数据
     */
    public void stopWork() {

        if (isPreview) {
            // controlFragment.setPreviewState(false);
        }

        isPreview = false;
        state.setImageResource(R.drawable.record_state_play);
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
        public void onWorking(Object sender, BackgroundWroker.WorkerEventArgs args)
                throws InterruptedException {

//			while (!isPreview) {
            Thread.sleep(500);
//			}
        }

        @Override
        public void onComplete(Object sender, BackgroundWroker.WorkerEventArgs args) {

            // 执行完结果通知列表fragment 设置点击事件
            //   listFragment.onCameraPreviewResult();
            dismissDelayQuickClickDialogDialog();
        }
    };


}
