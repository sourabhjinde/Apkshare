package com.apptech.android.apkshare;

import android.graphics.drawable.Drawable;

/**
 * Created by S on 04/05/2017.
 */

public class AppInfo {

    Drawable appImage;
    String appName;
    String appVersion;
    boolean isSelected;
    String filePath;

    public AppInfo(Drawable appImage, String appName, String appVersion, boolean isSelected, String filePath) {
        this.appImage = appImage;
        this.appName = appName;
        this.appVersion = appVersion;
        this.isSelected = isSelected;
        this.filePath = filePath;
    }

    public AppInfo() {

    }

    public AppInfo(Drawable appImage, String appName, String appVersion, String filePath) {
        this.appImage = appImage;
        this.appName = appName;
        this.appVersion = appVersion;
        this.filePath = filePath;
    }

    public Drawable getAppImage() {
        return appImage;
    }

    public void setAppImage(Drawable appImage) {
        this.appImage = appImage;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
