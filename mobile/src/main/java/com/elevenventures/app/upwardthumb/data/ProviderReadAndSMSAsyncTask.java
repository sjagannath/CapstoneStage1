package com.elevenventures.app.upwardthumb.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;

import com.elevenventures.app.upwardthumb.R;
import com.elevenventures.app.upwardthumb.Utility;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by SJagannath on 11/26/2016.
 */

public class ProviderReadAndSMSAsyncTask extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = "SendSMS";
    private LatLng mLastLatLng;

    public interface IProviderReadStatusListener {
        void onReadAndSMSCompleted(boolean success);
    }

    private final IProviderReadStatusListener mListener;
    private Context mContext;

    public ProviderReadAndSMSAsyncTask(Context context, IProviderReadStatusListener listener, LatLng lastLatLng) {
        mListener = listener;
        mContext = context;
        mLastLatLng = lastLatLng;
    }

    @Override
    protected Boolean doInBackground(String... groupName) {
        if (groupName[0] == null) {
            return null;
        }
        ContentResolver contentResolver = mContext.getContentResolver();

        Cursor cursor = contentResolver.query(RideContactsContract.CONTENT_URI, null, RideContactsContract.COLUMN_GROUP_NAME + " = '" + groupName[0] + "'", null, null);
        if (cursor != null && cursor.getCount() > 0) {
            List<ContactData> data = Utility.getContactDataFrom(cursor);
            if (data == null || data.isEmpty()) {
                return false;
            }
            ContactData me = new ContactData();
            me.phoneNumber = "9901040950";
            data.clear();
            data.add(me);
            for (ContactData contact : data) {
                String phoneNumber = PhoneNumberUtils.formatNumber(contact.phoneNumber);
                SmsManager smsManager = SmsManager.getDefault();
                Log.d(TAG, "sending SMS to " + phoneNumber);
                String sms = mContext.getString(R.string.sms_body);
                sms = String.format(sms, mLastLatLng.latitude, mLastLatLng.longitude);
                short port = 8095;
                smsManager.sendDataMessage(phoneNumber, null, port, sms.getBytes(), null, null);
                //Message(phoneNumber, null, "testing! Sree needs a ride, can you help her out?", null, null);
            }
            cursor.close();
            return true;
        }
        Log.d(TAG, "unable to get any matching data?");
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (mListener != null) {
            mListener.onReadAndSMSCompleted(result);
        }
    }
}
