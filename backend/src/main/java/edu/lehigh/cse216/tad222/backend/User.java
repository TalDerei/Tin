package edu.lehigh.cse216.tad222.backend;

public class User {

    private String email;
    private String nickname;
    private String userID;
    private String bio;

    public User(String email, String nickname, String userID, String bio) {
        this.email = email;
        this.nickname = nickname;
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }
    
    public String getNickName() {
        return nickname;
    }

    public void setNickName(String newNickName) {
        nickname = newNickName;
    }

    public String getUserID() {
        return userID;
    }

    public String getBio(){
        return bio;
    }
}