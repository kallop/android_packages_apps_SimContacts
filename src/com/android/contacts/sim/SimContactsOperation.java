/*
 * Copyright (C) 2011-2012, Code Aurora Forum. All rights reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above
       copyright notice, this list of conditions and the following
       disclaimer in the documentation and/or other materials provided
       with the distribution.
     * Neither the name of Code Aurora Forum, Inc. nor the names of its
       contributors may be used to endorse or promote products derived
       from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.android.contacts.sim;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;

public class SimContactsOperation {

    private static final String  TAG = "SimContactsOperation";
    private static final boolean DBG = true;
    private static final int SUB1 = SimContactsConstants.SUB_1;
    private static final int SUB2 = SimContactsConstants.SUB_2;
    private static final int QUERY_TOKEN = 0;
    private static final int INSERT_TOKEN = 1;
    private static final int UPDATE_TOKEN = 2;
    private static final int DELETE_TOKEN = 3;

    static final String[] ACCOUNT_PROJECTION = new String[] {
        RawContacts._ID,
        RawContacts.CONTACT_ID,
        RawContacts.ACCOUNT_NAME,
        RawContacts.ACCOUNT_TYPE,
    };


    private static final int ACCOUNT_COLUMN_RAW_ID = 0;
    private static final int ACCOUNT_COLUMN_CONTACT_ID = 1;
    private static final int ACCOUNT_COLUMN_NAME = 2;
    private static final int ACCOUNT_COLUMN_TYPE = 3;
    private static final int ACCOUNT_COLUMN_PHONE_NAME = 4;



    private static Context mContext;
    private ContentResolver mResolver;
    private ContentValues mValues = new ContentValues();


    public SimContactsOperation(Context context) {
        this.mContext = context;
        this.mResolver = context.getContentResolver();
    }


    public Uri insert(ContentValues values, int subscription) {

        Uri uri = getContentUri(subscription);
        String number = values.getAsString(SimContactsConstants.STR_NUMBER);
        String anrs = values.getAsString(SimContactsConstants.STR_ANRS);
        values.put(SimContactsConstants.STR_NUMBER,PhoneNumberUtils.stripSeparators(number));
        values.put(SimContactsConstants.STR_ANRS,PhoneNumberUtils.stripSeparators(anrs));

        Uri resultUri;
        resultUri = mResolver.insert(uri,values);
        return resultUri;
    }

    public int update(ContentValues values,int subscription) {
        Uri uri = getContentUri(subscription);
        //mValues.put("newNumber", PhoneNumberUtils.stripSeparators(newNum));

        int result;
        String oldNumber = values.getAsString(SimContactsConstants.STR_NUMBER);
        String newNumber = values.getAsString(SimContactsConstants.STR_NEW_NUMBER);
        String oldAnrs = values.getAsString(SimContactsConstants.STR_ANRS);
        String newAnrs = values.getAsString(SimContactsConstants.STR_NEW_ANRS);
        values.put(SimContactsConstants.STR_NUMBER,PhoneNumberUtils.stripSeparators(oldNumber));
        values.put(SimContactsConstants.STR_NEW_NUMBER,PhoneNumberUtils.stripSeparators(newNumber));
        values.put(SimContactsConstants.STR_ANRS,PhoneNumberUtils.stripSeparators(oldAnrs));
        values.put(SimContactsConstants.STR_NEW_ANRS,PhoneNumberUtils.stripSeparators(newAnrs));

        result = mResolver.update(uri,values,null,null);
        return result;

    }

    public int delete(ContentValues values, int subscription) {
        int result;
        StringBuilder buf = new StringBuilder();
        String num = null;
        String name = values.getAsString(SimContactsConstants.STR_TAG);
        String number = values.getAsString(SimContactsConstants.STR_NUMBER);
        String emails = values.getAsString(SimContactsConstants.STR_EMAILS);
        String anrs = values.getAsString(SimContactsConstants.STR_ANRS);
        if (number != null)
            num = PhoneNumberUtils.stripSeparators(number);
        if (anrs != null)
            anrs = PhoneNumberUtils.stripSeparators(anrs);
        Uri uri = getContentUri(subscription);


        if (!TextUtils.isEmpty(name)) {
            buf.append("tag='");
            buf.append(name);
            buf.append("'");
        }
        if (!TextUtils.isEmpty(name)) {
            buf.append(" AND number='");
            buf.append(num);
            buf.append("'");
        }
        if (!TextUtils.isEmpty(emails)) {
            buf.append(" AND emails='");
            buf.append(emails);
            buf.append("'");
        }
        if (!TextUtils.isEmpty(anrs)) {
            buf.append(" AND anrs='");
            buf.append(anrs);
            buf.append("'");
        }

        result = mResolver.delete(uri,buf.toString(),null);
        return result;

    }

    private Uri getContentUri(int subscription) {
        Uri uri = null;
        if (subscription != SUB1 && subscription != SUB2) {
            return uri;
        }
        int[] subId = SubscriptionManager.getSubId(subscription);
        if (subId != null && TelephonyManager.getDefault().isMultiSimEnabled()) {
            uri = Uri.parse(SimContactsConstants.SIM_SUB_URI + subId[0]);
        } else {
            uri = Uri.parse(SimContactsConstants.SIM_URI);
        }
        return uri;
    }

    private static Cursor setupAccountCursor(long contactId) {
        ContentResolver resolver = mContext.getContentResolver();
        Cursor cursor = resolver.query(RawContacts.CONTENT_URI,
                ACCOUNT_PROJECTION,
                RawContacts.CONTACT_ID + "="
                + Long.toString(contactId), null, null);

        if (cursor.moveToFirst()) {
            // TODO: make sure this is the correct one we are query
            return cursor;
        } else {
            cursor.close();
            return null;
        }
    }

    public static ContentValues getSimAccountValues(long contactId) {
        ContentValues mValues = new ContentValues();
        Cursor cursor = setupAccountCursor(contactId);
        if (cursor == null || cursor.getCount() == 0) {
            mValues.clear();
            return mValues;
        }
        long rawContactId = cursor.getLong(cursor.getColumnIndex(RawContacts._ID));
        String accountName = cursor.getString(cursor.getColumnIndex(RawContacts.ACCOUNT_NAME));
        String accountType = cursor.getString(cursor.getColumnIndex(RawContacts.ACCOUNT_TYPE));
        cursor.close();
        if (SimContactsConstants.ACCOUNT_TYPE_SIM.equals(accountType)) {
            mValues.clear();
            String name = getContactItems(rawContactId,
                    StructuredName.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
            mValues.put(SimContactsConstants.STR_TAG,name);


            String number = getContactPhoneNumber(rawContactId,
                Phone.CONTENT_ITEM_TYPE, String.valueOf(Phone.TYPE_MOBILE),
                ContactsContract.CommonDataKinds.Phone.DATA);
            mValues.put(SimContactsConstants.STR_NUMBER,number);
        } else {
            cursor.close();
        }

        return mValues;
    }

    public static int getSimSubscription(long contactId) {
        int subscription = -1;
        Cursor cursor = setupAccountCursor(contactId);
        if (cursor == null || cursor.getCount() == 0) {
            subscription = -1;
            return subscription;
        }

        String accountName = cursor.getString(cursor.getColumnIndex(RawContacts.ACCOUNT_NAME));
        String accountType = cursor.getString(cursor.getColumnIndex(RawContacts.ACCOUNT_TYPE));
        if (accountType == null || accountName == null) {
            subscription = -1;
            return subscription;
        }
        if (SimContactsConstants.ACCOUNT_TYPE_SIM.equals(accountType)) {
            if (SimContactsConstants.SIM_NAME.equals(accountName))
                subscription = 0;
            else if(SimContactsConstants.SIM_NAME_1.equals(accountName))
                subscription = 0;
            else if (SimContactsConstants.SIM_NAME_2.equals(accountName))
                subscription = 1;
            cursor.close();
        } else {
            subscription = -1;
            cursor.close();
        }
        return subscription;
    }


    private static String getContactItems(long rawContactId, String selectionArg, String columnName) {
        String retval = null;
        Uri baseUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
        Uri dataUri = Uri.withAppendedPath(baseUri, RawContacts.Data.CONTENT_DIRECTORY);

        Cursor c = mContext.getContentResolver().query(dataUri, null,
                Data.MIMETYPE + "=?", new String[] {selectionArg}, null);
        if (c == null || c.getCount() == 0) {
            if(c != null) {
                c.close();
            }
            return null;
        }
        c.moveToPosition(-1);

        while (c.moveToNext()) {
            retval = c.getString(c.getColumnIndex(columnName));
        }

        c.close();

        return retval;
    }

    private static
    String getContactPhoneNumber(long rawContactId, String selectionArg1, String selectionArg2, String columnName) {
        String retval = null;
        Uri baseUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
        Uri dataUri = Uri.withAppendedPath(baseUri, RawContacts.Data.CONTENT_DIRECTORY);

        Cursor c = mContext.getContentResolver().query(dataUri, null,
                Data.MIMETYPE + "=? AND " + Phone.TYPE + "=?",
                new String[] {selectionArg1,selectionArg2}, null);
        if (c == null || c.getCount() == 0) {
            if(c != null) {
                c.close();
            }
            return null;
        }
        c.moveToPosition(-1);

        while (c.moveToNext()) {
            retval = c.getString(c.getColumnIndex(columnName));
        }

        c.close();

        return retval;
    }


    private void log(String msg) {
        if (DBG) Log.d(TAG,  msg);
    }

}
