package com.example.bicycleapp;

public class UserDataManager {

    private static UserDataManager instance;
    private String fullName;

    private UserDataManager() {
        // Private constructor to prevent instantiation
    }

    public static synchronized UserDataManager getInstance() {
        if (instance == null) {
            instance = new UserDataManager();
        }
        return instance;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
