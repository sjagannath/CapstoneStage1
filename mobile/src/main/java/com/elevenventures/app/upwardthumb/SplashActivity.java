package com.elevenventures.app.upwardthumb;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {


    private static final String TAG = "Capstone";
    private boolean mNeedLocationPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mNeedLocationPermission = Utility.hasLocationPermission(this);
        Log.d(TAG, "mNeedLocationPermission =" + mNeedLocationPermission);
    }



    @Override
    protected void onResume() {
        super.onResume();
        checkLocationPermission();
        checkPermissionAndGoAhead();
    }

    private void checkPermissionAndGoAhead() {
        if (!mNeedLocationPermission) {
            startActivity(new Intent(this, MapsActivity.class));
            finish();
        }
    }

    private void checkLocationPermission() {
        if (mNeedLocationPermission) {
            Utility.requestLocationPermission(this);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (isFinishing()) {
            return;
        }
        if (requestCode == Utility.MY_PERMISSIONS_REQUEST_READ_LOCATION) {
            mNeedLocationPermission = (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
            if(!mNeedLocationPermission){
                //permission given
                startActivity(new Intent(this, MapsActivity.class));
            }
            finish();
        }
    }
}
