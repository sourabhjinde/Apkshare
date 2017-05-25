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
    String packageName;
    boolean isBackedUp;
    boolean isInstalled;
    String backupedPath;


    public AppInfo(Drawable appImage, String appName, String appVersion, boolean isSelected, String filePath, String packageName,
                   boolean isBackedUp, boolean isInstalled, String backupedPath) {
        this.appImage = appImage;
        this.appName = appName;
        this.appVersion = appVersion;
        this.isSelected = isSelected;
        this.filePath = filePath;
        this.packageName = packageName;
        this.isBackedUp = isBackedUp;
        this.isInstalled = isInstalled;
        this.backupedPath = backupedPath;

    }

    public AppInfo() {

    }

  /*  public AppInfo(Drawable appImage, String appName, String appVersion, String filePath) {
        this.appImage = appImage;
        this.appName = appName;
        this.appVersion = appVersion;
        this.filePath = filePath;
    }
*/
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isBackedUp() {
        return isBackedUp;
    }

    public void setBackedUp(boolean backedUp) {
        isBackedUp = backedUp;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public void setInstalled(boolean installed) {
        isInstalled = installed;
    }

    public String getBackupedPath() {
        return backupedPath;
    }

    public void setBackupedPath(String backupedPath) {
        this.backupedPath = backupedPath;
    }
}
