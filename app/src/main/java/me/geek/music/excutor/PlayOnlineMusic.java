package me.geek.music.excutor;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;

import me.geek.music.R;
import me.geek.music.callback.JsonCallback;
import me.geek.music.constants.Constants;
import me.geek.music.model.JsonDownloadInfo;
import me.geek.music.model.JsonOnlineMusic;
import me.geek.music.model.Music;
import me.geek.music.utils.FileUtils;
import me.geek.music.utils.NetWorkUtils;
import me.geek.music.utils.Preferences;
import okhttp3.Call;

/**
 * 播放在线音乐
 * @Author Geek-Lizc(394925542@qq.com)
 */

public abstract class PlayOnlineMusic {
    private Context mContext;
    private JsonOnlineMusic mJOnlineMusic;
    //计数器,保证获得了播放uri以后,再执行播放
    private int mCounter = 0;
    public PlayOnlineMusic(Context context, JsonOnlineMusic jOnlineMusic) {
        mContext = context;
        mJOnlineMusic = jOnlineMusic;
    }

    public void execute() {
        checkNetwork();
    }

    private void checkNetwork() {
        boolean mobileNetworkPlay = Preferences.enableMobileNetworkPlay();
        if (NetWorkUtils.isActiveNetWorkMobile(mContext) && !mobileNetworkPlay) {
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
        } else {
            getPlayInfo();
        }
    }
    /**
     * 获取播放信息的url,播放链接,专辑图链接
     * 必然要获得了歌曲播放uri,才凑够3
     */

    private void getPlayInfo() {
        onPrepare();
        String lrcFileName = FileUtils.getLrcFileName(mJOnlineMusic.getArtist_name(), mJOnlineMusic.getTitle());
        File lrcFile = new File(FileUtils.getLrcDir() + lrcFileName);
        //如果LRC下载链接不存在或者歌词文件已经存在
        if (TextUtils.isEmpty(mJOnlineMusic.getLrclink()) || lrcFile.exists()) {
            mCounter++;
        }
        //先获取小图,有小图了就是TextUtils.isEmpty(mJsonOnlineMusic.getPic_big()) ? mJsonOnlineMusic.getPic_small() : mJsonOnlineMusic.getPic_big();有大图的话就拿大图
        String picUrl = TextUtils.isEmpty(mJOnlineMusic.getPic_big()) ? TextUtils.isEmpty(mJOnlineMusic.getPic_small())
                ? null : mJOnlineMusic.getPic_small() : mJOnlineMusic.getPic_big();
        if (TextUtils.isEmpty(picUrl)) {//如果链接是空的
            mCounter++;
        }
        final Music music = new Music();
        music.setType(Music.Type.ONLINE);
        music.setTitle(mJOnlineMusic.getTitle());
        music.setArtist(mJOnlineMusic.getArtist_name());
        music.setAlbum(mJOnlineMusic.getAlbum_title());

        /**
         * 获取歌曲播放链接
         * 示例:http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.play&songid=262288851
         */
        OkHttpUtils.get().url(Constants.BASE_URL)
                .addParams(Constants.PARAM_METHOD, Constants.METHOD_DOWNLOAD_MUSIC)
                .addParams(Constants.PARAM_SONG_ID, mJOnlineMusic.getSong_id())
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
                        //当不用下载歌词文件,不用下载专辑图(没有专辑图链接),那么就播放吧!
                        if (mCounter == 3) {
                            onSuccess(music);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        onFail(call, e);
                    }
                });
        /**
         * 下载歌词
         */
        //当歌词链接非空,并且歌词文件不存在
        if (!TextUtils.isEmpty(mJOnlineMusic.getLrclink()) && !lrcFile.exists()) {
            OkHttpUtils.get().url(mJOnlineMusic.getLrclink()).build()
                    .execute(new FileCallBack(FileUtils.getLrcDir(), lrcFileName) {
                        @Override
                        public void inProgress(float progress, long total) {
                        }

                        @Override
                        public void onResponse(File response) {
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                        }

                        @Override
                        public void onAfter() {
                            mCounter++;
                            //有了专辑图并且有了歌曲链接
                            if (mCounter == 3) {
                                onSuccess(music);
                            }
                        }
                    });
        }
        /**
         * 下载歌曲封面
         */
        if (!TextUtils.isEmpty(picUrl)) {
            OkHttpUtils.get().url(picUrl).build()
                    .execute(new BitmapCallback() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            music.setCover(bitmap);
                            mCounter++;
                            if (mCounter == 3) {
                                onSuccess(music);
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            mCounter++;
                            if (mCounter == 3) {
                                onSuccess(music);
                            }
                        }
                    });
        }
    }

    public abstract void onPrepare();

    public abstract void onSuccess(Music music);

    public abstract void onFail(Call call, Exception e);
}
