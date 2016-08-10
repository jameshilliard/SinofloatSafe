package com.sinofloat.mobilesafe.replay.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.base.BaseFragment;
import com.sinofloat.mobilesafe.replay.activity.VideoPreviewActivity;
import com.sinofloat.mobilesafe.replay.activity.VideoSearchActivity;
import com.sinofloat.mobilesafe.replay.adapter.RecordListAdapter;
import com.sinofloat.mobilesafe.replay.entity.CameraRecord;
import com.sinofloat.mobilesafe.utils.Tools;
import com.sinofloat.mobilesafe.widget.BackgroundWroker;
import com.sinofloat.mobilesafe.widget.DividerItemDecoration;
import com.sinofloat.mobilesafe.wvp.BackgroundWorkerListener;
import com.sinofloat.mobilesafe.wvp.core.RemoteCameraWorker;

import java.util.ArrayList;

import sinofloat.wvp.tools.ToastUtil;

/**
 * Created by oyk on 2016/7/14.
 *
 */
public class VideoFragment extends BaseFragment {
    private Spinner year,month,day;
    private RecyclerView recyclerView;
    private TextView cameraSearch;
    private TextView path;
    //请求的标志
    public static final int REQUSET = 1;
    public static final int RESULT = 0;
    //选择的相机的ID和名字
    private String cameraID,cameraName;

    private ArrayAdapter<String> yearAdapter;
    private ArrayAdapter<String> monthAdapter;
    private ArrayAdapter<String> dayAdapter;
    private String year_str, month_str,day_str;
    private int yearPosition,monthPosition,dayPosition;

