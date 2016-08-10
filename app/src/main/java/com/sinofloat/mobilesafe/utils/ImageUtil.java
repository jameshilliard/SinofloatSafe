package com.sinofloat.mobilesafe.utils;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by oyk on 2016/7/12.
 *  加载图片的工具类
 */
public class ImageUtil {

    /**
     * 加载网络图片
     * @param context
     * @param path
     * @param imageView
     */
    public static void loadNetImage(Context context, String path, ImageView imageView){
        if (path!=null&&imageView!=null) {
            Picasso.with(context).load(path).into(imageView);
        }
    }

    /**
     * 加载本地图片
     * @param context
     * @param path
     * @param imageView
     */
    public static void loadLocalImage(Context context, String path, ImageView imageView){
        if (path!=null&&imageView!=null){
            Picasso.with(context).load(new File(path)).into(imageView);
        }

    }
}
