package com.placement.student.ui;

import com.placement.student.controller.ProfileController;
import com.placement.student.dao.*;
import com.placement.student.model.AcademicDetails;
import com.placement.student.model.StudentProfile;
import com.placement.student.service.*;
import com.placement.common.dao.UserDao;
import com.placement.common.ui.ProfilePictureScreen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import java.net.URI;

public class ProfileView extends JPanel {

    private final long studentId;
    private final ProfileController profileController;

    // Profile fields
    private final JLabel photoPreview = new JLabel();
    private final JLabel cvLabel = new JLabel("No CV uploaded");
    private File selectedCvFile = null;
    private final StudentDocumentService documentService = new StudentDocumentService(new StudentDocumentDAOImpl());
    private final UserDao userDao = new UserDao();
    private String studentEmail = null;
    private final JTextField firstNameField = new JTextField(12);
    private final JTextField lastNameField = new JTextField(12);
    private final JTextField genderField = new JTextField(8);
    private final JTextField imagePathField = new JTextField(18); // kept for DB value (hidden in UI)

    // Academic fields
    private final JTextField programField = new JTextField(12);
    private final JTextField yearOfStudyField = new JTextField(6);
    private final JTextField gpaField = new JTextField(6);
    private final JTextField cgpaField = new JTextField(6);
    private final JTextField backlogsField = new JTextField(6);
    private final JTextField graduationYearField = new JTextField(8);
    private final JTextField eligibilityStatusField = new JTextField(12);

    public ProfileView(long studentId) {
        
        setLayout(new BorderLayout());
        setBackground(StudentTheme.BG);
this.studentId = studentId;

        StudentDao studentDao = new StudentDao();
        AcademicDetailsDAO academicDAO = new AcademicDetailsDAOImpl();

        StudentProfileService profileService = new StudentProfileService(studentDao);
        AcademicDetailsService academicService = new AcademicDetailsService(academicDAO);
        this.profileController = new ProfileController(profileService, academicService);

        add(StudentTheme.header("Profile", "Update your profile and academic details."), BorderLayout.NORTH);

        JPanel center = StudentTheme.contentPanel();
        center.setLayout(new BorderLayout(12, 12));
        center.add(buildContent(), BorderLayout.CENTER);
        center.add(buildActions(), BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        loadProfile();
        loadAcademic();

    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new GridLayout(2, 1, 12, 12));
        root.setBackground(StudentTheme.BG);

        root.add(buildProfilePanel());
        root.add(buildAcademicPanel());

        return root;
    }

