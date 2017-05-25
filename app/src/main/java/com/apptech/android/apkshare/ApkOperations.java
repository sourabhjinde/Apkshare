package com.apptech.android.apkshare;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by S on 25/05/2017.
 */

public class ApkOperations extends AsyncTask<File, Integer, Void> {

    private String resp;
    ProgressDialog progressDialog;
    private static final String app_root = "/sdcard/APPSHARE";
    Context context;
    List<AppInfo> apps;

    ApkOperations(Context context, List<AppInfo> apps) {
        this.context = context;
        this.apps = apps;
    }

    ApkOperations(Context context)
    {
        this.context = context;
    }
    @Override
    protected Void doInBackground(File... params) {

        File file;
        File copiedFile;
        try {
            copiedFile = new File(app_root);
            copiedFile.mkdir();
            int i = 1;
            for (AppInfo app : apps) {
                file = new File(app.getFilePath());
                if (file.exists()) {
                    copiedFile = new File(app_root + "/" + app.getAppName() + ".apk");
                    copiedFile.createNewFile();
                    copyFile(file, copiedFile);
                    publishProgress(i * 100 / apps.size());
                    i++;
                }
            }

        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage() + " in the specified directory.");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Extracting Apk");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        progressDialog.dismiss();
        Toast.makeText(context,"Done successfully. Saved at " + app_root,Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onProgressUpdate(Integer... val) {
        progressDialog.setProgress(val[0]);

    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public void shareApk() {

        ArrayList<Uri> arrayListApkFilePath= getUris();

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        intent.setType("*/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                arrayListApkFilePath);

        context.startActivity(Intent.createChooser(intent, "Share " +
                arrayListApkFilePath.size() + " Files Via"));
    }

    public ArrayList<Uri> getUris()
    {
        ArrayList<Uri> arrayListApkFilePath= new ArrayList<>();
        for(AppInfo app : apps)
        {
            arrayListApkFilePath.add(Uri.fromFile(new File(app.filePath)));
        }

        return arrayListApkFilePath;
    }

    List<AppInfo> getInstalledAppList()
    {
        apps = new ArrayList<AppInfo>() ;
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packList = packageManager.getInstalledPackages(0);
        AppInfo appInfo;
        for (int i=0; i < packList.size(); i++)
        {
            appInfo = new AppInfo();
            PackageInfo packInfo = packList.get(i);
            if (  (packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
            {
                String filePath =packInfo.applicationInfo.publicSourceDir;
                String appName = packInfo.applicationInfo.loadLabel(packageManager).toString();
                Drawable appIcon = packInfo.applicationInfo.loadIcon(packageManager);
                String version = packInfo.versionName;
                String packageName = packInfo.packageName;
                appInfo.setAppImage(appIcon);
                appInfo.setAppName(appName);
                appInfo.setAppVersion(version);
                appInfo.setFilePath(filePath);
                appInfo.setPackageName(packageName);
                appInfo.setInstalled(true);
                apps.add(appInfo);
            }
        }
        return apps;
    }

}

