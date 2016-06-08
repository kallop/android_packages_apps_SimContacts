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

import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;

import android.util.Log;

public class AuthenticateService extends Service {
    private final String TAG = "AuthenticateService";

    public static final String OPERATION= "account_operation";
    public static final int ADD_ACCOUNT = 1;
    public static final int DELETE_ACCOUNT = 2;

    private static final int SUB_1 = 0;
    private static final int SUB_2 = 1;
    private Authenticator mAuthenticator;
    private volatile Handler mServiceHandler;

    @Override
    public void onCreate() {
        Log.d(TAG, "service onCreate!");
        mAuthenticator = new Authenticator(this);
        mServiceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle args = (Bundle)msg.obj;
            switch(msg.what) {
            case ADD_ACCOUNT:
                handleAddAccount(args);
                break;
            }
        }
    };
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bind. Action: " + intent.getAction());
        if (AccountManager.ACTION_AUTHENTICATOR_INTENT.equals(intent.getAction())) {
            return mAuthenticator.getIBinder();  // call Authenticator.addAccount()
        } else {
            return null;
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "service onStart!"  );
        if (intent == null ){
            Log.d(TAG, "service onStart! intent is null"  );
            return;
        }
        Bundle args = intent.getExtras();

        if (args == null) {
            Log.d(TAG, "service onStart! args is null"  );
            return;
        }
        Message msg = mServiceHandler.obtainMessage();

        msg.what = ADD_ACCOUNT;
        msg.obj = args;
        mServiceHandler.sendMessage(msg);


    }

    private void handleAddAccount(Bundle args) {
        String accountType = args.getString(SimContactsConstants.ACCOUNT_TYPE);
        Bundle args1 = new Bundle();
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            int  subscription = args.getInt(SimContactsConstants.SUB);
            Log.d(TAG, "message received: sub is" + subscription + "accountType "  + accountType);
            if (accountType.equals(SimContactsConstants.ACCOUNT_TYPE_SIM)) {
                if (subscription == SimContactsConstants.SUB_1) {
                    args1.putString(SimContactsConstants.ACCOUNT_NAME,
                                             SimContactsConstants.SIM_NAME_1);
                } else if (subscription == SimContactsConstants.SUB_2) {
                      args1.putString(SimContactsConstants.ACCOUNT_NAME,
                                             SimContactsConstants.SIM_NAME_2);
                }
                try {
                    if (subscription == SimContactsConstants.SUB_1 ||
                        subscription == SimContactsConstants.SUB_2)
                     mAuthenticator.addAccount(null,SimContactsConstants.ACCOUNT_TYPE_SIM, null,
                                null, args1);
                } catch (NetworkErrorException e) {
                          Log.d(TAG, "add account exception" + e);
                }
            }
        } else {
            Log.d(TAG, "message received: accountType "  + accountType);
            if (accountType.equals(SimContactsConstants.ACCOUNT_TYPE_SIM)) {
                         args1.putString(SimContactsConstants.ACCOUNT_NAME,
                                             SimContactsConstants.SIM_NAME);
                try {
                    mAuthenticator.addAccount(null,SimContactsConstants.ACCOUNT_TYPE_SIM, null,
                                    null, args1);
                } catch (NetworkErrorException e) {
                    Log.d(TAG, "add account exception" + e);
                }
            }

        }
    }

}
