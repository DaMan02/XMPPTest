package com.dayal.xmpptest2;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.dayal.xmpptest2.models.Contact;
import com.dayal.xmpptest2.utils.MessageUtils;
import com.dayal.xmpptest2.utils.Util;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.util.TLSUtils;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.jid.util.JidUtil;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by Manjeet Dayal on 07-07-2018.
 */

public class XmppConnection implements ConnectionListener {

    private static final String TAG = "XmppConnection";
     public static final String OPENFIRE_HOST = "192.168.43.210";
//    public static final String OPENFIRE_HOST = "10.0.0.4";

    private  final Context mApplicationContext;
    private  final String mUsername;
    private  final String mPassword;
    private  final String mServiceName;
    private XMPPTCPConnection mConnection;
    private BroadcastReceiver uiThreadMessageReceiver;//Receives messages from the ui thread.
    private BroadcastReceiver roomCreateRequestReceiver;

    //    private ChatManager chatManager;
     private XMPPTCPConnectionConfiguration.Builder conf;


    public enum ConnectionState
    {
        AUTHENTICATED, CONNECTED, CONNECTING ,DISCONNECTING ,DISCONNECTED;
    }

    public enum LoggedInState
    {
        LOGGED_IN , LOGGED_OUT;
    }

    public enum RegisterState
    {
        REGISTERED, UNREGISTERED ;
    }

//    public List<Contact> getAllContacts() throws SmackException.NotLoggedInException, InterruptedException, SmackException.NotConnectedException, XmppStringprepException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
//
//        Log.d(TAG, "getAllContacts called(), mConnection :" + mConnection);
//        List<Contact> contacts = new ArrayList<>();
//
//        if(XmppConnectService.sConnectionState == ConnectionState.AUTHENTICATED){
//
//                  mConnection = new XMPPTCPConnection(conf.build());
//                    mConnection.connect();
//                    Roster roster = Roster.getInstanceFor(mConnection);
//                    Log.d(TAG,"collecting contacts");
//
//                    Collection<RosterEntry> entries = roster.getEntries();
//
//                      if (!roster.isLoaded())
//                              roster.reloadAndWait();
//                    Presence presence;
//
//                    for (RosterEntry entry : entries) {
//                        Log.d(TAG," 1contact jid " + entry);
//                        BareJid currJid = JidCreate.bareFrom(entry.getJid());
//                        roster.createEntry(currJid,entry.getName(),new String[]{});
//                        Contact c = new Contact(entry.getJid().toString());
//                        Log.d(TAG," 2contact jid " + entry);
//                        presence = roster.getPresence(entry.getJid());
//                        Log.d(TAG," 3contact jid " + entry);
//
//                        contacts.add(c);
//
//
//                    }
//                }
//
//            }

//            if (!roster.isLoaded())
//                roster.reloadAndWait();
//            else {
//                       }

//
//        return contacts;
//
//    }

    public XmppConnection( Context context)
    {
        Log.d(TAG,"XmppConnection Constructor called.");
        mApplicationContext = context.getApplicationContext();
        String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid",null);
        mPassword = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_password",null);

        if( jid != null)
        {
            mUsername = jid.split("@")[0];
            mServiceName = jid.split("@")[1];
        }else
        {
            mUsername ="";
            mServiceName="";
        }
    }


    public void connect() throws IOException,XMPPException,SmackException
    {
        Log.d(TAG, "Connecting to server " + mServiceName);

        InetAddress addr = InetAddress.getByName(OPENFIRE_HOST);

        HostnameVerifier verifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
//                return false;
                return true;
            }
        };


        DomainBareJid serviceName = JidCreate.domainBareFrom(OPENFIRE_HOST);
        conf = XMPPTCPConnectionConfiguration.builder();
                conf.setXmppDomain(serviceName)
                .setHost(OPENFIRE_HOST)
                .setHostAddress(addr)
                .setUsernameAndPassword(mUsername,mPassword)
                .setPort(5222)
                .setHostnameVerifier(verifier)
                .setDebuggerEnabled(true)
                //Was facing this issue
                //https://discourse.igniterealtime.org/t/connection-with-ssl-fails-with-java-security-keystoreexception-jks-not-found/62566
                 .setKeystoreType(null) //This line seems to get rid of the problem

