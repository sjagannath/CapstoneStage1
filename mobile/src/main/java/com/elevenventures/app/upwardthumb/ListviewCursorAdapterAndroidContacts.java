package com.elevenventures.app.upwardthumb;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.elevenventures.app.upwardthumb.data.ContactData;

import java.util.HashMap;

/**
 * Created by SJagannath on 11/26/2016.
 */

public class ListviewCursorAdapterAndroidContacts extends CursorAdapter implements AdapterView.OnItemClickListener {

    private static final String TAG = "ListviewCursorAdapterAndroidContacts";
    private final HashMap<Integer, ContactData> mSelectedContacts = new HashMap();

    public ListviewCursorAdapterAndroidContacts(Context context, Cursor c) {
        super(context, c, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.select_contact_list_item, parent, false);
        return v;
    }

    public ContactData[] getSelectedContacts() {
        ContactData[] toReturn = new ContactData[mSelectedContacts.size()];
        int index = 0;
        for (ContactData contact : mSelectedContacts.values()) {
            toReturn[index] = contact;
            index++;
        }
        return toReturn;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView id, name, phonenumber;
        ImageView thumbnail;
        name = (TextView) view.findViewById(R.id.contactName);
        id = (TextView) view.findViewById(R.id.contactID);
        thumbnail = (ImageView) view.findViewById(R.id.contactPicture);
        phonenumber = (TextView) view.findViewById(R.id.contactNumber);
        name.setText(cursor.getString(cursor.getColumnIndex(Utility.COLOUMN_DISPLAY_NAME)));
        phonenumber.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
        int rowId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
        id.setText(String.valueOf(rowId));
        String uriString = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
        if (uriString != null) {
            Uri imageURI = Uri.parse(uriString);
            thumbnail.setImageURI(imageURI);
        } else {
            thumbnail.setImageDrawable(context.getDrawable(R.drawable.ic_face_black_24dp));
        }
        setImageviewCheckbox((ImageView) view.findViewById(R.id.checkboxSelectedContact), mSelectedContacts.containsKey(rowId));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Cursor cursor = getCursor();
        if (cursor == null || cursor.getCount() <= position) {
            return;
        }
        cursor.moveToPosition(position);
        boolean newState = changeCheckedState(cursor);
        setImageviewCheckbox((ImageView) view.findViewById(R.id.checkboxSelectedContact), newState);
    }

    private void setImageviewCheckbox(ImageView imageView, boolean newState) {
        if (newState) {
            imageView.setImageResource(R.drawable.ic_done_black_24dp);
        } else {
            imageView.setImageResource(0);
        }
    }

    private boolean changeCheckedState(Cursor cursor) {
        int rowId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
        boolean isNowChecked;
        if (mSelectedContacts.containsKey(rowId)) {
            isNowChecked = false;
            mSelectedContacts.remove(rowId);
        } else {
            isNowChecked = true;
            mSelectedContacts.put(rowId, Utility.getContactDataFromAndroidContacts(cursor));
        }
        return isNowChecked;
    }

}
