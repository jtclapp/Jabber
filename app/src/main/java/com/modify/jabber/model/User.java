package com.modify.jabber.model;

import java.io.Serializable;

public class User implements Serializable
{
    private String id;
    private String username;
    private String imageURL;
    private String status;
    private String typing;
    private String search;
    private String bio;

    public User(String id, String username, String imageURL,
                String status, String typing, String search, String bio) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.typing = typing;
        this.search = search;
        this.bio = bio;
    }

    public User() {

    }
    public String getTyping() {
        return typing;
    }
    public void setTyping(String typing) {
        this.typing = typing;
    }
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
