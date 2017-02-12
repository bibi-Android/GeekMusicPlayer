package me.geek.music.recevier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import me.geek.music.constants.Actions;
import me.geek.music.service.PlayService;

/**
 * 耳机线控广播接收器
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class RemoteControlReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if(event == null || event.getAction()!=KeyEvent.ACTION_UP){
            return;
        }

        Intent serviceIntent;
        switch (event.getKeyCode()){
            //获得KeyEvent的信息,并且提交到PlayService中进行处理
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                serviceIntent = new Intent(context, PlayService.class);
                serviceIntent.setAction(Actions.ACTION_MEDIA_PLAY_PAUSE);
                context.startService(serviceIntent);
                break;

            case KeyEvent.KEYCODE_MEDIA_NEXT:
                serviceIntent = new Intent(context, PlayService.class);
                serviceIntent.setAction(Actions.ACTION_MEDIA_NEXT);
                context.startService(serviceIntent);
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                serviceIntent = new Intent(context, PlayService.class);
                serviceIntent.setAction(Actions.ACTION_MEDIA_PREVIOUS);
                context.startService(serviceIntent);
                break;
        }
    }
}
