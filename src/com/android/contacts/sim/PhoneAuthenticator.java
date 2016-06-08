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

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

public class PhoneAuthenticator extends AbstractAccountAuthenticator {
    private final String TAG = "Authenticator";
    private final Context mContext;
    private final AccountManager mAccountManager;

    public PhoneAuthenticator(Context context) {
        super(context);
        mContext = context;
        mAccountManager = AccountManager.get(mContext);
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
            String accountType, String authTokenType,
            String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        Log.d(TAG, "Add account");
        String name = options.getString(SimContactsConstants.ACCOUNT_NAME);
        final Account account = new Account(name, accountType);
        mAccountManager.addAccountExplicitly(account, "", null);
        ContentResolver.setIsSyncable(account,"com.android.contacts",1);
        //ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
            Account account, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "confirmCredentials");
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
            String accountType) {
        Log.d(TAG, "editProperties");
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        Log.d(TAG, "getAuthToken");
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Log.d(TAG, "getAuthTokenLabel");
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
            Account account, String[] features) throws NetworkErrorException {
        Log.d(TAG, "hasFeatures");
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        Log.d(TAG, "updateCredentials");
        return null;
    }

}
