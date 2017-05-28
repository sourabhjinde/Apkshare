package com.apptech.android.apkshare;

import android.graphics.drawable.Drawable;

import java.util.Comparator;

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
    boolean isSytem;


    public AppInfo(Drawable appImage, String appName, String appVersion, boolean isSelected, String filePath, String packageName,
                   boolean isBackedUp, boolean isInstalled, String backupedPath,boolean isSytem) {
        this.appImage = appImage;
        this.appName = appName;
        this.appVersion = appVersion;
        this.isSelected = isSelected;
        this.filePath = filePath;
        this.packageName = packageName;
        this.isBackedUp = isBackedUp;
        this.isInstalled = isInstalled;
        this.backupedPath = backupedPath;
        this.isSytem = isSytem;
    }

    public AppInfo() {

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

    public boolean isSytem() {
        return isSytem;
    }

    public void setSytem(boolean sytem) {
        isSytem = sytem;
    }
}

 class PackageComparator implements Comparator{

    @Override
    public int compare(Object o1, Object o2) {
        AppInfo appInfo_1 = (AppInfo)o1;
        AppInfo appInfo_2 =(AppInfo)o2;
        if(appInfo_1.packageName==appInfo_1.packageName)
            return 0;
        else
            return -1;
    }
}
