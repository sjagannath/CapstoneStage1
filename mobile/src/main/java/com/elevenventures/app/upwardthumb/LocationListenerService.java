package com.elevenventures.app.upwardthumb;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationListenerService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "LocationListenerService";
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "google onLocationChanged" + location);
        if (mLocationListener != null) {
            mLocationListener.onLocationChanged(location);
        }
    }

    interface ILocationListener {
        int FAILURE_REASON_API_CLIENT_PROBLEM = 0;
        int FAILURE_REASON_NO_LOCATION_PERMISSION = 1;

        void onFailure(int reason, @Nullable ConnectionResult connectionResult);

        void onLocationChanged(Location location);
    }

    private GoogleApiClient mGoogleApiClient;
    private ILocationListener mLocationListener;

    private IBinder mServiceBinder = new LocationListenerServiceBinder();

    public LocationListenerService() {
    }

    public void setLocationListener(ILocationListener listener) {
        mLocationListener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        }
        mGoogleApiClient.connect();
        Log.d(TAG, "requested google connect");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (mLocationListener != null) {
                mLocationListener.onFailure(ILocationListener.FAILURE_REASON_NO_LOCATION_PERMISSION, null);
            }
            Log.d(TAG, "no permission for location");
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null && mLocationListener != null) {
            mLocationListener.onLocationChanged(mLastLocation);
//            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
//            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10 * 1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "requested google location");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mLocationListener != null) {
            mLocationListener.onFailure(ILocationListener.FAILURE_REASON_API_CLIENT_PROBLEM, connectionResult);
        }
    }

    class LocationListenerServiceBinder extends Binder {
        public LocationListenerService getService() {
            return LocationListenerService.this;
        }
    }
}
