package me.geek.music.excutor;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import me.geek.music.R;
import me.geek.music.callback.JsonCallback;
import me.geek.music.constants.Constants;
import me.geek.music.model.JsonDownloadInfo;
import me.geek.music.model.JsonLrc;
import me.geek.music.model.JsonSearchMusic;
import me.geek.music.model.Music;
import me.geek.music.utils.FileUtils;
import me.geek.music.utils.NetWorkUtils;
import me.geek.music.utils.Preferences;
import okhttp3.Call;

/**
 * 播放搜索音乐
 * @Author Geek-Lizc(394925542@qq.com)
 */

public abstract class PlaySearchMusic {
    private Context mContext;
    private JsonSearchMusic.JSong mJSong;
    //计数器,保证获得了播放uri以后,再执行播放
    private int mCounter = 0;

    public PlaySearchMusic(Context context, JsonSearchMusic.JSong jSong) {
        mContext = context;
        mJSong = jSong;
    }

    /**
     * 执行逻辑
     */
    public void execute(){
        checkNetWork();
    }

    /**
     * 检查网络
     */
    private void checkNetWork(){
        boolean mobileNetWorkPlay = Preferences.enableMobileNetworkPlay();
        //当有手机数据网络,但是又没有勾选设置里的允许手机网络播放
        if(NetWorkUtils.isActiveNetWorkMobile(mContext) && !mobileNetWorkPlay){
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.tips);
            builder.setMessage(R.string.play_tips);
            builder.setPositiveButton(R.string.play_tips_sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Preferences.saveMobileNetworkPlay(true);
                    getPlayInfo();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }else{
            getPlayInfo();
        }
    }

    /**
     * 获取播放信息
     * 歌曲播放链接,歌词
     * 必然要获得歌曲播放uri才到2
     */

    private void getPlayInfo() {
        onPrepare();
        String lrcFileName = FileUtils.getLrcFileName(mJSong.getArtistname(), mJSong.getSongname());
        File lrcFile = new File(FileUtils.getLrcDir() + lrcFileName);
        if (lrcFile.exists()) {
            mCounter++;
        }
        final Music music = new Music();
        music.setType(Music.Type.ONLINE);
        music.setTitle(mJSong.getSongname());
        music.setArtist(mJSong.getArtistname());
        //获取歌曲播放链接
        //http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.play&songid=252832
        OkHttpUtils.get().url(Constants.BASE_URL)
                .addParams(Constants.PARAM_METHOD, Constants.METHOD_DOWNLOAD_MUSIC)
                .addParams(Constants.PARAM_SONG_ID, mJSong.getSongid())
                .build()
                .execute(new JsonCallback<JsonDownloadInfo>(JsonDownloadInfo.class) {
                    @Override
                    public void onResponse(final JsonDownloadInfo response) {
                        if (response == null) {
                            onFail(null, null);
                            return;
                        }
                        music.setUri(response.getBitrate().getFile_link());
                        music.setDuration(response.getBitrate().getFile_duration() * 1000);
                        mCounter++;
                        if (mCounter == 2) {
                            onSuccess(music);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        onFail(call, e);
                    }
                });

        //下载歌词
        //http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.lry&songid=252832
        OkHttpUtils.get().url(Constants.BASE_URL)
                .addParams(Constants.PARAM_METHOD, Constants.METHOD_LRC)
                .addParams(Constants.PARAM_SONG_ID, mJSong.getSongid())
                .build()
                .execute(new JsonCallback<JsonLrc>(JsonLrc.class) {
                    @Override
                    public void onResponse(JsonLrc response) {
                        if (response == null || TextUtils.isEmpty(response.getLrcContent())) {
                            return;
                        }
                        String lrcFileName = FileUtils.getLrcFileName(mJSong.getArtistname(), mJSong.getSongname());
                        FileUtils.saveLrcFile(lrcFileName, response.getLrcContent());
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }

                    @Override
                    public void onAfter() {
                        mCounter++;
                        if (mCounter == 2) {
                            onSuccess(music);
                        }
                    }
                });

    }


    public abstract void onPrepare();

    public abstract void onSuccess(Music music);

    public abstract void onFail(Call call, Exception e);
}
