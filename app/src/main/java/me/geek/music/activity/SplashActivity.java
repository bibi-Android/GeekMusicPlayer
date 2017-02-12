package me.geek.music.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;

import me.geek.music.R;
import me.geek.music.application.AppCache;
import me.geek.music.callback.JsonCallback;
import me.geek.music.constants.Constants;
import me.geek.music.model.JsonSplash;
import me.geek.music.service.PlayService;
import me.geek.music.utils.FileUtils;
import me.geek.music.utils.Preferences;
import me.geek.music.utils.ToastUtils;
import me.geek.music.utils.permission.PermissionReq;
import me.geek.music.utils.permission.PermissionResult;
import me.geek.music.utils.permission.Permissions;
import okhttp3.Call;

/**
 * 启动引导页
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class SplashActivity extends BaseActivity {

    private ImageView ivSplash;
    private ServiceConnection mPlayServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ivSplash = (ImageView)findViewById(R.id.iv_splash);

        checkService();
    }

    @Override
    protected void checkServiceAlive() {
        // SplashActivity不需要检查Service是否活着
    }

    /**
     * 检查service
     */
    private void checkService(){
        if(PlayService.isRunning(this)){
            startMusicActivity();
            finish();
        }else {
            startService();//首次启动,service没有运行,启动服务
            initSplash();//加载引导页
            updateSplash();//通过外链在引导页上叠加背景图

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindService();//延时1s,将该活动与音乐后台播放服务绑定
                }
            }, 1000);
        }
    }

    /**
     * 启动服务
     */
    private void startService(){
        Intent intent = new Intent();
        intent.setClass(this, PlayService.class);
        startService(intent);
    }

    /**
     * 绑定服务
     */
    private void bindService(){
        Intent intent = new Intent();
        intent.setClass(this, PlayService.class);
        mPlayServiceConnection = new PlayServiceConnection();
        bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private class PlayServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final PlayService playService = ((PlayService.PlayBinder)service).getService();
            PermissionReq.with(SplashActivity.this)
                    .permissions(Permissions.STORAGE)
                    .result(new PermissionResult() {
                        @Override
                        public void onGranted() {
                            playService.updateMusicList();//服务更新音乐列表
                            startMusicActivity();
                            finish();
                        }

                        @Override
                        public void onDenied() {
                            ToastUtils.show(getString(R.string.no_permission, Permissions.STORAGE_DESC, "扫描本地歌曲"));
                            finish();
                            playService.stop();
                        }
                    })
                    .request();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    /**
     * 启动音乐活动
     */
    private void startMusicActivity(){
        Intent intent = new Intent(this, MusicActivity.class);
        intent.putExtras(getIntent());
        startActivity(intent);
    }

    /**
     * 加载引导页上的背景图
     */
    private void initSplash(){
        File splashImg = new File(FileUtils.getSplashDir(this), "splash.jpg");
        if(splashImg.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(splashImg.getAbsolutePath());
            ivSplash.setImageBitmap(bitmap);
        }
    }

    /**
     * 通过外链更新引导页
     */

    private void updateSplash(){
        String address = "http://news-at.zhihu.com/api/4/start-image/720*1184";
        OkHttpUtils.get().url(Constants.SPLASH_URL).build()
                .execute(new JsonCallback<JsonSplash>(JsonSplash.class) {

                    @Override
                    public void onResponse(final JsonSplash response) {
                        if (response == null || TextUtils.isEmpty(response.getImg())) {
                            return;
                        }
                        String lastImgUrl = Preferences.getSplashUrl();
                        if (TextUtils.equals(lastImgUrl, response.getImg())) {
                            return;
                        }

                OkHttpUtils.get().url(response.getImg()).build()
                        .execute(new FileCallBack(FileUtils.getSplashDir(AppCache.getContext()), "splash.jpg") {
                            @Override
                            public void inProgress(float progress, long total) {
                            }

                            @Override
                            public void onResponse(File file) {
                                Preferences.saveSplashUrl(response.getImg());
                            }

                            @Override
                            public void onError(Call call, Exception e) {
                            }
                        });

                }
                    @Override
                    public void onError(Call call, Exception e) {
                    }
        });
    }
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        if (mPlayServiceConnection != null) {
            unbindService(mPlayServiceConnection);
        }
        super.onDestroy();
    }

}
