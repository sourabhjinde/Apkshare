package com.apptech.android.apkshare;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by S on 26/05/2017.
 */

public class ArchivedFragment extends Fragment implements OnTaskCompletedListener,OnDeleteArchivedListener{

    RecyclerView recyclerView;
    static FileObserver fileObserver;
    ArrayList<AppInfo> apps= new ArrayList<>();
    RecycleViewAdapter adapter;
    ProgressBar progressBar;
    OnArchivedCheckListener onArchivedCheckListener;

    private static final String app_root = Environment.getExternalStorageDirectory()+"/AppShare";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.applistlayout, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.id_recycleview);

        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        progressBar = (ProgressBar) view.findViewById(R.id.pbHeaderProgress);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        fileObserver = new FileObserver(app_root) { // set up a file observer to watch this directory on sd card

            @Override
            public void onEvent(int event, String file) {
                if(event == FileObserver.CREATE  ){
                    new GetArchivedFilesInfo(ArchivedFragment.this).execute();
                }
            }
        };
        fileObserver.startWatching();

       new GetArchivedFilesInfo(this).execute();
        adapter = new RecycleViewAdapter(apps);
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
                new DeleteArchivedFiles(ArchivedFragment.this.getContext(),checkedList).execute();

                for(AppInfo appInfo: checkedList) {
                      appInfo.setBackedUp(false);
                      apps.remove(appInfo);
                }
                adapter.notifyDataSetChanged();
                onArchivedCheckListener.OnArchivedCheck(checkedList);
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
        for(AppInfo app : apps){
            if(isPackageInstalled(app.getPackageName())){
                app.setBackedUp(true);
               appInfoArrayList.add(app);

            }
        }
        if(onArchivedCheckListener!=null) {
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

    public boolean isPackageInstalled(String packageName) {
        final PackageManager packageManager = getActivity().getPackageManager();
        try {
            Intent intent = packageManager.getLaunchIntentForPackage(packageName);
            if (intent == null || !packageManager.getApplicationInfo(packageName, 0).enabled) {
                return false;
            }
            List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return !list.isEmpty();
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onDeleteArchived(ArrayList<AppInfo> apps) {

    }
}
