package me.geek.music.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import me.geek.music.R;
import me.geek.music.model.Music;
import me.geek.music.service.PlayService;
import me.geek.music.utils.CoverLoader;
import me.geek.music.utils.FileUtils;


/**
 * 本地音乐列表适配器
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class LocalMusicAdapter extends BaseAdapter {
    private OnMoreClickListener mListener;
    private int mPlayingPosition;

    @Override
    public int getCount() {//所包含的 Item 总个数
        return PlayService.getsMusicList().size();
    }

    @Override
    //getItemid不同于getItem的是，一些方法（如onclicklistener的onclick方法）有id这个参数，而这个id参数就是取决于
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return PlayService.getsMusicList().get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_music, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.v_playing = (View)convertView.findViewById(R.id.v_playing_holder);
            viewHolder.ivCover = (ImageView)convertView.findViewById(R.id.iv_cover_holder);
            viewHolder.tvTitle = (TextView)convertView.findViewById(R.id.tv_title_holder);
            viewHolder.tvArtist = (TextView)convertView.findViewById(R.id.tv_artist_holder);
            viewHolder.ivMore = (ImageView)convertView.findViewById(R.id.iv_more_holder);
            viewHolder.vDivider = (View)convertView.findViewById(R.id.v_divider_holder) ;

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if(position == mPlayingPosition){//ListView里有内容时
            viewHolder.v_playing.setVisibility(View.VISIBLE);
        }else {
            viewHolder.v_playing.setVisibility(View.INVISIBLE);
        }
        final Music music = PlayService.getsMusicList().get(position);
        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music.getCoverUri());
        viewHolder.ivCover.setImageBitmap(cover);
        viewHolder.tvTitle.setText(music.getTitle());
        String artist = FileUtils.getArtistAndAlbum(music.getArtist(), music.getAlbum());
        viewHolder.tvArtist.setText(artist);
        viewHolder.ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.onMoreClick(position);
                }
            }
        });
        viewHolder.vDivider.setVisibility(isShowDiver(position)? View.VISIBLE : View.GONE);
        return convertView;

    }
    private static class ViewHolder{
        View v_playing;
        ImageView ivCover;
        TextView tvTitle;
        TextView tvArtist;
        ImageView ivMore;
        View vDivider;
    }

    private boolean isShowDiver(int position){
        return position != PlayService.getsMusicList().size()-1;//只有最后的一首歌不用分割线
    }

    /**
     * 更新正在播放的音乐的position
     */
    public void updatePlayingPosition(PlayService playService){
        if(playService.getPlayingMusic() != null && playService.getPlayingMusic().getType() == Music.Type.LOCAL){
            mPlayingPosition = playService.getPlayingPosition();
        }else{
            mPlayingPosition = -1;
        }
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
        mListener = listener;
    }


}
