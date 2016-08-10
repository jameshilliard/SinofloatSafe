package com.sinofloat.mobilesafe.monitor.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.BaseFragment;
import com.sinofloat.mobilesafe.widget.DividerItemDecoration;
import com.sinofloat.mobilesafe.main.adapter.CameraListAdapter;
import com.sinofloat.mobilesafe.monitor.activity.CameraPreviewActivity;
import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by oyk on 2016/7/13.
 *
 */
public class ListFragment extends BaseFragment {
    @BindView(R.id.monitor_list_iv_back)
    ImageView cameraListBack;
    @BindView(R.id.monitor_list_tv_camera_path)
    TextView cameraPath;
    @BindView(R.id.monitor_list_rlv)
    RecyclerView recyclerView;

    private ArrayList<RemoteCamera> myCameraList;
    private CameraListAdapter adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitor_list, null);
        ButterKnife.bind(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //添加分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        CameraPreviewActivity cameraPreviewActivity = (CameraPreviewActivity) getActivity();
        cameraPath.setText(cameraPreviewActivity.getCurCameraPath());
        ArrayList<RemoteCamera> cameraList = cameraPreviewActivity.getRemoteCameras();
        myCameraList = new ArrayList<RemoteCamera>();
        //将获取的相机列表中的单个摄像头数据拷贝出来成为一个新的List
        for (int i = 0;i<cameraList.size();i ++){
           if (cameraList.get(i).camearType.equals(RemoteCamera.CAMEAR_TYPE_CAMGROUP)){
               // cameraList.remove(i);
            }else {
               RemoteCamera remoteCamera = new RemoteCamera();
               remoteCamera.camearID = cameraList.get(i).camearID;
               remoteCamera.displayName = cameraList.get(i).displayName;
               remoteCamera.camearType = cameraList.get(i).camearType;
               remoteCamera.selected = cameraList.get(i).selected;
               remoteCamera.setType(0);
               myCameraList.add(remoteCamera);
           }
        }
        adapter = new CameraListAdapter(myCameraList);
        recyclerView.setAdapter(adapter);
        adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int i) {
                recyclerView.setClickable(false);
                RemoteCamera entity = myCameraList.get(i);
                resetSelectState(i);
                CameraPreviewActivity cameraPreviewActivity = (CameraPreviewActivity) getActivity();
                cameraPreviewActivity.startWork(entity);
                adapter.notifyDataSetChanged();
            }
        });


        return view;
    }

    /**
     * 预览执行结果后  设置可点击 否则点击太快会出问题
     */
    public void onCameraPreviewResult() {
         recyclerView.setClickable(true);
    }

    /**
     * 重置选中效果
     * @param position
     */
    public void resetSelectState(int position) {

        int size = myCameraList.size();

        for (int i = 0; i < size; i++) {

            RemoteCamera entity = myCameraList.get(i);
            if (position == i) {
                entity.selected = true;
            } else {
                entity.selected = false;
            }
        }
    }

}
