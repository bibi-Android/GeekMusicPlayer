package me.geek.music.excutor;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;

import me.geek.music.callback.JsonCallback;
import me.geek.music.constants.Constants;
import me.geek.music.model.JsonLrc;
import me.geek.music.model.JsonSearchMusic;
import me.geek.music.utils.FileUtils;
import okhttp3.Call;

/**
 * 如果本地歌曲没有歌词则从网络搜索歌词
 * @Author Geek-Lizc(394925542@qq.com)
 */

public abstract class SearchLrc {

    private String artist;
    private String title;

    public SearchLrc(String artist, String title) {
        this.artist = artist;
        this.title = title;
    }

    public void execute(){
        onPrepare();
        searchLrc();
    }

    /**
     * 搜索歌词,由于要获得本地歌曲的歌名在百度上的songid,所以要拿着歌手与歌手通过API查询
     * http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.search.catalogSug&query=安静-周杰伦
     */
    private void searchLrc() {
        OkHttpUtils.get().url(Constants.BASE_URL)
                .addParams(Constants.PARAM_METHOD, Constants.METHOD_SEARCH_MUSIC)
                .addParams(Constants.PARAM_QUERY, title + "-" + artist)
                .build()
                .execute(new JsonCallback<JsonSearchMusic>(JsonSearchMusic.class) {
                    @Override
                    public void onResponse(JsonSearchMusic response) {
                        if (response == null || response.getSong() == null || response.getSong().size() == 0) {
                            onFinish(null);
                            return;
                        }
                        downloadLrc(response.getSong().get(0).getSongid());
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        onFinish(null);
                    }
                });


    }
    /**
     * 通过songid下载歌词
     * http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.lry&songid=696642
     */

    private void downloadLrc(String songid) {
        OkHttpUtils.get().url(Constants.BASE_URL)
                .addParams(Constants.PARAM_METHOD, Constants.METHOD_LRC)
                .addParams(Constants.PARAM_SONG_ID, songid)
                .build()
                .execute(new JsonCallback<JsonLrc>(JsonLrc.class) {
                    @Override
                    public void onResponse(JsonLrc response) {
                        if (response == null || TextUtils.isEmpty(response.getLrcContent())) {
                            onFinish(null);
                            return;
                        }
                        String lrcPath = FileUtils.getLrcDir() + FileUtils.getLrcFileName(artist, title);
                        FileUtils.saveLrcFile(lrcPath, response.getLrcContent());
                        onFinish(lrcPath);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        onFinish(null);
                    }
                });
    }


    public abstract void onPrepare();

    public abstract void onFinish(@Nullable String lrcPath);
}
