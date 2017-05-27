package com.apptech.android.apkshare;


import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    ViewPager viewPager;
    Button tvBackup;
    ImageButton check,tvSend;
    PagerAdapter adapter;
    private static final int PERMISSION_REQUEST_CODE = 717;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.id_viewpager);

        adapter = new PagerAdapter(getSupportFragmentManager());
        final AppListFragment appListFragment = new AppListFragment();
        adapter.addFragment(appListFragment, "Installed");
        adapter.addFragment(new ArchivedFragment(), "Archived");
      /*  adapter.addFragment(new AppListFragment(), "Google Drive");*/
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.id_tabs);
        tabLayout.setupWithViewPager(viewPager);
        ClickListener clickListener = new ClickListener();


        tvBackup = (Button) findViewById(R.id.tvBackup);
        tvBackup.setOnClickListener(clickListener);
        check =(ImageButton) findViewById(R.id.check);
        check.setOnClickListener(clickListener);

        tvSend = (ImageButton) findViewById(R.id.tvsend);
       tvSend.setOnClickListener(clickListener);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position == 1){
                    tvBackup.setText("Restore");
                }else if(position == 0){
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

        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
            } else {
                requestPermission(); // Code for permission
            }
        }
        else
        {

            // Code for Below 23 API Oriented Device
            // Do next code
        }


}

class ClickListener implements View.OnClickListener{

    @Override
    public void onClick(View v) {
        int item = viewPager.getCurrentItem();
        Fragment fragment = adapter.getItem(item);
        if (fragment instanceof AppListFragment) {
            ((AppListFragment) fragment).onTextViewClick(v);
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
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

}
