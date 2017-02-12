package me.geek.music.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.geek.music.R;
import me.geek.music.adapter.PlayPagerAdapter;
import me.geek.music.constants.Actions;
import me.geek.music.enums.PlayModeEnum;
import me.geek.music.excutor.SearchLrc;
import me.geek.music.model.Music;
import me.geek.music.utils.CoverLoader;
import me.geek.music.utils.FileUtils;
import me.geek.music.utils.ImageUtils;
import me.geek.music.utils.Preferences;
import me.geek.music.utils.ScreenUtils;
import me.geek.music.utils.SystemUtils;
import me.geek.music.utils.ToastUtils;
import me.geek.music.widget.AlbumCoverView;
import me.geek.music.widget.IndicatorLayout;
import me.wcy.lrcview.LrcView;


/**
 * 正在播放的界面
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class PlayFragment extends BaseFragment implements View.OnClickListener, ViewPager.OnPageChangeListener, SeekBar.OnSeekBarChangeListener {

    private LinearLayout llContent;//播放页顶部导航栏布局
    private ImageView ivPlayingBg;
    private ImageView ivBack;
    private TextView tvTitle;
    private TextView tvArtist;
    private ViewPager vpPlay;
    private IndicatorLayout ilIndicator;
    private SeekBar sbProgress;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private ImageView ivMode;
    private ImageView ivPlay;
    private ImageView ivNext;
    private ImageView ivPrev;

    private AlbumCoverView mAlbumCoverView;
    //使用第三方开源库,歌词滚动View
    private LrcView mLrcViewSingle;
    private LrcView mLrcViewFull;
    private SeekBar sbVolume;//音量拉动进度条
    private AudioManager mAudioManager;
    private List<View> mViewpagerContent;
    private int mLastProgress;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_play, container, false);

        llContent = (LinearLayout)view.findViewById(R.id.ll_content);
        ivPlayingBg =  (ImageView)view.findViewById(R.id.iv_play_page_bg);
        ivBack = (ImageView)view.findViewById(R.id.iv_back);
        tvTitle = (TextView)view.findViewById(R.id.tv_title_playFragment);
        tvArtist = (TextView)view.findViewById(R.id.tv_artist_playFragment);
        vpPlay = (ViewPager)view.findViewById(R.id.vp_play_page);
        ilIndicator = (IndicatorLayout)view.findViewById(R.id.il_indicator);
        sbProgress = (SeekBar)view.findViewById(R.id.sb_progress);
        tvCurrentTime = (TextView)view.findViewById(R.id.tv_current_time);
        tvTotalTime = (TextView)view.findViewById(R.id.tv_total_time);
        ivMode = (ImageView)view.findViewById(R.id.iv_mode);
        ivPlay = (ImageView)view.findViewById(R.id.iv_play);
        ivNext = (ImageView)view.findViewById(R.id.iv_next);
        ivPrev = (ImageView)view.findViewById(R.id.iv_prev);

        return view;
    }

    @Override
    protected void init() {
        initSystemBar();
        initViewPager();
        ilIndicator.create(mViewpagerContent.size());
        initPlayMode();
        onChange(getPlayService().getPlayingMusic());
    }

    /**
     * 当换歌之后,获取当前播放的歌曲,并且修改顶部歌手信息
     */
    public void onChange(Music music) {
        onPlay(music);
    }

    private void onPlay(Music music){
        if(music == null){
            return;
        }
        tvTitle.setText(music.getTitle());
        tvArtist.setText(music.getArtist());
        sbProgress.setMax((int)music.getDuration());//设置进度条最大值为歌曲时长
        sbProgress.setProgress(0);//设置进度条为0
        mLastProgress = 0;
        tvCurrentTime.setText(R.string.play_time_start);
        tvTotalTime.setText(formatTime(music.getDuration()));
        setCoverAndBg(music);
        setLrc(music);
        if(getPlayService().isPlaying()){
            ivPlay.setSelected(true);
            mAlbumCoverView.start();
        }else {
            ivPlay.setSelected(false);
            mAlbumCoverView.pause();
        }

    }

    /**
     * 设置换歌以后专辑图
     */
    private void setCoverAndBg(Music music) {
        //本地歌曲
        if(music.getType() == Music.Type.LOCAL){
            mAlbumCoverView.setCoverBitmap(CoverLoader.getInstance().loadRound(music.getCoverUri()));
            ivPlayingBg.setImageBitmap(CoverLoader.getInstance().loadBlur(music.getCoverUri()));

        }else {//网络歌曲
            if(music.getCover() == null){
                mAlbumCoverView.setCoverBitmap(CoverLoader.getInstance().loadRound(null));
                ivPlayingBg.setImageResource(R.drawable.play_page_default_bg);
            }else{
                Bitmap cover = ImageUtils.resizeImage(music.getCover(), ScreenUtils.getScreenWidth()/2, ScreenUtils.getScreenWidth()/2);
                cover = ImageUtils.createCircleImage(cover);
                mAlbumCoverView.setCoverBitmap(cover);
                Bitmap bg = ImageUtils.blur(music.getCover(), ImageUtils.BLUR_RADIUS);
                ivPlayingBg.setImageBitmap(bg);
            }
        }
    }

    /**
     * 设置换歌后的歌词
     */
    private void setLrc(final Music music){
        if(music.getType() == Music.Type.LOCAL){
            String lrcPath = FileUtils.getLrcFilePath(music);
            if(new File(lrcPath).exists()){
                loadLrc(lrcPath);
            }else{
                new SearchLrc(music.getArtist(), music.getTitle()) {
                    @Override
                    public void onPrepare() {
                        // 设置tag防止歌词下载完成后已切换歌曲
                        mLrcViewSingle.setTag(music);
                        loadLrc("");
                        setLrcLabel("正在搜索歌词");
                    }

                    @Override
                    public void onFinish(@Nullable String lrcPath) {
                        if(mLrcViewSingle.getTag() != music){
                            return;
                        }

                        //清除tag
                        mLrcViewSingle.setTag(null);
                        lrcPath = lrcPath == null? "" : lrcPath;
                        loadLrc(lrcPath);
                        setLrcLabel("暂无歌词");
                    }
                }.execute();
            }
        }else{//网络歌曲
            String lrcPath = FileUtils.getLrcDir() + FileUtils.getLrcFileName(music.getArtist(), music.getTitle());
            loadLrc(lrcPath);
        }
    }

    /**
     * 加载歌词和设置没有歌词的显示,都是LrcView的方法
     */

    private void loadLrc(String lrcPath) {
        File file = new File(lrcPath);
        mLrcViewSingle.loadLrc(file);
        mLrcViewFull.loadLrc(file);
    }

    private void setLrcLabel(String label) {
        mLrcViewSingle.setLabel(label);
        mLrcViewFull.setLabel(label);
    }


    /**
     * 接收系统音量变化广播,在接收器里同步更新音量条
     */
    @Override
    public void onResume() {
        super.onResume();
        //系统音量变化.Actions.VOLUME_CHANGED_ACTION
        IntentFilter filter = new IntentFilter(Actions.VOLUME_CHANGED_ACTION);
        getContext().registerReceiver(mVolumeReceiver, filter);
    }

    private BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //当前音量
            sbVolume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    };

    /**
     * 设置需要的点击或者拖动监听器
     */
    @Override
    protected void setListener() {
        ivBack.setOnClickListener(this);
        ivMode.setOnClickListener(this);
        ivPlay.setOnClickListener(this);
        ivPrev.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        sbProgress.setOnSeekBarChangeListener(this);
        sbVolume.setOnSeekBarChangeListener(this);
        vpPlay.addOnPageChangeListener(this);
    }

    /**
     * 设置沉浸式状态栏
     */
    private void initSystemBar(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            int top = ScreenUtils.getSystemBarHeight();
            llContent.setPadding(0, top, 0, 0);
        }
    }

    /**
     * 初始化viewPager
     */
    private void initViewPager(){
        View coverView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_play_page_cover, null);
        View lrcView =LayoutInflater.from(getContext()).inflate(R.layout.fragment_play_page_lrc, null);
        //自定义的专辑旋转View
        mAlbumCoverView = (AlbumCoverView) coverView.findViewById(R.id.album_cover_view);
        mLrcViewSingle = (LrcView) coverView.findViewById(R.id.lrc_view_single);
        mLrcViewFull = (LrcView) lrcView.findViewById(R.id.lrc_view_full);
        sbVolume = (SeekBar) lrcView.findViewById(R.id.sb_volume);
        mAlbumCoverView.initNeedle(getPlayService().isPlaying());
        initVolume();

        mViewpagerContent = new ArrayList<View>(2);
        mViewpagerContent.add(coverView);//带有专辑+歌词的一面ViewPager
        mViewpagerContent.add(lrcView);//全屏歌词的一面ViewPager
        //往PlayPagerAdapter传入一个mViewpagerContent的List
        vpPlay.setAdapter(new PlayPagerAdapter(mViewpagerContent));
    }

    /**
     * 初始化音量
     */
    private void initVolume(){
        mAudioManager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        //设置音量条的最大值
        sbVolume.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        sbVolume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    /**
     * 初始化播放模式
     */
    private void initPlayMode(){
        int mode = Preferences.getPlayMode();
        ivMode.setImageLevel(mode);//设置level-list中的maxLevel
    }

    /**
     * 更新播放进度
     */
    public void onPublish(int progress){
        sbProgress.setProgress(progress);
        if(mLrcViewSingle.hasLrc()){
            mLrcViewSingle.updateTime(progress);
            mLrcViewFull.updateTime(progress);
        }

        //更新当前播放时间
        if(progress - mLastProgress >= 1000){//如果是
            tvCurrentTime.setText(formatTime(progress));
            mLastProgress = progress;
        }
    }

    /**
     * 转换时间格式
     */
    private String formatTime(long time) {
        return SystemUtils.formatTime("mm:ss", time);
    }

    /**
     * 当处于播放器停止状态的时候
     */
    public void onPlayerPause(){
        ivPlay.setSelected(false);//让暂停图标消失
        mAlbumCoverView.pause();
    }

    /**
     * 当歌曲重新播放的时候
     */
    public void onPlayerResume(){
        ivPlay.setSelected(true);//让暂停图标显示
        mAlbumCoverView.start();
    }

    /**
     * 编写按钮监听器的逻辑
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_mode:
                switchPlayMode();
                break;
            case R.id.iv_play:
                play();
                break;
            case R.id.iv_next:
                next();
                break;
            case R.id.iv_prev:
                prev();
                break;
        }
    }



    /**
     * 点击返回
     */
    private void onBackPressed(){
        getActivity().onBackPressed();//返回
        //按键则会变成灰色的，按上去也没反应。当设为true后，才会正常使用
        ivBack.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ivBack.setEnabled(true);
            }
        }, 300);
    }

    /**
     * 判断播放模式
     */
    private void switchPlayMode() {
        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode){
            case LOOP://如果是当前是列表播放,那么点击就会变成随机播放
                mode = PlayModeEnum.SHUFFLE;
                ToastUtils.show(R.string.mode_shuffle);
                break;
            case SHUFFLE:
                mode = PlayModeEnum.ONE;
                ToastUtils.show(R.string.mode_one);
                break;
            case ONE:
                mode = PlayModeEnum.LOOP;
                ToastUtils.show(R.string.mode_loop);
                break;
        }
        Preferences.savePlayMode(mode.value());
        initPlayMode();//更新播放模式图标
    }

    /**
     * 点击播放按钮
     */
    private void play(){
        getPlayService().playPause();
    }

    /**
     * 点击下一首按钮
     */
    private void next(){
        getPlayService().next();
    }

    /**
     * 点击播放上一首
     */
    private void prev(){
        getPlayService().prev();
    }


    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(mVolumeReceiver);
        super.onDestroy();
    }

    /**
     *
     * 当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到调用。其中三个参数的含义分别为：
     * position :当前页面，及你点击滑动的页面
     * positionOffset:当前页面偏移的百分比
     * positionOffsetPixels:当前页面偏移的像素位置
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        ilIndicator.setCurrent(position);
    }

    /**
     * 此方法是页面跳转完后得到调用，position是你当前选中的页面的Position
     */
    @Override
    public void onPageSelected(int position) {

    }

    /**
     * 方法是在状态改变的时候调用，其中state这个参数
     * 有三种状态（0，1，2）。sate ==1的默示正在滑动，state==2的时辰默示滑动完毕了，state==0的时辰默示什么都没做。
     */
    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 该方法拖动进度条进度改变的时候调用
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    /**
     *该方法拖动进度条开始拖动的时候调用。
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 该方法拖动进度条停止拖动的时候调用
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(seekBar == sbProgress){//如果是音乐进度条的seekbar
            if(getPlayService().isPlaying() || getPlayService().isPause()){
                int progress = seekBar.getProgress();
                getPlayService().seekTo(progress);
                mLrcViewSingle.onDrag(progress);
                mLrcViewFull.onDrag(progress);
                tvCurrentTime.setText(formatTime(progress));
                mLastProgress = progress;
            }else{
                seekBar.setProgress(0);
            }
        }else if(seekBar == sbVolume){//如果是音量进度的seekbar
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
    }
}
