package com.apptech.android.shareapps;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
import com.kobakei.ratethisapp.RateThisApp;

import java.util.ArrayList;

import static com.apptech.android.shareapps.R.id.adView;


public class MainActivity extends AppCompatActivity implements OnArchivedCheckListener {

    ViewPager viewPager;
    Button tvBackup;
    ImageButton check, tvSend;
    PagerAdapter adapter;
    Toolbar toolbar;
    AppListFragment appListFragment;
    TabLayout tabLayout;
    ClickListener clickListener;
    View dialogView, extractDialogView;
    private AdView mAdView;
    NativeExpressAdView mNativeExpressAdView, extractNativeExpressAdView;
    public AlertDialog alertDialog, extractAlertDialog;
    public static final String BACKUP_FOLDER_PREFERENCE = "BackupFolderPref";
    public static SharedPreferences sharedPreferences;
    private static final int PERMISSION_REQUEST_CODE = 717;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isStoragePermissionGranted()) {

            setup();
            setAppRater();
        }
    }

    public void setup() {
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.id_viewpager);

        adapter = new PagerAdapter(getSupportFragmentManager());
        appListFragment = new AppListFragment();
        adapter.addFragment(appListFragment, "Installed");
        adapter.addFragment(new ArchivedFragment(), "Archived");
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.id_tabs);
        tabLayout.setupWithViewPager(viewPager);
        clickListener = new ClickListener();


        tvBackup = (Button) findViewById(R.id.tvBackup);
        tvBackup.setOnClickListener(clickListener);
        check = (ImageButton) findViewById(R.id.check);
        check.setOnClickListener(clickListener);

        tvSend = (ImageButton) findViewById(R.id.tvsend);
        tvSend.setOnClickListener(clickListener);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1) {
                    tvBackup.setText("Restore");
                } else if (position == 0) {
                    tvBackup.setText("Backup");
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        LayoutInflater inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.native_express_layout, null);

        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Do you want to EXIT ?")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("Back", null)
                .setView(dialogView).setCancelable(true).create();

        LayoutInflater extractInflater = this.getLayoutInflater();
        extractDialogView = extractInflater.inflate(R.layout.apk_extract_progress, null);

        extractAlertDialog = new AlertDialog.Builder(this)
                .setTitle("Extracting Apk")
                .setView(extractDialogView)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        extractAlertDialog.dismiss();
                    }
                })
                .setCancelable(false).create();

        sharedPreferences = getSharedPreferences(BACKUP_FOLDER_PREFERENCE, MODE_PRIVATE);
        if (sharedPreferences.getBoolean("first_run", true)) {
            sharedPreferences.edit().putBoolean("first_run", false).commit();
            sharedPreferences.edit().putString("folderPath", ApkOperations.app_root).commit();
        }

        setUpAdView();
    }

    public void setUpAdView() {
        MobileAds.initialize(this, "ca-app-pub-6149134339415138~8979258400");
        mAdView = (AdView) findViewById(adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                mAdView.setVisibility(View.GONE);
            }
        });

        mNativeExpressAdView = (NativeExpressAdView) dialogView.findViewById(R.id.adView);

        AdRequest request = new AdRequest.Builder().build();
        mNativeExpressAdView.loadAd(request);
        mNativeExpressAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mNativeExpressAdView.setVisibility(View.VISIBLE);
            }
        });

        extractNativeExpressAdView = (NativeExpressAdView) extractDialogView.findViewById(R.id.adView);

        extractNativeExpressAdView.loadAd(request);
        extractNativeExpressAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                extractNativeExpressAdView.setVisibility(View.VISIBLE);
            }
        });

    }

    public void setAppRater() {
        RateThisApp.onCreate(this);
        RateThisApp.Config config = new RateThisApp.Config(3, 4);
        RateThisApp.init(config);
        RateThisApp.showRateDialogIfNeeded(this);
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        if (mNativeExpressAdView != null) {
            mNativeExpressAdView.pause();
        }
        if (extractNativeExpressAdView != null) {
            extractNativeExpressAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        if (mNativeExpressAdView != null) {
            mNativeExpressAdView.resume();
        }
        if (extractNativeExpressAdView != null) {
            extractNativeExpressAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (mNativeExpressAdView != null) {
            mNativeExpressAdView.destroy();
        }
        if (extractNativeExpressAdView != null) {
            extractNativeExpressAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        alertDialog.show();
    }


    @Override
    public void OnArchivedCheck(ArrayList<AppInfo> appInfos) {
        Fragment fragment = adapter.getItem(0);
        if (fragment instanceof AppListFragment) {
            ((AppListFragment) fragment).setArchivedPackages(appInfos);
        }
    }

    class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int item = viewPager.getCurrentItem();
            Fragment fragment = adapter.getItem(item);
            if (fragment instanceof AppListFragment) {
                ((AppListFragment) fragment).onTextViewClick(v);
            } else if (fragment instanceof ArchivedFragment) {
                ((ArchivedFragment) fragment).onTextViewClick(v);
            }
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to do store Apk. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setup();
                } else {
                }
                break;
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
                return true;

            } else {
                requestPermission(); // Code for permission
                return false;
            }
        } else {

            // Code for Below 23 API Oriented Device
            // Do next code
            return true;
        }
    }

    public static String getStorageFolder() {
        String path = sharedPreferences.getString("folderPath", null);
        if (path == null || path.isEmpty()) {
            sharedPreferences.edit().putString("folderPath", ApkOperations.app_root).commit();
            path = ApkOperations.app_root;
        }
        return path;
    }

    public static void setStorageFolder(String folderPath) {
        if (folderPath != null && !folderPath.isEmpty()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("folderPath", folderPath);
            editor.commit();
        }
    }

}
