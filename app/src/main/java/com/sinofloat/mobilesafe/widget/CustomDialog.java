package com.sinofloat.mobilesafe.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * Created by oyk on 2016/7/27.
 * 自定义dialog
 */
public class CustomDialog extends Dialog {
    View customView;//自定义的view
    Context context;
    public CustomDialog(Context context) {
        super(context);
        this.context = context;
    }
    /**
     * 自定义布局的构造方法
     * @param context
     * @param customView
     */
    public CustomDialog(Context context,View customView){
        super(context);
        this.context = context;
        this.customView=customView;
    }
    /**
     * 自定义主题及布局的构造方法
     * @param context
     * @param theme
     * @param customView
     */
    public CustomDialog(Context context, int theme,View customView){
        super(context, theme);
        this.context = context;
        this.customView=customView;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(customView);
    }
}
