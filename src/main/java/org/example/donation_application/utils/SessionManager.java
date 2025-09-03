package org.example.donation_application.utils;

import org.example.donation_application.models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private String userType;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user, String userType) {
        this.currentUser = user;
        this.userType = userType;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getUserType() {
        return userType;
    }

    public void logout() {
        currentUser = null;
        userType = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
