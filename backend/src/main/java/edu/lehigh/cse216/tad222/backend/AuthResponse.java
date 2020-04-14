package edu.lehigh.cse216.tad222.backend;

public class AuthResponse {
    String mJWT;

    String mUser_id;

    public AuthResponse(String jwt, String uid) {
        mJWT = jwt;
        mUser_id = uid;
    }
}