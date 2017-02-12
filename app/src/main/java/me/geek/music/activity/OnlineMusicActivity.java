package me.geek.music.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.geek.music.R;
import me.geek.music.adapter.OnMoreClickListener;
import me.geek.music.adapter.OnlineMusicAdapter;
import me.geek.music.callback.JsonCallback;
import me.geek.music.constants.Constants;
import me.geek.music.constants.Extras;
import me.geek.music.enums.LoadStateEnum;
import me.geek.music.excutor.DownloadOnlineMusic;
import me.geek.music.excutor.PlayOnlineMusic;
import me.geek.music.excutor.ShareOnlineMusic;
import me.geek.music.model.JsonOnlineMusic;
import me.geek.music.model.JsonOnlineMusicList;
import me.geek.music.model.Music;
import me.geek.music.model.SongListInfo;
import me.geek.music.service.PlayService;
import me.geek.music.utils.FileUtils;
import me.geek.music.utils.ImageUtils;
import me.geek.music.utils.ScreenUtils;
import me.geek.music.utils.ToastUtils;
import me.geek.music.utils.ViewUtils;
import me.geek.music.widget.AutoLoadListView;
import okhttp3.Call;


/**
 * 点击歌榜信息后跳转的歌榜内容活动
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class OnlineMusicActivity extends BaseActivity implements AdapterView.OnItemClickListener, OnMoreClickListener, AutoLoadListView.OnLoadListener {
    private AutoLoadListView lvOnlineMusic;
    private LinearLayout llLoading;
    private LinearLayout llLoadFail;
    private View vHeader;
    private SongListInfo mListInfo;
    private JsonOnlineMusicList mJsonOnlineMusicList;
    private List<JsonOnlineMusic> mMusicList;
    private OnlineMusicAdapter mAdapter;
    private PlayService mPlayService;
    private ProgressDialog mProgressDialog;
    private int mOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_music);//包含一个自动加载ListView以及加载中,加载失败的布局

        lvOnlineMusic = (AutoLoadListView)findViewById(R.id.lv_online_music_list);
        llLoading = (LinearLayout)findViewById(R.id.ll_loading_onlineMusic);
        llLoadFail = (LinearLayout)findViewById(R.id.ll_load_fail_onlineMusic);

        mListInfo = (SongListInfo)getIntent().getSerializableExtra(Extras.MUSIC_LIST_TYPE);//由SongListFragment传导过来的信息
        setTitle(mListInfo.getTitle());//ToolBar的title
        init();
    }

    private void init() {
        vHeader = LayoutInflater.from(this).inflate(R.layout.activity_online_music_list_header, null);
        /**
         * 如果AbsListView.LayoutParams(-1, -2) 指定了该布局的宽和高（-1为宽，-2为高）；
         * -1代表LayoutParams.MATCH_PARENT，即该布局的尺寸将填满它的父控件；
         * -2代表LayoutParams.WRAP_CONTENT，即该布局的尺寸将为其自身内容的尺寸
         */
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(150));
        vHeader.setLayoutParams(params);//重新设置控件的布局
        lvOnlineMusic.addHeaderView(vHeader, null, false);//可以在ListView组件上方添加上其他组件，并且连结在一起像是一个新组件
        mMusicList = new ArrayList<JsonOnlineMusic>();
        mAdapter = new OnlineMusicAdapter(mMusicList);
        lvOnlineMusic.setAdapter(mAdapter);
        lvOnlineMusic.setOnLoadListener(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOADING);
        bindService();
    }

    /**
     * 设置各种监听器
     */
    @Override
    protected void setListener() {
        lvOnlineMusic.setOnItemClickListener(this);//点击音乐监听器
        mAdapter.setOnMoreClickListener(this);//点击更多监听器
    }

    /**
     * 绑定服务
     */
    private void bindService(){
        Intent intent = new Intent(this, PlayService.class);
        bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mPlayServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayService = ((PlayService.PlayBinder)service).getService();
            onLoad();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 根据type类型,即是歌榜类型来获取歌榜的所有歌曲
     * size = 20 //返回条目数量
     * offset = 0 //获取偏移
     * "http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.billboard.billList&type=23&size=20&offset=0";其中offset是从第几位开始
     */
    private void getMusic(final int offset) {
        OkHttpUtils.get().url(Constants.BASE_URL)
                .addParams(Constants.PARAM_METHOD, Constants.METHOD_GET_MUSIC_LIST)
                .addParams(Constants.PARAM_TYPE, mListInfo.getType())
                .addParams(Constants.PARAM_SIZE, String.valueOf(Constants.MUSIC_LIST_SIZE))
                .addParams(Constants.PARAM_OFFSET, String.valueOf(offset))
                .build()
                .execute(new JsonCallback<JsonOnlineMusicList>(JsonOnlineMusicList.class) {
                    @Override
                    public void onResponse(JsonOnlineMusicList response) {
                        lvOnlineMusic.onLoadComplete();//加载完成,去除ListView底部的footer
                        mJsonOnlineMusicList = response;
                        if (offset == 0 && response == null) {
                            ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                            return;
                        } else if (offset == 0) {
                            initHeader();
                            ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                        }
                        if (response == null || response.getSong_list() == null || response.getSong_list().size() == 0) {
                            lvOnlineMusic.setEnable(false);//让ListView不可点击
                            return;
                        }
                        mOffset += Constants.MUSIC_LIST_SIZE;//每一次会加载后面的20个
                        mMusicList.addAll(response.getSong_list());//把返回的MusicList列表加入到mMusicList
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        lvOnlineMusic.onLoadComplete();//加载完成,去除ListView底部的footer
                        if (e instanceof RuntimeException) {
                            // 歌曲全部加载完成
                            lvOnlineMusic.setEnable(false);
                            return;
                        }
                        if (offset == 0) {
                            ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                        } else {
                            ToastUtils.show(R.string.load_fail);
                        }
                    }
                });
    }

    @Override
    public void onLoad() {
        getMusic(mOffset);//启动服务后就开始获取音乐
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        play(mMusicList.get(position-1));//点击ListView后开始播放音乐
    }

    /**
     * 编写点击更多监听器的逻辑
     */
    @Override
    public void onMoreClick(int position) {
        final JsonOnlineMusic jsonOnlineMusic = mMusicList.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(mMusicList.get(position).getTitle());
        String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(jsonOnlineMusic.getArtist_name(), jsonOnlineMusic.getTitle());
        File file = new File(path);
        //如果已经下载过了,那么显示下载按钮,如果没有下载过那么就不显示下载按钮
        int itemId = file.exists()? R.array.online_music_dialog_without_download : R.array.online_music_dialog;
        dialog.setItems(itemId, new DialogInterface.OnClickListener() {//设置要在对话框中显示的项目列表
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0://分享
                        share(jsonOnlineMusic);
                        break;
                    case 1://查看歌手信息
                        artistInfo(jsonOnlineMusic);
                        break;
                    case 2://下载
                        download(jsonOnlineMusic);
                        break;
                }
            }
        });
        dialog.show();
    }

    /**
     * 加载头顶歌榜的图标,内容,更新,标题信息
     */
    private void initHeader(){
        final ImageView ivHeaderBg = (ImageView)vHeader.findViewById(R.id.iv_header_bg);
        final ImageView iConver = (ImageView)vHeader.findViewById(R.id.iv_cover);
        TextView tvTitle = (TextView)vHeader.findViewById(R.id.tv_title);
        TextView tvUpadteDte = (TextView)vHeader.findViewById(R.id.tv_update_date);
        TextView tvComment = (TextView) vHeader.findViewById(R.id.tv_comment);
        tvTitle.setText(mJsonOnlineMusicList.getBillboard().getName());
        tvUpadteDte.setText(getString(R.string.recent_update, mJsonOnlineMusicList.getBillboard().getUpdate_date()));
        tvComment.setText(mJsonOnlineMusicList.getBillboard().getComment());
        ImageSize imageSize = new ImageSize(200, 200);
        ImageLoader.getInstance().loadImage(mJsonOnlineMusicList.getBillboard().getPic_s640(),imageSize, ImageUtils.getCoverDisPlayOptions(), new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                //加载成功后
                iConver.setImageBitmap(loadedImage);
                ivHeaderBg.setImageBitmap(ImageUtils.blur(loadedImage, ImageUtils.BLUR_RADIUS));
            }
        });
    }

    /**
     * 播放音乐
     */
    private void play(final JsonOnlineMusic jsonOnlineMusic){
        new PlayOnlineMusic(this, jsonOnlineMusic) {


            @Override
            public void onPrepare() {
                mProgressDialog.show();//努力加载中...
            }

            @Override
            public void onSuccess(Music music) {
                mProgressDialog.cancel();
                mPlayService.play(music);
                ToastUtils.show(getString(R.string.now_play, jsonOnlineMusic.getTitle()));
            }

            @Override
            public void onFail(Call call, Exception e) {
                mProgressDialog.cancel();
                ToastUtils.show(R.string.unable_to_download);
            }
        }.execute();
    }



    /**
     * 分享
     */
    private void share(final JsonOnlineMusic jsonOnlineMusic){
        new ShareOnlineMusic(this, jsonOnlineMusic.getTitle(), jsonOnlineMusic.getSong_id()) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onSuccess() {
                mProgressDialog.cancel();
            }

            @Override
            public void onFail(Call call, Exception e) {
                mProgressDialog.cancel();
            }
        }.execute();
    }

    /**
     * 查看歌手信息
     */
    private void artistInfo(JsonOnlineMusic jsonOnlineMusic){
        ArtistInfoActivity.start(this, jsonOnlineMusic.getTing_uid());
    }

    /**
     * 下载歌曲
     */
    private void download(final JsonOnlineMusic jsonOnlineMusic){
        new DownloadOnlineMusic(this, jsonOnlineMusic) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onSuccess() {
                mProgressDialog.cancel();
                ToastUtils.show(getString(R.string.now_download, jsonOnlineMusic.getTitle()));
            }

            @Override
            public void onFail(Call call, Exception e) {
                mProgressDialog.cancel();
                ToastUtils.show(R.string.unable_to_download);
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        unbindService(mPlayServiceConnection);
        super.onDestroy();
    }

}
