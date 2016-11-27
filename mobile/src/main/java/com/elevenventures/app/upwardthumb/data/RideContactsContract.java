package com.elevenventures.app.upwardthumb.data;

import android.content.ContentResolver;
import android.net.Uri;

/**
 * Created by SJagannath on 11/25/2016.
 */

public class RideContactsContract {
    public static final String AUTHORITY = "com.elevenventures.app.upwardthumb.provider";
    public static final String TABLE_NAME = "ridecontacts";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_URI = "uri";
    public static final String COLUMN_PHONE_NUMBER = "phone";
    public static final String COLUMN_NUM_RESPONSES = "num_responses";
    public static final String COLUMN_GROUP_NAME = "group_name";

    public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + TABLE_NAME;
}
