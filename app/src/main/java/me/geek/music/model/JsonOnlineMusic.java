package me.geek.music.model;

import com.google.gson.annotations.SerializedName;

/**
 * 返回的Json信息,"song_list"后的信息
 * 可以尝试一下API进行查看Json信息
 * http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.billboard.billList&type=23&size=3
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class JsonOnlineMusic {

    @SerializedName("pic_pig")
    private String pic_big;//歌曲封面大图
    @SerializedName("pic_small")//歌曲封面小图
    private String pic_small;
    @SerializedName("lrclink")
    private String lrclink;
    @SerializedName("song_id")
    private String song_id;
    @SerializedName("title")
    private String title;
    @SerializedName("ting_uid")
    private String ting_uid;
    @SerializedName("album_title")
    private String album_title;
    @SerializedName("artist_name")
    private String artist_name;

    public String getAlbum_title() {
        return album_title;
    }

    public void setAlbum_title(String album_title) {
        this.album_title = album_title;
    }

    public String getPic_big() {
        return pic_big;
    }

    public void setPic_big(String pic_big) {
        this.pic_big = pic_big;
    }

    public String getPic_small() {
        return pic_small;
    }

    public void setPic_small(String pic_small) {
        this.pic_small = pic_small;
    }

    public String getLrclink() {
        return lrclink;
    }

    public void setLrclink(String lrclink) {
        this.lrclink = lrclink;
    }

    public String getSong_id() {
        return song_id;
    }

    public void setSong_id(String song_id) {
        this.song_id = song_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTing_uid() {
        return ting_uid;
    }

    public void setTing_uid(String ting_uid) {
        this.ting_uid = ting_uid;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }
}
