package me.geek.music.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import me.geek.music.R;
import me.geek.music.model.JsonSearchMusic;

/**
 * 搜索结果ListView的适配器
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class SearchMusicAdatper extends BaseAdapter {
    private List<JsonSearchMusic.JSong> mData;
    private OnMoreClickListener mListener;

    public SearchMusicAdatper(List<JsonSearchMusic.JSong> data){
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_music, null);
            viewHolder = new ViewHolder();

            viewHolder.ivCover = (ImageView)convertView.findViewById(R.id.iv_cover_holder);
            viewHolder.tvTitle = (TextView)convertView.findViewById(R.id.tv_title_holder);
            viewHolder.tvArtist = (TextView)convertView.findViewById(R.id.tv_artist_holder);
            viewHolder.ivMore = (ImageView)convertView.findViewById(R.id.iv_more_holder);
            viewHolder.vDivider = (View)convertView.findViewById(R.id.v_divider_holder) ;

            //搜索界面不需要专辑图
            viewHolder.ivCover.setVisibility(View.GONE);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvArtist.setText(mData.get(position).getArtistname());
        viewHolder.tvTitle.setText(mData.get(position).getSongname());
        viewHolder.ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onMoreClick(position);
            }
        });
        viewHolder.vDivider.setVisibility(isShowDivider(position)? View.VISIBLE : View.GONE);
        return convertView;
    }

    private boolean isShowDivider(int position) {

        return position != mData.size() - 1;
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
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