    private JPanel buildProfilePanel() {
        JPanel wrapper = new JPanel(new BorderLayout(12, 12));
        wrapper.setBackground(StudentTheme.BG);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Student Profile"),
                new EmptyBorder(8, 10, 10, 10)
        ));

        // Left: fields
        JPanel fields = new JPanel(new GridLayout(3, 2, 10, 8));
        fields.setBackground(StudentTheme.BG);

        StudentTheme.styleField(firstNameField);
        StudentTheme.styleField(lastNameField);
        StudentTheme.styleField(genderField);

        fields.add(new JLabel("First Name"));
        fields.add(firstNameField);

        fields.add(new JLabel("Last Name"));
        fields.add(lastNameField);

        fields.add(new JLabel("Gender"));
        fields.add(genderField);

        // Right: photo preview + actions
        JPanel photoBox = new JPanel(new BorderLayout(8, 8));
        photoBox.setBackground(StudentTheme.BG);

        photoPreview.setPreferredSize(new Dimension(120, 120));
        photoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        photoPreview.setVerticalAlignment(SwingConstants.CENTER);
        photoPreview.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        photoBox.add(photoPreview, BorderLayout.CENTER);

        JPanel photoBtns = new JPanel(new GridLayout(2, 1, 6, 6));
        photoBtns.setBackground(StudentTheme.BG);

        JButton changePicBtn = new JButton("Change Picture");
        StudentTheme.styleSecondaryButton(changePicBtn);
        changePicBtn.addActionListener(e -> openProfilePictureUpdater());

        JButton uploadCvBtn = new JButton("Upload CV");
        StudentTheme.styleSecondaryButton(uploadCvBtn);
        uploadCvBtn.addActionListener(e -> chooseCvAndUpload());

        photoBtns.add(changePicBtn);
        photoBtns.add(uploadCvBtn);

        photoBox.add(photoBtns, BorderLayout.SOUTH);

        wrapper.add(fields, BorderLayout.CENTER);
        wrapper.add(photoBox, BorderLayout.EAST);

        return wrapper;
    }


    private JPanel buildAcademicPanel() {
        JPanel p = new JPanel(new GridLayout(3, 7, 10, 8));
        p.setBackground(StudentTheme.BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Academic Details"),
                new EmptyBorder(8, 10, 10, 10)
        ));

        StudentTheme.styleField(programField);
        StudentTheme.styleField(yearOfStudyField);
        StudentTheme.styleField(gpaField);
        StudentTheme.styleField(cgpaField);
        StudentTheme.styleField(backlogsField);
        StudentTheme.styleField(graduationYearField);
        StudentTheme.styleField(eligibilityStatusField);

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

        // CV row
        p.add(new JLabel("CV"));
        p.add(cvLabel);
        JButton openCvBtn = new JButton("Open CV");
        StudentTheme.styleSecondaryButton(openCvBtn);
        openCvBtn.addActionListener(e -> {
            try {
                String cv = documentService.getCvPath(studentId);
                if (cv == null || cv.isBlank()) { UiUtil.error("No CV uploaded yet."); return; }
                Desktop.getDesktop().open(new java.io.File(cv));
            } catch (Exception ex) { UiUtil.error("Unable to open CV: " + ex.getMessage()); }
        });
        p.add(openCvBtn);
        // filler cells
        p.add(new JLabel(""));
        p.add(new JLabel(""));
        p.add(new JLabel(""));
        p.add(new JLabel(""));

        return p;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        p.setBackground(StudentTheme.BG);

        JButton saveProfileBtn = new JButton("Save Profile");
        JButton updateImageBtn = new JButton("Update Image Path");
        JButton saveAcademicBtn = new JButton("Save Academic");
        JButton changePwBtn = new JButton("Change Password");
        JButton refreshBtn = new JButton("Refresh");
        JButton closeBtn = new JButton("Close");

        StudentTheme.stylePrimaryButton(saveProfileBtn);
        StudentTheme.styleSecondaryButton(updateImageBtn);
        StudentTheme.stylePrimaryButton(saveAcademicBtn);
        StudentTheme.styleSecondaryButton(refreshBtn);
        StudentTheme.styleSecondaryButton(closeBtn);

        saveProfileBtn.addActionListener(e -> saveProfileIfMissing());
        updateImageBtn.addActionListener(e -> updateImagePath());
        changePwBtn.addActionListener(e -> showChangePasswordDialog());
        saveAcademicBtn.addActionListener(e -> saveAcademic());
        refreshBtn.addActionListener(e -> { loadProfile(); loadAcademic(); });
        closeBtn.addActionListener(e -> StudentNav.goHome(this));

        p.add(saveProfileBtn);
        p.add(updateImageBtn);
        p.add(changePwBtn);
        p.add(saveAcademicBtn);
        p.add(refreshBtn);
        p.add(closeBtn);

        return p;
    }

    private void loadProfile() {
        try {
            StudentProfile profile = profileController.viewProfile(studentId);
            // Resolve email for later actions
            try { studentEmail = userDao.getEmailByUserId((int) studentId); } catch (Exception ignore) {}

            if (profile == null) {
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

            // Preview image
            try {
                photoPreview.setIcon(StudentTheme.loadProfileImage(profile.getProfileImagePath(), profile.getGender(), 120));
            } catch (Exception ignore) {}

            // CV label
            try {
                String cv = documentService.getCvPath(studentId);
                cvLabel.setText((cv == null || cv.isBlank()) ? "No CV uploaded" : new java.io.File(cv).getName());
            } catch (Exception ignore) {}

        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void loadAcademic() {
        try {
            AcademicDetails d = profileController.viewAcademic(studentId);
            if (d == null) {
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
                UiUtil.info("Profile already exists (update not implemented in DAO). Use 'Update Image Path' for now.");
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

    private void openProfilePictureUpdater() {
        if (studentEmail == null || studentEmail.isBlank()) {
            UiUtil.error("Unable to load your email. Please refresh and try again.");
            return;
        }
        String gender = genderField.getText() == null ? "" : genderField.getText().trim();
        ProfilePictureScreen screen = new ProfilePictureScreen(studentEmail, gender, false);
        screen.setVisible(true);
        UiUtil.info("After updating your picture, click Refresh here to see it.");
    }

    private void chooseCvAndUpload() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select CV (PDF/DOC/DOCX)");
        int res = fc.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = fc.getSelectedFile();
        try {
            String saved = documentService.uploadCv(studentId, f);
            cvLabel.setText(new java.io.File(saved).getName());
            UiUtil.info("CV uploaded successfully.");
        } catch (Exception ex) {
            UiUtil.error("CV upload failed: " + ex.getMessage());
        }
    }

    
    private void showChangePasswordDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this); // ✅ get the JFrame that contains this JPanel

        JDialog dialog = new JDialog(owner, "Change Password", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(StudentTheme.BG);
        root.setBorder(new javax.swing.border.EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Change Password");
        title.setFont(StudentTheme.fontBold(15));
        title.setForeground(StudentTheme.TEXT);
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 8));
        form.setBackground(StudentTheme.BG);

        JPasswordField current = new JPasswordField(14);
        JPasswordField next = new JPasswordField(14);
        JPasswordField confirm = new JPasswordField(14);

        StudentTheme.styleField(current);
        StudentTheme.styleField(next);
        StudentTheme.styleField(confirm);

        form.add(new JLabel("Current password"));
        form.add(current);
        form.add(new JLabel("New password"));
        form.add(next);
        form.add(new JLabel("Confirm new password"));
        form.add(confirm);

        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(StudentTheme.BG);

        JButton cancel = new JButton("Cancel");
        JButton save = new JButton("Update");
        StudentTheme.styleSecondaryButton(cancel);
        StudentTheme.stylePrimaryButton(save);

        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            String cur = new String(current.getPassword());
            String np = new String(next.getPassword());
            String cp = new String(confirm.getPassword());

            if (np.length() < 8) { UiUtil.error("New password must be at least 8 characters."); return; }
            if (!np.equals(cp)) { UiUtil.error("New password and confirmation do not match."); return; }

            try {
                String existingHash = userDao.getPasswordHashByUserId((int) studentId);
                if (!com.placement.common.util.PasswordUtil.verify(cur, existingHash)) {
                    UiUtil.error("Current password is incorrect.");
                    return;
                }
                String newHash = com.placement.common.util.PasswordUtil.hash(np);
                userDao.updatePasswordHashByUserId((int) studentId, newHash);

                dialog.dispose();
                UiUtil.info("Password updated successfully.");
            } catch (Exception ex) {
                UiUtil.error("Failed to change password: " + ex.getMessage());
            }
        });

        actions.add(cancel);
        actions.add(save);
        root.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(owner); // ✅ center on the main window
        dialog.setVisible(true);
    }
}
