package com.sinofloat.mobilesafe.main.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.BaseActivity;
import com.sinofloat.mobilesafe.main.fragment.MapFragment;
import com.sinofloat.mobilesafe.main.fragment.MessageFragment;
import com.sinofloat.mobilesafe.main.fragment.MonitorFragment;
import com.sinofloat.mobilesafe.main.fragment.ReplayFragment;
import com.sinofloat.mobilesafe.utils.Tools;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {
    @BindView(R.id.main_rg)
    RadioGroup radioGroup;
    @BindView(R.id.main_rb_message_mark)
    TextView mark;
    private MonitorFragment monitorFragment;
    private MessageFragment messageFragment;
    private ReplayFragment replayFragment;
    private MapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        ButterKnife.bind(this);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.main_rb_monitor:
                        selectFragment(0);
                        mark.setText("10");
                        mark.setVisibility(View.VISIBLE);
                        break;
                    case R.id.main_rb_message:
                        selectFragment(1);
                        mark.setVisibility(View.GONE);
                        break;
                    case R.id.main_rb_replay:
                        selectFragment(2);
                        mark.setText("99+");
                        mark.setVisibility(View.VISIBLE);
                        break;
                    case R.id.main_rb_setting:
                        selectFragment(3);
                        mark.setText(" 2 ");
                        mark.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
        selectFragment(0);
    }

    /**
     * 设置当前位置的fragment
     *
     * @param position
     */
    private void selectFragment(int position) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        hideFragment(ft);
        switch (position) {
            case 0:
                if (monitorFragment == null) {
                    monitorFragment = new MonitorFragment();
                    ft.add(R.id.main_fl, monitorFragment);
                } else {
                    ft.show(monitorFragment);
                }
                break;
            case 1:
                if (messageFragment == null) {
                    messageFragment = new MessageFragment();
                    ft.add(R.id.main_fl, messageFragment);
                } else {
                    ft.show(messageFragment);
                }
                break;
            case 2:
                if (replayFragment == null) {
                    replayFragment = new ReplayFragment();
                    ft.add(R.id.main_fl, replayFragment);
                } else {
                    ft.show(replayFragment);
                }
                break;
            case 3:
                if (mapFragment == null) {
                    mapFragment = new MapFragment();
                    ft.add(R.id.main_fl, mapFragment);
                } else {
                    ft.show(mapFragment);
                }
                break;
        }
        ft.commit();
    }

    /**
     * 隐藏所有fragment
     *
     * @param transaction
     */
    private void hideFragment(FragmentTransaction transaction) {
        if (monitorFragment != null) {
            transaction.hide(monitorFragment);
        }
        if (mapFragment != null) {
            transaction.hide(mapFragment);
        }
        if (messageFragment != null) {
            transaction.hide(messageFragment);
        }
        if (replayFragment != null) {
            transaction.hide(replayFragment);
        }
    }

    @Override
    public void onBackPressed() {
        if (radioGroup.getCheckedRadioButtonId() == R.id.main_rb_monitor && monitorFragment.getCameraListBackState()) {
            monitorFragment.goBack();
        } else if (radioGroup.getCheckedRadioButtonId() == R.id.main_rb_setting && mapFragment.getCameraListBackState()) {
            mapFragment.goBack();
        } else {
            Tools.exitBy2Click(this);
        }

    }


}
