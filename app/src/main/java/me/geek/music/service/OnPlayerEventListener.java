package me.geek.music.service;

import me.geek.music.model.Music;

/**
 * 播放进度监听器
 * @Author Geek-Lizc(394925542@qq.com)
 */

public interface OnPlayerEventListener {
    /**
     * 更新进度
     */
    void onPublish(int progress);

    /**
     * 切换歌曲
     */
    void onChange(Music music);

    /**
     * 暂停播放
     */
    void onPlayerPause();

    /**
     * 继续播放
     */
    void onPlayerResume();

    /**
     * 更新定时停止播放时间
     */
    void onTimer(long remain);


}
