package me.geek.music.excutor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;

import me.geek.music.R;
import me.geek.music.application.AppCache;
import me.geek.music.callback.JsonCallback;
import me.geek.music.constants.Constants;
import me.geek.music.model.JsonDownloadInfo;
import me.geek.music.model.JsonOnlineMusic;
import me.geek.music.utils.FileUtils;
import me.geek.music.utils.NetWorkUtils;
import me.geek.music.utils.Preferences;
import okhttp3.Call;

/**
 * 下载歌曲
 * @Author Geek-Lizc(394925542@qq.com)
 */

public abstract class DownloadOnlineMusic {

    private Context mContext;
    private JsonOnlineMusic mJsonOnlineMusic;

    public DownloadOnlineMusic(Context mContext, JsonOnlineMusic mJsonOnlineMusic) {
        this.mContext = mContext;
        this.mJsonOnlineMusic = mJsonOnlineMusic;
    }

    /**
     * 对外提供开始工作的方法
     */
    public void execute(){
        checkNetwork();
    }

    /**
     * 检查网络
     */
    private void checkNetwork() {
        boolean mobileNetworkDownload = Preferences.enableMobileNetworkDownload();
        if(NetWorkUtils.isActiveNetWorkMobile(mContext) && !mobileNetworkDownload){
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
            /**
             * AlertDialog.Builder.show()
             * {
             *  AlertDialog dialog = create();
             *  dialog.show();
             *  return dialog;
             *  }
             * builder没有这个方法,而builder.show()会返回一个dialog
             * 所以也可以用builder.show().setCanceledOnTouchOutside(false);
             */
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

        }else{
            download();
        }
    }

    /**
     * 下载歌曲和歌词
     */
    private void download() {
        onPrepare();

        //下载歌曲

        /**
         * 获取歌曲下载地址
         * http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.play&songid=265046969
         */
        OkHttpUtils.get().url(Constants.BASE_URL)
                .addParams(Constants.PARAM_METHOD, Constants.METHOD_DOWNLOAD_MUSIC)
                .addParams(Constants.PARAM_SONG_ID, mJsonOnlineMusic.getSong_id())
                .build()
                .execute(new JsonCallback<JsonDownloadInfo>(JsonDownloadInfo.class) {
                    @Override
                    public void onResponse(final JsonDownloadInfo response) {
                        if (response == null) {
                            onFail(null, null);
                            return;
                        }
                        long id = FileUtils.downloadMusic(response.getBitrate().getFile_link(), mJsonOnlineMusic.getArtist_name(), mJsonOnlineMusic.getTitle());
                        AppCache.getDownloadList().put(id, mJsonOnlineMusic.getTitle());
                        onSuccess();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        onFail(call, e);
                    }
                });

        //下载歌词
        String lrcFileName = FileUtils.getLrcFileName(mJsonOnlineMusic.getArtist_name(), mJsonOnlineMusic.getTitle());
        File lrcFile = new File(FileUtils.getLrcDir() + lrcFileName);
        //歌词链接不为空,且歌词文件不存在的时候
        if(!TextUtils.isEmpty(mJsonOnlineMusic.getLrclink()) && !lrcFile.exists()){
            OkHttpUtils.get().url(mJsonOnlineMusic.getLrclink())
                    .build()
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
                    });
        }

    }

    public abstract void onPrepare();

    public abstract void onSuccess();

    public abstract void onFail(Call call, Exception e);


}
