package com.sinofloat.mobilesafe.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RemoteCameraPreview extends RelativeLayout {

	private boolean isDrawCenter;
	/**
	 * 无信号 还是正在连接中 预览中间的一个textView
	 */
	private TextView mStateText;

	/**
	 * 加载预览图像的等待进度转圈儿
	 */
	private ProgressBar mProgress;

	/**
	 * 预览的位图
	 */
	private Bitmap showBitmap;
	// 画的区域
	private RectF displayRect;

	/**
	 * 进度转圈儿是否是显示
	 */
	public boolean isProgressBarShowing = false;

	public RemoteCameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		loadView();
	}

	public RemoteCameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		loadView();
	}

	public RemoteCameraPreview(Context context) {
		super(context);
		loadView();
	}

	/**
	 * 手势监听
	 * 
	 * @param touchListener
	 * @param gestureListener
	 */
	public void setCameraPreviewMotionListener(OnTouchListener touchListener,
			OnGestureListener gestureListener) {
		this.setOnTouchListener(touchListener);
	}

	/**
	 * activity回调到视频数据 会设置bitmsap 加载数据进度框消失
	 * 
	 * @param bmp
	 */
	public void setImgBitmapAndDraw(Bitmap bmp) {

		showBitmap = bmp;
		this.invalidate();
	}

	/**
	 * 是否画中间
	 * 
	 * @param isDrawCenter
	 */
	public void setDrawCenter(boolean isDrawCenter) {
		this.isDrawCenter = isDrawCenter;
	}

	/**
	 * 初始化View
	 */
	private void loadView() {

		// 设置进度框在整个View的中间位置
		mProgress = new ProgressBar(getContext());

		LayoutParams progressParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);

		mProgress.setProgress(10);
		mProgress.setLayoutParams(progressParams);
		mProgress.setVisibility(View.GONE);

		this.addView(mProgress);

		// 设置textVIew在进度框的下边 中心位置
		LayoutParams textParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		textParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		textParams.addRule(RelativeLayout.BELOW, mProgress.getId());

		mStateText = new TextView(getContext());
		mStateText.setTextSize(10 * getResources().getDisplayMetrics().density);
		mStateText.setLayoutParams(textParams);
		this.addView(mStateText);

		setWillNotDraw(false);
	}

	/**
	 * 显示进度
	 */
	public void showProgressView() {

		isProgressBarShowing = true;
		mProgress.setVisibility(View.VISIBLE);
		mStateText.setText("连接中");
		Log.e("showProgressView()----------",
				String.valueOf(System.currentTimeMillis()));
	}

	/**
	 * 隐藏进度
	 */
	public void hideProgressView() {

		isProgressBarShowing = false;
		mProgress.setVisibility(View.GONE);
		mStateText.setText(null);
	}

	/**
	 * 解码超时 或者解码器异常时显示。
	 * 
	 * @param msg
	 */
	public void onDecoderError(String msg) {
		showBitmap = null;
		mProgress.setVisibility(View.GONE);
		mStateText.setText(msg);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	/**
	 * 画View
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (showBitmap == null) {

			canvas.drawColor(Color.BLACK);
			return;
		}

		// 获取View控件的宽高
		int viewW = getWidth();
		int viewH = getHeight();

		// 解码后 图像的宽高
		int bitmapW = showBitmap.getWidth();
		int bitmapH = showBitmap.getHeight();

		// 控件宽高对原始图像的宽高比 取相对较小的值
		float scaleW = (float) viewW / (float) bitmapW;
		float scaleH = (float) viewH / (float) bitmapH;
		float scale = scaleW < scaleH ? scaleW : scaleH;

		// 按比例缩放实际要画的宽高
		float targetWidth = bitmapW * scale;
		float targetHeight = bitmapH * scale;

		float targetX = 0;
		float targetY = 0;
		// 居中画
		if (isDrawCenter) {
			if (viewW / targetWidth > viewH / targetHeight) {
				targetX = (viewW - targetWidth) / 2;
			} else {
				targetY = (viewH - targetHeight) / 2;
			}
		}

		if (displayRect == null || displayRect.width() != targetWidth
				|| displayRect.height() != targetHeight) {
			displayRect = new RectF(targetX, targetY, targetX + targetWidth,
					targetY + targetHeight);
		}

		canvas.drawBitmap(showBitmap, null, displayRect, null);
	}

}
