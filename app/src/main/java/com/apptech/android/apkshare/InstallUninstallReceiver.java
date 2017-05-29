package com.apptech.android.apkshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by S on 26/05/2017.
 */

public class InstallUninstallReceiver extends BroadcastReceiver {

    static OnInstallUninstallListener onInstallUninstallListener,onInstallUninstallListener2;
    public InstallUninstallReceiver(){

    }

    public void setOnInstallUninstallListener(OnInstallUninstallListener onInstallUninstallListener ){
        if(onInstallUninstallListener instanceof AppListFragment) {
            this.onInstallUninstallListener = onInstallUninstallListener;
        }
        else if(onInstallUninstallListener instanceof ArchivedFragment){
            this.onInstallUninstallListener2 = onInstallUninstallListener;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(onInstallUninstallListener != null) {
            onInstallUninstallListener.onInstallUninstall(intent);
        }
        if(onInstallUninstallListener2 != null){
            onInstallUninstallListener2.onInstallUninstall(intent);
        }
    }
}
