package me.geek.music.widget;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import me.geek.music.R;
import me.geek.music.utils.ScreenUtils;

/**
 * 播放页Indicator,ViewPager指示器
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class IndicatorLayout extends LinearLayout {
    public IndicatorLayout(Context context) {

        this(context, null);
    }

    public IndicatorLayout(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    }

    public IndicatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //初始化时候设置LinearLayout的属性
    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
    }


    public void create(int count){
        for(int i=0; i<count; i++){
            ImageView imageView = new ImageView(getContext());
            //第一个参数为宽的设置，第二个参数为高的设置。
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            int padding = ScreenUtils.dp2px(3);
            //setPadding(3, 0, 3, 0)是分别padding-top=3 ,padding-right =0,padding-bottom=3,padding-left=0.
            imageView.setPadding(padding, 0, padding, 0);
            imageView.setImageResource(i==0? R.drawable.ic_play_page_indicator_selected : R.drawable.ic_play_page_indicator_unselected);
            addView(imageView);//往Linelayout中添加View
        }
    }

    public void setCurrent(int position){//传入所处的位置
        int count = getChildCount();//得到子视图的数量
        for(int i=0; i<count; i++){
            ImageView imageView = (ImageView)getChildAt(i);
            if(i == position){
                imageView.setImageResource(R.drawable.ic_play_page_indicator_selected);
            }else{
                imageView.setImageResource(R.drawable.ic_play_page_indicator_unselected);
            }
        }
    }
}
