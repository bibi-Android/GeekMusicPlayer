package me.geek.music.utils;

import android.view.View;

import me.geek.music.enums.LoadStateEnum;


/**
 * 视图工具类
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class ViewUtils {

    public static void changeViewState(View loadSuccess, View loading, View loadFail, LoadStateEnum state){
        switch (state){
            case LOADING:
                loadSuccess.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                loadFail.setVisibility(View.GONE);
                break;
            case LOAD_SUCCESS:
                loadSuccess.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
                loadFail.setVisibility(View.GONE);
                break;
            case LOAD_FAIL:
                loadSuccess.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                loadFail.setVisibility(View.VISIBLE);
                break;
        }
    }
}
