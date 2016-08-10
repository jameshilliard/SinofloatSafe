package com.sinofloat.mobilesafe.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.base.BaseFragment;
import com.sinofloat.mobilesafe.main.adapter.OnListGetCompleteListener;
import com.sinofloat.mobilesafe.monitor.MonitorListManager;
import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;
import com.sinofloat.mobilesafe.setting.SettingActivity;
import com.sinofloat.mobilesafe.widget.BackgroundWroker;
import com.sinofloat.mobilesafe.widget.DividerItemDecoration;
import com.sinofloat.mobilesafe.wvp.BackgroundWorkerListener;
import com.sinofloat.mobilesafe.wvp.core.RemoteCameraWorker;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by oyk on 2016/7/12.
 * 监控界面
 */
public class MonitorFragment extends BaseFragment implements OnListGetCompleteListener {
    @BindView(R.id.monitor_iv_back)
    ImageView cameraListBack;
    @BindView(R.id.monitor_title_path)
    TextView cameraPath;
    @BindView(R.id.monitor_title)
    TextView titleText;
    @BindView(R.id.monitor_rlv)
    RecyclerView recyclerView;

    @OnClick(R.id.monitor_setting)
    void goSetting() {
        Intent intent = new Intent(getActivity(), SettingActivity.class);
        startActivity(intent);
    }

    private RemoteCameraWorker cameraWorker;
    private MonitorListManager monitorListManager;
    private BackgroundWroker backgroundWroker;
    private ArrayList<RemoteCamera> cameraItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraWorker = RemoteCameraWorker.getInstance();
        monitorListManager = new MonitorListManager(getActivity());
        monitorListManager.setOnListGetCompleteListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitor, null);
        ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //添加分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        monitorListManager.initView(titleText, cameraPath, cameraListBack, recyclerView);
        monitorListManager.getCameraList();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        monitorListManager.clearCameraSelectState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 获取返回按钮是否隐藏
     *
     * @return
     */
    public boolean getCameraListBackState() {
        if (cameraListBack.getVisibility() == View.VISIBLE) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 返回上级菜单
     */
    public void goBack() {
        cameraListBack.performClick();
    }

    /**
     * 相机列表加载完成的回调方法
     *
     * @param cameraItem
     */
    @Override
    public void OnComplete(ArrayList<RemoteCamera> cameraItem) {
        this.cameraItem = cameraItem;
        getThumbImage();

    }

    /**
     * 异步下载
     */
    public void asyncLoadImage(final String curCameraID, final int i) {
        backgroundWroker = new BackgroundWroker(getActivity(), false, null,
                "正在获取相机略缩图...", BackgroundWroker.BUTTON_STYLE_NONE,
                new BackgroundWorkerListener() {
                    @Override
                    public void onWorking(Object sender, BackgroundWroker.WorkerEventArgs args) throws InterruptedException {
                        args.result = cameraWorker.getCameraSubImage(
                                App.ConnectionSet.ServiceAddress,
                                App.ConnectionSet.SafeServicePort,
                                App.ConnectionSet.CurrentUserID,
                                App.ConnectionSet.CurrentUserPWD,
                                App.ConnectionSet.UserGroupName,
                                curCameraID);
                    }

                    @Override
                    public void onComplete(Object sender, BackgroundWroker.WorkerEventArgs args) {
                        if (args.error == null) {
                            if (args.result == null) {
                                if (cameraWorker.subImages.size() > 0 && cameraItem.size() > 0) {
                                    if (cameraWorker.subImages.containsKey(cameraItem.get(i).camearID)) {
                                        cameraItem.get(i).bitmap = cameraWorker.subImages.get(cameraItem.get(i).camearID);
                                        monitorListManager.getAdapter().notifyDataSetChanged();
                                    } else {

                                    }
                                }

                            } else {
                            }
                        }
                    }
                });
        backgroundWroker.setIsShowDialog(false);
        backgroundWroker.startWork(null);
    }

    /**
     * 获取略缩图并且添加在cameraItem
     */
    public void getThumbImage() {
        cameraWorker.clearSubImages();
        for (int i = 0; i < cameraItem.size(); i++) {
            asyncLoadImage(cameraItem.get(i).camearID, i);
        }

    }

}
