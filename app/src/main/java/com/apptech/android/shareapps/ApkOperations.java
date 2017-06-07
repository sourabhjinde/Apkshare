package com.apptech.android.shareapps;


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
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by S on 25/05/2017.
 */

public class ApkOperations extends AsyncTask<File, Integer, Void> {

    NumberProgressBar progressBar;
    static final String app_root = Environment.getExternalStorageDirectory() + "/AppShare";
    AlertDialog dialog;
    Context context;
    List<AppInfo> apps;
    String storageFolder;

    ApkOperations(Context context, List<AppInfo> apps) {
        this.context = context;
        this.apps = apps;
    }

    @Override
    protected Void doInBackground(File... params) {

        File file;
        File copiedFile;
        try {
            storageFolder = MainActivity.getStorageFolder();
            copiedFile = new File(storageFolder);
            copiedFile.mkdir();
            int i = 1;
            for (AppInfo app : apps) {
                file = new File(app.getFilePath());
                if (file.exists()) {
                    copiedFile = new File(storageFolder + "/" + app.getAppName() + ".apk");
                    copiedFile.createNewFile();
                    copyFile(file, copiedFile);
                    publishProgress(i * 100 / apps.size());
                    i++;
                }
            }

        } catch (FileNotFoundException ex) {
        } catch (IOException e) {
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        dialog = ((MainActivity) context).extractAlertDialog;
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        progressBar = (NumberProgressBar) ((MainActivity) context).extractAlertDialog.findViewById(R.id.number_progress_bar);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        Toast.makeText(context, "Done successfully. Saved at " + storageFolder, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onProgressUpdate(Integer... val) {
        progressBar.setProgress(val[0]);

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

        ArrayList<Uri> arrayListApkFilePath = getUris();

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        intent.setType("*/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                arrayListApkFilePath);

        context.startActivity(Intent.createChooser(intent, "Share " +
                arrayListApkFilePath.size() + " Files Via"));
    }

    public ArrayList<Uri> getUris() {
        ArrayList<Uri> arrayListApkFilePath = new ArrayList<>();
        for (AppInfo app : apps) {
            arrayListApkFilePath.add(Uri.fromFile(new File(app.filePath)));
        }

        return arrayListApkFilePath;
    }

}

class GetInstalledApps extends AsyncTask<Void, Void, ArrayList<AppInfo>> {

    ArrayList<AppInfo> apps;
    OnTaskCompletedListener listener;
    boolean wantSystem;
    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final long MiB = 1024 * 1024;
    private static final long KiB = 1024;

    GetInstalledApps() {

    }

    GetInstalledApps(OnTaskCompletedListener onTaskCompletedListener, boolean wantSystem) {
        this.listener = onTaskCompletedListener;
        this.wantSystem = wantSystem;
    }

    @Override
    protected ArrayList<AppInfo> doInBackground(Void... params) {
        apps = new ArrayList<>();
        PackageManager packageManager = ((Fragment) listener).getActivity().getPackageManager();
        List<PackageInfo> packList = packageManager.getInstalledPackages(0);
        AppInfo appInfo;

        if (wantSystem) {
            for (int i = 0; i < packList.size(); i++) {
                PackageInfo packInfo = packList.get(i);
                appInfo = setAppInfo(packInfo, packageManager);
                appInfo.setInstalled(true);

                if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    appInfo.setSytem(false);
                } else {
                    appInfo.setSytem(true);
                }
                apps.add(appInfo);

            }
        } else {
            for (int i = 0; i < packList.size(); i++) {
                PackageInfo packInfo = packList.get(i);

                if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    appInfo = setAppInfo(packInfo, packageManager);
                    appInfo.setInstalled(true);
                    apps.add(appInfo);
                }
            }
        }
        return apps;
    }

    public static AppInfo setAppInfo(PackageInfo packInfo, PackageManager packageManager) {
        AppInfo appInfo = new AppInfo();
        try {
            String filePath = packInfo.applicationInfo.publicSourceDir;
            String appName = packInfo.applicationInfo.loadLabel(packageManager).toString();
            Drawable appIcon = packInfo.applicationInfo.loadIcon(packageManager);
            String version = packInfo.versionName;
            String packageName = packInfo.packageName;
            String installDate = new SimpleDateFormat("yyyy/mm/dd").format(new Date(packInfo.firstInstallTime));
            appInfo.setAppImage(appIcon);
            appInfo.setAppName(appName);
            appInfo.setAppVersion(version);
            appInfo.setFilePath(filePath);
            appInfo.setPackageName(packageName);
            appInfo.setDate(installDate);
            appInfo.setSize(getFileSize(new File(filePath)));

        } catch (Exception e) {
        }
        return appInfo;
    }


    @Override
    protected void onPostExecute(ArrayList<AppInfo> appInfos) {
        super.onPostExecute(appInfos);
        listener.onTaskCompleted(appInfos);
    }

    public static boolean isPackageInstalled(String packagename, Fragment fragment) {
        PackageManager packageManager = fragment.getActivity().getPackageManager();
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String getFileSize(File file) {

        if (!file.isFile()) {
            throw new IllegalArgumentException("Expected a file");
        }
        final double length = file.length();

        if (length > MiB) {
            return format.format(length / MiB) + " MB";
        }
        if (length > KiB) {
            return format.format(length / KiB) + " KB";
        }
        return format.format(length) + " B";
    }
}

class GetArchivedFilesInfo extends AsyncTask<Void, Void, ArrayList<AppInfo>> {

    OnTaskCompletedListener listener;
    String storageFolder;

    public GetArchivedFilesInfo(OnTaskCompletedListener onTaskCompletedListener) {
        this.listener = onTaskCompletedListener;
    }

    @Override
    protected ArrayList<AppInfo> doInBackground(Void... params) {

        ArrayList<AppInfo> apps =
                new ArrayList<>();
        try {
            storageFolder = MainActivity.getStorageFolder();
            File directory = new File(storageFolder);

            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                for (File f : files) {
                    if (f.isFile() && f.getPath().endsWith(".apk")) {
                        PackageManager packageManager = ((Fragment) listener).getActivity().getPackageManager();
                        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(f.getPath(), 0);
                        if (Build.VERSION.SDK_INT >= 8) {
                            packageInfo.applicationInfo.sourceDir = f.getPath();
                            packageInfo.applicationInfo.publicSourceDir = f.getPath();
                        }

                        AppInfo appInfo = GetInstalledApps.setAppInfo(packageInfo, packageManager);
                        appInfo.setBackupedPath(f.getPath());
                        appInfo.setBackedUp(true);
                        appInfo.setFilePath(f.getPath());
                        apps.add(appInfo);
                    }
                }
            }
        } catch (Exception ex) {

        }
        return apps;
    }

    public static boolean isArchivePresent(AppInfo app, Fragment fragment) {
        try {
            File directory = new File(MainActivity.getStorageFolder());
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                for (File f : files) {
                    if (f.isFile() && f.getPath().endsWith(".apk")) {
                        PackageManager packageManager = fragment.getActivity().getPackageManager();
                        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(f.getPath(), 0);
                        if (packageInfo.packageName.equalsIgnoreCase(app.getPackageName())) {
                            app.setBackedUp(true);
                            app.setBackupedPath(f.getPath());
                            return true;
                        }
                    }
                }
            }
            app.setBackedUp(false);
            app.setBackupedPath(null);
        } catch (Exception ex) {

        }

        return false;
    }

    @Override
    protected void onPostExecute(ArrayList<AppInfo> appInfos) {
        super.onPostExecute(appInfos);
        listener.onTaskCompleted(appInfos);
    }

    public static boolean createAppDirectory(Context context) {
        try {
            String state = Environment.getExternalStorageState();
            if (!state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                return false;
            } else {
                File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "AppShare");
                boolean success = true;
                if (!directory.exists()) {
                    success = directory.mkdir();
                }

                return success;
            }
        } catch (Exception ex) {
            return false;
        }
    }
}

