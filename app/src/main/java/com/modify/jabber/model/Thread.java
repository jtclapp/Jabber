package com.modify.jabber.model;
import java.io.Serializable;

public class Thread implements Serializable
{
    private String id;
    private String title;
    private String sender;
    private String type;
    private String caption;
    private String date;
    private String image1;
    private String image2;
    private String image3;

    public Thread(String id, String title, String sender, String type, String caption,
                  String date, String image1, String image2, String image3) {
        this.id = id;
        this.title = title;
        this.sender = sender;
        this.type = type;
        this.caption = caption;
        this.date = date;
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
    }
    public Thread()
    {

    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
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

    public String getImage1() {
        return image1;
    }
    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }
    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getImage3() {
        return image3;
    }
    public void setImage3(String image3) {
        this.image3 = image3;
    }
}
