package me.geek.music.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.List;

import me.geek.music.model.Music;

/**
 * 歌曲工具类
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class MusicUtils {

    /**
     * 扫描歌曲
     */
    public static void scanMusic(Context context, List<Music> musicList){
        musicList.clear();
        //通过系统提供的MediaStore内容提供器查询
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if(cursor == null){
            return;
        }
        if(cursor.moveToFirst()){
            do {
                //是否是音乐
                int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
                if (isMusic==0){
                    continue;
                }
                int id  = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String unknown = "未知";
                artist = artist.equals("<unkonwn>")? unknown : artist;
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));//歌曲文件路径
                long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String coverUri = getCoverUri(context, albumId);//专辑封面uri
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                long fileSize = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                String year = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));//发行日期
                Music music = new Music();
                music.setId(id);
                music.setAlbum(album);
                music.setType(Music.Type.LOCAL);
                music.setTitle(title);
                music.setArtist(artist);
                music.setDuration(duration);
                music.setUri(uri);
                music.setCoverUri(coverUri);
                music.setFileName(fileName);
                music.setFileSize(fileSize);
                music.setYear(year);
                musicList.add(music);
            }while (cursor.moveToNext());
        }
        cursor.close();

    }

    private static String getCoverUri(Context context, long albumId) {
        String uri = null;
        //通过AlbumId组合出专辑的Uri地址
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://media/external/audio/albums/" + albumId), new String[]{"album_art"}, null, null, null);
        if(cursor != null){
            cursor.moveToNext();//第一次调用moveToFirst或moveToNext都可以将cursor移动到第一条记录上。
            uri = cursor.getString(0);//返回cursor中第一列的值
            cursor.close();
        }
        CoverLoader.getInstance().loadThumbnail(uri);
        return uri;

    }
}
