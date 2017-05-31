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
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.Fragment;
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
    private static final String app_root = Environment.getExternalStorageDirectory()+"/AppShare";

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

}

class GetInstalledApps extends AsyncTask<Void,Void, ArrayList<AppInfo>>{

    ArrayList<AppInfo> apps;
    OnTaskCompletedListener listener;
   // boolean wantSystem;

    GetInstalledApps(){

    }

    GetInstalledApps(OnTaskCompletedListener onTaskCompletedListener/*, boolean wantSystem*/){
        this.listener = onTaskCompletedListener;
        //this.wantSystem = wantSystem;
    }

    @Override
    protected ArrayList<AppInfo> doInBackground(Void... params) {
        apps = new ArrayList<>() ;
        PackageManager packageManager = ((Fragment)listener).getActivity().getPackageManager();
        List<PackageInfo> packList = packageManager.getInstalledPackages(0);
        AppInfo appInfo;

      //  if(wantSystem) {
            for (int i = 0; i < packList.size(); i++) {
                PackageInfo packInfo = packList.get(i);
                    appInfo = setAppInfo(packInfo,packageManager);
                    appInfo.setInstalled(true);

                if (  (packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 )
                {
                     appInfo.setSytem(false);
                }else{
                     appInfo.setSytem(true);
                }
                apps.add(appInfo);

            }
        /*}else
        {
            for (int i = 0; i < packList.size(); i++) {
                PackageInfo packInfo = packList.get(i);

                if (  (packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 )
                {
                    appInfo = setAppInfo(packInfo,packageManager);
                    appInfo.setInstalled(true);
                    apps.add(appInfo);
                }
            }
        }*/
        return apps;
    }

    public static AppInfo setAppInfo(PackageInfo packInfo,PackageManager packageManager){
            AppInfo appInfo = new AppInfo();

            String filePath = packInfo.applicationInfo.publicSourceDir;
            String appName = packInfo.applicationInfo.loadLabel(packageManager).toString();
            Drawable appIcon = packInfo.applicationInfo.loadIcon(packageManager);
            String version = packInfo.versionName;
            String packageName = packInfo.packageName;
            appInfo.setAppImage(appIcon);
            appInfo.setAppName(appName);
            appInfo.setAppVersion(version);
            appInfo.setFilePath(filePath);
            appInfo.setPackageName(packageName);
            return appInfo;
    }


    @Override
    protected void onPostExecute(ArrayList<AppInfo> appInfos) {
        super.onPostExecute(appInfos);
        listener.onTaskCompleted(appInfos);
    }

    public static boolean isPackageInstalled(String packagename,Fragment fragment) {
        PackageManager packageManager = fragment.getActivity().getPackageManager();
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}

class GetArchivedFilesInfo extends AsyncTask<Void,Void,ArrayList<AppInfo>>{
    List<AppInfo> apps;
    OnTaskCompletedListener listener;
    private static final String app_root = Environment.getExternalStorageDirectory()+"/AppShare";

    public GetArchivedFilesInfo(){
    }

    public GetArchivedFilesInfo(OnTaskCompletedListener onTaskCompletedListener){
        this.listener = onTaskCompletedListener;
    }

    @Override
    protected ArrayList<AppInfo> doInBackground(Void... params) {
        File directory = new File(app_root);
        ArrayList<AppInfo> apps=
                new ArrayList<>();
        if(directory.isDirectory()) {
            File[] files = directory.listFiles();
            for (File f : files) {
                if (f.isFile() && f.getPath().endsWith(".apk")) {
                    PackageManager packageManager = ((Fragment)listener).getActivity().getPackageManager();
                    PackageInfo packageInfo = packageManager.getPackageArchiveInfo(f.getPath(),0);
                    if (Build.VERSION.SDK_INT >= 8) {
                        packageInfo.applicationInfo.sourceDir = f.getPath();
                        packageInfo.applicationInfo.publicSourceDir = f.getPath();
                    }

                    AppInfo appInfo =GetInstalledApps.setAppInfo(packageInfo,packageManager);
                    appInfo.setBackupedPath(f.getPath());
                    appInfo.setBackedUp(true);
                    appInfo.setFilePath(f.getPath());
                    apps.add(appInfo);
                }
            }
        }
        return apps;
    }

    public static boolean isArchivePresent(AppInfo app,Fragment fragment){
        File directory = new File(app_root);
        if(directory.isDirectory()) {
            File[] files = directory.listFiles();
            for (File f : files) {
                if (f.isFile() && f.getPath().endsWith(".apk")) {
                    PackageManager packageManager = fragment.getActivity().getPackageManager();
                    PackageInfo packageInfo = packageManager.getPackageArchiveInfo(f.getPath(),0);
                    if(packageInfo.packageName.equalsIgnoreCase(app.getPackageName())){
                        app.setBackedUp(true);
                        app.setBackupedPath(f.getPath());
                        return true;
                    }
                }
            }
        }
        app.setBackedUp(false);
        app.setBackupedPath(null);
        return false;
    }

    @Override
    protected void onPostExecute(ArrayList<AppInfo> appInfos) {
        super.onPostExecute(appInfos);
        listener.onTaskCompleted(appInfos);
    }
}

class DeleteArchivedFiles extends AsyncTask<Void,Integer,Void>{
    ArrayList<AppInfo> apps;
    Context context;
    ProgressDialog progressDialog;

    public DeleteArchivedFiles(){
    }

    public DeleteArchivedFiles(Context context, ArrayList<AppInfo> apps){
        this.context =context;
        this.apps = apps;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            int i=0;
            for (AppInfo appInfo : apps) {
                File file = new File(appInfo.getBackupedPath());
                if (file.exists()) {
                    file.delete();
                }
                appInfo.setBackedUp(false);
                appInfo.setBackupedPath(null);
                publishProgress(i*100/apps.size());
                i++;
            }
        }catch (Exception ex){

        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting Archive/s");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressDialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();
    }
}

class InstallArchiveFiles extends AsyncTask<Void,Integer,Void>{
    ArrayList<AppInfo> apps;
    Context context;
    ProgressDialog progressDialog;

    public InstallArchiveFiles(){
    }

    public InstallArchiveFiles(Context context, ArrayList<AppInfo> apps){
        this.context =context;
        this.apps = apps;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            int i=0;
            for (AppInfo appInfo : apps) {
                File file = new File(appInfo.getBackupedPath());
                if (file.exists()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                appInfo.setBackedUp(true);
                publishProgress(i*100/apps.size());
                i++;
            }
        }catch (Exception ex){

        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting Archive/s");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressDialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();
    }
}










