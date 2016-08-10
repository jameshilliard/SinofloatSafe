package com.sinofloat.mobilesafe.widget;



import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.wvp.BackgroundWorkerListener;

public class BackgroundWroker implements DialogInterface.OnClickListener,
		DialogInterface.OnCancelListener {


	public static final int BUTTON_STYLE_NONE = 0;
	public static final int BUTTON_STYLE_CANCEL = 1;
	public static final int BUTTON_STYLE_OK_CANCEL = 2;

	/**
	 * 确定
	 */
	public static final int OK = 0;
	/**
	 * 错误
	 */
	public static final int ERR = 1;
	/**
	 * 取消
	 */
	public static final int CANCEL = 2;

	private boolean isShowDialog = true;
	/**
	 * Dialog鐨勭偣鍑荤粨鏋?
	 */
	public int dialogResult = CANCEL;

	private AlertDialog dialog;
	private BackgroundWorkerListener listener;
	public Thread workThread = null;
	
	private WorkerEventArgs args;
	
	private TextView myDialogMsgText;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (listener != null) {
				listener.onComplete(BackgroundWroker.this,
						(WorkerEventArgs) msg.obj);
			}
		}
	};

	private boolean isWorking() {
		return (workThread != null);
	}


	public BackgroundWroker(){}

	
	public BackgroundWroker(Context context, boolean cancelable, String title,
			String showMessage, int buttonStyles, BackgroundWorkerListener bl) {
		listener = bl;
		Builder builder = new Builder(context);
		builder.setCancelable(cancelable);
		if (cancelable) {
			builder.setOnCancelListener(this);
		}
		if(title != null){
			builder.setTitle(title);
		}
//		if(showMessage != null){
//			builder.setMessage(showMessage);
//		}
		
		View view = LayoutInflater.from(context).inflate(R.layout.progress_dialog_view, null);
		myDialogMsgText = (TextView)view.findViewById(R.id.myDialogMsgText);
		myDialogMsgText.setText(showMessage);
		builder.setView(view);
		buildButtons(builder, buttonStyles);
		dialog = builder.create();
	}

	public BackgroundWroker(Context context, boolean cancelable, View view,
			String title, String showMessage, int buttonStyles,
			BackgroundWorkerListener bl) {
		listener = bl;
		Builder builder = new Builder(context);
		builder.setCancelable(cancelable);
		if (cancelable) {
			builder.setOnCancelListener(this);
		}
		if(title != null){
			builder.setTitle(title);
		}
		builder.setView(view);
		buildButtons(builder, buttonStyles);
		dialog = builder.create();

	}
	
	/**
	 * 设置是否显示dialog
	 * @param isShowDialog
	 */
	public void setIsShowDialog(boolean isShowDialog){
		this.isShowDialog = isShowDialog;
	}
	
	
	/**
	 * 设置dialog消息
	 * @param text
	 */
	public void setDialogMessage(String text){
		if(myDialogMsgText != null){
			myDialogMsgText.setText(text);
		}
	}

	public void startWork(final Object stratPram) {

		if (isWorking())
			return;

		try {
			
			if(isShowDialog){
				dialog.show();
			}
			
			workThread = new Thread(new Runnable() {

				@Override
				public void run() {

					Looper.prepare();
					args = new WorkerEventArgs();
					args.startParam = stratPram;

					try {
						if (listener != null) {
							listener.onWorking(BackgroundWroker.this, args);
						}
					} catch (InterruptedException e) {
						args.isCancel = true;
//						args.error = e;
					} finally {

						workThread = null;
						dialog.cancel();
						Message msg = handler.obtainMessage(0, args);
						handler.sendMessage(msg);
					}

				}
			});

			workThread.start();
		} catch (Exception e) {
			
		}
	}
	

	public void stopWork() {

		if (!isWorking())
			return;
		
		if(dialog != null){
			dialog.dismiss();
		}
		
		if(workThread != null){
			workThread.interrupt();
			workThread = null;
		}
	}

	private void buildButtons(Builder builder, int buttonStyles) {
		switch (buttonStyles) {

		case BUTTON_STYLE_CANCEL:
			builder.setNegativeButton("取 消", this);
			break;

		case BUTTON_STYLE_OK_CANCEL:
			builder.setPositiveButton("确 定", this);
			builder.setNegativeButton("取 消", this);
			break;
		case BUTTON_STYLE_NONE:
		default:
			return;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {

		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:

			dialogResult = OK;
            
			break;

		case DialogInterface.BUTTON_NEGATIVE: // 鍙栨秷

			dialogResult = CANCEL;
			args.isCancel = true;
			break;

		}

		stopWork();
	}

	public class WorkerEventArgs {
		//启动参数
		public Object startParam = null;
		//附属参数
		public Object tag = null;
		//是否点击按钮取消
		public boolean isCancel = false;
		//是否抛出异常
		public Exception error = null;
		//执行结果
		public Object result = null;
	}

	@Override
	public void onCancel(DialogInterface dialog) {

		dialogResult = CANCEL;
		args.isCancel = true;
		stopWork();

	}

}
