package com.apptech.android.apkshare;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
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

import java.util.ArrayList;

/**
 * Created by S on 26/05/2017.
 */

public class ArchivedFragment extends Fragment implements SearchView.OnQueryTextListener, OnTaskCompletedListener, OnTextViewClickListener, OnInstallUninstallListener {

    RecyclerView recyclerView;
    static FileObserver fileObserver;
    ArrayList<AppInfo> apps = new ArrayList<>();
    RecycleViewAdapter adapter;
    ProgressBar progressBar;
    OnArchivedCheckListener onArchivedCheckListener;

    private static final String app_root = Environment.getExternalStorageDirectory() + "/AppShare";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.applistlayout, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.id_recycleview);

        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        progressBar = (ProgressBar) view.findViewById(R.id.pbHeaderProgress);
        new InstallUninstallReceiver().setOnInstallUninstallListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        fileObserver = new FileObserver(app_root) { // set up a file observer to watch this directory on sd card

            @Override
            public void onEvent(int event, String file) {
                if (event == FileObserver.CREATE) {
                    new GetArchivedFilesInfo(ArchivedFragment.this).execute();
                }
            }
        };
        fileObserver.startWatching();

        new GetArchivedFilesInfo(this).execute();
        adapter = new RecycleViewAdapter(apps, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.archived_menu, menu);
        final MenuItem delete = menu.findItem(R.id.archived_delete);
        delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ArrayList<AppInfo> checkedList = adapter.getmCheckedAppList();
                new DeleteArchivedFiles(ArchivedFragment.this.getContext(), checkedList).execute();

                for (AppInfo appInfo : checkedList) {
                    appInfo.setBackedUp(false);
                    apps.remove(appInfo);
                }
                adapter.notifyDataSetChanged();
                onArchivedCheckListener.OnArchivedCheck(checkedList);
                return false;
            }
        });

        final MenuItem item = menu.findItem(R.id.archived_action_search);
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
    public void onTaskCompleted(ArrayList<AppInfo> apps) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        this.apps.clear();
        ArrayList<AppInfo> appInfoArrayList = new ArrayList<>();
        for (AppInfo app : apps) {
            if (GetInstalledApps.isPackageInstalled(app.getPackageName(), this)) {
                app.setBackedUp(true);
                app.setInstalled(true);
                appInfoArrayList.add(app);

            }
        }
        if (onArchivedCheckListener != null) {
            onArchivedCheckListener.OnArchivedCheck(appInfoArrayList);
        }
        this.apps.addAll(apps);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onArchivedCheckListener = (OnArchivedCheckListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(onArchivedCheckListener.toString()
                    + " must implement TextClicked");
        }
    }

    @Override
    public void onDetach() {
        onArchivedCheckListener = null;
        super.onDetach();
    }


    @Override
    public void onInstallUninstall(Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        if (intent.getAction() == Intent.ACTION_PACKAGE_REMOVED) {
            for (AppInfo app : apps) {
                if (packageName.equalsIgnoreCase(app.packageName)) {
                    app.setInstalled(false);
                    break;
                }
            }
        } else if (intent.getAction() == Intent.ACTION_PACKAGE_ADDED) {
            for (AppInfo app : apps) {
                if (packageName.equalsIgnoreCase(app.packageName)) {
                    app.setInstalled(true);
                    break;
                }
            }

        }
        adapter.notifyDataSetChanged();
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
        switch (id) {
            case R.id.check:
                if (adapter.getmCheckedAppList().size() == apps.size()) {
                    for (AppInfo app : apps) {
                        app.setSelected(false);

                    }
                } else {
                    for (AppInfo app : apps) {
                        app.setSelected(true);
                    }
                }
                adapter.notifyDataSetChanged();

                break;

            case R.id.tvBackup:
                new InstallArchiveFiles(this.getActivity(), adapter.getmCheckedAppList()).execute();
                break;

            case R.id.tvsend : new ApkOperations(this.getActivity(),adapter.getmCheckedAppList()).shareApk();
                break;

            default:
                break;
        }
    }
}