//                .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                // Security via TLS encryption is disabled and only un-encrypted connections will be used.
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setCompressionEnabled(true);
        try {
            TLSUtils.acceptAllCertificates(conf);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        mConnection = new XMPPTCPConnection(conf.build());
        Log.d(TAG, "mConnection" + mConnection);
 //        AccountManager accountManager =  AccountManager.getInstance(mConnection);
//        try {
//           boolean b =  accountManager.supportsAccountCreation();
//           Log.d(TAG, "accountManager: " + b);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//

        Log.d(TAG, "Username : "+mUsername);
        Log.d(TAG, "Password : "+mPassword);
        Log.d("servicename", "Server : "+mServiceName);


        //Set up the ui thread broadcast message receiver.
        setupUiThreadBroadCastMessageReceiver();

        mConnection.addConnectionListener(this);
        try {
            String s = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                    .getString("xmpp_registerState","");
            if( s.equals(Util.UNREGISTERED) || !PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                    .contains("xmpp_registerState")) {

                AccountManager objAccountManager = AccountManager.getInstance(mConnection);
                try {
                    objAccountManager.createAccount(Localpart.from(mUsername), mPassword);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d(TAG,"account not created");
                }

            }else{  // TODO

            }
            Log.d(TAG, "Calling connect() ");
            mConnection.connect();
            mConnection.login(mUsername,mPassword);
            Log.d(TAG, " login() Called ");

        } catch (InterruptedException e) {
            e.printStackTrace();
//            Toast.makeText(mApplicationContext, "Error authenticating user", Toast.LENGTH_SHORT).show();
        }
            //----------------------------------------------------------

//            Presence presence = new Presence(Presence.Type.available);
//            presence.setStatus("My status");
//            presence.setPriority(24);
//            mConnection.sendPacket(presence);
//
//            roster = Roster.getInstanceFor(mConnection);
//            if (!roster.isLoaded())
//                roster.reloadAndWait();
//
//                Collection<RosterEntry> entries = roster.getEntries();
//            for (RosterEntry entry : entries)
//                Log.d(TAG, "Here: " + entry);

            //------------------------------------------------------------------

         //

        ChatManager.getInstanceFor(mConnection).addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(final EntityBareJid messageFrom, Message message, Chat chat) {
                ///ADDED
                Log.d(TAG,"message.getBody() :"+message.getBody());
                Log.d(TAG,"message.getFrom() :"+message.getFrom());
                Log.d("type","message.getType() :" + message.getSubject());

                String from = message.getFrom().toString();

                String contactJid="";
                if ( from.contains("/"))
                {
                    contactJid = from.split("/")[0];
                    Log.d(TAG,"The real jid is :" +contactJid);
                    Log.d(TAG,"The message is from :" +from);
                }else
                {
                    contactJid=from;
                }

                //Bundle up the intent and send the broadcast.
                Intent intent = new Intent(XmppConnectService.NEW_MESSAGE);
                intent.setPackage(mApplicationContext.getPackageName());
                intent.putExtra(XmppConnectService.BUNDLE_FROM_JID,contactJid);
                intent.putExtra(XmppConnectService.MESSAGE_TYPE,message.getSubject());
                intent.putExtra(XmppConnectService.BUNDLE_MESSAGE_BODY,message.getBody());

                Log.d(TAG,"message body : " + message.getBody());
                Log.d(TAG,"message body : " + message.getSubject());
                mApplicationContext.sendBroadcast(intent);
                Log.d(TAG,"Received message from :"+contactJid+" broadcast sent.");

                ///ADDED

                //                 if(availability.getType() == Presence.Type.available)
            }
        });


        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
        reconnectionManager.setEnabledPerDefault(true);
        reconnectionManager.enableAutomaticReconnection();

    }



