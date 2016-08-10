package com.sinofloat.mobilesafe.replay;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.main.adapter.CameraListAdapter;
import com.sinofloat.mobilesafe.monitor.entity.RemoteCamera;
import com.sinofloat.mobilesafe.replay.activity.VideoSearchActivity;
import com.sinofloat.mobilesafe.widget.BackgroundWroker;
import com.sinofloat.mobilesafe.widget.DividerItemDecoration;
import com.sinofloat.mobilesafe.wvp.BackgroundWorkerListener;
import com.sinofloat.mobilesafe.wvp.core.RemoteCameraWorker;

import java.util.ArrayList;
import java.util.Stack;

import sinofloat.wvp.tools.ToastUtil;

/**
 * Created by oyk on 2016/8/3.
 *用于回放视频界面的相机列表管理
 */
public class VideoListManager {
    private Context context;
    private TextView cameraPath;
    private TextView cameraTitle;
    private ImageView cameraListBack;
    private RecyclerView recyclerView;
    private CameraListAdapter videoAdapter;
    private boolean ishavePath = true;
    private boolean ishaveBack = true;
    /**
     * 当前组id 组名
     */
    private String curCameraGroupId, curCameragroupNm;
    private BackgroundWroker cameraListWorker;
    private RemoteCameraWorker cameraWorker;

    /**
     * 当前组Id 和 组名
     */
    public String curGroupId;

    private Stack<String> cameraGroupIdStack = new Stack<String>();

    /**
     * 记录组/组/ groupName/groupName/cameraDisplayNm 预览的时候显示用
     */
    private ArrayList<String> cameraGroupNmList = new ArrayList<String>();
    /**
     * 被点击的item的类型
     */
    private String cameraType;

    private ArrayList<RemoteCamera> groupItem;
    private ArrayList<RemoteCamera> cameraItem;


    public VideoListManager(Context context) {
        this.context = context;
        cameraWorker = RemoteCameraWorker.getInstance();
        groupItem = new ArrayList<RemoteCamera>();
        cameraItem = new ArrayList<RemoteCamera>();
    }

    /**
     * 初始化view
     **/
    public void initView(RecyclerView recyclerView) {
        this.ishavePath = false;
        this.ishaveBack = false;
        this.recyclerView = recyclerView;
    }

    public void initView(TextView cameraTitle,TextView cameraPath, ImageView cameraListBack, RecyclerView recyclerView) {
        this.cameraTitle = cameraTitle;
        this.cameraPath = cameraPath;
        this.cameraListBack = cameraListBack;
        this.recyclerView = recyclerView;
    }

