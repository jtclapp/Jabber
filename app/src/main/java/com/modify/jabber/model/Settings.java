package com.modify.jabber.model;

public class Settings
{
    private String receivedColor;
    private String sentColor;
    private String receivedTextColor;
    private String sentTextColor;
    private String id;

    public Settings(String id, String sentColor, String receivedColor,String sentTextColor,String receivedTextColor) {
        this.id = id;
        this.sentColor = sentColor;
        this.receivedColor = receivedColor;
        this.sentTextColor = sentTextColor;
        this.receivedTextColor = receivedTextColor;
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
    public String getReceivedTextColor() {
        return receivedTextColor;
    }
    public void setReceivedTextColor(String receivedTextColor) {
        this.receivedTextColor = receivedTextColor;
    }
    public String getSentTextColor() {
        return sentTextColor;
    }
    public void setSentTextColor(String sentTextColor) {
        this.sentTextColor = sentTextColor;
    }
}
