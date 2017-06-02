package com.apptech.android.apkshare;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
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

/**
 * Created by S on 26/05/2017.
 */

public class ArchivedFragment extends Fragment implements SearchView.OnQueryTextListener, OnTaskCompletedListener, OnTextViewClickListener, OnInstallUninstallListener,
DialogChooseDirectory.Result, OnMoveFilesCompleteListener{

    RecyclerView recyclerView;
    static FileObserver fileObserver;
    ArrayList<AppInfo> apps = new ArrayList<>();
    RecycleViewAdapter adapter;
    ProgressBar progressBar;
    OnArchivedCheckListener onArchivedCheckListener;
    SearchView searchView;
    TextView emptyView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.archivelayout, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.id_recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        progressBar = (ProgressBar) view.findViewById(R.id.pbHeaderProgress);
        emptyView = (TextView) view.findViewById(R.id.empty_view);
        new InstallUninstallReceiver().setOnInstallUninstallListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        if (GetArchivedFilesInfo.createAppDirectory(this.getActivity())) {
            setFileObserver(MainActivity.getStorageFolder());
            new GetArchivedFilesInfo(this).execute();
            adapter = new RecycleViewAdapter(apps, this);
            recyclerView.setAdapter(adapter);
            if (apps.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.archived_menu, menu);

        final MenuItem item = menu.findItem(R.id.archived_action_search);
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

        final MenuItem delete = menu.findItem(R.id.archived_delete);
        delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final ArrayList<AppInfo> checkedList = adapter.getmCheckedAppList();
                if (checkedList.isEmpty()) {
                    Toast.makeText(ArchivedFragment.this.getActivity(), "Please select an app first", Toast.LENGTH_LONG).show();
                } else {

                    new AlertDialog.Builder(ArchivedFragment.this.getActivity())
                            .setTitle("Delete Confirm")
                            .setMessage("Do you really want to delete "+checkedList.size()+" item(s)?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    new DeleteArchivedFiles(ArchivedFragment.this.getContext(), checkedList).execute();

                                    for (AppInfo appInfo : checkedList) {
                                        appInfo.setBackedUp(false);
                                        apps.remove(appInfo);
                                    }
                                    adapter.notifyDataSetChanged();
                                    if (apps.isEmpty()) {
                                        recyclerView.setVisibility(View.GONE);
                                        emptyView.setVisibility(View.VISIBLE);
                                    }
                                    onArchivedCheckListener.OnArchivedCheck(checkedList);
                                }})
                            .setNegativeButton(android.R.string.no, null).show();

                }
                return false;
            }
        });

        final MenuItem folderChoser = menu.findItem(R.id.backup_path);
        folderChoser.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                         new DialogChooseDirectory(ArchivedFragment.this.getActivity(),ArchivedFragment.this,Environment.getExternalStorageDirectory().toString());
                                return false;
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
        if (searchView != null) {
            adapter.getFilter().filter(searchView.getQuery());
        } else {
            adapter.notifyDataSetChanged();
        }
        if (apps.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
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
        ArrayList<AppInfo> mCheckedAppList;
        switch (id) {
            case R.id.check:
                mCheckedAppList = adapter.getmCheckedAppList();
                if (mCheckedAppList.size() == apps.size()) {
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
                mCheckedAppList = adapter.getmCheckedAppList();
                if (mCheckedAppList.isEmpty()) {
                    Toast.makeText(this.getActivity(), "Please select an app first", Toast.LENGTH_LONG).show();
                } else {
                    new InstallArchiveFiles(this.getActivity(), mCheckedAppList).execute();
                }
                break;

            case R.id.tvsend:
                mCheckedAppList = adapter.getmCheckedAppList();
                if (mCheckedAppList.isEmpty()) {
                    Toast.makeText(this.getActivity(), "Please select an app first", Toast.LENGTH_LONG).show();
                } else {
                    new ApkOperations(this.getActivity(), mCheckedAppList).shareApk();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onChooseDirectory(String dir) {
        new MoveArchiveFiles(ArchivedFragment.this,MainActivity.getStorageFolder(),dir).execute();
        MainActivity.setStorageFolder(dir);
        Toast.makeText(this.getActivity(),"Backup path set to : "+dir,Toast.LENGTH_LONG).show();
        setFileObserver(dir);

    }

    @Override
    public void onMoveFilesComplete() {
        new GetArchivedFilesInfo(ArchivedFragment.this).execute();
    }

    public void setFileObserver(String filePath){
        if(fileObserver != null){
            fileObserver.stopWatching();
        }
        fileObserver = new FileObserver(filePath) { // set up a file observer to watch this directory on sd card

            @Override
            public void onEvent(int event, String file) {
                if (event == FileObserver.CREATE || event == FileObserver.MOVE_SELF || event == FileObserver.MOVED_FROM || event == FileObserver.MOVED_TO
                        || event == FileObserver.DELETE || event == FileObserver.DELETE_SELF) {
                    new GetArchivedFilesInfo(ArchivedFragment.this).execute();
                }
            }
        };
        fileObserver.startWatching();

    }
}
