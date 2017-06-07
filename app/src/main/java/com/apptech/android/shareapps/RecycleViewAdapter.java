package com.apptech.android.shareapps;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Filter;

import java.util.ArrayList;


/**
 * Created by S on 03/05/2017.
 */

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> implements Filterable {
    private ArrayList<AppInfo> mAppList;
    private ArrayList<AppInfo> mFilteredAppList;
    private Fragment mFragment;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;

        public final TextView mAppName;
        public final TextView mVersion;
        public final ImageView mAppIcon;
        public final CheckBox isChecked;
        public final TextView mArchivedorInstalled;
        public final TextView date;
        public final TextView size;


        public ViewHolder(View view, Fragment fragment) {
            super(view);
            mView = view;
            mAppIcon = (ImageView) view.findViewById(R.id.app_icon);
            mAppName = (TextView) view.findViewById(R.id.app_name);
            mVersion = (TextView) view.findViewById(R.id.version);
            isChecked = (CheckBox) view.findViewById(R.id.chkSelected);
            date = (TextView) view.findViewById(R.id.date);
            size = (TextView) view.findViewById(R.id.size);
            if (fragment instanceof AppListFragment)
                mArchivedorInstalled = (TextView) view.findViewById(R.id.archived);
            else
                mArchivedorInstalled = (TextView) view.findViewById(R.id.installed);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isChecked.toggle();
                }
            });
        }
    }

    public RecycleViewAdapter(ArrayList<AppInfo> items, Fragment fragment) {
        mFilteredAppList = items;
        mAppList = items;
        mFragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int id = R.layout.app_list_item;

        if (mFragment instanceof ArchivedFragment) {
            id = R.layout.archive_list_item;
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(id, parent, false);

        return new ViewHolder(view, mFragment);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final AppInfo appInfo = mFilteredAppList.get(position);
        holder.mAppName.setText(appInfo.getAppName());
        holder.mVersion.setText(appInfo.getAppVersion());
        holder.mAppIcon.setImageDrawable(appInfo.getAppImage());
        if (mFragment instanceof AppListFragment)
            holder.mArchivedorInstalled.setText(appInfo.isBackedUp ? "Archived" : "");
        else
            holder.mArchivedorInstalled.setText(appInfo.isInstalled ? "Installed" : "");

        holder.date.setText(appInfo.getDate() + " - ");
        holder.size.setText(appInfo.getSize());

        //in some cases, it will prevent unwanted situations
        holder.isChecked.setOnCheckedChangeListener(null);
        //if true, your checkbox will be selected, else unselected
        holder.isChecked.setChecked(appInfo.isSelected());

        holder.isChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //set your object's last status
                appInfo.setSelected(isChecked);
                if (holder.isChecked.isChecked()) {
                    // mCheckedAppList.add(appInfo);
                } else {
                    //  mCheckedAppList.remove(appInfo);
                }
            }
        });


    }


    @Override
    public int getItemCount() {
        return mFilteredAppList.size();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();
                ArrayList<AppInfo> tempList;

                if (charString.isEmpty()) {

                    tempList = mAppList;
                } else {

                    ArrayList<AppInfo> filteredList = new ArrayList<>();

                    for (AppInfo appInfo : mAppList) {

                        if (appInfo.getAppName().toLowerCase().contains(charString.toLowerCase()) || appInfo.getAppVersion().toLowerCase().contains(charString.toLowerCase())) {

                            filteredList.add(appInfo);
                        }
                    }

                    tempList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = tempList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredAppList = (ArrayList<AppInfo>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public ArrayList<AppInfo> getmCheckedAppList() {
        ArrayList<AppInfo> mCheckedAppList = new ArrayList<>();
        for (AppInfo app : mFilteredAppList) {
            if (app.isSelected())
                mCheckedAppList.add(app);
        }
        return mCheckedAppList;
    }
}
