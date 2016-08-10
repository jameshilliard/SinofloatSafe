package com.sinofloat.mobilesafe.widget.opengl;

import android.graphics.PointF;
import android.graphics.Rect;
import android.opengl.GLES20;

public class DisplayView extends GLES20 {
	
	/**
	 * 渲染视频的View
	 */
	private VideoRendererGLES20 _videoRenderer;
	private CommonRendererGLES20 _commonRenderer;
	
	
	private static int _backingWidth;
	private static int _backingHeight;

	private Object _userData;
	private boolean _isSelected = false;
	private boolean _isDrawBorder = true;
	private boolean _isDisplayVideo = false;
	private boolean _isVisible = true;
	private Rect _area = new Rect();
	private Rect _glArea = new Rect();

	private static String TAG = "DisplayView";

	public Rect area() {
		return new Rect(_area);
	}

	public static int backingWidth() {
		return _backingWidth;
	}

	public static int backingHeight() {
		return _backingHeight;
	}

	public static void onSurfaceCreated() {
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glDisable(GL_DEPTH_TEST);

		// Indicate that pixel rows are tightly packed
		// (defaults to stride of 4 which is kind of only good for
		// RGBA or FLOAT data types)
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	}

	public static void onSurfaceChanged(int width, int height) {
		_backingWidth = width;
		_backingHeight = height;
	}

	public static void onDrawFrame() {
		glClear(GLES20.GL_COLOR_BUFFER_BIT);
	}

	public synchronized void displayVideo(byte[] videoBuffer, int videoWidth,
			int videoHeight, int orientation) {
		if (null != _videoRenderer) {
			_videoRenderer.updateVideo(videoBuffer, videoWidth, videoHeight,
					orientation);
			_isDisplayVideo = true;
		}
	}

	public void setUserData(Object userData) {
		_userData = userData;
	}

	public Object userData() {
		return _userData;
	}

	public synchronized int init() {
		// Log.v(TAG, "DisplayView: init()");

		_videoRenderer = null;
		_commonRenderer = null;
		_videoRenderer = new VideoRendererGLES20();
		_commonRenderer = new CommonRendererGLES20();
		_videoRenderer.init();
		_commonRenderer.init();

		return 0;
	}

	public void destroy() {
		_videoRenderer = null;
		_commonRenderer = null;
	}

	/**
	 * 显示 画出view
	 */
	public void display() {
		_commonRenderer.renderBlock(_glArea);

		if (_isDisplayVideo) {
			_videoRenderer.render(_glArea);
		}

		if (_isDrawBorder) {
			_commonRenderer.render(_glArea, _isSelected);
		}
	}

	public synchronized void scaleAndMoveViewPort(float scaleInc,
			PointF origFocalPoint, float distanceX, float distanceY) {
		if (null != _videoRenderer) {
			_videoRenderer.scaleAndMoveViewPort(scaleInc, origFocalPoint,
					distanceX, distanceY);
		}
	}

	public synchronized void moveViewPort(float distanceX, float distanceY) {
		if (null != _videoRenderer) {
			_videoRenderer.moveViewPort(distanceX, distanceY);
		}
	}

	public synchronized void restoreViewPort() {
		if (null != _videoRenderer) {
			_videoRenderer.restoreViewPort();
		}
	}

	public void setStretchToFit(boolean isStretchToFit) {
		_videoRenderer.setStretchToFit(isStretchToFit);
	}

	public void clear() {
		_isDisplayVideo = false;
	}

	public void setSelected(boolean isSelected) {
		_isSelected = isSelected;
	}

	public boolean isSelected() {
		return _isSelected;
	}

	public void setDrawBorder(boolean isDrawBorder) {
		_isDrawBorder = isDrawBorder;
	}

	public boolean isDrawBorder() {
		return _isDrawBorder;
	}

	public void setDisplayVideo(boolean isDisplayVideo) {
		_isDisplayVideo = isDisplayVideo;
	}

	public boolean isDisplayVideo() {
		return _isDisplayVideo;
	}

	public void setVisible(boolean isVisible) {
		_isVisible = isVisible;
	}

	public boolean isVisible() {
		return _isVisible;
	}

	public void setArea(Rect area) {
		_area.set(area);

		// 因为 opengl 绘图原点位于绘图区域的左下角，所以要做以下的转换
		int height = area.height();
		int invertY = _backingHeight - area.height() - area.top;
		_glArea.set(area.left, invertY, area.right, invertY + height);
	}

}
