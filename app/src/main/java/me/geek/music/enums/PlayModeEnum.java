package me.geek.music.enums;

/**
 * @Author Geek-Lizc(394925542@qq.com)
 */

public enum PlayModeEnum {
    LOOP(0),//循环
    SHUFFLE(1),//随机
    ONE(2);//单曲循环

    private int value;

    PlayModeEnum(int value) {
        this.value = value;
    }

    public static PlayModeEnum valueOf(int value) {
        switch (value) {
            case 0:
                return LOOP;
            case 1:
                return SHUFFLE;
            case 2:
                return ONE;
            default:
                return LOOP;
        }
    }

    public int value() {
        return value;
    }
}
