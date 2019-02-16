package com.netobjex.mychatapp.entities;

/**
 * Created by jibin on 1/27/2019.
 */

public class ChatMessage {
    private String content;
    private boolean isMine;

    public ChatMessage(String content, boolean isMine) {
        this.content = content;
        this.isMine = isMine;
    }

    public String getContent() {
        return content;
    }

    public boolean isMine() {
        return isMine;
    }
}
