package me.geek.music.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络工具类
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class NetWorkUtils {

    /**
     * 是否有可用的网络
     */
    public static boolean isNetWorkAvailable(Context context){
        //网络连接管理类
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            NetworkInfo[] allNetWorkInfo = connectivityManager.getAllNetworkInfo();//获取设备支持的所有网络类型的链接状态信息。
            if(allNetWorkInfo != null){
                for(NetworkInfo networkInfo : allNetWorkInfo){
                    if(networkInfo.getState() == NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;

    }

    /**
     * 当前可用连接是否是手机数据
     */
    public static boolean isActiveNetWorkMobile(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            //获取当前连接可用的网络
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
                return true;
            }
        }
        return false;
    }

    /**
     * 当前可用网络是否是wifi
     */
    public static boolean isActiveNetWorkWifi(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                return true;
            }
        }
        return false;
    }
}
