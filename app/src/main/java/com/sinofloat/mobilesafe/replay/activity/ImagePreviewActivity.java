package com.sinofloat.mobilesafe.replay.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.BaseActivity;
import com.sinofloat.mobilesafe.replay.db.MediaDB;
import com.sinofloat.mobilesafe.replay.entity.MediaEntity;
import com.sinofloat.mobilesafe.utils.MyComponentManager;
import com.sinofloat.mobilesafe.widget.ExtendedViewPager;
import com.sinofloat.mobilesafe.widget.TouchImageView;

import java.io.IOException;
import java.util.ArrayList;

import sinofloat.wvp.tools.FileUtil;

public class ImagePreviewActivity extends BaseActivity {

	/**
	 * Step 1: Download and set up v4 support library:
	 * http://developer.android.com/tools/support-library/setup.html Step 2:
	 * Create ExtendedViewPager wrapper which calls
	 * TouchImageView.canScrollHorizontallyFroyo Step 3: ExtendedViewPager is a
	 * custom view and must be referred to by its full package name in XML Step
	 * 4: Write TouchImageAdapter, located below Step 5. The ViewPager in the
	 * XML should be ExtendedViewPager
	 */

	private MediaDB mediaDB;
	
	private ExtendedViewPager mViewPager;

	private ArrayList<MediaEntity> picturePathList;
	/**
	 * 当前显示的图片的位置
	 */
	private int curPosition;
	/**
	 * 第几张图片
	 */
	private TextView numberText;

	private ImageView deleteBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_replay_image_preview);
		//透明状态栏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		numberText = (TextView) findViewById(R.id.numberText);
		mViewPager = (ExtendedViewPager) findViewById(R.id.view_pager);
		deleteBtn = (ImageView) findViewById(R.id.deleteBtn);
		deleteBtn.setOnClickListener(onclick);

		picturePathList = (ArrayList<MediaEntity>) MyComponentManager
				.getTransferedData();

		// 设置具体哪个图片
		String positionStr = getIntent().getStringExtra(
				MyComponentManager.STRING_KEY);
		curPosition = Integer.parseInt(positionStr);
		initAdapter();
		
		mediaDB = new MediaDB(this);
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		picturePathList = null;
	}

	private void initAdapter() {
		
		if(picturePathList.size() == 1){
			curPosition = 0;// 从最后一张删除的时候剩下最后一张的时候 mViewPager.setCurrentItem(curPosition, true);这个方法没有走onPageChanged
		}
		TouchImageAdapter adapter = new TouchImageAdapter(picturePathList);
		mViewPager.setAdapter(adapter);
		mViewPager.setOnPageChangeListener(pageChangeListener);
		mViewPager.setCurrentItem(curPosition, true);
		setTextNumber();
	}

	/**
	 * 滑到哪一页
	 */
	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {

			curPosition = position;

			setTextNumber();
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}
	};

	/**
	 * 按钮点击监听
	 */
	private OnClickListener onclick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			//删除图片 文件和数据库都要删除
			if (v == deleteBtn) {

				deleteBtn.setEnabled(false);
				
				try {
					MediaEntity entity = picturePathList.get(curPosition);
					String fileFullPath =entity.MediaStoreLocation;
					FileUtil.deleteFile(fileFullPath);
					picturePathList.remove(curPosition);
					mediaDB.deleteByMsgId(entity.mediaId);
					if(picturePathList.size() == 0){
//						finish();
					}
					initAdapter();
					setTextNumber();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				deleteBtn.setEnabled(true);
			}
		}
	};

	/**
	 * 设置第几张图片
	 */
	private void setTextNumber() {
		numberText.setText((curPosition + 1) +  " / " + picturePathList.size());
	}

	/**
	 * 预览图片适配
	 * 
	 * @author staid
	 */
	private static class TouchImageAdapter extends PagerAdapter {

		private ArrayList<MediaEntity> imgsPathArray;

		public TouchImageAdapter(Object imgsPathArray) {

			this.imgsPathArray = (ArrayList<MediaEntity>) imgsPathArray;
		}

		@Override
		public int getCount() {
			return imgsPathArray.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			TouchImageView img = new TouchImageView(container.getContext());
			String imgPath = imgsPathArray.get(position).MediaStoreLocation;
			// Options op = new Options();
			// op.inSampleSize = 4;
			// Bitmap bmp = BitmapFactory.decodeFile(imgPath, op);
			Bitmap bmp = BitmapFactory.decodeFile(imgPath);

			img.setImageBitmap(bmp);
			img.setTag(bmp);
			container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			// Log.e("TouchImageAdapter",
			// "instantiateItem++++++++++++++++++++");
			return img;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {

			Bitmap bmp = (Bitmap) ((View) object).getTag();
			if (bmp != null && !bmp.isRecycled()) {
				bmp.recycle();
				bmp = null;
			}
			container.removeView((View) object);
			// Log.e("TouchImageAdapter", "destroyItem-----------------");
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}
}
