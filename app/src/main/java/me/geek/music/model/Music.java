package me.geek.music.model;


import android.graphics.Bitmap;

/**
 * 单曲信息类
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class Music {
    //歌曲类型  来自本地还是来自网络
    private Type type;

    public enum Type {
        LOCAL,
        ONLINE
    }

    //[本地歌曲]歌曲id
    private long id;

    //歌名
    private String title;

    //歌手
    private String artist;

    //专辑
    private String album;

    //歌曲的总时长
    private long duration;

    //音乐路径
    private String uri;

    //[本地歌曲]专辑封面路径
    private String coverUri;

    //文件名
    private String fileName;

    //[网络歌曲]专辑封面bitmap
    private Bitmap cover;

    //文件大小
    private long fileSize;

    //发行日期
    private String year;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCoverUri() {
        return coverUri;
    }

    public void setCoverUri(String coverUri) {
        this.coverUri = coverUri;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    /**
     * 对比本地歌曲是否相同
     */
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Music)){
            return false;
        }
        return this.getId() == ((Music)obj).getId();
    }
}
