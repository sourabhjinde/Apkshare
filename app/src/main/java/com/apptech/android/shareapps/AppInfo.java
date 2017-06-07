package com.apptech.android.shareapps;

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
    boolean isSytem;
    String date;
    String size;

    public AppInfo(Drawable appImage, String appName, String appVersion, boolean isSelected, String filePath, String packageName, boolean isBackedUp, boolean isInstalled, String backupedPath, boolean isSytem, String date, String size) {
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
        this.date = date;
        this.size = size;
    }

    public AppInfo() {

    }

    public Drawable getAppImage() {
        return appImage;
    }

    public void setAppImage(Drawable appImage) {
        this.appImage = appImage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
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

    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof AppInfo)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        AppInfo c = (AppInfo) o;

        // Compare the data members and return accordingly
        return this.packageName.equalsIgnoreCase(c.getPackageName()) && this.getAppVersion().equalsIgnoreCase(c.getAppVersion());
    }
}
