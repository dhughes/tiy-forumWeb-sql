package com.theironyard;

/**
 * Created by doug on 5/5/16.
 */
public class Message {
    int id;
    int replyId;
    String text;

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    User author;

    public Message(int id, int replyId, User author, String text) {
        this.id = id;
        this.replyId = replyId;
        this.author = author;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReplyId() {
        return replyId;
    }

    public void setReplyId(int replyId) {
        this.replyId = replyId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
