package me.geek.music.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast工具类
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class ToastUtils {
    private static Context mContext;
    private static Toast mToast;

    public static void init(Context context){
        mContext = context.getApplicationContext();
    }

    public static void show(int resId){
        show(mContext.getString(resId));
    }

    public static void show(String text){
        if(mToast == null){
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        }else {
            mToast.setText(text);
        }
        mToast.show();
    }
}
