package com.dayal.xmpptest2.models;

/**
 * Created by Manjeet Dayal on 07-07-2018.
 */

public class Contact {

    private String jid;
    private int id;
    private String userName;

    public Contact() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Contact(String contactJid )
    {
        jid = contactJid;
    }

    public String getUserName() {
        return userName;
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getJid()
    {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }
}
