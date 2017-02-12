package me.geek.music.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 正在播放ViewPaer适配器.包含歌词和封面
 * @Author Geek-Lizc(394925542@qq.com)
 *
 */

public class PlayPagerAdapter extends PagerAdapter {

    private List<View> mViews;

    public PlayPagerAdapter(List<View> views) {
        mViews = views;
    }

    /**
     * 返回用于滑动的fragment总数
     */
    @Override
    public int getCount() {
        return mViews.size();
    }

    /**
     * 该函数用来判断instantiateItem(ViewGroup, int)函数所返回来的Key与一个页面视图是否是代表的同一个视图
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * 第一：将参数里给定的position的视图，增加到conatiner中，供其创建并显示、。
     * 第二：返回当前position的View做为此视图的Key。
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mViews.get(position));
        return mViews.get(position);
    }

    /**
     * 实现的功能是移除一个给定位置的页面
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViews.get(position));
    }
}
