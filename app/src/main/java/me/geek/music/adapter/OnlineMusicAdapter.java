package me.geek.music.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import me.geek.music.R;
import me.geek.music.model.JsonOnlineMusic;
import me.geek.music.utils.FileUtils;
import me.geek.music.utils.ImageUtils;

/**
 * 在线音乐音乐列表适配,点击歌榜以后跳转进去的榜单,例如百度热歌榜.
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class OnlineMusicAdapter extends BaseAdapter {
    private List<JsonOnlineMusic> mData;
    private OnMoreClickListener mListener;

    public OnlineMusicAdapter(List<JsonOnlineMusic> data){
        this.mData = data;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_music, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.ivCover = (ImageView)convertView.findViewById(R.id.iv_cover_holder);
            viewHolder.tvTitle = (TextView)convertView.findViewById(R.id.tv_title_holder);
            viewHolder.tvArtist = (TextView)convertView.findViewById(R.id.tv_artist_holder);
            viewHolder.ivMore = (ImageView)convertView.findViewById(R.id.iv_more_holder);
            viewHolder.vDivider = (View)convertView.findViewById(R.id.v_divider_holder) ;

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        JsonOnlineMusic jsonOnlineMusic = mData.get(position);

        ImageLoader.getInstance().displayImage(jsonOnlineMusic.getPic_small(), viewHolder.ivCover, ImageUtils.getCoverDisPlayOptions());
        viewHolder.tvTitle.setText(jsonOnlineMusic.getTitle());
        String artist = FileUtils.getArtistAndAlbum(jsonOnlineMusic.getArtist_name(), jsonOnlineMusic.getAlbum_title());
        viewHolder.tvArtist.setText(artist);
        viewHolder.ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onMoreClick(position);
            }
        });
        viewHolder.vDivider.setVisibility(isShowDivider(position)? View.VISIBLE : View.GONE);
        return convertView;
    }

    /**
     * 是否是最后一首
     */
    private boolean isShowDivider(int position){
        return position != mData.size()-1;
    }

    /**
     * 对外提供一个设置更多按钮监听器的方法,由外部来设定这个监听器的逻辑
     */
    public void setOnMoreClickListener(OnMoreClickListener listener){
        mListener = listener;
    }

    private static class ViewHolder{
        ImageView ivCover;
        TextView tvTitle;
        TextView tvArtist;
        ImageView ivMore;
        View vDivider;

    }
}
