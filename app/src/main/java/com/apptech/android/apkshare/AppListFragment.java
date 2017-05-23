package com.apptech.android.apkshare;


import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by S on 03/05/2017.
 */

public class AppListFragment extends Fragment implements SearchView.OnQueryTextListener,OnTextViewClickListener{

    //convert array to list
    List<AppInfo> appslist = new ArrayList<AppInfo>() ;
    RecycleViewAdapter adapter;
    RecyclerView rv;
    private static final String root = Environment.getExternalStorageDirectory().toString();
    private static final String app_root = "/sdcard/APPSHARE";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.applistlayout, container, false);
        rv = (RecyclerView) view.findViewById(R.id.id_recycleview);

        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        GetInstalledAppList();
        adapter = new RecycleViewAdapter(appslist);
        rv.setAdapter(adapter);

    }

    void GetInstalledAppList()
    {
        PackageManager packageManager = getActivity().getPackageManager();
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
                appInfo.setAppImage(appIcon);
                appInfo.setAppName(appName);
                appInfo.setAppVersion(version);
                appInfo.setFilePath(filePath);
                appslist.add(appInfo);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        MenuItemCompat.setOnActionExpandListener(item,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when collapsed
                        adapter.getFilter().filter("");
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // Do something when expanded
                        return true; // Return true to expand action view
                    }
                });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return true;
    }

    @Override
    public void onTextViewClick(View v) {
        new AsyncTaskRunner().execute();
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

    private class AsyncTaskRunner extends AsyncTask<File, Integer, Void> {

        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(File... params) {
            List<AppInfo> mCheckedAppList =adapter.getmCheckedAppList();
            File file;
            File copiedFile;
            try {
                copiedFile = new File(app_root);
                copiedFile.mkdir();
                int i=1;
                for (AppInfo app : mCheckedAppList) {
                    file = new File(app.getFilePath());
                    if (file.exists()) {
                        copiedFile = new File(app_root+"/"+app.getAppName()+".apk");
                        copiedFile.createNewFile();
                        copyFile(file,copiedFile);
                        publishProgress(i*100/mCheckedAppList.size());
                        i++;
                    }
                }

            }catch(FileNotFoundException ex){
                System.out.println(ex.getMessage() + " in the specified directory.");
            }catch(IOException e){
                System.out.println(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
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
    }
}