    /**
     * 开始获取列表数据
     */
    public void getCameraList() {
        cameraWorker.clearVideoCameraList();
        if (cameraWorker.videoCameraList.size() > 0) {
            onCameraListLoaded();
        } else {

            getRemoteCameraListData(curCameraGroupId);
        }
        if (ishaveBack){
            cameraListBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 相机列表返回上层分组
                    getLastLevelCameraListData();
                }
            });
        }
    }

    /**
     * 获取远程相机列表
     */
    public void getRemoteCameraListData(String curGroupId) {

        this.curCameraGroupId = curGroupId;
        cameraWorker.clearVideoCameraList();
        cameraItem.clear();
        groupItem.clear();
        cameraListWorker = new BackgroundWroker(context, false, null,
                "正在获取相机列表...", BackgroundWroker.BUTTON_STYLE_NONE,
                remoteCameraListWorker);
        cameraListWorker.startWork(null);
    }

    /**
     * 获取远程相机列表的回调方法
     */
    private BackgroundWorkerListener remoteCameraListWorker = new BackgroundWorkerListener() {

        @Override
        public void onWorking(Object sender, BackgroundWroker.WorkerEventArgs args)
                throws InterruptedException {

            // 获取相机组列表
            args.result = cameraWorker.getVideoRemoteCameraList(
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
               // ((Activity) context).finish();
                return;
            }

            if (args.error == null) {
                if (args.result == null) {
                    onCameraListLoaded();
                } else {

                    ToastUtil.showSimpleToast(context,
                            args.result.toString(), true);
                 //   ((Activity) context).finish();
                }
            } else {
                ToastUtil.showSimpleToast(context, args.error.toString()
                        + args.error.toString(), true);
             //   ((Activity) context).finish();
            }
        }
    };

    /**
     * 相机列表数据加载完成
     */
    public void onCameraListLoaded() {
        // 组压入栈里
        addCamerGroupIdToStack(curCameraGroupId, curCameragroupNm);

        // 如果组下没有任何相机列表数据 那么重新请求第一级列表数据
        if (cameraWorker.videoCameraList.size() == 0) {
            getLastLevelCameraListData();
            return;
        }
        for (int i =0;i <cameraWorker.videoCameraList.size();i ++){
            if (cameraWorker.videoCameraList.get(i).camearType.equals(RemoteCamera.CAMEAR_TYPE_CAMGROUP)){
                groupItem.add(cameraWorker.videoCameraList.get(i));
            }else {
                cameraItem.add(cameraWorker.videoCameraList.get(i));
            }
        }

        for (int i = 0;i<cameraItem.size();i++){
            cameraItem.get(i).setType(0);
        }

        videoAdapter = new CameraListAdapter(cameraItem);

        //把分类好的组列表选出来放到头部的recycleview里面
        View headview = LayoutInflater.from(context).inflate(R.layout.recycle_headview, null);
        RecyclerView headRecycler  = (RecyclerView) headview.findViewById(R.id.head_recycle);
        headRecycler.setLayoutManager(new LinearLayoutManager(context));
        //添加分割线
        headRecycler.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
        CameraListAdapter headAdapter = new CameraListAdapter(groupItem);
        headRecycler.setAdapter(headAdapter);
        //头部recycle的组的点击监听
        headAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int i) {
                if (groupItem.size()>0){
                    if (ishaveBack){
                        cameraTitle.setVisibility(View.GONE);
                        cameraListBack.setVisibility(View.VISIBLE);
                        cameraPath.setVisibility(View.VISIBLE);
                    }
                    RemoteCamera entity = groupItem.get(i);
                    curCameraGroupId = entity.camearID;
                    curCameragroupNm = entity.displayName;
                    getRemoteCameraListData(curCameraGroupId);
                }



            }
        });
        //添加头部
        videoAdapter.addHeaderView(headview);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        //根据recycleview的滑动状态设决定是否加载略缩图
        //滑动的时候设置不能点返回,因为滑动的时候点击返回可能造成异常退出
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0){
                    //cameraListBack.setClickable(true);
                    videoAdapter.notifyDataSetChanged();
                }else {
                    //cameraListBack.setClickable(false);
                }
            }
        });

        //摄像头的点击监听()
        videoAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                RemoteCamera entity = cameraItem.get(position);
                //设置当前前被选中的摄像头在整个列表中的位置必须加上摄像头组的数量
                cameraWorker.resetVideoListItemSelectState(position+groupItem.size());
                videoAdapter.notifyDataSetChanged();
                // 防止重复点击
                recyclerView.setClickable(false);
                //携带当前点击的摄像头实体或者ID跳转到视频预览界面,并获取该摄像头下的预览列表
              /*  String cameraID = entity.camearID;
                String cameraName = entity.displayName;
                Intent intent = new Intent(context, VideoPreviewActivity.class);
                intent.putExtra("cameraID",cameraID);
                intent.putExtra("cameraName",cameraName);
                context.startActivity(intent);*/
                VideoSearchActivity videoSearchActivity = (VideoSearchActivity) context;
                String cameraID = entity.camearID;
                String cameraName = entity.displayName;
                videoSearchActivity.setResult(cameraID,cameraName);



                //判断当前是否处于相机预览的activity,是则直接开始预览不是则跳转到相机预览的activity
               /* String runningActivityName = Tools.getRunningActivityName(context);
                if (runningActivityName.equals("CameraPreviewActivity")) {
                    CameraPreviewActivity cameraPreviewActivity = (CameraPreviewActivity) context;
                    cameraPreviewActivity.startWork(entity);
                } else {
                    Intent intent = new Intent(context, CameraPreviewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("entity", entity);
                    bundle.putString("path", getCameraGroupDir());
                    bundle.putSerializable("list",cameraWorker.videoCameraList);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }*/
            }
        });
        recyclerView.setAdapter(videoAdapter);
    }


    /**
     * 摄像头组压入栈
     * 设置当前摄像头路径
     */
    public void addCamerGroupIdToStack(String curGroupId, String curGroupNm) {

        if (curGroupId != null && !cameraGroupIdStack.contains(curGroupId)) {
            this.curGroupId = curGroupId;
            cameraGroupIdStack.push(curGroupId);
            if (curGroupNm != null && curGroupNm.length() >= 0) {
                cameraGroupNmList.add(curGroupNm + " > ");
            }
        }
        if (ishavePath){
            //cameraPath.setText(getCameraGroupDir());
            cameraPath.setText(curGroupNm);
            cameraPath.postInvalidate();
        }
    }

    /**
     * 获取上一层相机列表
     */
    public void getLastLevelCameraListData() {

        try {
            // 移除最后一个元素
            cameraGroupNmList.remove(cameraGroupNmList.size() - 1);
            cameraGroupNmList.remove(cameraGroupNmList.size() - 1);
            curGroupId = cameraGroupIdStack.pop();
            curGroupId = cameraGroupIdStack.pop();
            getRemoteCameraListData(curGroupId);
        } catch (Exception e) {
            if (ishaveBack){
                cameraTitle.setVisibility(View.VISIBLE);
                cameraListBack.setVisibility(View.GONE);
                cameraPath.setVisibility(View.GONE);
            }
            cameraType = null;
            curGroupId = null;
            cameraGroupIdStack.clear();
            cameraGroupNmList.clear();
            // curGroupNm = null;
            getRemoteCameraListData(curGroupId);
        }

    }

    /**
     * 获取当前摄像头所在的组
     */
    public String getCameraGroupDir() {

        StringBuilder sb = new StringBuilder();
        for (String groupName : cameraGroupNmList) {
            sb.append(groupName);
        }
        return sb.toString();
    }

    /**
     * 清空当前摄像头的选中状态
     */
    public void clearCameraSelectState(){
        for (RemoteCamera remoteCamera : cameraWorker.videoCameraList){
            remoteCamera.selected = false;
        }
        if (videoAdapter!=null){
            videoAdapter.notifyDataSetChanged();
        }
    }
}
