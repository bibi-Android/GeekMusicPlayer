package me.geek.music.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * SharedPreferences工具类,用于存储各种信息
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class Preferences {


    private static final String Music_ID = "music_id";
    private static final String Play_MODE = "play_mode";
    private static final String SPLASH_URL = "splash_url";
    private static final String NIGHT_MODE = "night_mode";
    private static final String NETWORK_DOWNLOAD = "mobile_network_download";
    private static final String NETWORK_PLAY = "mobile_network_play";

    private static Context mcontext;

    public static void init(Context context){
        mcontext = context.getApplicationContext();
    }


    /**
     * 通过SharedPreference保存和获取long,String,ing,boolean等类型信息
     */
    private static SharedPreferences getPreferences(){
        return PreferenceManager.getDefaultSharedPreferences(mcontext);
    }

    private static void saveLong(String key, long value){
        getPreferences().edit().putLong(key, value).commit();
    }

    private static long getLong(String key, long defValue){
        return getPreferences().getLong(key, defValue);
    }

    private static void saveString(String key, String value){
        getPreferences().edit().putString(key, value).commit();
    }

    private static String getString(String key, String defValue){
        return getPreferences().getString(key, defValue);
    }

    private static void saveInt(String key, int value){
        getPreferences().edit().putInt(key, value).commit();
    }

    private static int getInt(String key, int defValue){
        return getPreferences().getInt(key, defValue);
    }

    private static void saveBoolean(String key, boolean value){
        getPreferences().edit().putBoolean(key, value).commit();
    }

    private static boolean getBoolean(String key, boolean defValue){
        return getPreferences().getBoolean(key, defValue);
    }


    /**
     * 保存和获取歌曲id
     */
    public static void saveCurrentSongId(long id){
        saveLong(Music_ID, id);
    }
    public static long getCurrentSongId(){
        return getLong(Music_ID, -1);
    }


    /**
     * 保存获取播放模式
     */
    public static void savePlayMode(int mode){
        saveInt(Play_MODE, mode);
    }

    public static int getPlayMode(){
        return getPreferences().getInt(Play_MODE, 0);
    }

    /**
     * 保存获取引导页Url
     */
    public static void saveSplashUrl(String url){
        saveString(SPLASH_URL, url);
    }

    public static String getSplashUrl(){
        return getPreferences().getString(SPLASH_URL, "");
    }

    /**
     * 保存和获取是否是网络数据播放,在软件内设置是否使用手机数据
     */
    public static void saveMobileNetworkPlay(boolean enable){
        saveBoolean(NETWORK_PLAY, enable);
    }

    public static boolean enableMobileNetworkPlay(){
        return getBoolean(NETWORK_PLAY, false);
    }

    /**
     * 保存和获取是否是网络下载音乐
     */
    public static void saveMobileNetworkDownload(boolean enable){
        saveBoolean(NETWORK_DOWNLOAD, enable);
    }

    public static boolean enableMobileNetworkDownload(){
        return getBoolean(NETWORK_DOWNLOAD, false);
    }

    /**
     * 是否是夜间模式
     */
    public static void saveNightMode(boolean on){
        saveBoolean(NIGHT_MODE, on);
    }
    public static boolean isNight(){
        return getBoolean(NIGHT_MODE, false);
    }



}
