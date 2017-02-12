package me.geek.music.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Gson处理api返回的歌单列表以及图标等信息的对象
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class JsonOnlineMusicList {
    @SerializedName("song_list")
    private List<JsonOnlineMusic> song_list;
    @SerializedName("billboard")
    private JsonBillboard billboard;

    public List<JsonOnlineMusic> getSong_list() {
        return song_list;
    }

    public void setSong_list(List<JsonOnlineMusic> song_list) {
        this.song_list = song_list;
    }

    public JsonBillboard getBillboard() {
        return billboard;
    }

    public void setBillboard(JsonBillboard billboard) {
        this.billboard = billboard;
    }

    /**
     * 大类下的小分类
     * 例如分类榜单下的:华语金曲榜,欧美金曲榜,影视金曲榜,情歌对唱榜,网络歌曲榜等等..
     */

    public static class JsonBillboard{
        @SerializedName("update_date")
        private String update_date;//歌单更新时间
        @SerializedName("name")
        private String name;//情歌对唱榜
        @SerializedName("comment")
        private String comment;//实时展现百度音乐最热门对唱歌曲排行
        @SerializedName("pic_s640")
        private String pic_s640;//情歌对唱榜点进去以后的大图,http://b.hiphotos.baidu.com//ting//pic//item//5bafa40f4bfbfbed8289cb8a7af0f736aec31f76.jpg
        @SerializedName("pic_s444")
        private String pic_s444;//放在最外面第一个显示的图,长方形图,http://c.hiphotos.baidu.com//ting//pic//item//f7246b600c33874400bd477a530fd9f9d72aa0b8.jpg
        @SerializedName("pic_s260")
        private String pic_s260;//放在最外面第一个显示的图,正方形,http://a.hiphotos.baidu.com//ting//pic//item//4610b912c8fcc3cea8b9a1359045d688d43f20be.jpg
        @SerializedName("pic_s210")
        private String pic_s210;//http://business.cdn.qianqian.com//qianqian//pic//bos_client_df3de3c8b3074ca0b9d6ca6a702f3226.jpg

        public String getUpdate_date() {
            return update_date;
        }

        public void setUpdate_date(String update_date) {
            this.update_date = update_date;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getPic_s640() {
            return pic_s640;
        }

        public void setPic_s640(String pic_s640) {
            this.pic_s640 = pic_s640;
        }

        public String getPic_s444() {
            return pic_s444;
        }

        public void setPic_s444(String pic_s444) {
            this.pic_s444 = pic_s444;
        }

        public String getPic_s260() {
            return pic_s260;
        }

        public void setPic_s260(String pic_s260) {
            this.pic_s260 = pic_s260;
        }

        public String getPic_s210() {
            return pic_s210;
        }

        public void setPic_s210(String pic_s210) {
            this.pic_s210 = pic_s210;
        }
    }

}
