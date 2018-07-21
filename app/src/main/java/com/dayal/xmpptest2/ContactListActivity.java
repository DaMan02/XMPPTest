package com.dayal.xmpptest2;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dayal.xmpptest2.chat.MessageListActivity;
import com.dayal.xmpptest2.database.DatabaseHandler;
import com.dayal.xmpptest2.models.Contact;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;


// ----------- TODO remove file
public class ContactListActivity extends AppCompatActivity {

    private static final String TAG = "ContactListActivity";

    private RecyclerView contactsRecyclerView;
    private ContactAdapter mAdapter;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private List<Contact> contacts;
    private List<Contact> listItems;
    private DatabaseHandler db;

    private EditText jid_text;
    private EditText name_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        ButterKnife.bind(this);

        getSupportActionBar().setTitle("My Contacts");

        setUpRecyclerView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contact_list, menu);
        return true;
    }

    @OnClick(R.id.add_contacts)
    public void onAddContactSelected(){

        Button saveBtn;

        dialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.add_contact_popup,null);
        saveBtn = view.findViewById(R.id.dialog_add_contact_btn);
        jid_text = view.findViewById(R.id.editText_dialog);
        name_text = view.findViewById(R.id.editText_name_dialog);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(jid_text.getText()) && !TextUtils.isEmpty(name_text.getText())){
                    saveContactToDatabase();
                }

            }
        });


    }

    private void saveContactToDatabase() {

        Contact c =new Contact();
        c.setUserName(name_text.getText().toString());
        c.setJid(jid_text.getText().toString());

        db.addContact(c);

        setUpRecyclerView();

        dialog.dismiss();
    }

    private void setUpRecyclerView() {

        db = new DatabaseHandler(this);

        contactsRecyclerView = findViewById(R.id.contact_list_recycler_view);
        contactsRecyclerView.setHasFixedSize(true);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        contacts = new ArrayList<>();
        listItems = new ArrayList<>();
        //get from DB
        contacts = db.getAllContacts();
        for(Contact c : contacts){
            Contact contact = new Contact();
            contact.setUserName(c.getUserName());
            contact.setJid(c.getJid());
            contact.setId(c.getId());

            listItems.add(contact);
        }
        mAdapter = new ContactAdapter(listItems);
        contactsRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.xmpp_logout)
        {
            //Disconnect from server

            Intent loginIntent = new Intent(this,LoginActivity.class);
            startActivity(loginIntent);
            Log.d(TAG,"Initiating the log out process");
            Intent i1 = new Intent(this,XmppConnectService.class);
            stopService(i1);

            //Finish this activity


            //Start login activity for user to login

            finish();

        }else  if(item.getItemId() == R.id.create_room){

            Intent i = new Intent(XmppConnectService.CREATE_ROOM);
            sendBroadcast(i);

        }

        return super.onOptionsItemSelected(item);
    }

    private class ContactHolder extends RecyclerView.ViewHolder
    {
        private TextView contactTextView;
        private Contact mContact;
        public ContactHolder ( View itemView)
        {
            super(itemView);

            contactTextView = itemView.findViewById(R.id.contact_name);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                     AlertDialog.Builder alert = new AlertDialog.Builder(ContactListActivity.this);
                     alert.setMessage("Remove Contact")
                           .setNegativeButton("No",null)
                             .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                      DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                                      db.deleteContact(mContact.getId());

                                      startActivity(getIntent());
                                                                                                             }
                             })
                     .show();

                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Inside here we start the chat activity
                    Intent intent = new Intent(ContactListActivity.this
                            ,MessageListActivity.class);
                     intent.putExtra("EXTRA_CONTACT_NAME",mContact.getUserName());
                    intent.putExtra("EXTRA_CONTACT_JID",mContact.getJid());
                    startActivity(intent);


                }
            });
        }


        public void bindContact( Contact contact)
        {
            mContact = contact;
            if (mContact == null)
            {
                Log.d(TAG,"Trying to work on a null Contact object ,returning.");
                return;
            }
            String userName = mContact.getUserName();

            contactTextView.setText(userName);

        }
    }


    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder>
    {
        private List<Contact> mContacts;

        public ContactAdapter( List<Contact> contactList)
        {
            mContacts = contactList;
        }

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater
                    .inflate(R.layout.list_item_contact, parent,
                            false);
            return new ContactHolder(view);
        }

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            Contact contact = mContacts.get(position);
            holder.bindContact(contact);

        }

        @Override
        public int getItemCount() {
            return mContacts.size();
        }
    }
}

