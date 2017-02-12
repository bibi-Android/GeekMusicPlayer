package me.geek.music.model;

import com.google.gson.annotations.SerializedName;

/**
 * 启动画面
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class JsonSplash {

    @SerializedName("text")
    private String text;

    @SerializedName("img")
    private String img;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
