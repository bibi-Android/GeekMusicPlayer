package me.geek.music.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;


import com.amap.api.location.AMapLocalWeatherLive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.geek.music.activity.BaseActivity;
import me.geek.music.constants.Actions;
import me.geek.music.enums.PlayModeEnum;
import me.geek.music.model.Music;
import me.geek.music.model.SongListInfo;
import me.geek.music.recevier.NoisyAudioStreamReciver;
import me.geek.music.utils.MusicUtils;
import me.geek.music.utils.Preferences;
import me.geek.music.utils.SystemUtils;

/**
 * 音乐播放后台服务
 * @Author Geek-Lizc(394925542@qq.com)
 */


/**
 * OnAudioFocusChangeListener这个监听器来监听声音焦点的改变的
 * MediaPlayer.OnCompletionListener 监听歌曲是否已经完成播放
 *
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "Service";
    private static final int NOTIFICATION_ID = 0x111;
    private static final long TIME_UPDATE = 100L;

    //本地歌曲列表
    private static final List<Music> mMusicList = new ArrayList<Music>();
    private static final List<BaseActivity> mActivityStack = new ArrayList<BaseActivity>();
    private MediaPlayer mPlayer = new MediaPlayer();
    private IntentFilter mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);//插拔耳机时候系统会发出这条广播
    private NoisyAudioStreamReciver mNoisyReceiver = new NoisyAudioStreamReciver();
    private Handler mHandler = new Handler();
    private AudioManager mAudioManager;//按照AudioFocus的机制，在使用Audio之前，需要申请AudioFocus，在获得AudioFocus之后才可以使用Audio；如果有别的程序竞争你正在使用的Audio，你的程序需要在收到通知之后做停止播放或者降低声音的处理。
    private NotificationManager mNotificationManager;
    private OnPlayerEventListener mListener;//播放进度监听器


    //正在播放的歌曲[本地/网络]
    private Music mPlayingMusic;

    //正在播放的本地歌曲序号
    private int mPlayingPosition;
    private boolean isPause = false;
    private long quitTimerRemain;

    //缓存歌单和天气信息
    public List<SongListInfo> mSongLists = new ArrayList<SongListInfo>();
    public AMapLocalWeatherLive mAMapLocalWeatherLive;//高德地图天气


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: "+getClass().getSimpleName());
        mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mPlayer.setOnCompletionListener(this);//设置歌曲播放完毕监听器
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    /**
     * 返回一个音乐播放服务binder
     */
    public class PlayBinder extends Binder{
        public PlayService getService(){
            return PlayService.this;
        }
    }

    /**
     * 每次服务被执行的时候所做的任务
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null || intent.getAction() == null){//跳转过来的intent什么都没有带就啥也不做
            return START_NOT_STICKY;//没有传递任何决定的intent传递给service或者没有传递给service,是不会开始service的
        }

        //用于耳机线控等携带信息跳转到服务之后的处理
        switch (intent.getAction()){
            case Actions.ACTION_MEDIA_PLAY_PAUSE: //intent携带了暂停播放信息
                playPause();
                break;
            case Actions.ACTION_MEDIA_NEXT://intent携带了播放下一首的信息
                next();
                break;
            case Actions.ACTION_MEDIA_PREVIOUS://intent携带了准备播放的信息
                prev();
                break;

        }
        return START_NOT_STICKY;
    }

    public static boolean isRunning(Context context){
        return SystemUtils.isServiceRunning(context, PlayService.class);//判断是否启动服务
    }

    public static List<Music> getsMusicList(){
        return mMusicList;
    }


    public static void addToStack(BaseActivity activity){
        mActivityStack.add(activity);
    }

    public static void removeFromStack(BaseActivity activity){
        mActivityStack.remove(activity);
    }

    /**
     * 每次启动时扫描音乐
     */
    public void updateMusicList(){
        MusicUtils.scanMusic(this, getsMusicList());
        if(getsMusicList().isEmpty()){
            return;
        }
        updatePlayingPosition();
        mPlayingMusic = mPlayingMusic == null? getsMusicList().get(mPlayingPosition) : mPlayingMusic;//是否有正在播放的音乐?如果有则获取该音乐,如果没有则从音乐列表中获取
    }


    /**
     * 删除或下载歌曲后刷新正在播放的本地歌曲的序号
     */
    public void updatePlayingPosition() {
        int position = 0;
        long id = Preferences.getCurrentSongId();
        for(int i=0; i<getsMusicList().size(); i++){
            if(getsMusicList().get(i).getId() == id){//如果扫描得到的新的音乐列表中有这个id,那么则用新的id
                position = i;
                break;
            }
        }
        mPlayingPosition = position;
        Preferences.saveCurrentSongId(getsMusicList().get(mPlayingPosition).getId());
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        next();//播放完音乐后,播放下一首
    }

    public void setOnPlayEventListener(OnPlayerEventListener listener) {
        mListener = listener;
    }


    private Runnable mBackgroudRunnable = new Runnable() {
        @Override
        public void run() {
            if(isPlaying() && mListener != null){
                mListener.onPublish(mPlayer.getCurrentPosition());//更新进度
            }
            mHandler.postDelayed(this, TIME_UPDATE);//调用此Runnable对象，以实现每0.1秒实现一次的定时器操作
        }
    };

    /**
     * 是否正在播放音乐
     */
    public boolean isPlaying(){
        return mPlayer != null && mPlayer.isPlaying();
    }

    /**
     * 是否暂停播放音乐
     */
    public boolean isPause(){
        return mPlayer != null && isPause;
    }

    /**
     * 播放音乐,传入position
     */
    public int play(int position){
        if(getsMusicList().isEmpty()){
            return -1;
        }

        /**
         * 传进的position有两种可能
         * 1.没有正在播放的歌曲,那么就播放最后一首
         * 2.播放了损坏的歌曲
         */
        if(position < 0){
            position = getsMusicList().size() -1;
        }else if(position >= getsMusicList().size()){
            position = 0;
        }

        mPlayingPosition = position;
        mPlayingMusic = getsMusicList().get(mPlayingPosition);
        try{
            mPlayer.reset();//调用reset方法之后,MediaPlayer处于空闲状态
            mPlayer.setDataSource(mPlayingMusic.getUri());//获取播放资源
            mPlayer.prepare();//让MediaPlayer处于播放状态
            start();
            if(mListener != null){
                mListener.onChange(mPlayingMusic);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        Preferences.saveCurrentSongId(mPlayingMusic.getId());//保存当前播放的音乐id
        return mPlayingPosition;
    }

    /**
     * 传入Music类,播放
     */
    public void play(Music music) {
        mPlayingMusic = music;
        try {
            mPlayer.reset();
            mPlayer.setDataSource(mPlayingMusic.getUri());
            mPlayer.prepare();
            start();
            if (mListener != null) {
                mListener.onChange(mPlayingMusic);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start(){
        mPlayer.start();
        isPause = false;
        mHandler.post(mBackgroudRunnable);//更新播放进度
        updateNotification(mPlayingMusic);//播放音乐就让状态栏通知变成前台通知,不可取消
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);//获取AudioFocus
        registerReceiver(mNoisyReceiver, mNoisyFilter);//当有来电或拔出耳机的时候暂停音乐
    }


    /**
     * 暂停播放
     */
    public void playPause(){
        if(isPlaying()){
            pause();
        }else if(isPause()){
            resume();
        }else{
            play(getPlayingPosition());
        }
    }

    public int pause(){
        if(!isPlaying()){
            return -1;
        }
        mPlayer.pause();
        isPause = true;
        mHandler.removeCallbacks(mBackgroudRunnable);
        cancelNotification(mPlayingMusic);
        mAudioManager.abandonAudioFocus(this);//放弃AudioFocus
        unregisterReceiver(mNoisyReceiver);
        if(mListener != null){
            mListener.onPlayerPause();
        }
        return mPlayingPosition;
    }

    /**
     * 继续播放音乐
     */
    public int resume(){
        if(isPlaying()){
            return -1;
        }
        start();
        if(mListener != null){
            mListener.onPlayerResume();
        }
        return mPlayingPosition;
    }


    /**
     * 播放下一首
     */

    public int next() {
        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case LOOP:
                return play(mPlayingPosition + 1);//列表循环就播放下一首
            case SHUFFLE:
                mPlayingPosition = new Random().nextInt(getsMusicList().size());//生成随机数
                return play(mPlayingPosition);
            case ONE:
                return play(mPlayingPosition);
            default:
                return play(mPlayingPosition + 1);
        }
    }

    /**
     * 播放上一首
     */
    public int prev(){
       PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case LOOP:
                return play(mPlayingPosition - 1);//列表循环就播放下一首
            case SHUFFLE:
                mPlayingPosition = new Random().nextInt(getsMusicList().size());//生成随机数
                return play(mPlayingPosition);
            case ONE:
                return play(mPlayingPosition);
            default:
                return play(mPlayingPosition - 1);
        }
    }

    /**
     * 跳转到指定的时间位置
     */
    public void seekTo(int msec){
        if(isPlaying() || isPause()){
            mPlayer.seekTo(msec);
            if(mListener != null){
                mListener.onPublish(msec);
            }
        }
    }

    /**
     * 获取正在播放的本地歌曲的序号
     */
    public int getPlayingPosition(){
        return mPlayingPosition;
    }

    /**
     * 获取正在播放的歌曲{本地/网络}
     */
    public Music getPlayingMusic(){
        return mPlayingMusic;
    }

    /**
     * 更新通知栏
     * 播放的时候转为前台服务(不可清除),暂停时候转为后台服务
     */
    private void updateNotification(Music music){
        mNotificationManager.cancel(NOTIFICATION_ID);
        startForeground(NOTIFICATION_ID, SystemUtils.createNotification(this, music));
    }

    private void cancelNotification(Music music){
        stopForeground(true);
        mNotificationManager.notify(NOTIFICATION_ID, SystemUtils.createNotification(this, music));
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange){//只要失去了媒体焦点,都要停止播放
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if(isPlaying()){
                    pause();
                }
                break;
        }
    }

    /**
     * 开启定时器,通过延时回调Handler来循环计时
     * @param milli
     */

    public void startQuitTimer(long milli){
        stopQuitTimer();//删除之前设定的runnable
        if(milli > 0){
            //设置多少分钟后停止
            quitTimerRemain = milli + DateUtils.SECOND_IN_MILLIS;
            mHandler.post(mQuitRunnable);
        }else {
            quitTimerRemain = 0;
            if(mListener != null){
                mListener.onTimer(quitTimerRemain);
            }
        }
    }

    public void stopQuitTimer(){
        mHandler.removeCallbacks(mQuitRunnable);//removeCallbacks方法是删除指定的Runnable对象，使线程对象停止运行
    }

    private Runnable mQuitRunnable = new Runnable() {
        @Override
        public void run() {
            quitTimerRemain -= DateUtils.SECOND_IN_MILLIS;//时间工具类,常量,表示1s
            if(quitTimerRemain > 0){
                if(mListener != null){
                    mListener.onTimer(quitTimerRemain);//实时更新剩余时间显示
                }
                mHandler.postDelayed(this, DateUtils.SECOND_IN_MILLIS);
            }else {//当时间倒数完了,则停止
                SystemUtils.clearStack(mActivityStack);
                stop();
            }
        }
    };

    public void stop(){
        pause();
        stopQuitTimer();
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
        mNotificationManager.cancel(NOTIFICATION_ID);
        stopSelf();//服务停止

    }

    @Override
    public boolean onUnbind(Intent intent) {
        mListener = null;

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy:" + getClass().getSimpleName());
    }


}
