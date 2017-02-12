package me.geek.music.utils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import java.util.List;

import me.geek.music.R;
import me.geek.music.activity.BaseActivity;
import me.geek.music.activity.SplashActivity;
import me.geek.music.constants.Extras;
import me.geek.music.model.Music;

/**
 * 系统工具类
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class SystemUtils {

    /**
     * 判断是否有Activity在运行
     */

    public static boolean isStackResumed(Context context){
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);//返回正在运行的的程序,参数为返回的最大个数
        ActivityManager.RunningTaskInfo runningTaskInfo = runningTaskInfos.get(0);
        return runningTaskInfo.numActivities > 1;

    }

    /**
     * 判断Service是否在运行
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass){
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){//表示int能表示的最大值
            if(serviceClass.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    /**
     * 创建通知
     * 告白气球
     * 周杰伦-床边的故事
     */
    public static Notification createNotification(Context context, Music music){
        String title = music.getTitle();
        String subtitle = FileUtils.getArtistAndAlbum(music.getArtist(), music.getAlbum());//获得通知标题的格式
        Bitmap cover;//专辑图片

        if(music.getType() == Music.Type.LOCAL){
            cover = CoverLoader.getInstance().loadThumbnail(music.getCoverUri());//获取本地歌曲的专辑封面
        }else{
            cover = music.getCover();//获取在线音乐的专辑图片
        }
        //点击通知返回到主页
        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtra(Extras.FROM_NOTIFICATION, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        builder.setContentText(subtitle);
        builder.setSmallIcon(R.drawable.ic_notification);//小图标,基础图标
        builder.setLargeIcon(cover);//设置大图标
        return builder.build();
    }


    /**
     * 退出所有活动
     */
    public static void clearStack(List<BaseActivity> activityStack){
        for(int i = activityStack.size() - 1; i >= 0; i--){//应该在栈上从下往上退出
            BaseActivity activity = activityStack.get(i);
            activityStack.remove(activity);
            if(!activity.isFinishing()){
                activity.finish();
            }
        }
    }

    /**
     * 将时间转换成字符串
     *
     */
    public static String formatTime(String pattern, long milli){
        int m = (int)(milli / (60*1000));
        int s = (int)((milli/1000) % 60);
        //自定义字符串格式
        String mm = String.format("%02d", m);//如果整数不够2列就补上0
        String ss = String.format("%02d", s);
        return pattern.replace("mm",mm).replace("ss", ss);
    }
}
