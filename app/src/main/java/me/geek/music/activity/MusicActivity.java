package me.geek.music.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.security.Permission;

import me.geek.music.R;
import me.geek.music.adapter.FragmentAdapter;
import me.geek.music.constants.Extras;
import me.geek.music.excutor.NaviMenuExecutor;
import me.geek.music.excutor.WeatherExecutor;
import me.geek.music.fragment.LocalMusicFragment;
import me.geek.music.fragment.PlayFragment;
import me.geek.music.fragment.SongListFragment;
import me.geek.music.model.Music;
import me.geek.music.recevier.RemoteControlReceiver;
import me.geek.music.service.OnPlayerEventListener;
import me.geek.music.service.PlayService;
import me.geek.music.utils.CoverLoader;
import me.geek.music.utils.SystemUtils;
import me.geek.music.utils.ToastUtils;
import me.geek.music.utils.permission.PermissionReq;
import me.geek.music.utils.permission.PermissionResult;
import me.geek.music.utils.permission.Permissions;

/**
 * 引导页之后进入的音乐列表页面
 *
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class MusicActivity extends BaseActivity implements View.OnClickListener, OnPlayerEventListener, NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {
    private DrawerLayout drawerLayout;//支持左划右划的布局
    private NavigationView navigationView;//侧滑菜单
    private ImageView ivMenu;
    private ImageView ivSearch;
    private TextView tvLocalMusic;
    private TextView tvOnlineMusic;
    private ViewPager mViewPager;
    private FrameLayout flPlayBar;//下方播放状态栏布局
    private ImageView ivPlayBarCover;//专辑封面
    private TextView tvPlayBarTitle;
    private TextView tvPlayBarArtist;
    private ImageView ivPlayBarPlay;
    private ImageView ivPlayBarNext;
    private ProgressBar mProgressBar;

    private View vNavigationHeader;
    private LocalMusicFragment mLocalMusicFragment;
    private SongListFragment mSongListFragment;
    private PlayFragment mPlayFragment;
    private PlayService mPlayService;
    private PlayServiceConnection mPlayServiceConnection;
    private AudioManager mAudioManager;
    private ComponentName mRemoteReceiver;//ComponentName（组件名称）是用来打开其他应用程序中的Activity或服务的。new ComponentName(String packageName,String activityName )
    private boolean isPlayFragmentShow = false;
    private MenuItem timerItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navigationView = (NavigationView)findViewById(R.id.navigation_view);//侧滑菜单
        ivMenu = (ImageView)findViewById(R.id.iv_menu);
        ivSearch = (ImageView)findViewById(R.id.iv_search);
        tvLocalMusic = (TextView)findViewById(R.id.tv_local_music);
        tvOnlineMusic = (TextView)findViewById(R.id.tv_online_music);
        mViewPager = (ViewPager)findViewById(R.id.viewpager);
        flPlayBar = (FrameLayout)findViewById(R.id.fl_play_bar);//下方播放状态栏布局
        ivPlayBarCover = (ImageView)findViewById(R.id.iv_play_bar_cover);//专辑封面
        tvPlayBarTitle = (TextView)findViewById(R.id.tv_play_bar_title);
        tvPlayBarArtist = (TextView)findViewById(R.id.tv_play_bar_artist);
        ivPlayBarPlay = (ImageView)findViewById(R.id.iv_play_bar_play);
        ivPlayBarNext = (ImageView)findViewById(R.id.iv_play_bar_next);
        mProgressBar = (ProgressBar)findViewById(R.id.pd_play_bar);

        bindService();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent(intent);
    }

    /**
     * 绑定服务
     */
    private void bindService() {
        Intent intent = new Intent(this, PlayService.class);
        mPlayServiceConnection = new PlayServiceConnection();
        bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);
    }




    /**
     * 与服务连接以后要做的事情
     */
    private class PlayServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayService = ((PlayService.PlayBinder) service).getService();

            mPlayService.setOnPlayEventListener(MusicActivity.this);
            init();
            parseIntent(getIntent());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    /**
     * 初始化
     * 1.设置所需要的View
     * 2.更新天气
     * 3.注册耳机线控级广播接收器
     * 4.让当前视图信息显示为正在播放的歌曲
     */
    private void init() {
        setupView();
        updateWeather();
        registerReceiver();
        onChange(mPlayService.getPlayingMusic());
    }



    /**
     * 设置view
     */
    private void setupView() {
        //设置NavigationView的头部布局
        vNavigationHeader = LayoutInflater.from(this).inflate(R.layout.navigation_header, navigationView, false);
        navigationView.addHeaderView(vNavigationHeader);

        //设置viewPager
        mLocalMusicFragment = new LocalMusicFragment();
        mSongListFragment = new SongListFragment();
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(mLocalMusicFragment);
        adapter.addFragment(mSongListFragment);
        mViewPager.setAdapter(adapter);
        tvLocalMusic.setSelected(true);//设置selected状态为true,显示高亮白色
    }

    /**
     * 更新天气信息
     */
    private void updateWeather(){
        PermissionReq.with(this)
                .permissions(Permissions.LOCATION)
                .result(new PermissionResult() {
                    @Override
                    public void onGranted() {
                        new WeatherExecutor(mPlayService, vNavigationHeader).execute();

                    }

                    @Override
                    public void onDenied() {
                        ToastUtils.show(getString(R.string.no_permission, Permissions.LOCATION_DESC, "更新天气"));
                    }
                })
                .request();


    }

    /**
     * 注册耳机插入后,耳机线控广播接收器
     */
    private void registerReceiver(){
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mRemoteReceiver = new ComponentName(getPackageName(), RemoteControlReceiver.class.getName());
        //在AudioManager对象注册一个MediaoButtonRecevie，使它成为MEDIA_BUTTON的唯一接收器(
        mAudioManager.registerMediaButtonEventReceiver(mRemoteReceiver);
    }

    /**
     * 换歌以后更新视图信息
     * @param music
     */
    @Override
    public void onChange(Music music) {
        onPlay(music);//更新视图
        if(mPlayFragment != null && mPlayFragment.isInitialized()){
            mPlayFragment.onChange(music);//把播放界面的信息也同时更新
        }
    }

    private void onPlay(Music music) {
        if(music == null){
            return;
        }
        Bitmap cover;
        if(music.getCover() == null){
            cover = CoverLoader.getInstance().loadThumbnail(music.getCoverUri());
        }else{
            cover = music.getCover();
        }
        ivPlayBarCover.setImageBitmap(cover);
        tvPlayBarTitle.setText(music.getTitle());
        tvPlayBarArtist.setText(music.getArtist());
        if(getPlayService().isPlaying()){
            ivPlayBarPlay.setSelected(true);//显示暂停按钮
        }else{
            ivPlayBarPlay.setSelected(false);
        }
        mProgressBar.setMax((int)music.getDuration());
        mProgressBar.setProgress(0);

        if (mLocalMusicFragment != null && mLocalMusicFragment.isInitialized()) {
            mLocalMusicFragment.onItemPlay();
        }
    }

    public PlayService getPlayService() {
        return mPlayService;
    }


    /**
     * 如果是从通知栏中跳转进来的intent,那么久跳转到播放界面
     */
    private void parseIntent(Intent intent) {
        if (intent.hasExtra(Extras.FROM_NOTIFICATION)) {
            showPlayFragment();//显示播放界面
        }
    }

    private void showPlayFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //设置Fragment进栈动画,两个参数分别是进入和离开两个动画的资源文件ID
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mPlayFragment == null) {
            mPlayFragment = new PlayFragment();
            ft.replace(android.R.id.content, mPlayFragment);//android.R.id.content获得当前视图
        }else{
            ft.show(mPlayFragment);
        }
        ft.commit();
        isPlayFragmentShow = true;
    }




    /**
     * 重写OnPlayEventListener的方法,自定义逻辑,都把这些逻辑传递给后台服务去执行
     * 我们都同时去调用播放音乐页面的OnPlayEventListener方法
     */


    //更新进度
    public void onPublish(int progress) {
        mProgressBar.setProgress(progress);
        if (mPlayFragment != null && mPlayFragment.isInitialized()){
            mPlayFragment.onPublish(progress);
        }
    }

    //暂停播放
    @Override
    public void onPlayerPause() {
        ivPlayBarPlay.setSelected(false);
        if (mPlayFragment != null && mPlayFragment.isInitialized()) {
            mPlayFragment.onPlayerPause();
        }
    }

    //继续播放
    @Override
    public void onPlayerResume() {
        ivPlayBarPlay.setSelected(true);
        if(mPlayFragment != null && mPlayFragment.isInitialized()) {
            mPlayFragment.onPlayerResume();
        }
    }

    //更新定时停止播放时间
    @Override
    public void onTimer(long remain) {
        if(timerItem == null){
            timerItem = navigationView.getMenu().findItem(R.id.action_timer);
        }
        String title = getString(R.string.menu_timer);
        //当有时间传入时,更新倒计时时间
        timerItem.setTitle(remain==0? title : SystemUtils.formatTime(title + "(mm:ss)", remain));
    }

    /**
     * 设置各类监听器
     */

    @Override
    protected void setListener() {
        ivMenu.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        tvLocalMusic.setOnClickListener(this);
        tvOnlineMusic.setOnClickListener(this);
        mViewPager.setOnPageChangeListener(this);
        flPlayBar.setOnClickListener(this);
        ivPlayBarPlay.setOnClickListener(this);
        ivPlayBarNext.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * 编写监听器点击逻辑
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_menu:
                drawerLayout.openDrawer(GravityCompat.START);//打开抽屉
                break;
            case R.id.iv_search:
                startActivity(new Intent(this, SearchMusicActivity.class));//跳转到搜索信息的活动
                break;
            case R.id.tv_local_music:
                mViewPager.setCurrentItem(0);//设置Viewpager现在的Item是0,因此在把碎片加入ViewPager时候的顺序很重要
                break;
            case R.id.tv_online_music:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.fl_play_bar:
                showPlayingFragment();
                break;
            case R.id.iv_play_bar_play:
                play();
                break;
            case R.id.iv_play_bar_next:
                next();
                break;
        }
    }

    private void showPlayingFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mPlayFragment == null) {
            mPlayFragment = new PlayFragment();
            ft.replace(android.R.id.content, mPlayFragment);
        } else {
            ft.show(mPlayFragment);
        }
        ft.commit();
        isPlayFragmentShow = true;
    }

    /**
     * 播放,交由后台服务去运行
     */
    private void play() {
        getPlayService().playPause();
    }

    private void next() {
        getPlayService().next();
    }

    /**
     * 定义点击返回按钮逻辑
     * 1.如果是正在播放界面,隐藏
     * 2.如果侧滑菜单打开了,关闭抽屉
     */
    @Override
    public void onBackPressed() {
        if(mPlayFragment != null && isPlayFragmentShow){
            hidePlayFragment();
            return;
        }
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers();
            return;
        }
    }

    private void hidePlayFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mPlayFragment);
        ft.commit();
        isPlayFragmentShow = false;
    }



    /**
     * 我们可以用setNavigationItemSelectedListener方法来设置当导航项被点击时的回调。OnNavigationItemSelectedListener会提供给我们被选中的MenuItem
     * 通过这个回调方法，我们可以处理点击事件，改变item的选中状态，更新页面内容，关闭导航菜单，以及任何我们需要的操作。
     */
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        //点击导航栏以后收回这个抽屉
        drawerLayout.closeDrawers();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setChecked(false);// 改变item选中状态
            }
        },500);
        return NaviMenuExecutor.onNavigationItemSelected(item, this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * pager选择以后执行的方法
     */
    @Override
    public void onPageSelected(int position) {
        if(position == 0){//选中本地音乐
            tvLocalMusic.setSelected(true);
            tvOnlineMusic.setSelected(false);
        }else{//选中在线音乐
            tvLocalMusic.setSelected(false);
            tvOnlineMusic.setSelected(true);

        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // 切换夜间模式不保存状态,相当于重新onCreate
    }


    @Override
    protected void onDestroy() {
        if (mPlayServiceConnection != null) {
            unbindService(mPlayServiceConnection);
        }
        if (mRemoteReceiver != null) {
            mAudioManager.unregisterMediaButtonEventReceiver(mRemoteReceiver);
        }
        super.onDestroy();
    }


}
