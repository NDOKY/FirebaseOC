package com.openclassrooms.firebaseoc.models;

import androidx.annotation.Nullable;


public class User {

    private String uid;
    private String username;
    private Boolean isMentor;
    @Nullable
    private String urlPicture;

    public User(){}

    public User(String uid, String username, String urlPicture) {
        this.uid = uid;
        this.username = username;
        this.isMentor = false;
        this.urlPicture = urlPicture;
    }

    // ---GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public Boolean getIsMentor() { return isMentor; }
    public String getUrlPicture() { return urlPicture; }

    // --- SETTERS ----

    public void setUid(String uid) { this.uid = uid; }
    public void setUsername(String username) { this.username = username; }
    public void setMentor(Boolean mentor) { isMentor = mentor; }
    public void setUrlPicture(String urlPicture) { this.urlPicture = urlPicture; }
}
