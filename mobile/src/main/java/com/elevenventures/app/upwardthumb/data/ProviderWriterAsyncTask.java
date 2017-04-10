package com.elevenventures.app.upwardthumb.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import com.elevenventures.app.upwardthumb.Utility;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by SJagannath on 11/26/2016.
 */

public class ProviderWriterAsyncTask extends AsyncTask<ContactData, Void, Boolean> {

    private final IProviderWriteStatusListener mListener;
    private final String mGroupName;
    private Context mContext;

    public interface IProviderWriteStatusListener {
        void onWriteCompleted(boolean success);
    }

    public ProviderWriterAsyncTask(Context context, IProviderWriteStatusListener listener, String groupName) {
        mListener = listener;
        mContext = context;
        mGroupName = groupName;
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
            values.put(RideContactsContract.COLUMN_GROUP_NAME, mGroupName);
            values.put(RideContactsContract.COLUMN_NUM_RESPONSES, data.numResponses);
            values.put(RideContactsContract.COLUMN_URI, data.uri);
            contentResolver.insert(RideContactsContract.CONTENT_URI, values);
        }
        Set<String> groupNames = Utility.getGroupNames(mContext);
        if (groupNames == null) {
            groupNames = new HashSet<>();
            groupNames.add(mGroupName);
            Utility.writeGroupNamesToPreference(groupNames, mContext);
        } else if (!groupNames.contains(mGroupName)) {
            groupNames.add(mGroupName);
            Utility.writeGroupNamesToPreference(groupNames, mContext);
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
