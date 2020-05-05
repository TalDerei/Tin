package edu.lehigh.cse216.tad222.backend;

public class User {

    private int id;
    private String email;
    private String nickname;
    private String userID;
    private String picture;
    private String bio;

    public User(int id, String email, String nickname, String userID, String picture, String bio) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.userID = userID;
        this.picture = picture;
        this.bio = bio;
    }

    public int getId() {
        return id;
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

    public String getPicture(){
        return picture;
    }

    public String getBio(){
        return bio;
    }
}