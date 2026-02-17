package com.placement.student.ui;

import javax.swing.*;
import java.awt.*;

public class StudentDashboardView extends BaseFrame {

    private final long studentId;  // you’ll pass this after login

    public StudentDashboardView(long studentId) {
        super("Student Dashboard");
        this.studentId = studentId;

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildMenu(), BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Welcome, Student ID: " + studentId);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        top.add(title, BorderLayout.WEST);
        return top;
    }

    private JPanel buildMenu() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton jobsBtn = new JButton("Job Listings");
        JButton offersBtn = new JButton("Offers");
        JButton interviewsBtn = new JButton("Interviews");
        JButton offCampusBtn = new JButton("Off-Campus Jobs");
        JButton profileBtn = new JButton("Profile & Academic");
        JButton logoutBtn = new JButton("Logout");

        jobsBtn.addActionListener(e -> new JobListingsView(studentId));
        offersBtn.addActionListener(e -> new OffersView(studentId));
        interviewsBtn.addActionListener(e -> new InterviewsView(studentId));
        offCampusBtn.addActionListener(e -> new OffCampusView(studentId));
        profileBtn.addActionListener(e -> new ProfileView(studentId));
        logoutBtn.addActionListener(e -> dispose());

        panel.add(jobsBtn);
        panel.add(offersBtn);
        panel.add(interviewsBtn);
        panel.add(offCampusBtn);
        panel.add(profileBtn);
        panel.add(logoutBtn);

        return panel;
    }
}
