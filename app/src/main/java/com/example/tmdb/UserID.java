package com.example.tmdb;

public class UserID {
 static    String  id;
 static String img_uri;

    public static String getImg_uri() {
        return img_uri;
    }

    public static void setImg_uri(String img_uri) {
        UserID.img_uri = img_uri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
