package com.dayal.xmpptest2.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dayal.xmpptest2.R;
import com.dayal.xmpptest2.chat.MessageListActivity;
import com.dayal.xmpptest2.database.DatabaseHandler;
import com.dayal.xmpptest2.models.Contact;

import java.util.ArrayList;
import java.util.List;


public class ContactFragment extends Fragment {

    private RecyclerView contactsRecyclerView;
    private ContactAdapter mAdapter;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private List<Contact> contacts;
    private List<Contact> listItems;
    private DatabaseHandler db;

    private EditText jid_text;
    private EditText name_text;
    private static String TAG = "ContactFragment";

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        db = new DatabaseHandler(getContext());

        FloatingActionButton addContactfab = view.findViewById(R.id.add_contacts);

        contactsRecyclerView = view.findViewById(R.id.contact_list_recycler_view);

        contacts = new ArrayList<>();
        listItems = new ArrayList<>();

        contactsRecyclerView.setHasFixedSize(true);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ContactAdapter(listItems);
        contactsRecyclerView.setAdapter(mAdapter);
        addContactfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddContactSelected();
            }
        });

        setUpRecyclerView();
        // Inflate the layout for this fragment
        return view;
    }

    public void onAddContactSelected(){

        Log.w(TAG,"fab clicked()");

        Button saveBtn;

        dialogBuilder = new AlertDialog.Builder(getContext());
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

        Log.w(TAG,"save to db called");
        Contact c =new Contact();
        c.setUserName(name_text.getText().toString());
        c.setJid(jid_text.getText().toString());

        db.addContact(c);

        setUpRecyclerView();

        dialog.dismiss();
    }

    private void setUpRecyclerView() {

        listItems.clear();
//        contacts = new ArrayList<>();
//        listItems = new ArrayList<>();
        Log.w(TAG,"setting up recyclerview");
        //get from DB
        contacts = db.getAllContacts();
        for(Contact c : contacts){
            Contact contact = new Contact();
            contact.setUserName(c.getUserName());
            contact.setJid(c.getJid());
            contact.setId(c.getId());

            listItems.add(contact);
        }

        mAdapter.notifyDataSetChanged();
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
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setMessage("Remove Contact")
                            .setNegativeButton("No",null)
                            .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DatabaseHandler db = new DatabaseHandler(getContext());
                                    db.deleteContact(mContact.getId());

                                    setUpRecyclerView();
//                                    startActivity(getActivity().getIntent());
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
                    Intent intent = new Intent(getContext()
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
//                Log.d(TAG,"Trying to work on a null Contact object ,returning.");
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

    @Override
    public void onDetach() {
        super.onDetach();

    }

}
