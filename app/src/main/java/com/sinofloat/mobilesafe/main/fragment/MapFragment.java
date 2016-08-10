package com.sinofloat.mobilesafe.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.base.BaseFragment;
import com.sinofloat.mobilesafe.base.C;
import com.sinofloat.mobilesafe.map.MapListManager;
import com.sinofloat.mobilesafe.map.adapter.OnMapListGetCompleteListener;
import com.sinofloat.mobilesafe.map.entity.RemoteGroup;
import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;
import com.sinofloat.mobilesafe.setting.SettingActivity;
import com.sinofloat.mobilesafe.widget.BackgroundWroker;
import com.sinofloat.mobilesafe.widget.DividerItemDecoration;
import com.sinofloat.mobilesafe.wvp.BackgroundWorkerListener;
import com.sinofloat.mobilesafe.wvp.core.RemoteCameraWorker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sinofloat.wvp.tools.ToastUtil;

/**
 * Created by oyk on 2016/7/12.
 *地图界面
 */
public class MapFragment extends BaseFragment implements AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener,
        AMap.OnMapLoadedListener, AMap.InfoWindowAdapter ,OnMapListGetCompleteListener {
    @BindView(R.id.map)
    MapView mapView;
    @OnClick(R.id.map_setting)
    void goSetting(){
        Intent intent = new Intent(getActivity(), SettingActivity.class);
        startActivity(intent);
    }
    private AMap aMap;
    private RecyclerView recyclerView;
    private ArrayList<MarkerOptions> markerOptionsList;
    private ArrayList<Marker> markerList;
    /**
     * 用来获取相机组的工具类
     */
    private RemoteCameraWorker cameraWorker;
    private BackgroundWroker remoteGroupsWorker;
    private ArrayList<RemoteGroup> remoteGroups;
    private Map<String,String> remoteID;
    /**
     * 当前组id 组名
     */
    private String curCameraGroupId, curCameragroupNm;
    private ImageView cameraListBack;
    private TextView cameraTitle,cameraPath;
    private MapListManager mapListManager;
    private ArrayList<RemoteCamera> cameraItem;
    private BackgroundWroker backgroundWroker;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraWorker = RemoteCameraWorker.getInstance();
        mapListManager = new MapListManager(getActivity());
        mapListManager.setOnMapListGetCompleteListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, null);
        ButterKnife.bind(this, view);
        getRemoteGroupsData();
        cameraListBack = (ImageView) view.findViewById(R.id.map_iv_back);
        cameraTitle = (TextView) view.findViewById(R.id.map_title);
        cameraPath = (TextView) view.findViewById(R.id.map_title_path);
        recyclerView = (RecyclerView) view.findViewById(R.id.map_rlv);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        //添加分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
        mapListManager.initView(cameraTitle, cameraPath, cameraListBack, recyclerView);
       // mapListManager.getCameraList();
       // recyclerView.setVisibility(View.VISIBLE);
     //   mapView.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mapView.onResume();
        //remoteGroupsManager.clearCameraSelectState();
    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 获取带坐标的相机组列表
     */
    public void getRemoteGroupsData() {
        cameraWorker.clearRemoteGroups();
        remoteGroupsWorker = new BackgroundWroker(getActivity(),false,null,"正在获取相机组数据...",
                BackgroundWroker.BUTTON_STYLE_NONE,remoteGroupsListener);

        remoteGroupsWorker.startWork(null);
    }
    /**
     * 获取带坐标的相机组列表的回调方法
     */
    private BackgroundWorkerListener remoteGroupsListener = new BackgroundWorkerListener() {
        @Override
        public void onWorking(Object sender, BackgroundWroker.WorkerEventArgs args) throws InterruptedException {
            // 获取相机组列表
            args.result = cameraWorker.getRemoteCameraGroupForLocationList(
                    App.ConnectionSet.ServiceAddress,
                    App.ConnectionSet.SafeServicePort,
                    App.ConnectionSet.CurrentUserID,
                    App.ConnectionSet.CurrentUserPWD,
                    App.ConnectionSet.UserGroupName,
                    curCameraGroupId);
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
                   remoteGroups =  cameraWorker.remoteGroups;
                     getMarkerList();

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

    /******************************************高德地图********************************************/
    /**
     * 监听amap地图加载成功事件回调
     */
    @Override
    public void onMapLoaded() {
        // 设置所有maker显示在当前可视区域地图中
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(C.XIAN).include(C.CHENGDU)
                .include(C.BEIJING).build();
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
    }

    /**
     * 对marker标注点点击响应事件
     * 给marker添加点击动画什么的
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
       marker.showInfoWindow();
        Toast.makeText(getActivity(), "窗口状态:" + marker.isInfoWindowShown(), Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * 监听点击infowindow窗口事件回调
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(getActivity(), "你点击了infoWindow窗口" + marker.getTitle(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(getActivity(), "当前地图可视区域内Marker数量:"+ aMap.getMapScreenMarkers().size(), Toast.LENGTH_SHORT).show();
        String id = remoteID.get(marker.getTitle());
         mapListManager.setClickItem(id,marker.getTitle());
        recyclerView.setVisibility(View.VISIBLE);

    }

    /**
     * 监听自定义infowindow窗口的infowindow事件回调
     */
    @Override
    public View getInfoWindow(Marker marker) {
        View infoWindow = getActivity().getLayoutInflater().inflate(
                R.layout.custom_info_window, null);
        render(marker, infoWindow);
        return infoWindow;
    }

    /**
     * 监听自定义infowindow窗口的infocontents事件回调
     */
    @Override
    public View getInfoContents(Marker marker) {
        View infoContent = getActivity().getLayoutInflater().inflate(
                R.layout.custom_info_contents, null);
        render(marker, infoContent);
        return null;
    }


    private void setUpMap() {
        aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
        aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
        aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
        aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
       // addMarkersToMap();// 往地图上添加marker
    }

    /**
     * 从服务器获取marker数据集合
     */
    private void getMarkerList() {
        markerOptionsList = new ArrayList<MarkerOptions>();
        remoteID = new HashMap<String, String>();
        for (int i = 0; i<remoteGroups.size(); i++ ){
            MarkerOptions options = new MarkerOptions();
            RemoteGroup group = remoteGroups.get(i);
            remoteID.put(group.displayName,group.camearID);
            DecimalFormat df = new DecimalFormat("#.00");
            String strLat = df.format(group.latitude);
            String strLng = df.format(group.longitude);
            LatLng latLng = new LatLng(group.latitude,group.longitude);
            options.position(latLng)
                    .title(group.displayName)
                    .snippet(group.displayName+"："+strLat+","+strLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker));
            markerOptionsList.add(options);
        }
        markerList = aMap.addMarkers(markerOptionsList, true);

    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap() {
        getMarkerList();
    }

    /**
     * 自定义infowinfow窗口
     */
    public void render(Marker marker, View view) {
        String cameraName = marker.getTitle();
        TextView tvCameraName = ((TextView) view.findViewById(R.id.info_window_camera_name));
        if (cameraName != null) {
            tvCameraName.setText(cameraName);
        } else {
            tvCameraName.setText("");
        }
        String cameraID = "";
        TextView tvCameraID = ((TextView) view.findViewById(R.id.info_window_camera_id));
        if (cameraID != null) {
            tvCameraID.setText(cameraID);
        } else {
            tvCameraID.setText("");
        }
        String cameraAddress = marker.getSnippet();
        TextView tvCameraAddress = ((TextView) view.findViewById(R.id.info_window_camera_address));
        if (cameraAddress != null) {
            tvCameraAddress.setText(cameraAddress);
        } else {
            tvCameraAddress.setText("");
        }
    }

    /**
     * 获取返回按钮是否隐藏
     * @return
     */
    public boolean getCameraListBackState(){
        if (cameraListBack.getVisibility() == View.VISIBLE){
            return true;
        }else {
            return false;
        }

    }
    /**
     * 返回上级菜单
     */
    public void goBack(){
        cameraListBack.performClick();
    }


    /**
     * 列表数据是否加载完成,加载完成就开始加载略缩图
     * @param cameraItem
     */
    @Override
    public void OnMapComplete(ArrayList<RemoteCamera> cameraItem) {
      this.cameraItem = cameraItem;
          getThumbImage();
    }

    /**
     * 获取略缩图并且添加在cameraItem
     *
     */
    public void getThumbImage(){
        cameraWorker.clearSubImages();
        for (int i = 0;i < cameraItem.size(); i++){
            asyncLoadImage(cameraItem.get(i).camearID,i);
        }

    }

    /**
     * 异步下载
     */
    public void asyncLoadImage(final String curCameraID,final int i) {
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
                        if (args.error ==null){
                            if (args.result == null){
                                if (cameraWorker.subImages.size()>0 && cameraItem.size()>0) {
                                    if (cameraWorker.subImages.containsKey(cameraItem.get(i).camearID)) {
                                        cameraItem.get(i).bitmap = cameraWorker.subImages.get(cameraItem.get(i).camearID);
                                        mapListManager.getAdapter().notifyDataSetChanged();
                                    } else {

                                    }
                                }
                            }
                        }
                    }
                });
        backgroundWroker.setIsShowDialog(false);
        backgroundWroker.startWork(null);
    }
}
