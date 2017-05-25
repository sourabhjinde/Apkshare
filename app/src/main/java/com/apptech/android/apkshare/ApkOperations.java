package com.apptech.android.apkshare;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

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

    public void shareApk(ArrayList<Uri> arrayListApkFilePath) {


        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        intent.setType("application/vnd.android.package-archive");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                arrayListApkFilePath);

        context.startActivity(Intent.createChooser(intent, "Share " +
                arrayListApkFilePath.size() + " Files Via"));
    }
}

