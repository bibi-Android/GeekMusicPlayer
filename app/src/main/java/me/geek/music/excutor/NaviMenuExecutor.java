package me.geek.music.excutor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import me.geek.music.R;
import me.geek.music.activity.AboutActivity;
import me.geek.music.activity.MusicActivity;
import me.geek.music.activity.SettingActivity;
import me.geek.music.application.AppCache;
import me.geek.music.model.Music;
import me.geek.music.service.PlayService;
import me.geek.music.utils.Preferences;
import me.geek.music.utils.ToastUtils;

/**
 * 导航菜单执行器,点击菜单项逻辑
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class NaviMenuExecutor {

    public static boolean onNavigationItemSelected(MenuItem item, Context context){
        switch (item.getItemId()){
            case R.id.action_setting:
                startActivity(context, SettingActivity.class);
                return true;
            case R.id.action_night:
                nightMode(context);
                break;
            case R.id.action_timer:
                timeDialog(context);
                break;
            case R.id.action_exit:
                exit(context);
                return true;
            case R.id.action_about:
                startActivity(context, AboutActivity.class);
                return true;
        }
        return false;
    }

    /**
     * 跳转活动
     */
    private static void startActivity(Context context, Class<?>cls){
        Intent intent = new Intent(context,cls);
        context.startActivity(intent);
    }


    /**
     * 夜间模式
     */
    private static void nightMode(Context context){
        if(!(context instanceof MusicActivity)){
            return;
        }
        final MusicActivity activity = (MusicActivity)context;
        final boolean on = !Preferences.isNight();//不处于夜间模式
        final ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setCancelable(false);
        dialog.show();
        AppCache.updateNightMode(on);
        Handler handler = new Handler(activity.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.cancel();
                activity.recreate();
                Preferences.saveNightMode(on);
            }
        }, 500);

    }

    /**
     * 设置定时停止播放
     */

    private static void timeDialog(final Context context){
        if(!(context instanceof MusicActivity)){
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle(R.string.menu_timer)
                .setItems(context.getResources().getStringArray(R.array.timer_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int[] times = context.getResources().getIntArray(R.array.timer_int);
                        startTimer(context, times[which]);
                    }
                })
                .show();
    }

    private static void startTimer(Context context, int minute) {
        if (!(context instanceof MusicActivity)) {
            return;
        }
        MusicActivity activity = (MusicActivity)context;
        PlayService service = activity.getPlayService();
        service.startQuitTimer(minute * 60 *1000);
        if(minute > 0){
            ToastUtils.show(context.getString(R.string.timer_set, String.valueOf(minute)));
        } else {
            ToastUtils.show(R.string.timer_cancel);
        }
    }

    /**
     * 退出应用
     */
    private static void exit(Context context){
        if (!(context instanceof MusicActivity)) {
            return;
        }
        MusicActivity activity = (MusicActivity) context;
        PlayService service = activity.getPlayService();
        activity.finish();
        service.stop();
    }


}
