package com.modify.jabber.model;

public class Settings
{
    private String receivedColor;
    private String sentColor;
    private String id;

    public Settings(String id, String sentColor, String receivedColor) {
        this.id = id;
        this.sentColor = sentColor;
        this.receivedColor = receivedColor;
    }
    public Settings() {
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getReceivedColor() {
        return receivedColor;
    }

    public void setReceivedColor(String receivedColor) {
        this.receivedColor = receivedColor;
    }

    public String getSentColor() {
        return sentColor;
    }

    public void setSentColor(String sentColor) {
        this.sentColor = sentColor;
    }
}
