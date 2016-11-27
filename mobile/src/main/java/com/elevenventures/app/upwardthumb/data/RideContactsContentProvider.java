package com.elevenventures.app.upwardthumb.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class RideContactsContentProvider extends ContentProvider {
    private static final String TAG = "RideContactsProvider";
    private RideContactsDBHelper mDBHelper;

    public RideContactsContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = mDBHelper.getWritableDatabase().delete(RideContactsContract.TABLE_NAME, selection, selectionArgs);
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        return RideContactsContract.CONTENT_TYPE;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Uri returnUri;
        long _id = db.insert(RideContactsContract.TABLE_NAME, null, values);
        Log.d(TAG, "insert id = " + _id + " values = " + values);
//        if (_id > 0)
        returnUri = ContentUris.withAppendedId(uri, _id);
//        else {
//            Log.d(TAG, "Failed to insert row into " + uri + "\n" + values);
//        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new RideContactsDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor = mDBHelper.getReadableDatabase().query(
                RideContactsContract.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int rowsUpdated = mDBHelper.getWritableDatabase().update(RideContactsContract.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