    private RemoteCameraWorker cameraWorker;
    private BackgroundWroker recordListWorker;
    private String beginTime, endTime;
    private RecordListAdapter recordListAdapter;
    private ArrayList<CameraRecord> previewList;
    private String recordStart,recordEnd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraWorker = RemoteCameraWorker.getInstance();
        initSpinner();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_replay_video, null);
        year = (Spinner) view.findViewById(R.id.replay_video_sp_left);
        month = (Spinner) view.findViewById(R.id.replay_video_sp_center);
        day = (Spinner) view.findViewById(R.id.replay_video_sp_right);
        year.setAdapter(yearAdapter);
        month.setAdapter(monthAdapter);
        day.setAdapter(dayAdapter);
        year.setSelection(yearPosition);
        month.setSelection(monthPosition);
        day.setSelection(dayPosition);
        cameraSearch = (TextView) view.findViewById(R.id.replay_video_search_bt);
        path = (TextView) view.findViewById(R.id.replay_video_path);
        recyclerView = (RecyclerView) view.findViewById(R.id.replay_video_rlv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //添加分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        //摄像头选择按钮
        path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), VideoSearchActivity.class);
                startActivityForResult(intent,REQUSET);
            }
        });
        //年份选择
        year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                  year_str = yearAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //月份选择
        month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                month_str = monthAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //日期选择
        day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                day_str = dayAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cameraSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   Logger.e(year_str.substring(0,4)+month_str.substring(0,2)+day_str.substring(0,2));
                if (cameraID != null){
                    String date = year_str.substring(0,4)+month_str.substring(0,2)+day_str.substring(0,2);
                    getRecordList(date);
                }else {
                    ToastUtil.showSimpleToast(getActivity(),
                            "请先选择一个摄像头", true);
                }


            }
        });

        return view;
    }

    /**
     * 获取视频列表
     */
    private void getRecordList(String date) {
        //beginTime = Tools.getTimesmorning()+"";
        //endTime = Tools.getTimesnight()+"";
        beginTime = date + "000000";
        endTime = date + "235959";
        cameraWorker.clearCameraRecordList();
        recordListWorker = new BackgroundWroker(getActivity(), false, null,
                "正在获取录像视频列表...", BackgroundWroker.BUTTON_STYLE_NONE,
                workerListener);
        recordListWorker.startWork(null);


    }

    private BackgroundWorkerListener workerListener = new BackgroundWorkerListener() {
        @Override
        public void onWorking(Object sender, BackgroundWroker.WorkerEventArgs args) throws InterruptedException {
// 获取相机组列表
            args.result = cameraWorker.getCameraRecordList(
                    App.ConnectionSet.ServiceAddress,
                    App.ConnectionSet.SafeServicePort,
                    App.ConnectionSet.CurrentUserID,
                    App.ConnectionSet.CurrentUserPWD,
                    App.ConnectionSet.UserGroupName,
                    cameraID, beginTime, endTime);
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
                    previewList = cameraWorker.cameraRecords;
                    /*CameraRecord cameraRecord = new CameraRecord();
                    cameraRecord.setBeginTime("20160801080520");
                    cameraRecord.setEndTime("20160801132110");
                    previewList.add(cameraRecord);*/
                    if(previewList.size() == 0){
                        ToastUtil.showSimpleToast(getActivity(),
                                "没有获取到录像列表", true);
                    }
                    recordListAdapter = new RecordListAdapter(R.layout.item_record_list, previewList);
                    recyclerView.setAdapter(recordListAdapter);
                    recordListAdapter.notifyDataSetChanged();
                    recordListAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int i) {
                            //开始播放视频
                            if (previewList.size()>0){
                                resetSelectState(i);
                                recordStart = previewList.get(i).getBeginTime();
                                recordEnd = previewList.get(i).getEndTime();
                                Intent intent = new Intent(getActivity(), VideoPreviewActivity.class);
                                intent.putExtra("cameraID",cameraID);
                                intent.putExtra("recordStart",recordStart);
                                intent.putExtra("recordEnd",recordEnd);
                                intent.putExtra("cameraName",cameraName);
                                startActivity(intent);
                                //startWork(cameraID, recordStart,recordEnd , rate);
                            }


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

    /**
     * 重置选中效果
     * @param position
     */
    public void resetSelectState(int position) {

        int size = previewList.size();

        for (int i = 0; i < size; i++) {

            CameraRecord cameraRecord = previewList.get(i);
            if (position == i) {
                cameraRecord.selected = true;
                recordListAdapter.notifyDataSetChanged();
            } else {
                cameraRecord.selected = false;
            }
        }
    }


    /**
     * 初始化日期选择器
     */
    private void initSpinner() {
        String[] years = getResources().getStringArray(R.array.year);
        String[] months = getResources().getStringArray(R.array.month);
        String[] days = getResources().getStringArray(R.array.day);
        // 建立Adapter并且绑定数据源
        yearAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_spinner, years);
        monthAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_spinner, months);
        dayAdapter = new ArrayAdapter<String>(getActivity(),R.layout.item_spinner,days);
        yearAdapter.setDropDownViewResource(R.layout.item_spinner);
        monthAdapter.setDropDownViewResource(R.layout.item_spinner);
        dayAdapter.setDropDownViewResource(R.layout.item_spinner);
        String toDate = Tools.getStrToday(System.currentTimeMillis());
        String toYear = toDate.substring(0,4)+"年";
        String toMonth = toDate.substring(4,6)+"月";
        String toDay = toDate.substring(6)+"日";
        for (int i = 0;i<years.length;i++){
            if (toYear.equals(years[i])){
               yearPosition = i;
            }
        }
        for (int i = 0;i<months.length;i++){
            if (toMonth.equals(months[i])){
                monthPosition = i;
            }
        }
        for (int i = 0;i<days.length;i++){
            if (toDay.equals(days[i])){
                dayPosition = i;
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUSET && resultCode == RESULT && data != null){
            cameraID = data.getStringExtra("cameraID");
            cameraName = data.getStringExtra("cameraName");
            path.setText(cameraName);
          //  Logger.e(cameraID+"========"+cameraName);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


}
