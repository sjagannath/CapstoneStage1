package com.elevenventures.app.upwardthumb.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by SJagannath on 11/25/2016.
 */

public class RideContactsDBHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DB_NAME = "RideContacts.db";

    public RideContactsDBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_CONTACT_TABLE = "CREATE TABLE " + RideContactsContract.TABLE_NAME + " (" +
                RideContactsContract.COLUMN_ID + " INTEGER PRIMARY KEY," +
                RideContactsContract.COLUMN_NAME + " TEXT UNIQUE NOT NULL, " +
                RideContactsContract.COLUMN_PHONE_NUMBER + " NUMBER NOT NULL, " +
                RideContactsContract.COLUMN_GROUP_NAME + " TEXT NOT NULL, " +
                RideContactsContract.COLUMN_NUM_RESPONSES + " INTEGER NOT NULL, " +
                RideContactsContract.COLUMN_URI + " TEXT " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_CONTACT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RideContactsContract.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
