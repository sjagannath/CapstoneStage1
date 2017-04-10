package com.elevenventures.app.upwardthumb;

import android.Manifest;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.elevenventures.app.upwardthumb.data.ContactData;
import com.elevenventures.app.upwardthumb.data.FetchContactAsyncTask;
import com.elevenventures.app.upwardthumb.data.ProviderWriterAsyncTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddContactsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, FetchContactAsyncTask.IProviderFetchContactStatusListener {

    private static final String TAG = "Capstone";
    private static final int MSG_ADD_CONTACTS = 0;
    private static final int MSG_ADD_CONTACTS_COMPLETE = 1;
    private static final int MSG_SHOW_CONTACT_CHOOSER = 2;
    private boolean mNeedContactsPermission;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private ListView mListView;
    private EditText mSearchEditText;
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
                    mProviderWriterAsyncTask = new ProviderWriterAsyncTask(AddContactsActivity.this, mProviderWriteStatusListener, (String) msg.obj);
                    mProviderWriterAsyncTask.execute(mListViewAdapter.getSelectedContacts());
                    break;
                case MSG_ADD_CONTACTS_COMPLETE:
                    boolean success = (boolean) msg.obj;
                    Toast.makeText(AddContactsActivity.this, (success ? R.string.add_contacts_success : R.string.add_contacts_failed), Toast.LENGTH_SHORT).show();
                    break;
                case MSG_SHOW_CONTACT_CHOOSER:

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);
        initListView();
        setupSearchView();
        Button button = (Button) findViewById(R.id.addContactsButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListViewAdapter.getSelectedContacts() == null || mListViewAdapter.getSelectedContacts().length == 0) {
                    Toast.makeText(AddContactsActivity.this, "No contacts selected yet! Press Back to exit.", Toast.LENGTH_SHORT).show();
                    return;
                }
                showGroupChooserDialog();
            }
        });
        checkContactsPermission();
        Log.d(TAG, "phones authority  =" + ContactsContract.CommonDataKinds.Phone.CONTENT_URI.toString());
    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) findViewById(R.id.searchView);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(searchableInfo);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (ContactsContract.Intents.SEARCH_SUGGESTION_CLICKED.equals(intent.getAction())) {
            //handles suggestion clicked query
            Log.d(TAG, "SEARCH_SUGGESTION_CLICKED: " + intent.getData());
            updateSelectedContacts(intent);
        }
    }

    private void updateSelectedContacts(Intent intent) {
        List<String> pathSegments = intent.getData().getPathSegments();
        String lookupKey = pathSegments.get(pathSegments.size() - 2);
        Log.d(TAG, "path segments = " + pathSegments + "lookupKey = " + lookupKey);
        FetchContactAsyncTask fetchTask = new FetchContactAsyncTask(this, this);
        fetchTask.execute(lookupKey);
    }

    private void showGroupChooserDialog() {
        final Dialog groupNameDialog = new Dialog(this);
        groupNameDialog.setContentView(R.layout.choose_group_layout);
        groupNameDialog.setTitle(R.string.title_select_group);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Set<String> groupNames = preferences.getStringSet("GroupNames", null);
        if(groupNames == null){
            groupNames = new HashSet<>();
        }

        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<>(groupNames));
        ListView listView = (ListView) groupNameDialog.findViewById(R.id.groupNamesList);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String groupName = (String) adapterView.getAdapter().getItem(i);
                Log.d(TAG, "group Name chosen = " + groupName);
                Message msg = mHandler.obtainMessage(MSG_ADD_CONTACTS);
                msg.obj = groupName;
                mHandler.sendMessage(msg);
                groupNameDialog.dismiss();
                AddContactsActivity.this.finish();
            }
        });
        final EditText editText = (EditText) groupNameDialog.findViewById(R.id.editGroupName);
        (groupNameDialog.findViewById(R.id.buttonOK)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText() == null || editText.getText().toString().trim().length() == 0) {
                    return;
                }
                String groupName = editText.getText().toString().trim();
                Log.d(TAG, "group Name typed in = " + groupName);
                Message msg = mHandler.obtainMessage(MSG_ADD_CONTACTS);
                msg.obj = groupName;
                mHandler.sendMessage(msg);
                groupNameDialog.dismiss();
                AddContactsActivity.this.finish();
            }
        });
        groupNameDialog.show();
    }

    private void checkContactsPermission() {
        mNeedContactsPermission = Utility.hasContactsPermission(this);
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

    @Override
    public void onFetchCompleted(Cursor cursor) {
        Log.d(TAG, "contactDataList data fetched: " + cursor);
        if (cursor != null && mListViewAdapter != null) {
            if (cursor.getCount() == 1) {
                List<ContactData> contactData = Utility.getContactDataFromAndroidContacts(cursor, true);
                if (contactData != null) {
                    mListViewAdapter.addToSelectedContacts(contactData.get(0));
                }
            } else {
                showContactChooser(cursor);
            }
        }
    }

    private void showContactChooser(Cursor cursor) {
        final ListviewCursorAdapterAndroidContacts adapterAndroidContacts = new ListviewCursorAdapterAndroidContacts(this, cursor);
        View root = getLayoutInflater().inflate(R.layout.contact_chooser_dialog, null, false);
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Which one?");
        ListView contactChooserList = (ListView) root.findViewById(R.id.listview);
        (root.findViewById(R.id.addContactsButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ContactData[] data = adapterAndroidContacts.getSelectedContacts();
                if (data != null && data.length > 0 && mListViewAdapter != null) {
                    for (ContactData dataItem : data) {
                        Log.d(TAG, "adding contact " + dataItem.phoneNumber);
                        mListViewAdapter.addToSelectedContacts(dataItem);
                    }
                }
            }
        });
        contactChooserList.setAdapter(adapterAndroidContacts);
        contactChooserList.setOnItemClickListener(adapterAndroidContacts);
        dialog.setContentView(root);
        dialog.show();
    }


}
