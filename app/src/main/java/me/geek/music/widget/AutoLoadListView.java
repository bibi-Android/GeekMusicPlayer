package me.geek.music.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import java.io.File;

import me.geek.music.R;

/**
 * 自动加载更多ListView
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class AutoLoadListView extends ListView implements AbsListView.OnScrollListener {//ListView的滚动监听--AbsListView.OnScrollListener
    private View vFooter;
    private OnLoadListener mListener;
    private int mFirstVisibleItem = 0;
    private boolean mEnableLoad = true;
    private boolean mIsLoading = false;

    public AutoLoadListView(Context context){
        super(context);
        init();
    }

    public AutoLoadListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoLoadListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        vFooter = LayoutInflater.from(getContext()).inflate(R.layout.auto_load_list_view_footer, null);
        addFooterView(vFooter, null, false);
        setOnScrollListener(this);
        onLoadComplete();
    }

    /**
     * 是否可以点击
     * @param enable
     */
    public void setEnable(boolean enable) {
        mEnableLoad = enable;
    }

    public void onLoadComplete() {
        mIsLoading = false;
        removeFooterView(vFooter);//加载完ListView后移除底部View
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    //在滑动屏幕的过程中，onScroll方法会一直调用
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //firstVisibleItem,表示在现时屏幕第一个ListItem在整个ListView的位置（下标从0开始）
        //visibleItemCount表示在现时屏幕可以见到的ListItem总数
        boolean isPullDown = firstVisibleItem > mFirstVisibleItem;//现在首个可视Item位置不是0,则表示用户已经向下拉动了
        if(mEnableLoad && !mIsLoading && isPullDown){
            int lastVisibleItem = firstVisibleItem + visibleItemCount;//最后一个可视ListItem的位置
            if(lastVisibleItem >= totalItemCount-1){//如果最后一个可视ListItem的位置并不是整个ListView的最后一个,那么就是在加载中
                onLoad();
            }
        }

    }

    /**
     * 自动加载
     */
    private void onLoad() {
        mIsLoading = true;
        addFooterView(vFooter, null, false);
        if(mListener != null){
            mListener.onLoad();
        }
    }

    public void setOnLoadListener(OnLoadListener listener) {
        mListener = listener;
    }

    public interface OnLoadListener {
        void onLoad();
    }
}