//    public static int retrieveState_mode(Presence.Mode userMode, boolean isOnline) {
//        int userState = 1;
//        /** 0 for offline, 1 for online, 2 for away,3 for busy*/
//        if(userMode == Presence.Mode.dnd) {
//            userState = 3;
//        } else if (userMode == Presence.Mode.away || userMode == Presence.Mode.xa) {
//            userState = 2;
//        } else if (!isOnline) {
//            userState = 0;
//        }
//        return userState;
//    }

    private void setupUiThreadBroadCastMessageReceiver()
    {
        uiThreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context,Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();

                switch(action){

                    case XmppConnectService.SEND_MESSAGE:
                        sendMessage(intent.getStringExtra(XmppConnectService.BUNDLE_MESSAGE_BODY),
                            intent.getStringExtra(XmppConnectService.MESSAGE_TYPE),
                            intent.getStringExtra(XmppConnectService.BUNDLE_TO));
                        break;

                    case XmppConnectService.CREATE_ROOM:
                        final String name = intent.getStringExtra("EXTRA_GROUP_NAME");
                        Log.w(TAG,"gp name received:" + name);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                Looper.prepare();
                                try{
                                    createGroup(name);
//                                    joinRoom(name);

                                }
                                catch(Exception e){
                                    Log.w(TAG,e);
                                }
                                Looper.loop();
                            }
                        }).start();

                        break;

                    case XmppConnectService.INVITE:

                        String friendJid = intent.getStringExtra(Util.EXTRA_FRIEND_TO_INVITE);
                        try {
                            inviteToRoom(friendJid);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                }


                }

        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(XmppConnectService.SEND_MESSAGE);
        filter.addAction(XmppConnectService.CREATE_ROOM);
        filter.addAction(XmppConnectService.INVITE);


        mApplicationContext.registerReceiver(uiThreadMessageReceiver,filter);

    }



    private void sendMessage ( String body ,String msgType,String toJid)
    {
        Log.d(TAG,"Sending message to :"+ toJid);

        EntityBareJid jid = null;


        ChatManager chatManager = ChatManager.getInstanceFor(mConnection);


        try {
            jid = JidCreate.entityBareFrom(toJid);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        Chat chat;
        if(jid != null)
         chat = chatManager.chatWith(jid);
        try {
            Message message = new Message(jid, Message.Type.chat);
            message.setBody(body);
            message.setSubject(msgType);
//            chat.send(message);             //for message body only
            mConnection.sendStanza(message);

        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void disconnect()
    {
        Log.d(TAG,"Disconnecting from server "+ mServiceName);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        prefs.edit().putBoolean("xmpp_logged_in",false).apply();


        if (mConnection != null)
        {
            mConnection.disconnect();
        }

        mConnection = null;
        // Unregister the message broadcast receiver.
        if( uiThreadMessageReceiver != null)
        {
            mApplicationContext.unregisterReceiver(uiThreadMessageReceiver);
            uiThreadMessageReceiver = null;
        }

    }

    @Override
    public void connected(XMPPConnection connection) {
        XmppConnectService.sConnectionState = ConnectionState.CONNECTED;
        Log.w(TAG,"Connected Successfully; id:" + XmppConnectService.sConnectionState);

        Log.d(TAG, "connected(), mConnection" + mConnection);

    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        XmppConnectService.sConnectionState = ConnectionState.AUTHENTICATED;
        Log.w(TAG,"Authenticated Successfully");

//----------------------join
        EntityBareJid mucJid ;
        String room = "protectedroom";
        try {
            mucJid = JidCreate.entityBareFrom(room + "@conference.desktop-19vmnri");
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
//--------------------------------------------

        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(mConnection);




        Log.d(TAG, "authenticated(), mConnection" + mConnection);
        showContactListActivityWhenAuthenticated();


    }


    @Override
    public void connectionClosed() {
        XmppConnectService.sConnectionState=ConnectionState.DISCONNECTED;
        Log.w(TAG,"Connectionclosed()");

    }

    @Override
    public void connectionClosedOnError(Exception e) {
        XmppConnectService.sConnectionState=ConnectionState.DISCONNECTING;
        Log.w(TAG,"ConnectionClosedOnError, error "+ e.toString());

    }

    @Override
    public void reconnectingIn(int seconds) {
        XmppConnectService.sConnectionState = ConnectionState.CONNECTING;
        Log.w(TAG,"ReconnectingIn() ");

    }

    @Override
    public void reconnectionSuccessful() {
        XmppConnectService.sConnectionState = ConnectionState.CONNECTED;
        Log.w(TAG,"ReconnectionSuccessful()");

    }

    @Override
    public void reconnectionFailed(Exception e) {
        XmppConnectService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.w(TAG,"ReconnectionFailed()");

    }

    private void showContactListActivityWhenAuthenticated()
    {
        Intent i = new Intent(XmppConnectService.UI_AUTHENTICATED);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
        // ------------------group --------------------------------
        // ----------------------------------------------------------------------

        Log.d(TAG, "showContactListActivityWhenAuthenticated(): mConnection" + mConnection);
        Log.w(TAG,"Sent the broadcast that we are authenticated");
    }
//--------------------------------------------------ROOM --------------------------------

    protected void inviteToRoom(String jid) throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException {
        // Get the MultiUserChatManager
        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(mConnection);

        EntityBareJid mucJid = JidCreate.entityBareFrom(jid);

// Create a MultiUserChat using an XMPPConnection for a room
        MultiUserChat muc2 = manager.getMultiUserChat(mucJid);
//        muc2.join("Friend's name");
   // User2 listens for invitation rejections
        muc2.addInvitationRejectionListener(new InvitationRejectionListener() {
            @Override
            public void invitationDeclined(EntityBareJid invitee, String reason, Message message, MUCUser.Decline rejection) {

            }
        });
// User2 invites user3 to join to the room
        muc2.invite(mucJid, "Meet me in this excellent room");
        Log.d(TAG,"invitation sent to " + jid);

    }


    public void sendMsg(String msg) throws XmppStringprepException, XMPPException.XMPPErrorException, MultiUserChatException.NotAMucServiceException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {

        EntityBareJid mucJid ;
        String room = "protectedroom";
        mucJid = JidCreate.entityBareFrom(room + "@conference.desktop-19vmnri");
//        Resourcepart nickname = Resourcepart.from(name);

        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(mConnection);

//        ChatManager chatManager = ChatManager.getInstanceFor(mConnection);

        Message message = new Message(mucJid,Message.Type.groupchat);
        message.setBody(msg);
        message.setStanzaId("file");
        mConnection.sendStanza(message);

        MultiUserChat muc2 = manager.getMultiUserChat(mucJid);

//        muc2.su

//        muc2.join(nickname, "mypassword");


    }
    public void joinRoom(String name) throws XmppStringprepException, XMPPException.XMPPErrorException, MultiUserChatException.NotAMucServiceException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {

        EntityBareJid mucJid ;
        String room = "protectedroom";
        mucJid = JidCreate.entityBareFrom(room + "@conference.desktop-19vmnri");
        Resourcepart nickname = Resourcepart.from(name);

        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(mConnection);

        MultiUserChat muc2 = manager.getMultiUserChat(mucJid);

        muc2.join(nickname, "mypassword");


    }


    public void createGroup(String room) throws XmppStringprepException, InterruptedException, SmackException.NoResponseException, MultiUserChatException.MucAlreadyJoinedException, SmackException.NotConnectedException, XMPPException.XMPPErrorException, MultiUserChatException.MissingMucCreationAcknowledgeException, MultiUserChatException.NotAMucServiceException, MultiUserChatException.MucConfigurationNotSupportedException {
        EntityBareJid mucJid ;

        Log.w(TAG,"createGroup() called,roomname:" + room);

        mucJid = JidCreate.entityBareFrom(room + "@conference.desktop-19vmnri");

        Resourcepart nickname = Resourcepart.from("dayalbot");
        Set<Jid> owners = JidUtil.jidSetFrom(new String[] { "dayal@" + OPENFIRE_HOST});


        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(mConnection);
        MultiUserChat muc = manager.getMultiUserChat(mucJid);
//        muc.create(nickname).makeInstant();
        muc.create(nickname);
        //        if (!muc.isJoined())
//            muc.join(nickname);

        Form form = muc.getConfigurationForm();
        Form submitForm = form.createAnswerForm();

        for(FormField field : form.getFields() ){

            if(!FormField.Type.hidden.name().equals(field.getType()) && field.getVariable() != null) {

                submitForm.setDefaultAnswer(field.getVariable());
            }

        }

        submitForm.setAnswer("muc#roomconfig_roomname", room);
        submitForm.setAnswer("muc#roomconfig_persistentroom",true);
        submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
//        submitForm.setAnswer("muc#roomconfig_publicroom", false);
        submitForm.setAnswer("muc#roomconfig_roomsecret", "mypassword");
        submitForm.setAnswer("muc#roomconfig_roomdesc", "The description. It should be longer.");

        muc.sendConfigurationForm(submitForm);

        muc.join(nickname);

        // -----------------------------------
//        muc.create(nickname)
//                .getConfigFormManager()
//                .setRoomOwners(owners)
//                .submitConfigurationForm();
        //-------------------------------------

        RoomInfo info = manager.getRoomInfo(mucJid);

        Log.w(TAG,"Room info:" + info);




    }

}