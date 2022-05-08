package com.geri.chat.data.model;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private String text;
    private User sender;
    private User receiver;
    private LocalDateTime timeSent;



    public Message(String text, User sender, User receiver, LocalDateTime timeSent) {
        this.text = text;
        this.sender = sender;
        this.receiver = receiver;
        this.timeSent = timeSent;
    }

    public Message(String text, User sender, User receiver, String timeSent) {
        this.text = text;
        this.sender = sender;
        this.receiver = receiver;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        this.timeSent = LocalDateTime.parse(timeSent.replace('T', ' '));
    }



    public String getText() {
        return text;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public LocalDateTime getTimeSent() {
        return timeSent;
    }

}
