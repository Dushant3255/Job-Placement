package com.placement.common.model;

public class User {
    private final int id;
    private final UserRole role;
    private final String username;
    private final String email;
    private final boolean verified;

    public User(int id, UserRole role, String username, String email, boolean verified) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.email = email;
        this.verified = verified;
    }

    public int getId() { return id; }
    public UserRole getRole() { return role; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isVerified() { return verified; }
}
