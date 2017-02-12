package me.geek.music.application;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.widget.Toast;

import me.geek.music.utils.Preferences;
import me.geek.music.utils.ScreenUtils;
import me.geek.music.utils.ToastUtils;

/**
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class AppCache {
    private Context mContext;
    private LongSparseArray<String> mDownloadList = new LongSparseArray<String>();//安卓中代替HashMap


    private static class SingetonHolder{
        private static AppCache sAppCache = new AppCache();
    }

    private static AppCache getInstances(){
        return SingetonHolder.sAppCache;
    }

    public static Context getContext(){
        return getInstances().mContext;
    }

    public static void init(Context context){
        if(getContext() != null){
            return;
        }
        getInstances().onInit(context);
    }

    private void onInit(Context context){
        mContext = context.getApplicationContext();//生命周期是整个应用
        ToastUtils.init(mContext);
        Preferences.init(mContext);
        ScreenUtils.init(mContext);
    }

    public static LongSparseArray<String> getDownloadList(){
        return getInstances().mDownloadList;
    }

    /**
     * 通过UIMODE设置夜间模式
     */
    public static void updateNightMode(boolean on){
        Resources resources = getContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();//获取手机屏幕参数
        Configuration config = resources.getConfiguration();//获得设置对象
        config.uiMode &= ~Configuration.UI_MODE_NIGHT_MASK;
        config.uiMode |= on ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO;
        resources.updateConfiguration(config, dm);
    }

}
