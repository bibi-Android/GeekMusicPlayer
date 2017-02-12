package me.geek.music.utils.permission;

public interface PermissionResult {
    void onGranted();

    void onDenied();
}
