package edu.lehigh.cse216.tad222.backend;

public class User {

    private String name;
    private String userID;
    private String secret;

    public User(String name, String uid, String secret) {
        this.name = name;
        userID = uid;
        this.secret = secret;
    }

    public String getName() {
        return name;
    }
}