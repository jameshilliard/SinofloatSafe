package com.sinofloat.mobilesafe.main.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.App;
import com.sinofloat.mobilesafe.base.BaseActivity;
import com.sinofloat.mobilesafe.utils.MyComponentManager;
import com.sinofloat.mobilesafe.widget.BackgroundWroker;
import com.sinofloat.mobilesafe.widget.BackgroundWroker.WorkerEventArgs;
import com.sinofloat.mobilesafe.wvp.BackgroundWorkerListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sinofloat.wvp.tools.ToastUtil;
import sinofloat.wvp.tools.Util;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.btn_login)
    ImageView loginBtn;
    @BindView(R.id.login_et_server)
    EditText serverEdit;
    @BindView(R.id.login_et_userName)
    EditText userNameEdit;
    @BindView(R.id.login_et_passWord)
    EditText passwordEdit;

    @OnClick(R.id.btn_login)
    void login() {
        if (checkInput()) {
            doLogin();
        }
    }

    /**
     * 登陆服务端口。
     */
    private int servicePort;

    /**
     * 登陆服务Ip，用户民，密码...。
     */
    private String serviceAddress, userName, password, passwordMD5, groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //绑定事件
        ButterKnife.bind(this);

        setViewValues();
    }

    /**
     * 检查输入项
     *
     * @return
     */
    private boolean checkInput() {
        //AppComm.ConnectionSet.UserGroupName = "csv";
        serviceAddress = serverEdit.getText().toString().trim();
        userName = userNameEdit.getText().toString().trim();
        password = passwordEdit.getText().toString().trim();
        groupName = "demo";
        servicePort = App.ConnectionSet.ServicePort;

        if (serviceAddress == null || serviceAddress.length() == 0) {
            ToastUtil.showSimpleToast(this, "请输入IP", true);
            return false;
        }

        if (userName == null || userName.length() == 0) {
            ToastUtil.showSimpleToast(this, "请输入用户名", true);
            return false;
        }

        if (password == null || password.length() == 0) {
            ToastUtil.showSimpleToast(this, "请输入密码", true);
            return false;
        }

//		if (groupName == null || groupName.length() == 0) {
//			ToastUtil.showSimpleToast(this, "请输入用户组", true);
//			return false;
//		}

        try {
            if (serviceAddress.contains(":")) {
                String[] array = serviceAddress.split(":");
                serviceAddress = array[0];
                servicePort = Integer.parseInt(array[1]);
            }

            passwordMD5 = Util.getMd5(password);

        } catch (Exception e) {
            ToastUtil.showSimpleToast(this, "请正确输入服务地址！", true);
            return false;
        }

        return true;
    }

    /**
     * 给editText设值
     */
    private void setViewValues() {

        if (App.ConnectionSet.CurrentUserLoginName.length() == 0) {
            userNameEdit.setText(userName);
            serverEdit.setText(serviceAddress);
            // passwordEdit.setText(isAutoLogin ? password : null);
            //groupEdit.setText(groupName);
        } else {
            userNameEdit.setText(App.ConnectionSet.CurrentUserLoginName);
            serverEdit.setText(App.ConnectionSet.ServiceAddress);
            // passwordEdit.setText(isAutoLogin ? AppComm.ConnectionSet.CurrentUserLoginPass : null);
            //groupEdit.setText(AppComm.ConnectionSet.UserGroupName);
        }
    }

    /**
     * 登录
     */
    private void doLogin() {

        BackgroundWroker worker = new BackgroundWroker(this, false, null,
                "正在登录中...", BackgroundWroker.BUTTON_STYLE_CANCEL, loginListener);
        worker.startWork(null);
    }

    /**
     * 登陆的监听
     */
    private BackgroundWorkerListener loginListener = new BackgroundWorkerListener() {

        @Override
        public void onWorking(Object sender, WorkerEventArgs args)
                throws InterruptedException {

            args.result = App.userUtil.Login(serviceAddress, servicePort,
                    Util.getMd5(userName), userName, passwordMD5, groupName);
        }

        @Override
        public void onComplete(Object sender, WorkerEventArgs args) {

            if (args.isCancel) {

                setViewValues();
                return;
            }

            if (args.error == null) { // 如果登录成功

                if (args.result == null) {

                    if (password != null && password.length() > 0) {
                        //保存密码 记住密码
                        App.ConnectionSet.CurrentUserLoginPass = password;
                        App.ConnectionSet.Save();
                    }
                    MyComponentManager.startActivity(false,
                            Intent.FLAG_ACTIVITY_CLEAR_TOP,
                            LoginActivity.this, MainActivity.class);//RemoteCameraFragmentActivity  RemoteCameraActivity
                    finish();
                } else {

                    setViewValues();
                    ToastUtil.showSimpleToast(LoginActivity.this,
                            args.result.toString(), true);
                }
            } else { // 如果登录失败

                setViewValues();
                ToastUtil.showSimpleToast(LoginActivity.this,
                        args.error.toString(), true);
            }
        }
    };
}
