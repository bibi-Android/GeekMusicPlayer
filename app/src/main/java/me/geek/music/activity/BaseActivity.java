package me.geek.music.activity;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import me.geek.music.R;
import me.geek.music.service.PlayService;
import me.geek.music.utils.permission.PermissionReq;

/**
 * 基础活动类
 * @Author Geek-Lizc(394925542@qq.com)
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "Activity";
    protected Handler mHandler = new Handler(Looper.getMainLooper());//表示在主线程中运行

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate:" + getClass().getSimpleName());

        setSystemBarTransparent();
        PlayService.addToStack(this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);//使用扬声器外放，即使已经插入耳机
        checkServiceAlive();
    }

    /**
     * 应用运行期间进入“设置”修改应用权限会导致应用被kill。
     * 如果这时从最近任务列表启动应用，检查下Service是否活着，如果已经被kill，则关闭应用。
     */
    protected void checkServiceAlive() {
        if (!PlayService.isRunning(this)) {
            Process.killProcess(Process.myPid());//传入布局ID就用布局ID
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initView();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);//传入整个view就用view
        initView();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        initView();
    }

    private void initView() {

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            throw new IllegalStateException("Layout is required to include a Toolbar with id 'toolbar'");
        }
        setSupportActionBar(mToolbar);//将ToolBar设置到活动中
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setListener();
    }

    protected void setListener() {
    }

    /**
     * 沉浸状态栏
     */
    private void setSystemBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // LOLLIPOP解决方案
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // KITKAT解决方案
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {//导航栏的返回箭头←
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        PlayService.removeFromStack(this);
        super.onDestroy();
        Log.i(TAG, "onDestroy:" + getClass().getSimpleName());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionReq.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void showSoftKeyboard(final EditText editText) {//显示键盘
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);//显示键盘
            }
        }, 200L);
    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {//隐藏键盘
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}

