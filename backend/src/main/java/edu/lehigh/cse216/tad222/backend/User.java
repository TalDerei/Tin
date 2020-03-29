package edu.lehigh.cse216.tad222.backend;

public class User {

    private String name;
    private String clientID;
    private String userID;

    public User(String name, String cid, String userID) {
        this.name = name;
        clientID = cid;
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public String getClientID() {
        return clientID;
    }

    public String getUserID() {
        return userID;
    }
}