package me.geek.music.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.geek.music.constants.Actions;
import me.geek.music.service.PlayService;

/**
 * 来电或者插拔耳机时候暂停播放
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class NoisyAudioStreamReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, PlayService.class);
        serviceIntent.setAction(Actions.ACTION_MEDIA_PLAY_PAUSE);//携带一个暂停播放的信息给PlayService
        context.startService(serviceIntent);
    }
}