class DeleteArchivedFiles extends AsyncTask<Void, Integer, Void> {
    ArrayList<AppInfo> apps;
    Context context;
    ProgressDialog progressDialog;

    public DeleteArchivedFiles(Context context, ArrayList<AppInfo> apps) {
        this.context = context;
        this.apps = apps;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            int i = 0;
            for (AppInfo appInfo : apps) {
                File file = new File(appInfo.getBackupedPath());
                if (file.exists()) {
                    file.delete();
                }
                appInfo.setBackedUp(false);
                appInfo.setBackupedPath(null);
                publishProgress(i * 100 / apps.size());
                i++;
            }
        } catch (Exception ex) {

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

class InstallArchiveFiles extends AsyncTask<Void, Integer, Void> {
    ArrayList<AppInfo> apps;
    Context context;
    ProgressDialog progressDialog;

    public InstallArchiveFiles(Context context, ArrayList<AppInfo> apps) {
        this.context = context;
        this.apps = apps;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            int i = 0;
            for (AppInfo appInfo : apps) {
                File file = new File(appInfo.getBackupedPath());
                if (file.exists()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                appInfo.setBackedUp(true);
                publishProgress(i * 100 / apps.size());
                i++;
            }
        } catch (Exception ex) {

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

class MoveArchiveFiles extends AsyncTask<Void, Integer, Void> {
    OnMoveFilesCompleteListener onMoveFilesCompleteListener;
    ProgressDialog progressDialog;
    String oldFolderPath, newFolderPath;

    public MoveArchiveFiles(OnMoveFilesCompleteListener onMoveFilesCompleteListener, String oldFolderPath, String newFolderPath) {
        this.onMoveFilesCompleteListener = onMoveFilesCompleteListener;
        this.oldFolderPath = oldFolderPath;
        this.newFolderPath = newFolderPath;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            File oldFolder = new File(oldFolderPath);
            File newFolder = new File(newFolderPath);
            if (oldFolder.exists() && oldFolder.isDirectory()) {
                File[] files = oldFolder.listFiles();
                int i = 0;
                for (File file : files) {
                    if (!file.isDirectory()) {
                        moveFile(file, newFolder);
                    }
                    publishProgress(i * 100 / files.length);
                    i++;
                }
            }
        } catch (Exception ex) {

        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(((Fragment) onMoveFilesCompleteListener).getActivity());
        progressDialog.setMessage("Moving Archive/s from old folder to new folder");
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
        onMoveFilesCompleteListener.onMoveFilesComplete();
        progressDialog.dismiss();
    }

    private void moveFile(File file, File dir) throws IOException {
        File newFile = new File(dir, file.getName());
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            file.delete();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

    }
}





