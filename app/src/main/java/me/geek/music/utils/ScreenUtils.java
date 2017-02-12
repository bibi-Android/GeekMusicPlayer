package me.geek.music.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * 屏幕信息获取工具类
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class ScreenUtils {
    private static Context mContext;

    public static void init(Context context){
        mContext = context.getApplicationContext();
    }

    /**
     * 获取屏幕的宽度
     */
    public static int getScreenWidth(){
        WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取状态栏高度
     */
    public static int getSystemBarHeight(){
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0){//如果能获取到状态栏资源id
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    /**
     * dp转换成px,用于通过setPadding自定义view尺寸
     */
    public static int dp2px(float dpValue){
        final float scale = mContext.getResources().getDisplayMetrics().density;//屏幕密度
        return (int)(dpValue * scale + 0.5f);

    }

    public static int px2dp(float pxValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static int sp2px(float spValue) {
        final float fontScale = mContext.getResources().getDisplayMetrics().scaledDensity;//字体缩放比例
        return (int) (spValue * fontScale + 0.5f);
    }
}
