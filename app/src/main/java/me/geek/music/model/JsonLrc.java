package me.geek.music.model;

import com.google.gson.annotations.SerializedName;

/**
 * @Author Geek-Lizc(394925542@qq.com)
 */

public class JsonLrc {
    @SerializedName("lrcContent")
    private String lrcContent;

    public String getLrcContent() {
        return lrcContent;
    }

    public void setLrcContent(String lrcContent) {
        this.lrcContent = lrcContent;
    }
}
