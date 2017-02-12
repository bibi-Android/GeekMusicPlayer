package me.geek.music.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Json返回数据的 本地歌曲在服务器上的信息
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class JsonSearchMusic {
    @SerializedName("song")
    private List<JSong> song;

    public List<JSong> getSong() {

        return song;
    }

    public void setSong(List<JSong> song) {

        this.song = song;
    }

    public static class JSong {
        @SerializedName("songname")
        private String songname;
        @SerializedName("artistname")
        private String artistname;
        @SerializedName("songid")
        private String songid;

        public String getSongname() {
            return songname;
        }

        public void setSongname(String songname) {
            this.songname = songname;
        }

        public String getArtistname() {
            return artistname;
        }

        public void setArtistname(String artistname) {
            this.artistname = artistname;
        }

        public String getSongid() {
            return songid;
        }

        public void setSongid(String songid) {
            this.songid = songid;
        }
    }
}
