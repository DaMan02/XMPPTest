package com.dayal.xmpptest2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.io.IOException;

public class XmppConnectService extends Service {
    private static final String TAG ="XmppService";

    public static final String UI_AUTHENTICATED = "com.dayal.uiauthenticated";
    public static final String SEND_MESSAGE = "com.dayal.sendmessage";
    public static final String MESSAGE_TYPE = "com.dayal.messagetype";
    public static final String BUNDLE_MESSAGE_BODY = "b_body";
    public static final String BUNDLE_TO = "b_to";
    public static final String CREATE_ROOM = "com.dayal.createnewroom";

    public static final String INVITE = "com.dayal.inviteafriend";
    public static final String NEW_MESSAGE = "com.dayal.newmessage";
    public static final String BUNDLE_FROM_JID = "b_from";

    public static XmppConnection.ConnectionState sConnectionState;
    public static XmppConnection.LoggedInState sLoggedInState;
    private boolean mActive;//Stores whether or not the thread is active
    private Thread mThread;
    private Handler mTHandler;//We use this handler to post messages to
    //the background thread.
    private XmppConnection mConnection;

    public XmppConnectService() {

    }
    public static XmppConnection.ConnectionState getState()
    {
        if (sConnectionState == null)
        {
            Log.d("connection state","disconnected");

            return XmppConnection.ConnectionState.DISCONNECTED;


        }
        Log.d(TAG,"state: " + sConnectionState);
        return sConnectionState;
    }

    public static XmppConnection.LoggedInState getLoggedInState()
    {
        if (sLoggedInState == null)
        {
            return XmppConnection.LoggedInState.LOGGED_IN;
        }
        return sLoggedInState;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate()");
    }

    private void initConnection()
    {
        Log.d(TAG,"initConnection()");
        if( mConnection == null)
        {
            mConnection = new XmppConnection(this);
        }
        try
        {
            mConnection.connect();

        }catch (IOException |SmackException |XMPPException e)
        {
            //TODO toast
            Log.d(TAG,"Something went wrong while connecting ,make sure the credentials are right and try again");
            e.printStackTrace();
            //Stop the service all together.
            stopSelf();
        }

    }


    public void start()
    {
        Log.d(TAG," Service Start() function called.");
        if(!mActive)
        {
            mActive = true;
            if( mThread ==null || !mThread.isAlive())
            {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Looper.prepare();
                        mTHandler = new Handler();
                        initConnection();
                        //THE CODE HERE RUNS IN A BACKGROUND THREAD.
                        Looper.loop();

                    }
                });
                mThread.start();
            }


        }

    }

    public void stop()
    {
        Log.d(TAG,"stop()");
        mActive = false;
        mTHandler.post(new Runnable() {
            @Override
            public void run() {
                if( mConnection != null)
                {
                    mConnection.disconnect();
                }
            }
        });

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand()");
        start();
        return Service.START_STICKY;
        //RETURNING START_STICKY CAUSES OUR CODE TO STICK AROUND WHEN THE APP ACTIVITY HAS DIED.
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy()");
        super.onDestroy();
        stop();
    }
}