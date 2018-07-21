package com.dayal.xmpptest2.models;

/**
 * Created by Manjeet Dayal on 19-07-2018.
 */

public class Group  {

    private String address;
    private int id;
    private String gpName;

    public Group() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGpName() {
        return gpName;
    }

    public void setGpName(String gpName) {
        this.gpName = gpName;
    }
}
