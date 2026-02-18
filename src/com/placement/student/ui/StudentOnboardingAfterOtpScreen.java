package com.placement.student.ui;

import com.placement.common.dao.UserDao;
import com.placement.common.ui.LoginScreen;
import com.placement.common.ui.ProfilePictureScreen;
import com.placement.student.dao.AcademicDetailsDAO;
import com.placement.student.dao.AcademicDetailsDAOImpl;
import com.placement.student.dao.StudentDocumentDAOImpl;
import com.placement.student.service.AcademicDetailsService;
import com.placement.student.service.StudentDocumentService;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class StudentOnboardingAfterOtpScreen extends BaseFrame {

    private final String email;
    private final String gender;
    private final UserDao userDao = new UserDao();

    private long studentId;

    // academic fields
    private final JTextField programField = new JTextField(14);
    private final JTextField yearField = new JTextField(6);
    private final JTextField gpaField = new JTextField(6);
    private final JTextField cgpaField = new JTextField(6);
    private final JTextField backlogsField = new JTextField(6);
    private final JTextField gradYearField = new JTextField(8);

    private final JLabel cvStatus = new JLabel("No CV uploaded (you can do it later)");
    private File selectedCv;

    private final AcademicDetailsService academicService;
    private final StudentDocumentService documentService;

    public StudentOnboardingAfterOtpScreen(String email, String gender) {
        super("Complete Your Profile");
        this.email = email;
        this.gender = gender;

        AcademicDetailsDAO acadDao = new AcademicDetailsDAOImpl();
        this.academicService = new AcademicDetailsService(acadDao);
        this.documentService = new StudentDocumentService(new StudentDocumentDAOImpl());

        resolveStudentId();

        add(StudentTheme.header("Almost done!", "Enter academic details and optionally upload your CV."), BorderLayout.NORTH);

        JPanel center = StudentTheme.contentPanel();
        center.setLayout(new BorderLayout(12, 12));
        center.add(buildForm(), BorderLayout.CENTER);
        center.add(buildActions(), BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        setVisible(true);
    }

    private void resolveStudentId() {
        try {
            com.placement.common.model.User u = userDao.findByUsernameOrEmail(email);
            if (u == null) throw new Exception("User not found for email: " + email);
            this.studentId = u.getId();
        } catch (Exception ex) {
            UiUtil.error(ex.getMessage());
            dispose();
            new LoginScreen().setVisible(true);
        }
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(StudentTheme.BG);

        StudentTheme.styleField(programField);
        StudentTheme.styleField(yearField);
        StudentTheme.styleField(gpaField);
        StudentTheme.styleField(cgpaField);
        StudentTheme.styleField(backlogsField);
        StudentTheme.styleField(gradYearField);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int r = 0;

        addRow(p, g, r++, "Program", programField);
        addRow(p, g, r++, "Year of study", yearField);
        addRow(p, g, r++, "GPA", gpaField);
        addRow(p, g, r++, "CGPA", cgpaField);
        addRow(p, g, r++, "Backlogs", backlogsField);
        addRow(p, g, r++, "Graduation year", gradYearField);

        // CV row
        g.gridx = 0; g.gridy = r; g.gridwidth = 1;
        p.add(new JLabel("CV"), g);

        JButton chooseBtn = new JButton("Choose File");
        StudentTheme.styleSecondaryButton(chooseBtn);
        chooseBtn.addActionListener(e -> chooseCv());

        JPanel cvBox = new JPanel(new BorderLayout(8, 8));
        cvBox.setBackground(StudentTheme.BG);
        cvStatus.setFont(StudentTheme.fontRegular(12));
        cvBox.add(cvStatus, BorderLayout.CENTER);
        cvBox.add(chooseBtn, BorderLayout.EAST);

        g.gridx = 1; g.gridy = r; g.gridwidth = 1;
        p.add(cvBox, g);

        return p;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row;
        p.add(new JLabel(label), g);

        g.gridx = 1; g.gridy = row;
        p.add(field, g);
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        p.setBackground(StudentTheme.BG);

        JButton saveBtn = new JButton("Save & Continue");
        JButton skipPicBtn = new JButton("Finish (Later)");

        StudentTheme.stylePrimaryButton(saveBtn);
        StudentTheme.styleSecondaryButton(skipPicBtn);

        saveBtn.addActionListener(e -> saveAndGoToPicture());
        skipPicBtn.addActionListener(e -> finishToLogin());

        p.add(skipPicBtn);
        p.add(saveBtn);
        return p;
    }

    private void chooseCv() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select CV (PDF/DOC/DOCX)");
        int r = fc.showOpenDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;

        selectedCv = fc.getSelectedFile();
        cvStatus.setText("Selected: " + selectedCv.getName());
    }

    private void saveAcademic() throws Exception {
        String program = programField.getText().trim();
        int year = Integer.parseInt(yearField.getText().trim());
        double gpa = Double.parseDouble(gpaField.getText().trim());
        double cgpa = Double.parseDouble(cgpaField.getText().trim());
        int backlogs = Integer.parseInt(backlogsField.getText().trim());
        int gradYear = Integer.parseInt(gradYearField.getText().trim());

        com.placement.student.model.AcademicDetails d = new com.placement.student.model.AcademicDetails();
        d.setProgram(program);
        d.setYearOfStudy(year);
        d.setGpa(gpa);
        d.setCgpa(cgpa);
        d.setBacklogs(backlogs);
        d.setGraduationYear(gradYear);
        d.setEligibilityStatus("PENDING");
        academicService.addOrUpdate(studentId, d);

        if (selectedCv != null) {
            documentService.uploadCv(studentId, selectedCv);
        }
    }

    private void saveAndGoToPicture() {
        try {
            saveAcademic();
            // Optional: let them set profile picture now
            new ProfilePictureScreen(email, gender, false).setVisible(true);
            dispose();
        } catch (Exception ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void finishToLogin() {
        try {
            saveAcademic();
        } catch (Exception ex) {
            // If they skip finishing and the form is blank, don't block them.
            // They can complete later from Profile.
        }
        new LoginScreen().setVisible(true);
        dispose();
    }
}
