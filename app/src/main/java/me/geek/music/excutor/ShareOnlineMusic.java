package me.geek.music.excutor;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;

import me.geek.music.R;
import me.geek.music.callback.JsonCallback;
import me.geek.music.constants.Constants;
import me.geek.music.model.JsonDownloadInfo;
import me.geek.music.utils.ToastUtils;
import okhttp3.Call;

/**
 * 分享在线歌曲
 * @Author Geek-Lizc(394925542@qq.com)
 */

public abstract class ShareOnlineMusic {

    private Context mContext;
    private String mTitle;
    private String mSongId;

    public ShareOnlineMusic(Context mContext, String mTitle, String mSongId) {
        this.mContext = mContext;
        this.mTitle = mTitle;
        this.mSongId = mSongId;
    }

    public void execute(){
        share();
    }

    /**
     * 分享音乐逻辑
     */
    private void share() {
        onPrepare();
        /**
         * 获取歌曲播放链接
         * http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.play&songid=262288851
         */
        OkHttpUtils.get().url(Constants.BASE_URL)
                .addParams(Constants.PARAM_METHOD, Constants.METHOD_DOWNLOAD_MUSIC)
                .addParams(Constants.PARAM_SONG_ID, mSongId)
                .build()
                .execute(new JsonCallback<JsonDownloadInfo>(JsonDownloadInfo.class) {
                    @Override
                    public void onResponse(final JsonDownloadInfo response) {
                        if (response == null) {
                            onFail(null, null);
                            ToastUtils.show(R.string.unable_to_share);
                            return;
                        }
                        onSuccess();
                        Intent intent = new Intent(Intent.ACTION_SEND);// 启动分享发送的属性
                        intent.setType("text/plain");// 分享发送的数据类型,是无格式正文
                        intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.share_music, mContext.getString(R.string.app_name),
                                mTitle, response.getBitrate().getFile_link()));
                        mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share)));
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        onFail(call, e);
                        ToastUtils.show(R.string.unable_to_share);
                    }
                });

    }


    public abstract void onPrepare();

    public abstract void onSuccess();

    public abstract void onFail(Call call, Exception e);
}
