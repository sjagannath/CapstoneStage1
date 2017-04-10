package com.elevenventures.app.upwardthumb;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.elevenventures.app.upwardthumb.data.ContactData;
import com.elevenventures.app.upwardthumb.data.RideContactsContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by SJagannath on 11/25/2016.
 */

public class Utility {
    private static final String TAG = "Utility";
    public static final String PREF_KEY_GROUP_NAMES = "GroupNames";

    public static Set<String> getGroupNames(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getStringSet(PREF_KEY_GROUP_NAMES, null);
    }

    public static List<ContactData> getContactDataFrom(Cursor data) {
        List<ContactData> contacts = new ArrayList<>();
        if (data != null) {
            while (data.moveToNext()) {
                Log.d(TAG, DatabaseUtils.dumpCurrentRowToString(data));
                ContactData contactData = new ContactData();
                contactData.name = data.getString(data.getColumnIndex(RideContactsContract.COLUMN_NAME));
                contactData.groupName = data.getString(data.getColumnIndex(RideContactsContract.COLUMN_GROUP_NAME));
                contactData.numResponses = data.getInt(data.getColumnIndex(RideContactsContract.COLUMN_NUM_RESPONSES));
                contactData.id = data.getInt(data.getColumnIndex(RideContactsContract.COLUMN_ID));
                contactData.phoneNumber = data.getString(data.getColumnIndex(RideContactsContract.COLUMN_PHONE_NUMBER));
                contactData.uri = data.getString(data.getColumnIndex(RideContactsContract.COLUMN_URI));
                contacts.add(contactData);
            }
        }
        return contacts;
    }

    public static final String COLOUMN_DISPLAY_NAME = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY :
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

    public static List<ContactData> getContactDataFromAndroidContacts(Cursor data, boolean isSearchResult) {
        //Log.d("Utility", "" + data);
        if (data == null) {
            return null;
        }
        List<ContactData> contactDataList = new ArrayList<>();
        data.moveToFirst();
        do {
            ContactData contactData = new ContactData();
            contactData.name = data.getString(data.getColumnIndex(COLOUMN_DISPLAY_NAME));
            contactData.id = data.getInt(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
            try {
                if (isSearchResult) {
                    contactData.uri = data.getString(data.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                } else {
                    contactData.uri = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                }
            } catch (IllegalStateException ex) {
                Log.e(TAG, ex.getMessage());
            }
            try {
                if (isSearchResult) {
                    contactData.phoneNumber = data.getString(data.getColumnIndex(ContactsContract.Contacts.Data.DATA1));
                } else {
                    contactData.phoneNumber = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
            } catch (IllegalStateException ex) {
                Log.e(TAG, ex.getMessage());
            }
            Log.d("Utility", contactData.id + ":" + contactData.phoneNumber);
            contactDataList.add(contactData);
        } while (data.moveToNext());
        return contactDataList;
    }

    public static void writeGroupNamesToPreference(Set<String> groupNames, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(PREF_KEY_GROUP_NAMES, groupNames).apply();
    }

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasContactsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED;
    }

    public static final int MY_PERMISSIONS_REQUEST_READ_LOCATION = 1;

    public static void requestLocationPermission(Context context) {
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_READ_LOCATION);
    }
}
