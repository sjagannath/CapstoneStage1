package com.elevenventures.app.upwardthumb;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.elevenventures.app.upwardthumb.data.ContactData;
import com.elevenventures.app.upwardthumb.data.ProviderReadAndSMSAsyncTask;
import com.elevenventures.app.upwardthumb.data.RideContactsContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListenerService.ILocationListener, ServiceConnection,
        LoaderManager.LoaderCallbacks<Cursor>, ProviderReadAndSMSAsyncTask.IProviderReadStatusListener {

    private static final String TAG = "MapsActivity";
    private static final int MSG_HITCH_REQUESTED = 0;
    private static final int MSG_HITCH_CONTACTS = 1;
    private GoogleMap mMap;
    private LocationListenerService mLocationListenerService;
    private static final int LOADER_ID_LOCAL_CONTACTS = 0;
    private ProviderReadAndSMSAsyncTask mProviderReadAndSMSAsyncTask;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_HITCH_REQUESTED:
                    //contacts not setup, setup now?
                    if (mContactsList == null || mContactsList.size() == 0) {
                        showSetUpContacts();
                    } else {
                        showGroupChooser();
                    }
                    break;
                case MSG_HITCH_CONTACTS:
                    String groupName = (String) msg.obj;
                    Log.d(TAG, "asking rides from group: " + groupName);
                    mProviderReadAndSMSAsyncTask = new ProviderReadAndSMSAsyncTask(MapsActivity.this, MapsActivity.this, mLastLatLng);
                    mProviderReadAndSMSAsyncTask.execute(groupName);
                    break;
            }
        }
    };
    private LatLng mLastLatLng;

    private void showGroupChooser() {
        if (mProviderReadAndSMSAsyncTask != null && mProviderReadAndSMSAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            Toast.makeText(this, R.string.processing_last_hitch_request, Toast.LENGTH_SHORT).show();
            return;
        }
        final Dialog groupNameDialog = new Dialog(this);
        groupNameDialog.setContentView(R.layout.choose_group_layout_no_edit);
        groupNameDialog.setTitle(R.string.title_select_group);
        Set<String> groupNames = Utility.getGroupNames(this);
//        if (groupNames == null) {
//            groupNames = new HashSet<>();
//            groupNames.add("default");
//        }
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<>(groupNames));
        ListView listView = (ListView) groupNameDialog.findViewById(R.id.groupNamesList);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String groupName = (String) adapterView.getAdapter().getItem(i);
                Log.d(TAG, "group Name chosen = " + groupName);
                Message msg = mHandler.obtainMessage(MSG_HITCH_CONTACTS);
                msg.obj = groupName;
                mHandler.sendMessage(msg);
                groupNameDialog.dismiss();
            }
        });
        groupNameDialog.show();
    }

    private List<ContactData> mContactsList;

    private void showSetUpContacts() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.sym_contact_card)
                .setTitle(R.string.setup_contacts_title)
                .setCancelable(false)
                .setMessage(R.string.setup_contacts_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(MapsActivity.this, AddContactsActivity.class));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        bindService(new Intent(this, LocationListenerService.class), this, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bindService");
        initViews();
        getSupportLoaderManager().initLoader(LOADER_ID_LOCAL_CONTACTS, null, this);
        //new EndpointsAsyncTask().execute(new Pair<Context, String>(this, "Hannibal"));
    }

    private void initViews() {
        FloatingActionButton mHitchHikeButton = (FloatingActionButton) findViewById(R.id.fab_hitchhike);
        mHitchHikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(MSG_HITCH_REQUESTED);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onFailure(int reason, @Nullable ConnectionResult connectionResult) {
        Log.d(TAG, "onFailure + " + reason);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged + " + location);
        if (mMap != null) {
            mMap.clear();
            mLastLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(mLastLatLng).title("You are here"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLatLng, 13.0f));
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(TAG, "onServiceConnected + " + iBinder);
        mLocationListenerService = ((LocationListenerService.LocationListenerServiceBinder) iBinder).getService();
        mLocationListenerService.setLocationListener(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mLocationListenerService = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, RideContactsContract.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mContactsList = Utility.getContactDataFrom(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onReadAndSMSCompleted(boolean success) {
        Toast.makeText(this, "Did we send an SMS? " + (success ? "Yes we did!" : "No we didn't :-("), Toast.LENGTH_SHORT).show();
    }
}
