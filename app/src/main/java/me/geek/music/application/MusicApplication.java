package me.geek.music.application;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import me.geek.music.utils.Preferences;

/**
 * 自定义Application
 * 就是说application是用来保存全局变量的，并且是在package创建的时候就跟着存在了。所以当我们需要创建全局变量的时候，不需 要再像j2se那样需要创建public权限的static变量，而直接在application中去实现。只需要调用Context的getApplicationContext或者Activity的getApplication方法来获得一个application对象，再做出相应的处理。
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class MusicApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCache.init(this);
        AppCache.updateNightMode(Preferences.isNight());
        initOkHttpUtils();
        initImageLoader();
    }

    private void initOkHttpUtils() {
        OkHttpUtils.getInstance().setConnectTimeout(30, TimeUnit.SECONDS);
        OkHttpUtils.getInstance().setReadTimeout(30, TimeUnit.SECONDS);
        OkHttpUtils.getInstance().setWriteTimeout(30, TimeUnit.SECONDS);
    }

    private void initImageLoader() {
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheSize(2 * 1024 * 1024) // 2MB
                .diskCacheSize(50 * 1024 * 1024) // 50MB
                .build();
        ImageLoader.getInstance().init(configuration);
    }
}
