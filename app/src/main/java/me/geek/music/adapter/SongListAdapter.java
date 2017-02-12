package me.geek.music.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.L;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.List;

import me.geek.music.R;
import me.geek.music.callback.JsonCallback;
import me.geek.music.constants.Constants;
import me.geek.music.model.JsonOnlineMusic;
import me.geek.music.model.JsonOnlineMusicList;
import me.geek.music.model.SongListInfo;
import me.geek.music.utils.ImageUtils;
import okhttp3.Call;


/**
 * 歌单列表适配器
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class SongListAdapter extends BaseAdapter {
    private static final int TYPE_PROFILE = 0;
    private static final int TYPE_MUSIC_LIST = 1;
    private Context mContext;
    private List<SongListInfo> mData;

    public SongListAdapter(List<SongListInfo> data){
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_MUSIC_LIST;//是音乐则可以点击
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).getType().equals("#")) {
            return TYPE_PROFILE;
        } else {
            return TYPE_MUSIC_LIST;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mContext = parent.getContext();
        ViewHolderProfile holderProfile;
        ViewHolderMusicList holderMusicList;
        SongListInfo songListInfo = mData.get(position);
        int itemViewType = getItemViewType(position);
        switch (itemViewType){
            case TYPE_PROFILE://当ViewType是标题栏时有#标识
                if(convertView == null){
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.view_holder_song_list_profile, parent, false);
                    holderProfile = new ViewHolderProfile();

                    holderProfile.tvProfile = (TextView)convertView.findViewById(R.id.tv_profile);

                    convertView.setTag(holderProfile);

                }else{
                    holderProfile = (ViewHolderProfile) convertView.getTag();
                }
                holderProfile.tvProfile.setText(songListInfo.getTitle());
                break;

            case TYPE_MUSIC_LIST:
                if(convertView == null){//榜单下的分类歌榜,以数字为分类
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.view_holder_song_list, parent, false);
                    holderMusicList = new ViewHolderMusicList();

                    holderMusicList.ivCover = (ImageView)convertView.findViewById(R.id.iv_cover_song_list);
                    holderMusicList.tvMusic1 = (TextView)convertView.findViewById(R.id.tv_music_1);
                    holderMusicList.tvMusic2 = (TextView)convertView.findViewById(R.id.tv_music_2);
                    holderMusicList.tvMusic3 = (TextView)convertView.findViewById(R.id.tv_music_3);
                    holderMusicList.vDivider = (View)convertView.findViewById(R.id.v_divier_song_list);

                    convertView.setTag(holderMusicList);
                }else {
                    holderMusicList = (ViewHolderMusicList)convertView.getTag();
                }
                getMusicListInfo(songListInfo, holderMusicList);
                holderMusicList.vDivider.setVisibility(isShowDivider(position)? View.VISIBLE : View.GONE);
        }

        return convertView;
    }

    /**
     * 获取在线音乐的列表
     * "http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.billboard.billList&type=23&size=3";
     */


    private void getMusicListInfo(final SongListInfo songListInfo, final ViewHolderMusicList holderMusicList) {
        if(songListInfo.getCoverUrl() == null){
            holderMusicList.ivCover.setTag(songListInfo.getTitle());//存储View外的信息
            holderMusicList.ivCover.setImageResource(R.drawable.default_cover);
            holderMusicList.tvMusic1.setText("1.加载中...");
            holderMusicList.tvMusic2.setText("2.加载中...");
            holderMusicList.tvMusic3.setText("3.加载中...");
            OkHttpUtils.get().url(Constants.BASE_URL)
                    .addParams(Constants.PARAM_METHOD, Constants.METHOD_GET_MUSIC_LIST)
                    .addParams(Constants.PARAM_TYPE, songListInfo.getType())
                    .addParams(Constants.PARAM_SIZE, "3")
                    .build()
                    .execute(new JsonCallback<JsonOnlineMusicList>(JsonOnlineMusicList.class) {
                        @Override
                        public void onResponse(JsonOnlineMusicList response) {
                            if (response == null || response.getSong_list() == null) {
                                return;
                            }
                            if (!songListInfo.getTitle().equals(holderMusicList.ivCover.getTag())) {
                                return;
                            }
                            parse(response, songListInfo);
                            setData(songListInfo, holderMusicList);
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                        }
                    });
        }else{
            holderMusicList.ivCover.setTag(null);
            setData(songListInfo, holderMusicList);
        }
    }



    /**
     * 根据返回的Json信息,设置榜单的歌名与歌手名,榜单图标
     */
    private void parse(JsonOnlineMusicList musicList, SongListInfo songListInfo) {
        List<JsonOnlineMusic> jsonOnlineMusics = musicList.getSong_list();
        songListInfo.setCoverUrl(musicList.getBillboard().getPic_s260());//把榜单图标Url传入
        if(jsonOnlineMusics.size() >= 1){
            songListInfo.setMusic1(mContext.getString(R.string.song_list_item_title_1, jsonOnlineMusics.get(0).getTitle(), jsonOnlineMusics.get(0).getArtist_name()));//1.%1$s - %2$s
        }else{
            songListInfo.setMusic1("");
        }

        if (jsonOnlineMusics.size() >= 2){
            songListInfo.setMusic2(mContext.getString(R.string.song_list_item_title_2, jsonOnlineMusics.get(1).getTitle(), jsonOnlineMusics.get(1).getArtist_name()));
        }else{
            songListInfo.setMusic2("");
        }

        if (jsonOnlineMusics.size() >= 3) {
            songListInfo.setMusic3(mContext.getString(R.string.song_list_item_title_3, jsonOnlineMusics.get(2).getTitle(), jsonOnlineMusics.get(2).getArtist_name()));
        } else {
            songListInfo.setMusic3("");
        }
    }

    /**
     * 设置榜单图标,设置榜单歌曲前三的歌曲名-歌手名
     *ImageLoader.getInstance().displayImage(imageUrl, imageView，options);
     * imageUrl代表图片的URL地址，imageView代表承载图片的IMAGEVIEW控件 ， options代表DisplayImageOptions配置文件
     */
    private void setData(SongListInfo songListInfo, ViewHolderMusicList holderMusicList) {
        ImageLoader.getInstance().displayImage(songListInfo.getCoverUrl(), holderMusicList.ivCover, ImageUtils.getCoverDisPlayOptions());
        holderMusicList.tvMusic1.setText(songListInfo.getMusic1());
        holderMusicList.tvMusic2.setText(songListInfo.getMusic2());
        holderMusicList.tvMusic3.setText(songListInfo.getMusic3());
    }





    private boolean isShowDivider(int position) {
        return position != mData.size()-1;//最后一项不加分割线
    }

    private static class ViewHolderProfile{
        TextView tvProfile;

    }

    private static class ViewHolderMusicList{
        ImageView ivCover;
        TextView tvMusic1;
        TextView tvMusic2;
        TextView tvMusic3;
        View vDivider;

    }
}
