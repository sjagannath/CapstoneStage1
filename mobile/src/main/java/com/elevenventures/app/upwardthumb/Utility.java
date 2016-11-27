package com.elevenventures.app.upwardthumb;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import com.elevenventures.app.upwardthumb.data.ContactData;
import com.elevenventures.app.upwardthumb.data.RideContactsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SJagannath on 11/25/2016.
 */

public class Utility {
    private static final String TAG = "Utility";

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

    public static ContactData getContactDataFromAndroidContacts(Cursor data) {
        //Log.d("Utility", "" + data);
        if (data != null) {
            //Log.d("Utility", DatabaseUtils.dumpCurrentRowToString(data));
            ContactData contactData = new ContactData();
            contactData.name = data.getString(data.getColumnIndex(COLOUMN_DISPLAY_NAME));
            contactData.id = data.getInt(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
            contactData.phoneNumber = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contactData.uri = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
            return contactData;
        }
        return null;
    }
}
