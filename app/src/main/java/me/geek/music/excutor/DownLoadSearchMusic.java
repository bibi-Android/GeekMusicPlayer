package me.geek.music.excutor;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.File;
import java.io.FileWriter;

import me.geek.music.R;
import me.geek.music.application.AppCache;
import me.geek.music.callback.JsonCallback;
import me.geek.music.constants.Constants;
import me.geek.music.model.JsonDownloadInfo;
import me.geek.music.model.JsonLrc;
import me.geek.music.model.JsonSearchMusic;
import me.geek.music.utils.FileUtils;
import me.geek.music.utils.NetWorkUtils;
import me.geek.music.utils.Preferences;
import okhttp3.Call;

/**
 * 下载搜索音乐
 * @Author Geek-Lizc(394925542@qq.com)
 */

public abstract class DownLoadSearchMusic {
    private Context mContext;
    private JsonSearchMusic.JSong mJSong;

    public DownLoadSearchMusic(Context context, JsonSearchMusic.JSong jSong) {
        mContext = context;
        mJSong = jSong;
    }

    public void execute(){
        checkNetwork();
    }

    private void checkNetwork() {
        boolean mobileNetworkDownload = Preferences.enableMobileNetworkDownload();
        if (NetWorkUtils.isActiveNetWorkMobile(mContext) && !mobileNetworkDownload) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.tips);
            builder.setMessage(R.string.download_tips);
            builder.setPositiveButton(R.string.download_tips_sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    download();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else {
            download();
        }
    }

    private void download() {
        onPrepare();

        //获取歌曲下载链接
        //http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.play&songid=265046969
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
                        long id = FileUtils.downloadMusic(response.getBitrate().getFile_link(), mJSong.getArtistname(), mJSong.getSongname());
                        AppCache.getDownloadList().put(id, mJSong.getSongname());
                        onSuccess();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        onFail(call, e);
                    }
                });

        //下载歌词
        //http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.lry&songid=252832
        String lrcFileName = FileUtils.getLrcFileName(mJSong.getArtistname(), mJSong.getSongname());
        File lrcFile = new File(FileUtils.getLrcDir() + lrcFileName);
        if (!lrcFile.exists()) {
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
                            String lrcPath = FileUtils.getLrcDir() + FileUtils.getLrcFileName(mJSong.getArtistname(), mJSong.getSongname());
                            FileUtils.saveLrcFile(lrcPath, response.getLrcContent());
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                        }
                    });
        }
    }

    public abstract void onPrepare();

    public abstract void onSuccess();

    public abstract void onFail(Call call, Exception e);
}
