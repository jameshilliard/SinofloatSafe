package com.sinofloat.mobilesafe.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.orhanobut.logger.Logger;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.BaseFragment;
import com.sinofloat.mobilesafe.widget.DividerItemDecoration;
import com.sinofloat.mobilesafe.message.adapter.MessageAdapter;
import com.sinofloat.mobilesafe.message.activity.MessagePreviewActivity;
import com.sinofloat.mobilesafe.replay.entity.MediaEntity;
import com.sinofloat.mobilesafe.setting.SettingActivity;
import com.sinofloat.mobilesafe.utils.MyComponentManager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

/**
 * Created by oyk on 2016/7/12.
 *报警界面
 */
public class MessageFragment extends BaseFragment {
    @BindView(R.id.message_sp_left)
    Spinner spinnerLeft;
    @BindView(R.id.message_sp_right)
    Spinner spinnerRight;
    @BindView(R.id.message_rlv)
    RecyclerView recyclerView;

    @OnClick(R.id.message_setting)
    void goSetting() {
        Intent intent = new Intent(getActivity(), SettingActivity.class);
        startActivity(intent);
    }

    @OnItemSelected(R.id.message_sp_left)
    void selectYear(int position) {
        year = yearAdapter.getItem(position);
        searchData();
    }

    @OnItemSelected(R.id.message_sp_right)
    void selectMonth(int position) {
        month = monthAdapter.getItem(position);
        searchData();
    }


    private ArrayAdapter<String> yearAdapter;
    private ArrayAdapter<String> monthAdapter;
    private ArrayList<MediaEntity> messageList;
    private MessageAdapter messageAdapter;
    private String year, month;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, null);
        ButterKnife.bind(this, view);
        initData();
        spinnerLeft.setAdapter(yearAdapter);
        spinnerRight.setAdapter(monthAdapter);
        spinnerLeft.setSelection(2);
        spinnerRight.setSelection(2);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //添加分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        messageAdapter = new MessageAdapter(R.layout.item_message_rlv, messageList);
        messageAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int i) {
                //将要预览的信息传到预览的activity
                MyComponentManager.setTransferedData(messageList.get(i));
                MyComponentManager.startActivity(false, getActivity(),
                        MessagePreviewActivity.class);
            }
        });
        recyclerView.setAdapter(messageAdapter);
        return view;
    }

    protected void initData() {
        messageList = new ArrayList<MediaEntity>();
        for (int i = 'A'; i < 'z'; i++) {
            MediaEntity mediaEntity = new MediaEntity();
            mediaEntity.mediaId = ("" + (char) i);
            messageList.add(mediaEntity);
        }

        // 建立数据源
        String[] years = getResources().getStringArray(R.array.year);
        String[] months = getResources().getStringArray(R.array.month);
        // 建立Adapter并且绑定数据源
        yearAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_spinner, years);
        monthAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_spinner, months);
        yearAdapter.setDropDownViewResource(R.layout.item_spinner);
        monthAdapter.setDropDownViewResource(R.layout.item_spinner);
    }

    /**
     * 根据条件查询数据库
     */
    public void searchData() {
        if ((year != null) && (month != null)) {
            Logger.e(year + ":" + month);
        }
    }

}
