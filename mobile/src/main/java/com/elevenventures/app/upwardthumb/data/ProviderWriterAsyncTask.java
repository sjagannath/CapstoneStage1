package com.elevenventures.app.upwardthumb.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by SJagannath on 11/26/2016.
 */

public class ProviderWriterAsyncTask extends AsyncTask<ContactData, Void, Boolean> {

    private final IProviderWriteStatusListener mListener;
    private Context mContext;

    public interface IProviderWriteStatusListener {
        void onWriteCompleted(boolean success);
    }

    RideContactsContentProvider mRideContactsContentProvider = new RideContactsContentProvider();

    public ProviderWriterAsyncTask(Context context, IProviderWriteStatusListener listener) {
        mListener = listener;
        mContext = context;
    }

    @Override
    protected Boolean doInBackground(ContactData... contactDatas) {
        if (contactDatas == null || contactDatas.length == 0) {
            return false;
        }
        ContentResolver contentResolver = mContext.getContentResolver();
        for (ContactData data : contactDatas) {
            ContentValues values = new ContentValues();
            values.put(RideContactsContract.COLUMN_ID, data.id);
            values.put(RideContactsContract.COLUMN_NAME, data.name);
            values.put(RideContactsContract.COLUMN_PHONE_NUMBER, data.phoneNumber);
            values.put(RideContactsContract.COLUMN_GROUP_NAME, data.groupName);
            values.put(RideContactsContract.COLUMN_NUM_RESPONSES, data.numResponses);
            values.put(RideContactsContract.COLUMN_URI, data.uri);
            Uri inserted = contentResolver.insert(RideContactsContract.CONTENT_URI, values);
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (mListener != null) {
            mListener.onWriteCompleted(success);
        }
    }
}
