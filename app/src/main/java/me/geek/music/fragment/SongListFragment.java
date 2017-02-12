package me.geek.music.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

import me.geek.music.R;
import me.geek.music.activity.OnlineMusicActivity;
import me.geek.music.adapter.SongListAdapter;
import me.geek.music.constants.Extras;
import me.geek.music.enums.LoadStateEnum;
import me.geek.music.model.SongListInfo;
import me.geek.music.utils.NetWorkUtils;
import me.geek.music.utils.ViewUtils;

import static me.geek.music.application.AppCache.getContext;

/**
 * 在线音乐
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class SongListFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ListView lvSongList;
    private LinearLayout llLoading;
    private LinearLayout llLoadFail;
    private List<SongListInfo> mSongLists;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_onlinesong_list, container, false);

        lvSongList = (ListView)view.findViewById(R.id.lv_song_list);
        llLoading = (LinearLayout)view.findViewById(R.id.ll_loding_onlinesong);
        llLoadFail = (LinearLayout)view.findViewById(R.id.ll_load_fail_onlinesong);

        return view;
    }

    @Override
    protected void init() {
        if(!NetWorkUtils.isNetWorkAvailable(getContext())){
            //无网络,加载在线失败
            ViewUtils.changeViewState(lvSongList, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            return;
        }
        mSongLists = getPlayService().mSongLists;
        if(mSongLists.isEmpty()){
            /* 把这个格式的songListInfo存入mSongList中
             * #主打榜单
             * 1.新歌榜
             * 2.热歌榜
             * #分类榜单
             * 20.华语金曲榜
             * 21.欧美金曲榜
             * 24.影视金曲榜
             * 23.情歌对唱榜
             * 25.网络歌曲榜
             * 22.经典老歌榜
             * 11.摇滚榜
             * #媒体榜单
             * 6.KTV热歌榜
             * 8.Billboard
             * 18.Hito中文榜
             * 7.叱咤歌曲榜
             */
            String[] title = getResources().getStringArray(R.array.online_music_list_title);
            String[] types = getResources().getStringArray(R.array.online_music_list_type);
            for(int i=0; i<title.length; i++){
                SongListInfo info = new SongListInfo();
                info.setTitle(title[i]);
                info.setType(types[i]);// #或者数字. #则是榜单  数字为榜单下的分类
                mSongLists.add(info);
            }
        }
        SongListAdapter adapter = new SongListAdapter(mSongLists);
        lvSongList.setAdapter(adapter);
    }

    @Override
    protected void setListener() {
        lvSongList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SongListInfo songListInfo = mSongLists.get(position);
        Intent intent = new Intent(getContext(), OnlineMusicActivity.class);//跳转到对应的歌榜活动
        intent.putExtra(Extras.MUSIC_LIST_TYPE, songListInfo);//带着该歌榜信息的对象跳转
        startActivity(intent);
    }
}
