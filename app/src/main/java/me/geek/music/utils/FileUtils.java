package me.geek.music.utils;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.geek.music.application.AppCache;
import me.geek.music.constants.Constants;
import me.geek.music.model.Music;

/**
 * 文件工具类,定义文件名和获取文件存放路径
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class FileUtils {

    /**
     * 获取App在外置存储中的位置
     */
    private static String getAppDir(){
        return Environment.getExternalStorageDirectory() + "/GeekMusic";
    }

    /**
     * 判断是否存在文件夹,如果不存在就创建,存在就返回地址
     */
    private static String mkdirs(String dir){
        File file = new File(dir);
        if(!file.exists()){
            file.mkdirs();
        }
        return dir;
    }

    /**
     * 存放音乐的文件夹
     */
    public static String getMusicDir(){
        String dir = getAppDir() + "Music/";
        return mkdirs(dir);
    }

    /**
     * 存放歌词的文件夹
     */
    public static String getLrcDir(){
        String dir = getAppDir() + "Lyric/";
        return mkdirs(dir);
    }

    /**
     * 获取启动页存放的文件夹
     */
    public static String getSplashDir(Context context){
        String dir = context.getFilesDir() + "/splash/";//data/data/me.geek.music/splash
        return mkdirs(dir);
    }

    /**
     * 在线歌曲下载存放文件夹
     */
    public static String getRelativeMusicDir(){
        String dir = "GeekMusic/Music";
        return mkdirs(dir);
    }


    /**
     * 获取歌词路径
     * 首先从已下载的歌词中查找,如果不存在,再通过歌曲文件所在文件夹查找
     */

    public static String getLrcFilePath(Music music){
        String lrcFilePath = getLrcDir() + music.getFileName().replace(Constants.FILENAME_MP3, Constants.FILENAME_LRC);//将音乐文件.mp3改成.lrc
        File file = new File(lrcFilePath);
        if(!file.exists()){
            lrcFilePath = music.getUri().replace(Constants.FILENAME_MP3, Constants.FILENAME_LRC);

        }
        return lrcFilePath;
    }


    /**
     * 获取MP3文件名
     */
    public static String getMp3FileName(String artist, String title){
        artist = stringFilter(artist);
        title = stringFilter(title);
        if(TextUtils.isEmpty(artist)){
            artist = "未知";
        }
        if(TextUtils.isEmpty(title)){
            title = "未知";
        }
        return artist + "-" + title +Constants.FILENAME_MP3;
    }
    /**
     * 获取歌词文件名
     */
    public static String getLrcFileName(String artist, String title){
        artist = stringFilter(artist);
        title = stringFilter(title);
        if(TextUtils.isEmpty(artist)){
            artist = "未知";
        }
        if(TextUtils.isEmpty(title)){
            title = "未知";
        }
        return artist + "-" + title +Constants.FILENAME_LRC;
    }

    /**
     * 获取歌手和专辑
     */
    public static String getArtistAndAlbum(String artist, String album){
        if(TextUtils.isEmpty(artist) && TextUtils.isEmpty(album)){
            return "";
        }else if (!TextUtils.isEmpty(artist) && TextUtils.isEmpty(album)){
            return artist;
        }else if (TextUtils.isEmpty(artist) && !TextUtils.isEmpty(album)){
            return album;
        }
        return artist + "-" + album;
    }

    /**
     * 过滤特殊字符(\/:*?"<>|)
     */
    private static String stringFilter(String str){
        if(str == null){
            return null;
        }
        String regEx ="[\\/:*?\"<>|]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();//删除空格
    }

    /**
     * 保存歌词
     */
    public static void saveLrcFile(String path, String content){
        try{
            FileWriter fw = new FileWriter(path);
            fw.flush();
            fw.write(content);
            fw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 下载在线歌曲,使用Android的DownloadManager.Request
     */

    public static long downloadMusic(String url, String artist, String song){
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        String mp3FileName = FileUtils.getMp3FileName(artist, song);
        request.setDestinationInExternalPublicDir(FileUtils.getRelativeMusicDir(), mp3FileName);//设置下载文件存放路径
        request.setMimeType(MimeTypeMap.getFileExtensionFromUrl(url));//设置MimeType用于响应点击下载完后响应的事件
        request.allowScanningByMediaScanner();//表示允许MediaScanner扫描到这个文件，默认不允许
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);//设置允许下载的数据类型
        request.setAllowedOverRoaming(false);//移动网络情况下是否允许漫游,不允许
        DownloadManager downloadManager = (DownloadManager) AppCache.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        return downloadManager.enqueue(request);

    }
    public static float b2mb(int b) {
        String mb = String.format(Locale.getDefault(), "%.2f", b / 1024f / 1024);
        return Float.valueOf(mb);
    }




}
