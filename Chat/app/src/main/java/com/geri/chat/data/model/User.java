package com.geri.chat.data.model;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User {
    private String id;
    private String name;
    private String lastMessage;
    private LocalDateTime lastMessageSent;
    private DocumentReference chatRef;

    public DocumentReference getChatRef() {
        return chatRef;
    }

    public User(String id, String name, String lastMessage, LocalDateTime lastMessageSent) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.lastMessageSent = lastMessageSent;
    }

    public User(String id, String name, String lastMessage, LocalDateTime lastMessageSent, DocumentReference chatRef) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.lastMessageSent = lastMessageSent;
        this.chatRef = chatRef;
    }

    public User(String id, String name, String lastMessage, String lastMessageSent) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        this.lastMessageSent = LocalDateTime.parse(lastMessageSent.replace('T', ' '),formatter);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public String getLastMessage() {
        return lastMessage;
    }

    public LocalDateTime getLastMessageSent() {
        return lastMessageSent;
    }

    public String getLastMessageSentAsString() {
        return lastMessageSent.toString();
    }

}
