package com.elevenventures.app.upwardthumb.data;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Created by SJagannath on 11/26/2016.
 */

public class FetchContactAsyncTask extends AsyncTask<String, Void, Cursor> {

    private static final String TAG = "FetchContact";

    public interface IProviderFetchContactStatusListener {
        void onFetchCompleted(Cursor data);
    }

    private final IProviderFetchContactStatusListener mListener;
    private Context mContext;

    public FetchContactAsyncTask(Context context, IProviderFetchContactStatusListener listener) {
        mListener = listener;
        mContext = context;
    }

    @Override
    protected Cursor doInBackground(String... lookupKey) {
        if (lookupKey[0] == null || lookupKey[0].trim().length() == 0) {
            return null;
        }
        Log.d(TAG, "Got  lookupkey" + lookupKey[0]);
        String[] whereArgs = new String[]{lookupKey[0], String.valueOf(1), String.valueOf(1), String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)};
//        Uri uri = Uri.parse(ContactsContract.Contacts.P + "/" + lookupKey);

        Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ? AND "
                        + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + " = ? AND "
                        + ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP + " = ? AND "
                        + ContactsContract.CommonDataKinds.Phone.TYPE + " = ?",
                whereArgs,
                null);
        return cursor;
    }

    @Override
    protected void onPostExecute(Cursor result) {
        super.onPostExecute(result);
        if (mListener != null) {
            mListener.onFetchCompleted(result);
        }
    }
}
