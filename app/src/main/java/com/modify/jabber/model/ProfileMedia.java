package com.modify.jabber.model;

public class ProfileMedia
{
    private String sender;
    private String message;
    private String type;
    private String caption;
    private String date;

    public ProfileMedia() {
    }

    public ProfileMedia(String sender, String message, String type, String caption, String date) {
        this.sender = sender;
        this.message = message;
        this.type = type;
        this.caption = caption;
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
}
