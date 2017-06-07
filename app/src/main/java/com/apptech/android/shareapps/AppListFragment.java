package com.apptech.android.shareapps;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;

/**
 * Created by S on 03/05/2017.
 */

public class AppListFragment extends Fragment implements SearchView.OnQueryTextListener, OnTextViewClickListener, OnTaskCompletedListener, OnInstallUninstallListener {

    //convert array to list
    ArrayList<AppInfo> appslist = new ArrayList<AppInfo>();
    ArrayList<AppInfo> archivedList;
    RecycleViewAdapter adapter;
    RecyclerView rv;
    ProgressBar progressBar;
    boolean WantSystem = false;
    SearchView searchView;
    TextView emptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.applistlayout, container, false);
        rv = (RecyclerView) view.findViewById(R.id.id_recycleview);

        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));

        progressBar = (ProgressBar) view.findViewById(R.id.pbHeaderProgress);
        emptyView = (TextView) view.findViewById(R.id.empty_view);
        new InstallUninstallReceiver().setOnInstallUninstallListener(this);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        new GetInstalledApps(this, false).execute();
        adapter = new RecycleViewAdapter(appslist, this);
        rv.setAdapter(adapter);
        if (appslist.isEmpty()) {
            rv.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
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
        final MenuItem delete = menu.findItem(R.id.delete);
        delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ArrayList<AppInfo> mCheckedList = adapter.getmCheckedAppList();
                if (!mCheckedList.isEmpty()) {
                    for (AppInfo appInfo : adapter.getmCheckedAppList()) {
                        Intent intent;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
                        } else {
                            intent = new Intent(Intent.ACTION_DELETE);
                        }
                        intent.setData(Uri.parse("package:" + appInfo.getPackageName()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                        startActivityForResult(intent, 1);
                    }
                } else {
                    Toast.makeText(AppListFragment.this.getActivity(), "Please select an app first", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });

        final MenuItem showSystemApps = menu.findItem(R.id.action_showSystemApps);
        showSystemApps.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                rv.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                WantSystem = true;
                new GetInstalledApps(AppListFragment.this, true).execute();
                WantSystem = true;
                return false;
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
            } else if (resultCode == RESULT_CANCELED) {
            } else if (resultCode == RESULT_FIRST_USER) {
            }
        }
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

        int id = v.getId();
        ArrayList<AppInfo> mCheckedAppList;
        switch (id) {
            case R.id.tvBackup:
                mCheckedAppList = adapter.getmCheckedAppList();
                if (!mCheckedAppList.isEmpty()) {
                    new ApkOperations(this.getActivity(), mCheckedAppList).execute();
                } else {
                    Toast.makeText(this.getActivity(), "Please select an app first", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.check:
                mCheckedAppList = adapter.getmCheckedAppList();
                if (mCheckedAppList.size() == appslist.size()) {
                    for (AppInfo app : appslist) {
                        app.setSelected(false);
                    }
                } else {
                    for (AppInfo app : appslist) {
                        app.setSelected(true);
                    }
                }
                adapter.notifyDataSetChanged();

                break;

            case R.id.tvsend:
                mCheckedAppList = adapter.getmCheckedAppList();
                if (!mCheckedAppList.isEmpty()) {
                    new ApkOperations(this.getActivity(), adapter.getmCheckedAppList()).shareApk();
                } else {
                    Toast.makeText(this.getActivity(), "Please select an app first", Toast.LENGTH_LONG).show();
                }

                break;

            default:
                break;
        }
    }

    @Override
    public void onTaskCompleted(ArrayList<AppInfo> apps) {
        progressBar.setVisibility(View.GONE);
        rv.setVisibility(View.VISIBLE);
        appslist.clear();

        appslist.addAll(apps);
        if (WantSystem == true) {
            setArchivedPackages(archivedList);
        } else {
            adapter.notifyDataSetChanged();
        }
        if (appslist.isEmpty()) {
            rv.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rv.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onInstallUninstall(Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        if (intent.getAction() == Intent.ACTION_PACKAGE_REMOVED) {
            for (AppInfo app : appslist) {
                if (packageName.equalsIgnoreCase(app.packageName)) {
                    app.setInstalled(false);
                    appslist.remove(app);
                    break;
                }
            }
        } else if (intent.getAction() == Intent.ACTION_PACKAGE_ADDED) {
            try {
                PackageManager packageManager = getActivity().getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                AppInfo appInfo = new GetInstalledApps().setAppInfo(packageInfo, packageManager);
                appInfo.setInstalled(true);
                GetArchivedFilesInfo.isArchivePresent(appInfo, this);
                appslist.add(appInfo);

            } catch (PackageManager.NameNotFoundException ex) {
            }
        }

        if (searchView != null) {
            adapter.getFilter().filter(searchView.getQuery());
        } else {
            adapter.notifyDataSetChanged();
        }
        if (appslist.isEmpty()) {
            rv.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rv.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    public void setArchivedPackages(ArrayList<AppInfo> archivedApps) {
        archivedList = archivedApps;
        for (AppInfo archivedApp : archivedApps) {
            if (appslist.contains(archivedApp))
                appslist.get(appslist.indexOf(archivedApp)).setBackedUp(archivedApp.isBackedUp);
        }
        adapter.notifyDataSetChanged();
    }

}