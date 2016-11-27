package com.elevenventures.app.upwardthumb;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.elevenventures.app.upwardthumb.data.ProviderWriterAsyncTask;

public class AddContactsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "Capstone";
    private static final int MSG_ADD_CONTACTS = 0;
    private static final int MSG_ADD_CONTACTS_COMPLETE = 1;
    private boolean mNeedContactsPermission;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private ListView mListView;
    private static final String SELECTION = ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP + " = ?" + " AND " + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER
            + "= ?";//+ " AND LENGTH(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ")> '6'";
    private static final String[] SELECTION_ARGS = new String[]{"1", "1"};
    private static final String sortOrder = Utility.COLOUMN_DISPLAY_NAME + " ASC";
    private final String[] PROJECTION = new String[]{ContactsContract.CommonDataKinds.Phone._ID,
            Utility.COLOUMN_DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI};

    private ListviewCursorAdapterAndroidContacts mListViewAdapter;
    private ProviderWriterAsyncTask mProviderWriterAsyncTask;
    private ProviderWriterAsyncTask.IProviderWriteStatusListener mProviderWriteStatusListener = new ProviderWriterAsyncTask.IProviderWriteStatusListener() {
        @Override
        public void onWriteCompleted(boolean success) {
            Message msg = mHandler.obtainMessage(MSG_ADD_CONTACTS_COMPLETE);
            msg.obj = success;
            mHandler.sendMessage(msg);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ADD_CONTACTS:
                    mProviderWriterAsyncTask = new ProviderWriterAsyncTask(AddContactsActivity.this, mProviderWriteStatusListener);
                    mProviderWriterAsyncTask.execute(mListViewAdapter.getSelectedContacts());
                    break;
                case MSG_ADD_CONTACTS_COMPLETE:
                    boolean success = (boolean) msg.obj;
                    Toast.makeText(AddContactsActivity.this, (success ? R.string.add_contacts_success : R.string.add_contacts_failed), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_request);
        //initToolbar();
        initListView();
        Button button = (Button) findViewById(R.id.addContactsButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListViewAdapter.getSelectedContacts() == null || mListViewAdapter.getSelectedContacts().length == 0) {
                    Toast.makeText(AddContactsActivity.this, "No contacts selected yet! Press Back to exit.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mHandler.sendEmptyMessage(MSG_ADD_CONTACTS);
            }
        });
        checkContactsPermission();
    }

    private void checkContactsPermission() {
        mNeedContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "need contacts permissions? " + mNeedContactsPermission);
        if (mNeedContactsPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (isFinishing()) {
            return;
        }
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            mNeedContactsPermission = (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
            if (!mNeedContactsPermission) {
                getSupportLoaderManager().initLoader(0, null, this);
            }
        }
    }

    private void initListView() {
        mListView = (ListView) findViewById(R.id.listview);
        mListViewAdapter =
                new ListviewCursorAdapterAndroidContacts(this, null);
        // Assign adapter to ListView
        mListView.setAdapter(mListViewAdapter);
        mListView.setOnItemClickListener(mListViewAdapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, SELECTION, SELECTION_ARGS, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mListViewAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mListViewAdapter.swapCursor(null);
    }

}
