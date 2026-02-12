package com.placement.student.model;

public class StudentProfile {
    private final String firstName;
    private final String lastName;
    private final String gender;
    private final String profileImagePath; // can be null for now

    public StudentProfile(String firstName, String lastName, String gender, String profileImagePath) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.profileImagePath = profileImagePath;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getGender() { return gender; }
    public String getProfileImagePath() { return profileImagePath; }
}
