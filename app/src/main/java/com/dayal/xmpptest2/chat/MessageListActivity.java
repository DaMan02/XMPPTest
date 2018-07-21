package com.dayal.xmpptest2.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.dayal.xmpptest2.R;
import com.dayal.xmpptest2.XmppConnectService;
import com.dayal.xmpptest2.XmppConnection;
import com.dayal.xmpptest2.models.ChatMessage;
import com.dayal.xmpptest2.models.Contact;
import com.dayal.xmpptest2.utils.MessageUtils;
import com.downloader.PRDownloader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.FileUtils;

import java.util.ArrayList;
import java.util.List;
import android.Manifest;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MessageListActivity extends AppCompatActivity {

    private MessageListAdapter mMessageAdapter;
    private static final String TAG ="MessageListActivity";

    private String contactName;
    private String contactJid;
    private String currUserId;
    private Contact contact;
    private Contact currUser;

    private StorageReference mStorageRef;
    private FirebaseStorage storage;

    @BindView(R.id.edittext_chatbox)
    EditText chatBoxEdittext;
    @BindView(R.id.reyclerview_message_list)
    RecyclerView mMessageRecycler;

    private static final int REQUEST_IMAGE_PICKER = 2;
    private static final int PICKFILE_REQUEST_CODE = 10;

    private static final int REQUEST_STORAGE_PERMISSION_CODE = 5;

    private BroadcastReceiver mBroadcastReceiver;
    private final List<ChatMessage> messagesList = new ArrayList<>();

    private String[] permissions = {READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_message_list);

        ButterKnife.bind(this);

        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(myPrefs.contains("xmpp_jid"))
         currUserId = myPrefs.getString("xmpp_jid","");

        storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference("Chat");

        Intent intentContactName = getIntent();
        contactName = intentContactName.getStringExtra("EXTRA_CONTACT_NAME");

        Intent intentContactJid = getIntent();
        contactJid = intentContactJid.getStringExtra("EXTRA_CONTACT_JID");

//        contact = new Contact(contactName);
        currUser = new Contact(currUserId);

        String activityTitle = contactName;

        getSupportActionBar().setTitle(activityTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMessageAdapter = new MessageListAdapter( this,messagesList, currUserId);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);
    }

    @OnClick(R.id.attach_msg)
    public void onAttachmentCicked(){
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.setType("image/*");

        startActivityForResult(pickIntent, REQUEST_IMAGE_PICKER);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case REQUEST_IMAGE_PICKER:

                    Uri imageUri = data.getData();
                    //final String imagePath = imageUri.getPath();

                    final StorageReference imagePath = mStorageRef.child(imageUri.getPath() + ".jpeg");

                    imagePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot task) {

                            imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    sendMessage(MessageUtils.IMAGE, uri.toString());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                }
                            });
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),"Error, not sent",Toast.LENGTH_LONG).show();
                            Log.d("url", "Message failed");
                        }
                    });

                    break;

                case PICKFILE_REQUEST_CODE:
                    final Uri fileUri = data.getData();

                    final StorageReference filePath = mStorageRef.child(fileUri.getPath());

                    Toast.makeText(getApplicationContext(),"Sending...",Toast.LENGTH_LONG).show();

                    filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Toast.makeText(getApplicationContext(),"File Sent",Toast.LENGTH_LONG).show();
                                    sendMessage(MessageUtils.DOCUMENT,uri.toString());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });


            }

        } else{ //nothing selected

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void sendMessage(String msgType,String msg){

        Intent intent = new Intent(XmppConnectService.SEND_MESSAGE);
        intent.putExtra(XmppConnectService.BUNDLE_MESSAGE_BODY,msg);
        intent.putExtra(XmppConnectService.MESSAGE_TYPE,msgType);
        intent.putExtra(XmppConnectService.BUNDLE_TO, contactJid);

        sendBroadcast(intent);

        //show the sent message
        ChatMessage message = new ChatMessage();
        message.setMessage(msg);
        message.setSender(currUser);
        message.setMesssageType(msgType);
        message.setTimestamp(System.currentTimeMillis());
        messagesList.add(message);
//        mMessageRecycler.scrollToPosition(messagesList.size() - 1);
        mMessageAdapter.notifyDataSetChanged();

    }

    @OnClick(R.id.button_chatbox_send)
    public void onfabClicked(){
        if (XmppConnectService.getState().equals(XmppConnection.ConnectionState.AUTHENTICATED)) {
            Log.d(TAG, "The client is connected to the server,Sending Message");
            //Send the message to the server

            String thisMessage = chatBoxEdittext.getText().toString();

            if(!TextUtils.isEmpty(thisMessage) || !chatBoxEdittext.getText().toString().equals(""))
                sendMessage(MessageUtils.TEXT,thisMessage);

        } else {
            Toast.makeText(getApplicationContext(),
                    "Client not connected to server ,Message not sent!",
                    Toast.LENGTH_LONG).show();
        }

        chatBoxEdittext.setText("");

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action)
                {
                    case XmppConnectService.NEW_MESSAGE:
                        String from = intent.getStringExtra(XmppConnectService.BUNDLE_FROM_JID);
                        String type = intent.getStringExtra(XmppConnectService.MESSAGE_TYPE);
                        String body = intent.getStringExtra(XmppConnectService.BUNDLE_MESSAGE_BODY);

                        Log.d(TAG, "contactJID:" + contactJid);

                        if ( from.equals(contactJid))
                        {
                            ChatMessage message = new ChatMessage();
                            message.setMessage(body);
                            message.setMesssageType(type);
                            message.setSender(contact);
                            contact.setUserName(contact.getJid());
                            message.setTimestamp(System.currentTimeMillis());
                            messagesList.add(message);
                            mMessageAdapter.notifyDataSetChanged();

                        }else
                        {
                            //if the person is not in contact list
                            Log.d(TAG,"Got a message from jid :"+from);
                        }

                        return;
                }

            }
        };

        IntentFilter filter = new IntentFilter(XmppConnectService.NEW_MESSAGE);
        registerReceiver(mBroadcastReceiver,filter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.chat_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.attach_chat_menu:
                if(permissionsGranted()){
                         filePick();
                }else{
                   ActivityCompat.requestPermissions(this,
                           permissions,REQUEST_STORAGE_PERMISSION_CODE);
                }


                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void filePick(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(Intent.createChooser(intent,"Choose application"), PICKFILE_REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_STORAGE_PERMISSION_CODE ){
            if(grantResults.length > 0 && (grantResults[0] + grantResults[1]) == PackageManager.PERMISSION_GRANTED){
                filePick();
            }
        }
    }

    private boolean permissionsGranted() {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }
}

