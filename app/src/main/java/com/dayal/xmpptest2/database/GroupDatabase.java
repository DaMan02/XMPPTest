package com.dayal.xmpptest2.database;

import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

import com.dayal.xmpptest2.models.Group;
import com.dayal.xmpptest2.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manjeet Dayal on 18-07-2018.
 */

public class GroupDatabase extends SQLiteOpenHelper {
    private Context context;

    public GroupDatabase(Context context) {
        super(context, Util.GROUP_DB_NAME,null , Util.GROUP_DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_CONTACTS_TABLE= "CREATE TABLE " + Util.GROUP_TABLE_NAME + "(" + Util.KEY_GP_ID + " INTEGER PRIMARY KEY," +
                Util.KEY_GP_NAME + " TEXT," + Util.KEY_ADDRESS + " TEXT);";
        db.execSQL(CREATE_CONTACTS_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + Util.GROUP_TABLE_NAME);
        onCreate(db);

    }

    public void addGroup(Group Group){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(Util.KEY_GP_NAME,Group.getGpName());
        values.put(Util.KEY_ADDRESS,Group.getAddress());


        db.insert(Util.GROUP_TABLE_NAME,null,values);

    }

    public Group getGroup(int id){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.query(Util.GROUP_TABLE_NAME,new String[]{Util.KEY_GP_ID,Util.KEY_GP_NAME,Util.KEY_ADDRESS},
                Util.KEY_GP_ID + "=?",new String[]{String.valueOf(id)},null,null,null,null);
        if(cursor!=null)
            cursor.moveToFirst();
        Group Group=new Group();
        Group.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Util.KEY_GP_ID))));
        Group.setGpName(cursor.getString(cursor.getColumnIndex(Util.KEY_GP_NAME)));
        Group.setAddress(cursor.getString(cursor.getColumnIndex(Util.KEY_ADDRESS)));
        //convert timestamp to something readable

        return Group;
    }

    public List<Group> getAllGroups(){
        SQLiteDatabase db=this.getReadableDatabase();
        List<Group> GroupList=new ArrayList<>();
        Cursor cursor=db.query(Util.GROUP_TABLE_NAME,new String[]{Util.KEY_GP_ID,Util.KEY_GP_NAME,Util.KEY_ADDRESS}
                ,null,null,null,null,Util.KEY_GP_ID );

        if(cursor.moveToFirst()){
            do{
                Group Group=new Group();
                Group.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Util.KEY_GP_ID))));
                Group.setGpName(cursor.getString(cursor.getColumnIndex(Util.KEY_GP_NAME)));
                Group.setAddress(cursor.getString(cursor.getColumnIndex(Util.KEY_ADDRESS)));
                //convert timestamp to something readable

                GroupList.add(Group);
            }while ((cursor.moveToNext()));
        }
        cursor.close();
        return  GroupList;
    }

    public void deleteGroup(int id){
        SQLiteDatabase db =this.getWritableDatabase();
        db.delete(Util.GROUP_TABLE_NAME,Util.KEY_GP_ID + "=?",new String[]{String.valueOf(id)});
        db.close();
    }

}
