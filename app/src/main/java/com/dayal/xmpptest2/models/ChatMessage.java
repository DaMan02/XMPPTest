package com.dayal.xmpptest2.models;

import android.media.Image;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by Manjeet Dayal on 12-07-2018.
 */

public class ChatMessage {

    private  String message;
    private Contact sender;
    private long timestamp;
    private String MesssageType;

    public ChatMessage() {
    }

    public ChatMessage(String message, Contact sender, long timestamp) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;

    }

    public String getMesssageType() {
        return MesssageType;
    }

    public void setMesssageType(String messsageType) {
        MesssageType = messsageType;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Contact getSender() {
        return sender;
    }

    public void setSender(Contact sender) {
        this.sender = sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
