package com.apptech.android.apkshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by S on 26/05/2017.
 */

public class InstallUninstallReceiver extends BroadcastReceiver {

    static OnInstallUninstallListener onInstallUninstallListener;
    public InstallUninstallReceiver(){

    }

    public void setOnInstallUninstallListener(OnInstallUninstallListener onInstallUninstallListener ){
        this.onInstallUninstallListener=onInstallUninstallListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        onInstallUninstallListener.onInstallUninstall(intent);
    }
}
