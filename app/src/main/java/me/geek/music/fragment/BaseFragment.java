package me.geek.music.fragment;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import me.geek.music.activity.MusicActivity;
import me.geek.music.service.PlayService;
import me.geek.music.utils.permission.PermissionReq;

/**
 * 基础碎片类
 * @Author Geek-Lizc(394925542@qq.com)
 */

public abstract class BaseFragment extends Fragment {
    private PlayService mPlayService;
    protected Handler mHandler = new Handler(Looper.getMainLooper());//表示放到主UI线程去处理
    private boolean isInitialized;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof MusicActivity){
            mPlayService = ((MusicActivity) activity).getPlayService();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {//onViewCreated在onCreateView执行完后立即执行
        init();
        setListener();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                isInitialized = true;//初始化完毕
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionReq.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected abstract void init();

    protected abstract void setListener();

    public boolean isInitialized() {
        return isInitialized;
    }

    protected PlayService getPlayService(){
        return mPlayService;
    }
}
