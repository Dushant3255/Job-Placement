package com.placement.student.ui;

import com.placement.common.ui.LoginScreen;
import com.placement.student.dao.StudentDao;
import com.placement.student.model.StudentProfile;
import com.placement.student.service.StudentProfileService;

import javax.swing.*;
import java.awt.*;

/**
 * Student shell window: sidebar navigation + header + CardLayout pages.
 */
public class StudentDashboardView extends BaseFrame {

    private final long studentId;
    private final StudentProfileService profileService;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    public StudentDashboardView(long studentId) {
        super("Student Dashboard");
        this.studentId = studentId;
        this.profileService = new StudentProfileService(new StudentDao());

        StudentProfile profile = null;
        try {
            profile = profileService.getProfile(studentId);
        } catch (Exception ignored) {}

        String displayName = (profile == null)
                ? ("Student #" + studentId)
                : (safe(profile.getFirstName()) + " " + safe(profile.getLastName())).trim();

        JLabel avatar = StudentTheme.avatarLabel(
                profile == null ? null : profile.getProfileImagePath(),
                profile == null ? null : profile.getGender(),
                44
        );

        add(StudentTheme.dashboardHeader(
                "Student Dashboard",
                "Manage applications, interviews, offers, and your profile.",
                avatar,
                displayName
        ), BorderLayout.NORTH);

        add(buildSidebar(), BorderLayout.WEST);

        // Pages
        JPanel home = buildHome();
        JobListingsView jobs = new JobListingsView(studentId);
        MyApplicationsView apps = new MyApplicationsView(studentId);
        OffersView offers = new OffersView(studentId);
        InterviewsView interviews = new InterviewsView(studentId);
        OffCampusView offCampus = new OffCampusView(studentId);
        ProfileView profileView = new ProfileView(studentId);
        PolicyView policy = new PolicyView();

        Runnable goHome = () -> cardLayout.show(cards, "home");
        // Allow pages to navigate back to home using StudentNav.goHome(this)
        for (JComponent page : new JComponent[]{home, jobs, apps, offers, interviews, offCampus, profileView, policy}) {
            page.putClientProperty("goHome", goHome);
        }

        cards.add(home, "home");
        cards.add(jobs, "jobs");
        cards.add(apps, "apps");
        cards.add(offers, "offers");
        cards.add(interviews, "interviews");
        cards.add(offCampus, "offcampus");
        cards.add(profileView, "profile");
        cards.add(policy, "policy");

        add(cards, BorderLayout.CENTER);

        cardLayout.show(cards, "home");
        setVisible(true);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = StudentTheme.sidebar();
        sidebar.setPreferredSize(new Dimension(210, 0));

        JLabel section = new JLabel("Navigation");
        section.setFont(StudentTheme.fontBold(12));
        section.setForeground(StudentTheme.TEXT_MUTED);
        section.setBorder(new javax.swing.border.EmptyBorder(0, 4, 10, 0));
        sidebar.add(section);

        JButton homeBtn = StudentTheme.navButton("Home");
        JButton jobsBtn = StudentTheme.navButton("Job Listings");
        JButton appsBtn = StudentTheme.navButton("My Applications");
        JButton offersBtn = StudentTheme.navButton("Offers");
        JButton interviewsBtn = StudentTheme.navButton("Interviews");
        JButton offCampusBtn = StudentTheme.navButton("Off-Campus Jobs");
        JButton profileBtn = StudentTheme.navButton("Profile & Academic");
        JButton policyBtn = StudentTheme.navButton("Policy");
        JButton logoutBtn = StudentTheme.navButtonDanger("Logout");

        homeBtn.addActionListener(e -> cardLayout.show(cards, "home"));
        jobsBtn.addActionListener(e -> cardLayout.show(cards, "jobs"));
        appsBtn.addActionListener(e -> cardLayout.show(cards, "apps"));
        offersBtn.addActionListener(e -> cardLayout.show(cards, "offers"));
        interviewsBtn.addActionListener(e -> cardLayout.show(cards, "interviews"));
        offCampusBtn.addActionListener(e -> cardLayout.show(cards, "offcampus"));
        profileBtn.addActionListener(e -> cardLayout.show(cards, "profile"));
        policyBtn.addActionListener(e -> cardLayout.show(cards, "policy"));

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginScreen().setVisible(true);
        });

        sidebar.add(homeBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(jobsBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(appsBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(offersBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(interviewsBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(offCampusBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(profileBtn);

        sidebar.add(Box.createVerticalStrut(14));
        JSeparator sep = new JSeparator();
        sep.setForeground(StudentTheme.BORDER);
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(policyBtn);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JPanel buildHome() {
        JPanel wrap = StudentTheme.contentPanel();
        wrap.setLayout(new BorderLayout(12, 12));

        JPanel welcomeCard = new JPanel(new BorderLayout(10, 10));
        welcomeCard.setBackground(StudentTheme.BG);
        welcomeCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StudentTheme.BORDER),
                new javax.swing.border.EmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Quick tips");
        title.setFont(StudentTheme.fontBold(15));
        title.setForeground(StudentTheme.TEXT);

        JTextArea tips = new JTextArea(
                "- Browse Job Listings (OPEN and CLOSED)\n" +
                "- Upload your CV in Profile (used automatically when you apply)\n" +
                "- Check Interviews and open meeting links when scheduled\n" +
                "- My Applications shows company + job title + current status"
        );
        tips.setFont(StudentTheme.fontPlain(13));
        tips.setForeground(StudentTheme.TEXT_MUTED);
        tips.setBackground(StudentTheme.BG);
        tips.setEditable(false);
        tips.setBorder(null);

        welcomeCard.add(title, BorderLayout.NORTH);
        welcomeCard.add(tips, BorderLayout.CENTER);

        wrap.add(welcomeCard, BorderLayout.NORTH);
        wrap.add(StudentTheme.emptyState("Welcome", "Use the left sidebar to navigate your student portal."), BorderLayout.CENTER);

        return wrap;
    }
}
