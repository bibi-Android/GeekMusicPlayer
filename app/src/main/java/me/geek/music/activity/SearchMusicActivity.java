package me.geek.music.activity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zhy.http.okhttp.OkHttpUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import me.geek.music.R;
import me.geek.music.adapter.OnMoreClickListener;
import me.geek.music.adapter.SearchMusicAdatper;
import me.geek.music.callback.JsonCallback;
import me.geek.music.constants.Constants;
import me.geek.music.enums.LoadStateEnum;
import me.geek.music.excutor.DownLoadSearchMusic;
import me.geek.music.excutor.DownloadOnlineMusic;
import me.geek.music.excutor.PlaySearchMusic;
import me.geek.music.excutor.ShareOnlineMusic;
import me.geek.music.model.JsonSearchMusic;
import me.geek.music.model.Music;
import me.geek.music.service.PlayService;
import me.geek.music.utils.FileUtils;
import me.geek.music.utils.ToastUtils;
import me.geek.music.utils.ViewUtils;
import okhttp3.Call;

/**
 * 搜索音乐活动
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class SearchMusicActivity extends BaseActivity implements SearchView.OnQueryTextListener
        , AdapterView.OnItemClickListener, OnMoreClickListener {


    private ListView lvSearchMusic;
    private LinearLayout llLoading;
    private LinearLayout llLoadFail;

    private SearchMusicAdatper mAdapter;
    private List<JsonSearchMusic.JSong> mSearchMusicList;
    private PlayService mPlayService;
    private ServiceConnection mPlayServiceConnection;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_music);

        lvSearchMusic = (ListView)findViewById(R.id.lv_search_music_list);
        llLoading = (LinearLayout)findViewById(R.id.ll_loding_search);
        llLoadFail = (LinearLayout)findViewById(R.id.ll_load_fail_search);

        bindService();
        mSearchMusicList = new ArrayList<JsonSearchMusic.JSong>();
        mAdapter = new SearchMusicAdatper(mSearchMusicList);
        lvSearchMusic.setAdapter(mAdapter);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        //将加载失败,请检查网络后重试  改成  未找到相关结果
        ((TextView)llLoadFail.findViewById(R.id.tv_load_fail_text)).setText(R.string.search_empty);
    }

    /**
     * 绑定服务
     */
    private void bindService(){
        Intent intent = new Intent(this, PlayService.class);
        mPlayServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mPlayService = ((PlayService.PlayBinder)service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 设置各类监听器
     */
    @Override
    protected void setListener() {
        lvSearchMusic.setOnItemClickListener(this);
        mAdapter.setOnMoreClickListener(this);
    }


    /**
     * 设置搜索框SearchView属性
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_music, menu);
        SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);//设置最大宽度
        searchView.onActionViewExpanded();//表示在内容为空时不显示取消的x按钮，内容不为空时显示.
        searchView.setQueryHint(getString(R.string.search_tips));//设置查询提示字符串
        searchView.setOnQueryTextListener(this);//为搜索框设置监听器
        searchView.setSubmitButtonEnabled(true);//设置是否显示搜索按钮

        return super.onCreateOptionsMenu(menu);
    }

    /**
     *  当点击搜索按钮时触发该方法
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOADING);
        searchMusic(query);
        return false;
    }

    /**
     * 搜索音乐
     * http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.search.catalogSug&query=周杰伦
     */
    private void searchMusic(String keyword) {
        OkHttpUtils.get().url(Constants.BASE_URL)
                .addParams(Constants.PARAM_METHOD, Constants.METHOD_SEARCH_MUSIC)
                .addParams(Constants.PARAM_QUERY, keyword)
                .build()
                .execute(new JsonCallback<JsonSearchMusic>(JsonSearchMusic.class) {
                    @Override
                    public void onResponse(JsonSearchMusic response) {
                        if (response == null || response.getSong() == null) {
                            ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                            return;
                        }
                        ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                        mSearchMusicList.clear();
                        mSearchMusicList.addAll(response.getSong());
                        mAdapter.notifyDataSetChanged();
                        lvSearchMusic.requestFocus();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //表示将列表移动到指定的Position处,当拉到listview下方再重新点击搜索就会回到顶部
                                lvSearchMusic.setSelection(0);
                            }
                        });
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                    }
                });
    }

    /**
     * 当搜索内容改变时触发该方法
     *
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }




    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        new PlaySearchMusic(this, mSearchMusicList.get(position)){

            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onSuccess(Music music) {
                mProgressDialog.cancel();
                mPlayService.play(music);
                ToastUtils.show(getString(R.string.now_play, music.getTitle()));
            }

            @Override
            public void onFail(Call call, Exception e) {
                mProgressDialog.cancel();
                ToastUtils.show(R.string.unable_to_play);
            }
        }.execute();
    }


    /**
     * 点击更多按钮
     */
    @Override
    public void onMoreClick(int position) {
        final JsonSearchMusic.JSong jSong = mSearchMusicList.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(jSong.getSongname());
        String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(jSong.getArtistname(), jSong.getSongname());
        File file = new File(path);
        //如果歌曲已经存在本地,则不显示下载按钮
        int itemId = file.exists()? R.array.search_music_dialog_no_download :  R.array.search_music_dialog;
        dialog.setItems(itemId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0://分享
                        share(jSong);
                        break;
                    case 1://下载
                        downLoad(jSong);
                        break;
                }
            }
        });
        dialog.show();
    }

    /**
     * 下载音乐
     */
    private void downLoad(final JsonSearchMusic.JSong jSong) {
        new DownLoadSearchMusic(this, jSong) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onSuccess() {
                mProgressDialog.cancel();
                ToastUtils.show(getString(R.string.now_download, jSong.getSongname()));
            }

            @Override
            public void onFail(Call call, Exception e) {
                mProgressDialog.cancel();
                ToastUtils.show(getString(R.string.now_download, jSong.getSongname()));
            }
        }.execute();
    }

    /**
     * 分享音乐
     */
    private void share(JsonSearchMusic.JSong jSong) {
        new ShareOnlineMusic(this, jSong.getSongname(), jSong.getSongid()) {
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


    @Override
    protected void onDestroy() {
        unbindService(mPlayServiceConnection);
        super.onDestroy();
    }
}
