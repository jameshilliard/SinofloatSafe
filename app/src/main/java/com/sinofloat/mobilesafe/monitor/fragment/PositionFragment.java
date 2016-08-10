package com.sinofloat.mobilesafe.monitor.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.base.BaseFragment;
import com.sinofloat.mobilesafe.widget.DividerItemDecoration;
import com.sinofloat.mobilesafe.monitor.dapter.PositionAdapter;
import com.sinofloat.mobilesafe.monitor.entity.CameraPosition;
import com.sinofloat.mobilesafe.widget.BackgroundWroker;
import com.sinofloat.mobilesafe.wvp.BackgroundWorkerListener;
import com.sinofloat.mobilesafe.wvp.core.RemoteCameraWorker;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import sinofloat.wvp.messages._MoveTypes;
import sinofloat.wvp.tools.ToastUtil;

/**
 * Created by oyk on 2016/7/13.
 *
 */
public class PositionFragment extends BaseFragment {
    @BindView(R.id.monitor_position_rlv)
    RecyclerView recyclerView;
    private ArrayList<CameraPosition> positionList;
    private PositionAdapter positionAdapter;
    private RemoteCameraWorker cameraWorker;
    private BackgroundWroker backgroundWroker;
    private String curCameraId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraWorker = RemoteCameraWorker.getInstance();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitor_position, null);
        ButterKnife.bind(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //添加分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        return view;
    }

    /**
     * 设置cameraID并且获取预位置
     */
    public void setCameraId(String cameraId){
        this.curCameraId  = cameraId;
        cameraWorker.clearPresetList();
        backgroundWroker = new BackgroundWroker(getActivity(),false,null,
                "正在获取预位置列表...", BackgroundWroker.BUTTON_STYLE_NONE,
                workerListener);
        backgroundWroker.startWork(null);

    }

    private BackgroundWorkerListener workerListener = new BackgroundWorkerListener() {
        @Override
        public void onWorking(Object sender, BackgroundWroker.WorkerEventArgs args) throws InterruptedException {
// 获取相机组列表
            args.result = cameraWorker.getRemoteCameraPositionList(
                    App.ConnectionSet.ServiceAddress,
                    App.ConnectionSet.SafeServicePort,
                    App.ConnectionSet.CurrentUserID,
                    App.ConnectionSet.CurrentUserPWD,
                    App.ConnectionSet.UserGroupName,
                    curCameraId);
        }

        @Override
        public void onComplete(Object sender, BackgroundWroker.WorkerEventArgs args) {
            // 任务完成
            // 用户点了取消
            if (args.isCancel) {
                return;
            }
            if (args.error == null) {
                if (args.result == null) {
                  //成功获取到数据后
                  positionList = cameraWorker.cameraPositions;
                    positionAdapter = new PositionAdapter(R.layout.item_monitor_position_list, positionList);
                    recyclerView.setAdapter(positionAdapter);
                    positionAdapter.notifyDataSetChanged();
                    positionAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int i) {
                            cameraWorker.sendCommand(_MoveTypes.Preset,positionList.get(i).getPositionKey());
                        }
                    });
                } else {
                    ToastUtil.showSimpleToast(getActivity(),
                            args.result.toString(), true);
                }
            } else {
                ToastUtil.showSimpleToast(getActivity(), args.error.toString()
                        + args.error.toString(), true);
            }

        }
    };
}
