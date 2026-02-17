package com.placement.student.ui;

import com.placement.student.controller.ProfileController;
import com.placement.student.dao.*;
import com.placement.student.model.AcademicDetails;
import com.placement.student.model.StudentProfile;
import com.placement.student.service.*;
import javax.swing.*;
import java.awt.*;

public class ProfileView extends BaseFrame {

    private final long studentId;

    private final ProfileController profileController;

    // Profile fields (create if missing)
    private final JTextField firstNameField = new JTextField(12);
    private final JTextField lastNameField = new JTextField(12);
    private final JTextField genderField = new JTextField(8);
    private final JTextField imagePathField = new JTextField(18);

    // Academic fields
    private final JTextField programField = new JTextField(12);
    private final JTextField yearOfStudyField = new JTextField(5);
    private final JTextField gpaField = new JTextField(5);
    private final JTextField cgpaField = new JTextField(5);
    private final JTextField backlogsField = new JTextField(5);
    private final JTextField graduationYearField = new JTextField(6);
    private final JTextField eligibilityStatusField = new JTextField(10);

    public ProfileView(long studentId) {
        super("Profile & Academic Details");
        this.studentId = studentId;

        // light wiring
        StudentDao studentDao = new StudentDao();
        AcademicDetailsDAO academicDAO = new AcademicDetailsDAOImpl();

        StudentProfileService profileService = new StudentProfileService(studentDao);
        AcademicDetailsService academicService = new AcademicDetailsService(academicDAO);

        this.profileController = new ProfileController(profileService, academicService);

        add(buildContent(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        loadProfile();
        loadAcademic();

        setVisible(true);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new GridLayout(2, 1, 10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(buildProfilePanel());
        root.add(buildAcademicPanel());

        return root;
    }

    private JPanel buildProfilePanel() {
        JPanel p = new JPanel(new GridLayout(2, 4, 8, 8));
        p.setBorder(BorderFactory.createTitledBorder("Student Profile"));

        p.add(new JLabel("First Name"));
        p.add(new JLabel("Last Name"));
        p.add(new JLabel("Gender"));
        p.add(new JLabel("Profile Image Path"));

        p.add(firstNameField);
        p.add(lastNameField);
        p.add(genderField);
        p.add(imagePathField);

        return p;
    }

    private JPanel buildAcademicPanel() {
        JPanel p = new JPanel(new GridLayout(2, 7, 8, 8));
        p.setBorder(BorderFactory.createTitledBorder("Academic Details"));

        p.add(new JLabel("Program"));
        p.add(new JLabel("Year"));
        p.add(new JLabel("GPA"));
        p.add(new JLabel("CGPA"));
        p.add(new JLabel("Backlogs"));
        p.add(new JLabel("Grad Year"));
        p.add(new JLabel("Eligibility"));

        p.add(programField);
        p.add(yearOfStudyField);
        p.add(gpaField);
        p.add(cgpaField);
        p.add(backlogsField);
        p.add(graduationYearField);
        p.add(eligibilityStatusField);

        return p;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveProfileBtn = new JButton("Save Profile");
        JButton updateImageBtn = new JButton("Update Image Path");
        JButton saveAcademicBtn = new JButton("Save Academic");
        JButton refreshBtn = new JButton("Refresh");
        JButton closeBtn = new JButton("Close");

        saveProfileBtn.addActionListener(e -> saveProfileIfMissing());
        updateImageBtn.addActionListener(e -> updateImagePath());
        saveAcademicBtn.addActionListener(e -> saveAcademic());
        refreshBtn.addActionListener(e -> { loadProfile(); loadAcademic(); });
        closeBtn.addActionListener(e -> dispose());

        p.add(saveProfileBtn);
        p.add(updateImageBtn);
        p.add(saveAcademicBtn);
        p.add(refreshBtn);
        p.add(closeBtn);

        return p;
    }

    private void loadProfile() {
        try {
            // IMPORTANT: StudentDao uses userId. For now we treat studentId as userId.
            StudentProfile profile = profileController.viewProfile(studentId);

            if (profile == null) {
                // allow create
                firstNameField.setText("");
                lastNameField.setText("");
                genderField.setText("");
                imagePathField.setText("");
                UiUtil.info("No profile found. Fill details and click 'Save Profile'.");
                return;
            }

            firstNameField.setText(profile.getFirstName());
            lastNameField.setText(profile.getLastName());
            genderField.setText(profile.getGender());
            imagePathField.setText(profile.getProfileImagePath() == null ? "" : profile.getProfileImagePath());

        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void loadAcademic() {
        try {
            AcademicDetails d = profileController.viewAcademic(studentId);
            if (d == null) {
                // keep fields empty
                programField.setText("");
                yearOfStudyField.setText("");
                gpaField.setText("");
                cgpaField.setText("");
                backlogsField.setText("");
                graduationYearField.setText("");
                eligibilityStatusField.setText("");
                return;
            }

            programField.setText(nullToEmpty(d.getProgram()));
            yearOfStudyField.setText(String.valueOf(d.getYearOfStudy()));
            gpaField.setText(String.valueOf(d.getGpa()));
            cgpaField.setText(String.valueOf(d.getCgpa()));
            backlogsField.setText(String.valueOf(d.getBacklogs()));
            graduationYearField.setText(String.valueOf(d.getGraduationYear()));
            eligibilityStatusField.setText(nullToEmpty(d.getEligibilityStatus()));

        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void saveProfileIfMissing() {
        try {
            StudentProfile existing = profileController.viewProfile(studentId);
            if (existing != null) {
                UiUtil.info("Profile already exists (update not implemented in DAO).");
                return;
            }

            StudentProfile profile = new StudentProfile(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    genderField.getText().trim(),
                    emptyToNull(imagePathField.getText().trim())
            );

            profileController.createProfile(studentId, profile);
            UiUtil.info("Profile created.");
            loadProfile();

        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void updateImagePath() {
        try {
            String path = imagePathField.getText().trim();
            profileController.updateProfileImage(studentId, emptyToNull(path));
            UiUtil.info("Profile image path updated.");
            loadProfile();
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void saveAcademic() {
        try {
            AcademicDetails d = new AcademicDetails();
            d.setProgram(programField.getText().trim());
            d.setYearOfStudy(parseInt(yearOfStudyField.getText(), "Year of study"));
            d.setGpa(parseDouble(gpaField.getText(), "GPA"));
            d.setCgpa(parseDouble(cgpaField.getText(), "CGPA"));
            d.setBacklogs(parseInt(backlogsField.getText(), "Backlogs"));
            d.setGraduationYear(parseInt(graduationYearField.getText(), "Graduation year"));
            d.setEligibilityStatus(eligibilityStatusField.getText().trim());

            profileController.addOrUpdateAcademic(studentId, d);
            UiUtil.info("Academic details saved.");
            loadAcademic();

        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private static int parseInt(String s, String fieldName) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            throw new ServiceException("Invalid number for " + fieldName);
        }
    }

    private static double parseDouble(String s, String fieldName) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            throw new ServiceException("Invalid number for " + fieldName);
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
