package com.sinofloat.mobilesafe.replay.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.sinofloat.mobilesafe.R;
import com.sinofloat.mobilesafe.base.BaseFragment;
import com.sinofloat.mobilesafe.replay.activity.ImagePreviewActivity;
import com.sinofloat.mobilesafe.replay.adapter.PictureAdapter;
import com.sinofloat.mobilesafe.replay.db.MediaDB;
import com.sinofloat.mobilesafe.replay.entity.MediaEntity;
import com.sinofloat.mobilesafe.utils.MyComponentManager;
import com.sinofloat.mobilesafe.utils.Tools;
import com.sinofloat.mobilesafe.widget.BackgroundWroker;
import com.sinofloat.mobilesafe.widget.BackgroundWroker.WorkerEventArgs;
import com.sinofloat.mobilesafe.wvp.BackgroundWorkerListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import sinofloat.wvp.tools.FileUtil;
import sinofloat.wvp.tools.ToastUtil;

/**
 * Created by oyk on 2016/7/14.
 *
 */
public class PictureFragment extends BaseFragment {
    @BindView(R.id.replay_picture_sp_left)
    Spinner spinnerLeft;
    @BindView(R.id.replay_picture_sp_right)
    Spinner spinnerRight;
    @BindView(R.id.replay_picture_rlv)
    RecyclerView recyclerView;
    @BindView(R.id.replay_picture_bottom_cb)
    CheckBox allSelect;
    @BindView(R.id.replay_picture_bottom_btn_delete)
    ImageView delete;
    @BindView(R.id.replay_picture_bottom_btn_cancel)
    ImageView cancel;
    @BindView(R.id.replay_picture_bottom_btn_save)
    ImageView save;
    @BindView(R.id.replay_picture_bottom)
    RelativeLayout bottomPart;
    private MediaDB mediaDB;
    private ArrayList<MediaEntity> pictureList;
    private PictureAdapter pictureAdapter;
    private ArrayAdapter<String> yearAdapter;
    private ArrayAdapter<String> monthAdapter;
    private String year, month;
    /**
     * 第一条添加的一条记录
     */
    private MediaEntity firstMediaEntity;
    private int firstYear, firstMonth;
    /**
     * 最后添加的一条记录
     */
    private MediaEntity lastMediaEntity;
    private int lastYear, lastMonth;
    /**
     * 是否是编辑模式
     */
    private boolean isEditMode = false;
    //FIXME 应该是做成滑动加载 以后再完善
    private static final int pazeSize = 10000;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //    Logger.e("是否被隐藏"+hidden);
        //假如当前页面是这个fragment
        if (!hidden) {

            Tools.SDCardReady(getActivity());

            if (mediaDB == null) {

                return;
            }

        }else {
            if (isEditMode){
                cancel();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_replay_picture, null);
        ButterKnife.bind(this, view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        initData();
        /**
         * 全选按钮监听
         */
        allSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pictureAdapter.allSelect(isChecked);

            }
        });
        /**
         * 删除按钮监听
         */
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });
        /**
         * 取消按钮监听
         */
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        //保存图片到相册
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        /**
         * 年份选择按钮监听
         */
        spinnerLeft.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                year = yearAdapter.getItem(position);
                searchData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /**
         * 月份选择按钮监听
         */
        spinnerRight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                month = monthAdapter.getItem(position);
                searchData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }




    @Override
    public void onResume() {
        super.onResume();
        //刷新
        searchData();
        pictureAdapter.notifyDataSetChanged();
    }

    /**
     * 初始化 加载数据 FIXME 应该是分段加载
     */
    private void initPictureData() {

        if (!Tools.SDCardReady(getActivity())) {
            return;
        }

        // FIXME 需要滑动加载
        pictureList = mediaDB.quieryPictureByPageNumber(1, pazeSize);
        pictureAdapter = new PictureAdapter(R.layout.item_replay_picture_rlv, pictureList, allSelect);
       // Logger.d(pictureList);
        recyclerView.setAdapter(pictureAdapter);
    }

    protected void initData() {
        mediaDB = MediaDB.getMediaDBInstance(getActivity());
       /*for (int i = 0;i<10;i++){
            MediaEntity entity = new MediaEntity();
            entity.mediaCreatTime = System.currentTimeMillis();
            entity.mediaCreatUserNm = Tools.getDate(entity.mediaCreatTime);
            entity.mediaType = _WvpMediaMessageTypes.PICTUREJPEG;
            entity.MediaStoreLocation = App.fileSetting.safeScreenshotDir
                    + i + ".jpg";
            mediaDB.add(entity);
        }*/

        firstMediaEntity = mediaDB.queryTheFirstesMedia();
        lastMediaEntity = mediaDB.queryTheLatestMedia();
        if (firstMediaEntity == null || lastMediaEntity == null) {
            firstYear = Tools.getYear(System.currentTimeMillis());
            firstMonth = Tools.getMonth(System.currentTimeMillis());
            lastYear = Tools.getYear(System.currentTimeMillis());
            lastMonth = Tools.getMonth(System.currentTimeMillis());
        } else {
            firstYear = Tools.getYear(firstMediaEntity.mediaCreatTime);
            firstMonth = Tools.getMonth(firstMediaEntity.mediaCreatTime);
            lastYear = Tools.getYear(lastMediaEntity.mediaCreatTime);
            lastMonth = Tools.getMonth(lastMediaEntity.mediaCreatTime);
        }

        initPictureData();

        // 建立数据源
        String[] years = new String[lastYear - firstYear + 1];
        for (int i = 0; i < years.length; i++) {
            years[i] = (firstYear + i) + "年";
        }
        String[] months = getResources().getStringArray(R.array.month);
        // 建立Adapter并且绑定数据源
        yearAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_spinner, years);
        monthAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_spinner, months);
        yearAdapter.setDropDownViewResource(R.layout.item_spinner);
        monthAdapter.setDropDownViewResource(R.layout.item_spinner);
        spinnerLeft.setAdapter(yearAdapter);
        spinnerRight.setAdapter(monthAdapter);
        spinnerLeft.setSelection(lastYear - firstYear);
        spinnerRight.setSelection(lastMonth - 1);
    }

    /**
     * 根据条件查询数据库
     */
    public void searchData() {
        //退出编辑模式
        if(isEditMode){
            cancel();
        }

        if ((year != null) && (month != null)) {
          //  Logger.e(year + ":" + month);
            Date startDate = Tools.str2Date(year + month, "yyyy年MM月");
            Date endDate = Tools.addMonth(startDate, 1);
            long startTimeStamp = Tools.getMillis(startDate);
            long endTimeStamp = Tools.getMillis(endDate);
            pictureList = mediaDB.quieryPictureByMonth(startTimeStamp, endTimeStamp);
            pictureAdapter = new PictureAdapter(R.layout.item_replay_picture_rlv, pictureList, allSelect);
            /**
             * 图片点击事件监听
             */
            pictureAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int i) {
                    if (!isEditMode) {
                        // 所有路径传递到预览的activity
                        MyComponentManager.setTransferedData(pictureList);
                        MyComponentManager.startActivity(false, getActivity(),
                                ImagePreviewActivity.class, String.valueOf(i));
                    }

                }
            });
            /**
             * 图片长按事件监听
             */
            pictureAdapter.setOnRecyclerViewItemLongClickListener(new BaseQuickAdapter.OnRecyclerViewItemLongClickListener() {
                @Override
                public boolean onItemLongClick(View view, int i) {
                    isEditMode = true;
                    pictureAdapter.showCheckBox(true);
                    bottomPart.setVisibility(View.VISIBLE);
                    return true;
                }
            });

            recyclerView.setAdapter(pictureAdapter);
            pictureAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 执行删除操作
     */
    /**
     * 删除文件
     */
    private void delete() {

        boolean canDelete = false;
        if (pictureList != null && pictureList.size() > 0) {
            for (int i = 0; i < pictureList.size(); i++) {
                if (pictureList.get(i).selectState) {
                    canDelete = true;
                    break;
                }
            }
        }
        if (!canDelete) {
            ToastUtil.showSimpleToast(getActivity(), "请选择要删除的图片", true);
        } else {
            BackgroundWroker delete = new BackgroundWroker(getActivity(),
                    false, null, "正在删除图片...",
                    BackgroundWroker.BUTTON_STYLE_NONE, deleteListener);
            delete.startWork(null);
        }
    }


    /**
     * 删除监听
     */
    private BackgroundWorkerListener deleteListener = new BackgroundWorkerListener() {

        @Override
        public void onWorking(Object sender, WorkerEventArgs args)
                throws InterruptedException {
            args.result = deleteFile();
        }

        @Override
        public void onComplete(Object sender, WorkerEventArgs args) {

            if (args.result == null) {

                ToastUtil.showSimpleToast(getActivity(), "删除成功", true);
                if (pictureList.size() == 0) {
                    ToastUtil.showSimpleToast(getActivity(), "数据已经完全删除", true);
                }
            } else {
                ToastUtil.showSimpleToast(getActivity(),
                        String.valueOf(args.result), true);
            }
            pictureAdapter.notifyDataSetChanged();

        }
    };


    /**
     * 具体删除操作
     */
    private String deleteFile() {

        String errorMsg = null;
        for (int i = pictureList.size() - 1; i >= 0; i--) {

            MediaEntity entity = pictureList.get(i);
            boolean state = entity.selectState;
            String filePath = entity.MediaStoreLocation;

            if (state) {
                try {
                    FileUtil.deleteFile(filePath);
                    pictureList.remove(i);
                    mediaDB.deleteByMsgId(entity.mediaId);
                } catch (IOException e) {
                    e.printStackTrace();
                    errorMsg = "删除文件出现异常";
                    break;
                }
            }
        }
        return errorMsg;
    }

    /**
     * 退出编辑模式
     */
    public void cancel() {
        isEditMode = false;
        bottomPart.setVisibility(View.GONE);
        pictureAdapter.allSelect(false);
        pictureAdapter.showCheckBox(false);
        allSelect.setChecked(false);
    }

    /**
     * 保存图片到本地文件夹
     */
    private void save() {

        boolean canSave = false;
        if (pictureList != null && pictureList.size() > 0) {
            for (int i = 0; i < pictureList.size(); i++) {
                if (pictureList.get(i).selectState) {
                    canSave = true;
                    break;
                }
            }
        }
        if (!canSave) {
            ToastUtil.showSimpleToast(getActivity(), "请选择要导出的图片", true);
        } else {
            BackgroundWroker save = new BackgroundWroker(getActivity(),
                    false, null, "正在导出...",
                    BackgroundWroker.BUTTON_STYLE_NONE, saveListener);
            save.startWork(null);
        }
    }
    /**
     * 保存图片监听器
     *
     */
    private BackgroundWorkerListener saveListener = new BackgroundWorkerListener() {
        @Override
        public void onWorking(Object sender, WorkerEventArgs args) throws InterruptedException {
                   args.result = savePicture();
        }

        @Override
        public void onComplete(Object sender, WorkerEventArgs args) {
            if (args.result == null) {

                ToastUtil.showSimpleToast(getActivity(), "图片已经成功导出至SD卡SafePictures目录", true);
            } else {
                ToastUtil.showSimpleToast(getActivity(),
                        String.valueOf(args.result), true);
            }
        }
    };
    /**
     * 保存图片具体操作
     */
    private String savePicture(){
        String errorMsg = null;

        File pictureDir = new File(Environment.getExternalStorageDirectory(),"SafePictures");
        if (!pictureDir.exists()){
            pictureDir.mkdirs();
        }
        for (int i = 0; i <pictureList.size(); i++) {

            MediaEntity entity = pictureList.get(i);
            boolean state = entity.selectState;
            String filePath = entity.MediaStoreLocation;
            String[] strings = filePath.split("/");
            String newName = strings[strings.length-1];
            File oldFile = new File(filePath);
            File newFile = new File(pictureDir,newName);
            if (state && !newFile.exists()) {
                try {
                    Tools.copyFile(oldFile,newFile);
                    // 把文件插入到系统图库
                    try {
                        MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                                newFile.getAbsolutePath(), newName, null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    // 通知图库更新
                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newFile)));
                } catch (Exception e) {
                    e.printStackTrace();
                    errorMsg = "保存图片出现异常";
                    break;
                }
            }

        }


        return errorMsg;
    }


}
