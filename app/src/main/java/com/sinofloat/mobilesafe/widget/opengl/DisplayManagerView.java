package com.sinofloat.mobilesafe.widget.opengl;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DisplayManagerView extends GLSurfaceView
{
	private static final String TAG = "DisplayManagerView";
	
	public static final int LayoutType_Unknown = 0;
	public static final int LayoutType_1x1 = 1;// 非独占模式
	public static final int LayoutType_2x2 = 4;
	public static final int LayoutType_3x3 = 9;
	public static final int LayoutType_4x4 = 16;
        /* 优先显示单元位于整个区域 左上方
         * <LayoutType_A_B_C_D>
         * A: 优先显示单元 占总显示面积的比例
         * B: 优先显示单元右边区域 占总显示面积的比例
         * C: 优先显示单元底部区域 占总显示面积的比例
         * D: 优先显示单元对角区域 占总显示面积的比例
         */
    public static final int LayoutType_4_2_2_1 = 0x6;
	public static final int LayoutType_9_3_3_1 = 0x8;
    public static final int LayoutType_16_4_4_1 = 0xA;
    public static final int LayoutType_25_5_5_1 = 0xC;
    public static final int LayoutType_36_6_6_1 = 0xE;
        // 优先显示单元位于整个区域 右上方
    public static final int LayoutType_2_4_2_1 = 0x16;
    public static final int LayoutType_3_9_3_1 = 0x18;
    public static final int LayoutType_4_16_4_1 = 0x1A;
    public static final int LayoutType_5_25_5_1 = 0x1C;
    public static final int LayoutType_6_36_6_1 = 0x1E;
        // 优先显示单元位于整个区域 左下方
    public static final int LayoutType_2_1_4_2 = 0x26;
    public static final int LayoutType_3_1_9_3 = 0x28;
    public static final int LayoutType_4_1_16_4 = 0x2A;
    public static final int LayoutType_5_1_25_5 = 0x2C;
    public static final int LayoutType_6_1_36_6 = 0x2E;
        // 优先显示单元位于整个区域 右上方
    public static final int LayoutType_1_2_2_4 = 0x36;
    public static final int LayoutType_1_3_3_9 = 0x38;
    public static final int LayoutType_1_4_4_16 = 0x3A;
    public static final int LayoutType_1_5_5_25 = 0x3C;
    public static final int LayoutType_1_6_6_36 = 0x3E;
    
    

	// 以下两成员用于处理 DisplayView 的选中与反选中
	private DisplayView _currentDisplay;
        private DisplayView _previousDisplay;

	private boolean _isExclusive = false;// 标记当前是否处于单画面独占模式
        private int _areaIndex;// 用于更新显示单元坐标
        private int _currentDisplayNum;// 当前可见的显示单元数量
        private int _currentLayout = LayoutType_Unknown;// 当前显示布局
        private int _blankIndex;// 当前没放置 DisplayView 的位置索引
	private int _displayNum = 0;// DisplayView 的数量，必须首先设置并只能设置一次
 
	// 下列成员用于手势监听判断
	private OnDisplayManagerViewListener _onDisplayManagerViewListener;
	private GestureDetector _gestureDetector;
	private boolean _isScrolling = false;

	// 下列成员用于数字缩放
	private boolean _isScale = false;
	private float _prevDistance;
	private PointF _prevFocalPoint = new PointF();
	private PointF _prevPoint = new PointF();

	// 用于屏蔽数字缩放结束时触发的fling
	private boolean _isBlockFling = false;

	private DisplayArea[] _displayArea;
	
	/**
	 * 内部类
	 * @author staid
	 */
	private class DisplayArea {
		public Rect frame;
		public DisplayView display;
	}
	        

	public DisplayManagerView(Context context) {
		super(context);
	}

	public DisplayManagerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	static public interface OnDisplayManagerViewListener {
		void onSelectionChanged(DisplayView fromDisplay, DisplayView toDisplay);
		void onSwipeOnDisplay(DisplayView display, int direction);
		void onTouchStart();
		//void onCenterButtonClick(DisplayView display);
	}

	public void setOnDisplayManagerViewListener(OnDisplayManagerViewListener listener) {
		_onDisplayManagerViewListener = listener;
	}

	/**
	 * 返回指定index的 displayView
	 * @param index
	 * @return
	 */
	public DisplayView displayAtIndex(int index) {
		return _displayArea[index].display;
	}

	/**
	 * 返回当前选中的预览的相机画面
	 * @return
	 */
	public DisplayView currentDisplay() {
		return _currentDisplay;
	}

	// displayNum: 1, 4, 9, 16, 6, 8, 10, 12, 14
	public void setDisplayNum(int displayNum) {
		if (0 != _displayNum) {
			Log.e(TAG, "setDisplayNum take no effect");
			return;
		}

		_displayNum = displayNum;
		_currentDisplayNum = _displayNum;

		init();
	}

	/**
	 * 设置显示几个画面
	 * @param layoutType
	 */
	public void setLayout(int layoutType) {
		int displayNum;
		if (LayoutType_1x1 == layoutType ||
		    LayoutType_2x2 == layoutType ||
		    LayoutType_3x3 == layoutType ||
		    LayoutType_4x4 == layoutType) {
			displayNum = layoutType;
		} else {
			displayNum = layoutType & 0xf;
		}
		
		if (_displayNum < displayNum) {
			Log.e(TAG, "setLayout take no effect");
			return;
		}

		if (layoutType != _currentLayout) {
			_isExclusive = false;
			_currentLayout = layoutType;
			
			updateDisplayArea();

			// 将数字缩放还原为初始状态
			_currentDisplay.restoreViewPort();

			_currentDisplay = _displayArea[0].display;
			updateCurrentSelection();
		} else {
			// 当前正处于目标布局，并处于独占模式，则退出独占模式
			setExclusive(false);
		}
	}

	public void setExclusive(boolean isExclusive) {
		if ((isExclusive && _isExclusive) ||
		    (!isExclusive && !_isExclusive)) {
			return;
		}

		_currentDisplay.setSelected(!isExclusive);
		_currentDisplay.setDrawBorder(!isExclusive);

		_isExclusive = isExclusive;
		updateDisplays();
	}
	
	
	/**
	 * 事件
	 * @param event
	 * @return
	 */
	public boolean onTouchEvent(MotionEvent event) {
		if (_isScrolling && 
		    (event.getAction() == MotionEvent.ACTION_UP ||
		     event.getAction() == MotionEvent.ACTION_CANCEL)) {
			Log.v(TAG, "onTouchEvent, up/cancel");

			_isScrolling = false;
			
			// 将 _currentDisplay 最终放置在 _blankIndex 所索引的位置
			_currentDisplay.setArea(_displayArea[_blankIndex].frame);
			_displayArea[_blankIndex].display = _currentDisplay;
		}
			
		if (_isExclusive) {
			handleVideoScaleAndMove(event);
		}

		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP &&
		    _isBlockFling) {
			Log.v(TAG, "reset flag: _isBlockFling");
			_isBlockFling = false;
			return true;
		}
			
		return _gestureDetector.onTouchEvent(event);
	}
	
	// TODO: to be remove
	protected void finalize() {
		for (int i=0; i<_displayNum; i++) {
			_displayArea[i].display.destroy();
			_displayArea[i].display = null;
			_displayArea[i].frame = null;
			_displayArea[i] = null;
		}
	}

	// Stategy: hide replay_picture_all at first, then show needed.
	private void updateDisplays() {
		for (int i=0; i<_displayNum; i++) {
			_displayArea[i].display.setVisible(false);
		}

		if (_isExclusive) {
			_currentDisplay.setArea(new Rect(0, 0, getWidth(), getHeight()));
			_currentDisplay.setVisible(true);
		} else {
			for (int i=0; i<(int)_currentDisplayNum; i++) {
				_displayArea[i].display.setArea(_displayArea[i].frame);
				_displayArea[i].display.setVisible(true);
			}
		}	       
	}

	private void updateDisplayBlockAt(Point origin, int row, int col, int w, int h, boolean isMerge) {
		if (isMerge) {
			//Rect frame = new Rect(origin.x, origin.y, origin.x + (w * col), origin.y + (h * row));
			//_displayArea[_areaIndex].frame = frame;
			_displayArea[_areaIndex].frame.set(origin.x, origin.y, origin.x + (w * col), origin.y + (h * row));
			++_areaIndex;
			Log.v(TAG, "isMerge, index: " + (_areaIndex - 1));
			Log.v(TAG, "x: " + origin.x + ", y: " + origin.y + ", w: " + w + ", h: " + h);
		} else {
			for (int i=0; i<row; i++) {
				int originY = i * h + origin.y;
				for (int j=0; j<col; j++) {
					int originX = j * w + origin.x;
					//Rect frame = new Rect(originX, originY, originX + w, originY + h);
					//_displayArea[_areaIndex].frame = frame;
					_displayArea[_areaIndex].frame.set(originX, originY, originX + w, originY + h);
					++_areaIndex;
				}
			}
		}
	}

	/* 适用于优先显示区域在左上方的情况
	 * total: 单元格的总数
	 */
	private void updateDisplayAreaPriorityTopLeftWithCellNumber(int total) {
		int row, col;//整个显示区域的行列数
		int w, h;//单元格的宽高值
		Point origin;

		row = col = (int)Math.sqrt((double)total);
		w = (int)(getWidth() / row);
		h = (int)(getHeight() / row);

		origin = new Point(0, 0);
		updateDisplayBlockAt(origin, row - 1, col - 1, w, h, true);

		origin = new Point(w * (col - 1), 0);
		updateDisplayBlockAt(origin, row - 1, 1, w, h, false);

		origin = new Point(0, h * (row - 1));
		updateDisplayBlockAt(origin, 1, col - 1, w, h, false);

		origin = new Point(w * (col - 1), h * (row - 1));
		updateDisplayBlockAt(origin, 1, 1, w, h, false);
	}

	private void updateDisplayAreaPriorityTopRightWithCellNumber(int total) {
		int row, col;//整个显示区域的行列数
		int w, h;//单元格的宽高值
		Point origin;

		row = col = (int)Math.sqrt((double)total);
		w = (int)(getWidth() / row);
		h = (int)(getHeight() / row);

		origin = new Point(0, 0);
		updateDisplayBlockAt(origin, row - 1, 1, w, h, false);

		origin = new Point(w, 0);
		updateDisplayBlockAt(origin, row - 1, col - 1, w, h, true);

		origin = new Point(0, h * (row - 1));
		updateDisplayBlockAt(origin, 1, 1, w, h, false);

		origin = new Point(w, h * (row - 1));
		updateDisplayBlockAt(origin, 1, col - 1, w, h, false);
	}

	private void updateDisplayAreaPriorityBottomLeftWithCellNumber(int total) {
		int row, col;//整个显示区域的行列数
		int w, h;//单元格的宽高值
		Point origin;

		row = col = (int)Math.sqrt((double)total);
		w = (int)(getWidth() / row);
		h = (int)(getHeight() / row);

		origin = new Point(0, 0);
		updateDisplayBlockAt(origin, 1, col - 1, w, h, false);

		origin = new Point(w * (col - 1), 0);
		updateDisplayBlockAt(origin, 1, 1, w, h, false);

		origin = new Point(0, h);
		updateDisplayBlockAt(origin, row - 1, col - 1, w, h, true);

		origin = new Point(w * (col - 1), h);
		updateDisplayBlockAt(origin, row - 1, 1, w, h, false);
	}

	private void updateDisplayAreaPriorityBottomRightWithCellNumber(int total) {
		int row, col;//整个显示区域的行列数
		int w, h;//单元格的宽高值
		Point origin;

		row = col = (int)Math.sqrt((double)total);
		w = (int)(getWidth() / row);
		h = (int)(getHeight() / row);

		origin = new Point(0, 0);
		updateDisplayBlockAt(origin, 1, 1, w, h, false);

		origin = new Point(w, 0);
		updateDisplayBlockAt(origin, 1, col - 1, w, h, false);

		origin = new Point(0, h);
		updateDisplayBlockAt(origin, row - 1, 1, w, h, false);

		origin = new Point(w, h);
		updateDisplayBlockAt(origin, row - 1, col - 1, w, h, true);
	}

	private void updateDisplayAreaPriorityWithCellNumber(int total) {
		switch (_currentLayout >> 4) {
		case 0:
			updateDisplayAreaPriorityTopLeftWithCellNumber(total);
			break;
		case 1:
			updateDisplayAreaPriorityTopRightWithCellNumber(total);
			break;
		case 2:
			updateDisplayAreaPriorityBottomLeftWithCellNumber(total);
			break;
		case 3:
			updateDisplayAreaPriorityBottomRightWithCellNumber(total);
			break;
		default:
			break;
		}
	}

	private void updateDisplayAreaCoordinate() {
		int row, col;// 整个显示区域的行列数
		int w, h;// 单元格的宽高值
		Point origin;

		// 这步至关重要
		_areaIndex = 0;

		// 根据显示布局，更新当前可见的显示单元数量
		_currentDisplayNum = _currentLayout & 0xf;

		switch (_currentLayout) {
		case LayoutType_1x1:
		case LayoutType_2x2:
		case LayoutType_3x3:
		case LayoutType_4x4:
			// NOTE: 对于标准显示布局
			_currentDisplayNum = _currentLayout;

			row = col = (int)Math.sqrt((double)_currentLayout);
			w = (int)(getWidth() / row);
			h = (int)(getHeight() / row);

			origin = new Point(0, 0);
			updateDisplayBlockAt(origin, row, col, w, h, false);
			break;

		case LayoutType_4_2_2_1://相当于3x3小格
		case LayoutType_2_4_2_1:
		case LayoutType_2_1_4_2:
		case LayoutType_1_2_2_4:
			updateDisplayAreaPriorityWithCellNumber(9);
			break;

		case LayoutType_9_3_3_1://相当于4x4小格
		case LayoutType_3_9_3_1:
		case LayoutType_3_1_9_3:
		case LayoutType_1_3_3_9:
			updateDisplayAreaPriorityWithCellNumber(16);
			break;

		case LayoutType_16_4_4_1://相当于5x5小格
		case LayoutType_4_16_4_1:
		case LayoutType_4_1_16_4:
		case LayoutType_1_4_4_16:
			updateDisplayAreaPriorityWithCellNumber(25);
			break;

		case LayoutType_25_5_5_1://相当于6x6小格
		case LayoutType_5_25_5_1:
		case LayoutType_5_1_25_5:
		case LayoutType_1_5_5_25:
			updateDisplayAreaPriorityWithCellNumber(36);
			break;

		case LayoutType_36_6_6_1://相当于7x7小格
		case LayoutType_6_36_6_1:
		case LayoutType_6_1_36_6:
		case LayoutType_1_6_6_36:
			updateDisplayAreaPriorityWithCellNumber(49);
			break;
		}
	}

	private void updateDisplayArea() {
		updateDisplayAreaCoordinate();
		updateDisplays();
	}

	private void updateCurrentSelection() {
		if (_currentDisplay == _previousDisplay) {
			return;
		}

		_previousDisplay.setSelected(false);

		if (!_isExclusive) {
			_currentDisplay.setSelected(true);
		}

		if (null != _onDisplayManagerViewListener) {
			_onDisplayManagerViewListener.onSelectionChanged(_previousDisplay, _currentDisplay);
		}

		_previousDisplay = _currentDisplay;
	}

	/*
	 * 获得指定点所在的显示区域索引号
	 * point: 当前视图坐标系
	 * excludeIndex: 被排除在外的索引号。传递-1时，为不排除任何索引号
	 */
	private int displayAreaIndexFromPoint(Point point, int excludeIndex) {
		int i;
		for (i=0; i<(int)_currentDisplayNum; i++) {
			if (i != excludeIndex && _displayArea[i].frame.contains(point.x, point.y)) {
				break;
			}
		}

		return i;
	}

	/**
	 *  初始化显示区域
	 */
	private void allocDisplayArea() {
		// NOTE: 这一步只对对象的引用分配了内存
		_displayArea = new DisplayArea[_displayNum];
		for (int i=0; i<_displayNum; ++i) {
			_displayArea[i] = new DisplayArea();
			_displayArea[i].frame = new Rect();
			_displayArea[i].display = new DisplayView();
			_displayArea[i].display.setDrawBorder(true);
			_displayArea[i].display.setVisible(false);
		}
	}

	private void init() {
		// must do this at first
		allocDisplayArea();

		_currentDisplay = _displayArea[0].display;
		_previousDisplay = _currentDisplay;

		// 将第0个位置设置为选中状态
		_currentDisplay.setSelected(true);

		MyOnGestureListener onGestureListener = new MyOnGestureListener();
		_gestureDetector = new GestureDetector(getContext(), onGestureListener);
		_gestureDetector.setOnDoubleTapListener(onGestureListener);

		setEGLContextClientVersion(2);
		setRenderer(new Renderer());
	}

	
	/**
	 * 内部类 openGL渲染画出
	 * @author staid
	 *
	 */
	private class Renderer implements GLSurfaceView.Renderer {
		/* Note that when the EGL context is lost, replay_picture_all OpenGL resources associated with that context
		   will be automatically deleted. You do not need to call the corresponding "glDelete" methods 
		   such as glDeleteTextures to manually delete these lost resources.
		 */
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {		
			//Log.e(TAG, "onSurfaceCreated");

			DisplayView.onSurfaceCreated();
			
			for (int i=0; i<_displayNum; i++) {
				_displayArea[i].display.init();
			}
		}
						
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			//Log.e(TAG, "onSurfaceChanged");

			DisplayView.onSurfaceChanged(width, height);
			
			updateDisplayArea();
		}

		public void onDrawFrame(GL10 gl) {
			//Log.v(TAG, "onDrawFrame");

			DisplayView.onDrawFrame();

			for (int i=0; i<_displayNum; i++) {
				if (_displayArea[i].display.isVisible()) {
					if (_displayArea[i].display != _currentDisplay) {
						_displayArea[i].display.display();
					}
				}
			}

			// 选中的区域画最后，保证显示在最上层
			if(_currentDisplay != null){
				_currentDisplay.display();
			}

			// 刷新频率控制
			/*
			try {
				Thread.sleep(25);//40fps
			} catch (InterruptedException ie) {
					
			}
			*/
		}

		private static final String TAG = "GLSurfaceView.Renderer";
        }

	private class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {
		private static final String TAG = "MyOnGestureListener";

		/*
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.v(TAG, "onSingleTapConfirmed");
			return true;
		}

		public void onShowPress(MotionEvent e) {
			Log.v(TAG, "onShowPress");
		}
		*/

		public boolean onDoubleTapEvent (MotionEvent e) {
			Log.v(TAG, "onDoubleTapEvent");
			
			if (MotionEvent.ACTION_UP == (e.getAction() & MotionEvent.ACTION_MASK)) {
				Log.v(TAG, "onDoubleTapEvent: MotionEvent.ACTION_UP");

				if (_isExclusive) {
					// NOTE: restore viewPort here
					_currentDisplay.restoreViewPort();

					setExclusive(false);
				} else {
					setExclusive(true);

					/*
					  Point point = new Point((int)e.getX(0), (int)e.getY(0));
					  int index = displayAreaIndexFromPoint(point, -1);
					  _currentDisplay = _displayArea[index].display;
					  updateCurrentSelection();
					*/
				}
			}

			return true;
		}

		public boolean onDoubleTap(MotionEvent e) {
			Log.v(TAG, "onDoubleTap");
			return true;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Log.v(TAG, "onFling");

			if (_isExclusive) {
				// may show dynamic image view here
				if (null != _onDisplayManagerViewListener) {
					int direction = 0;
					Log.v(TAG, "vx: "  + velocityX + ", vy: " + velocityY);
					//Log.v(TAG, "distanceX: " + distanceX + ", distanceY: " + distanceY);
					// 0：上，1：下，2：左, 3：右
					if (Math.abs(velocityX) > Math.abs(velocityY)) {
						if (0 < velocityX) {
							direction = 3;
						} else {
							direction = 2;
						}
					} else {
						if (0 < velocityY) {
							direction = 1;
						} else {
							direction = 0;
						}
					}
					_onDisplayManagerViewListener.onSwipeOnDisplay(_currentDisplay, direction);
				}
			}

			return true;
		}
		
		// NOTE: onDown必须return true，默认返回false
		public boolean onDown(MotionEvent e) {
			Log.v(TAG, "onDown");
			
			if (_isExclusive) {
				// do nothing here
			} else {
				_blankIndex = displayAreaIndexFromPoint(new Point((int)e.getX(0), (int)e.getY(0)), -1);
				_currentDisplay = _displayArea[_blankIndex].display;

				// 设置 _currentDisplay 为选中状态
				updateCurrentSelection();
			}						

			if (null != _onDisplayManagerViewListener) {
				_onDisplayManagerViewListener.onTouchStart();
			}

			return true;
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {		       
			if (_isExclusive) {
				// do nothing
			} else {
				_isScrolling = true;
				
				Rect newFrame = _currentDisplay.area();
				newFrame.offset((int)-distanceX, (int)-distanceY);
				_currentDisplay.setArea(newFrame);
							
				// 如果当前 _currentDisplay 的中心点已经位于其它 DisplayView 之内，
				// 则移动该 DisplayView 至 _blankIndex 所索引的位置，更新 _bankIndex，
				// <可选>并调整 _currentDisplay 尺寸
				Point centerPoint = new Point(newFrame.centerX(), newFrame.centerY());
				int i = displayAreaIndexFromPoint(centerPoint, _blankIndex);
				if (i < _currentDisplayNum) {
					_displayArea[i].display.setArea(_displayArea[_blankIndex].frame);
					_displayArea[_blankIndex].display = _displayArea[i].display;
					_blankIndex = i;
				}
			}
						
			return true;
		}
	}

	private void handleVideoScaleAndMove(MotionEvent event) {
		int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> 
			MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		int pointerId = event.getPointerId(pointerIndex);
			
		//Log.v(TAG, "pointerIndex: " + pointerIndex + ", pointerId: " + pointerId);

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			//Log.v(TAG, "action down, x: " + event.getX() + ", y: " + event.getY());
			_prevPoint.set(event.getX(pointerIndex), event.getY(pointerIndex));
			break;
		case MotionEvent.ACTION_UP:
			Log.v(TAG, "action up");
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			Log.v(TAG, "pointer down: " + event.getPointerCount());
			if (2 == event.getPointerCount()) {
				_isScale = true;
				_prevDistance = distance(event);
				midPoint(event, _prevFocalPoint);

				_isBlockFling = true;
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			Log.v(TAG, "pointer up: " + event.getPointerCount());
			if (2 == event.getPointerCount()) {
				_isScale = false;
				Log.v(TAG, "pointerIndex: " + pointerIndex + ", pointerId: " + pointerId);
				_prevPoint.set(event.getX(pointerIndex == 0 ? 1 : 0), event.getY(pointerIndex == 0 ? 1 : 0));
			}
			break;
		case MotionEvent.ACTION_MOVE:
			float distanceX, distanceY;		       
			//Log.v(TAG, "getHistorySize(): " + event.getHistorySize());
			if (_isScale) {
				PointF focalPoint = new PointF();
				midPoint(event, focalPoint);
				distanceX = focalPoint.x - _prevFocalPoint.x;
				distanceY = focalPoint.y - _prevFocalPoint.y;
				doScale(event, distanceX, distanceY);
			} else {
				distanceX = event.getX(pointerIndex) - _prevPoint.x;
				distanceY = event.getY(pointerIndex) - _prevPoint.y;
				_currentDisplay.moveViewPort(distanceX, distanceY);
				_prevPoint.set(event.getX(pointerIndex), event.getY(pointerIndex));
			}
			break;
		}
	}

	private void doScale(MotionEvent event, float distanceX, float distanceY) {
		float distance;
		float scaleInc = 0.0f;
		
		distance = distance(event);
		scaleInc = distance / _prevDistance - 1.0f;

		Log.v(TAG, "doScale, scaleInc: " + scaleInc);
		
		_currentDisplay.scaleAndMoveViewPort(scaleInc * 2, _prevFocalPoint, distanceX, distanceY);
			
		//_prevFocalPoint = midPoint(event);
		midPoint(event, _prevFocalPoint);
		_prevDistance = distance;
	}

	private void midPoint(MotionEvent event, PointF midPoint) {
		float x = (event.getX(0) + event.getX(1)) / 2;
		float y = (event.getY(0) + event.getY(1)) / 2;
		midPoint.set(x, y);
	}

	private float distance(PointF p1, PointF p2) {
		float x = p1.x - p2.x;
		float y = p1.y - p2.y;
		return (float)Math.sqrt(x * x + y * y);
	}

	private float distance(MotionEvent event) {
		float x = event.getX(1) - event.getX(0);
		float y = event.getY(1) - event.getY(0);
		return (float)Math.sqrt(x * x + y * y);
	}
}
