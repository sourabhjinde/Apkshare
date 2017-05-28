package com.apptech.android.apkshare;

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



    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;

        public final TextView mAppName;
        public final TextView mVersion;
        public final ImageView mAppIcon;
        public final CheckBox isChecked;
        public final TextView mArchived;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            mAppIcon = (ImageView) view.findViewById(R.id.app_icon);
            mAppName = (TextView) view.findViewById(R.id.app_name);
            mVersion = (TextView) view.findViewById(R.id.version);
            isChecked = (CheckBox) view.findViewById(R.id.chkSelected);
            mArchived = (TextView) view.findViewById(R.id.archived);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isChecked.toggle();
                }
            });
        }
    }

    public RecycleViewAdapter(ArrayList<AppInfo> items) {
        mFilteredAppList = items;
        mAppList = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final AppInfo appInfo = mFilteredAppList.get(position);
        holder.mAppName.setText(appInfo.getAppName());
        holder.mVersion.setText(appInfo.getAppVersion());
        holder.mAppIcon.setImageDrawable(appInfo.getAppImage());
        holder.mArchived.setText(appInfo.isBackedUp ?"Archived" :"");
        //in some cases, it will prevent unwanted situations
        holder.isChecked.setOnCheckedChangeListener(null);
        //if true, your checkbox will be selected, else unselected
        holder.isChecked.setChecked(appInfo.isSelected());

        holder.isChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //set your object's last status
                appInfo.setSelected(isChecked);
                if(holder.isChecked.isChecked()){
                      // mCheckedAppList.add(appInfo);
                }
                else {
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

                if (charString.isEmpty()) {

                    mFilteredAppList = mAppList;
                } else {

                    ArrayList<AppInfo> filteredList = new ArrayList<>();

                    for (AppInfo appInfo : mAppList) {

                        if (appInfo.getAppName().toLowerCase().contains(charString.toLowerCase()) || appInfo.getAppVersion().toLowerCase().contains(charString.toLowerCase())) {

                            filteredList.add(appInfo);
                        }
                    }

                    mFilteredAppList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredAppList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredAppList = (ArrayList<AppInfo>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public ArrayList<AppInfo> getmCheckedAppList()
    {
       ArrayList<AppInfo> mCheckedAppList = new ArrayList<>() ;
        for(AppInfo app : mFilteredAppList) {
            if(app.isSelected())
                mCheckedAppList.add(app);
        }
        return mCheckedAppList;
    }


}
