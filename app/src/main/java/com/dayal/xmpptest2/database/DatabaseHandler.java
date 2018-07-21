package com.dayal.xmpptest2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dayal.xmpptest2.models.Contact;
import com.dayal.xmpptest2.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manjeet Dayal on 18-07-2018.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    private Context context;

    public DatabaseHandler(Context context) {
        super(context, Util.DB_NAME,null , Util.DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_CONTACTS_TABLE= "CREATE TABLE " + Util.TABLE_NAME + "(" + Util.KEY_ID + " INTEGER PRIMARY KEY," +
                Util.KEY_NAME + " TEXT," + Util.KEY_JID + " TEXT);";
        db.execSQL(CREATE_CONTACTS_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + Util.TABLE_NAME);
        onCreate(db);

    }

    public void addContact(Contact Contact){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(Util.KEY_NAME,Contact.getUserName());
        values.put(Util.KEY_JID,Contact.getJid());


        db.insert(Util.TABLE_NAME,null,values);

    }

    public Contact getContact(int id){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.query(Util.TABLE_NAME,new String[]{Util.KEY_ID,Util.KEY_NAME,Util.KEY_JID},
                Util.KEY_ID + "=?",new String[]{String.valueOf(id)},null,null,null,null);
        if(cursor!=null)
            cursor.moveToFirst();
        Contact Contact=new Contact();
        Contact.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Util.KEY_ID))));
        Contact.setUserName(cursor.getString(cursor.getColumnIndex(Util.KEY_NAME)));
        Contact.setJid(cursor.getString(cursor.getColumnIndex(Util.KEY_JID)));
        //convert timestamp to something readable

        return Contact;
    }

    public List<Contact> getAllContacts(){
        SQLiteDatabase db=this.getReadableDatabase();
        List<Contact> ContactList=new ArrayList<>();
        Cursor cursor=db.query(Util.TABLE_NAME,new String[]{Util.KEY_ID,Util.KEY_NAME,Util.KEY_JID}
                ,null,null,null,null,Util.KEY_ID );

        if(cursor.moveToFirst()){
            do{
                Contact Contact=new Contact();
                Contact.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Util.KEY_ID))));
                Contact.setUserName(cursor.getString(cursor.getColumnIndex(Util.KEY_NAME)));
                Contact.setJid(cursor.getString(cursor.getColumnIndex(Util.KEY_JID)));
                //convert timestamp to something readable

                ContactList.add(Contact);
            }while ((cursor.moveToNext()));
        }
        cursor.close();
        return  ContactList;
    }

    public void deleteContact(int id){
        SQLiteDatabase db =this.getWritableDatabase();
        db.delete(Util.TABLE_NAME,Util.KEY_ID + "=?",new String[]{String.valueOf(id)});
        db.close();
    }

}
