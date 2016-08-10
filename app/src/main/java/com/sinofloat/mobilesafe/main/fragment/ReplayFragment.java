package com.sinofloat.mobilesafe.main.fragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.BaseFragment;
import com.sinofloat.mobilesafe.replay.fragment.PictureFragment;
import com.sinofloat.mobilesafe.replay.fragment.VideoFragment;
import com.sinofloat.mobilesafe.setting.SettingActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by oyk on 2016/7/12.
 * 回放界面
 */
public class ReplayFragment extends BaseFragment {
    @BindView(R.id.replay_rg)
    RadioGroup radioGroup;

    @OnClick(R.id.replay_setting)
    void goSetting() {
        Intent intent = new Intent(getActivity(), SettingActivity.class);
        startActivity(intent);
    }

    private VideoFragment videoFragment;
    private PictureFragment pictureFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_replay, null);
        ButterKnife.bind(this, view);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.replay_rb_video:
                        selectFragment(0);
                        break;
                    case R.id.replay_rb_picture:
                        selectFragment(1);
                        break;
                }
            }
        });
        selectFragment(0);
        return view;
    }

    /**
     * 设置当前位置的fragment
     * @param position
     */
    private void selectFragment(int position) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        hideFragment(ft);
        switch (position) {
            case 0:
                if (videoFragment == null) {
                    videoFragment = new VideoFragment();
                    ft.add(R.id.replay_fl, videoFragment);
                } else {
                    ft.show(videoFragment);
                }
                break;
            case 1:
                if (pictureFragment == null) {
                    pictureFragment = new PictureFragment();
                    ft.add(R.id.replay_fl, pictureFragment);
                } else {
                    ft.show(pictureFragment);
                }
                break;

        }
        ft.commit();
    }

    /**
     * 隐藏所有fragment
     * @param transaction
     */
    private void hideFragment(FragmentTransaction transaction) {
        if (videoFragment != null) {
            transaction.hide(videoFragment);
        }
        if (pictureFragment != null) {
            transaction.hide(pictureFragment);
        }
    }

    /**
     * 获取VideoFragment
     * @return VideoFragment
     */
    public  VideoFragment getVideoFragment(){
        return videoFragment;
    }
}
