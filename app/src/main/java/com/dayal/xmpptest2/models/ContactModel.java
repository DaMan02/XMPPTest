package com.dayal.xmpptest2.models;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import com.dayal.xmpptest2.ContactListActivity;
import com.dayal.xmpptest2.XmppConnection;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manjeet Dayal on 07-07-2018.
 */

// TODO Remove class

public class ContactModel {

    private static ContactModel sContactModel;
    private List<Contact> mContacts;
    private static String TAG = "ContactModel";

    public static ContactModel get(Context context, String jid)
    {
        if(sContactModel == null)
        {
            sContactModel = new ContactModel(context,jid);
        }
        return  sContactModel;
    }

    private ContactModel(Context context, String jid)
    {
        mContacts = new ArrayList<>();
        populateWithInitialContacts(context,jid);

    }


    private void populateWithInitialContacts(final Context context,final String jid)
    {

         Contact contact1 = new Contact(jid);
          mContacts.add(contact1);

    }

    public List<Contact> getContacts()
    {
        return mContacts;
    }
}
