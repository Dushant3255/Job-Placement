package com.placement.student.ui;

import com.placement.student.controller.InterviewController;
import com.placement.student.dao.InterviewDAO;
import com.placement.student.dao.InterviewDAOImpl;
import com.placement.student.model.Interview;
import com.placement.student.service.InterviewService;
import com.placement.student.service.ServiceException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class InterviewsView extends JPanel {

    private final long studentId;
    private final InterviewController interviewController;

    private final DefaultTableModel model;
    private final JTable table;

    private final CardLayout card = new CardLayout();
    private final JPanel tableOrEmpty = new JPanel(card);

    // Filters
    private final JComboBox<String> modeFilter = new JComboBox<>(new String[]{"All", "Online", "Face-to-face"});
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "SCHEDULED", "COMPLETED", "CANCELLED"});

    // Cache last loaded list so filter changes don't hit DB every time
    private List<Interview> cache = new ArrayList<>();

    public InterviewsView(long studentId) {
        setLayout(new BorderLayout());
        setBackground(StudentTheme.BG);
        this.studentId = studentId;

        InterviewDAO interviewDAO = new InterviewDAOImpl();
        InterviewService interviewService = new InterviewService(interviewDAO);
        this.interviewController = new InterviewController(interviewService);

        model = new DefaultTableModel(new Object[]{
                "Interview ID", "Application ID", "Scheduled At", "Mode", "Office Location", "Meeting Link", "Status", "Notes"
        }, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        StudentTheme.styleTable(table);
        table.getColumnModel().getColumn(6).setCellRenderer(StudentTheme.statusChipRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(StudentTheme.statusChipRenderer());

        add(StudentTheme.header("Interviews", "View interview schedule and open meeting links."), BorderLayout.NORTH);

        JPanel center = StudentTheme.contentPanel();
        center.setLayout(new BorderLayout(12, 12));
        center.add(buildTopBar(), BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        tableOrEmpty.add(sp, "TABLE");
        tableOrEmpty.add(StudentTheme.emptyState(
                "No interviews scheduled yet",
                "If a company schedules an interview, it will show up here."), "EMPTY");
        center.add(tableOrEmpty, BorderLayout.CENTER);

        center.add(buildActions(), BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        refreshInterviews();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(StudentTheme.BG);

        JLabel title = new JLabel("Interviews for Student ID: " + studentId);
        title.setFont(StudentTheme.fontBold(13));
        bar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(StudentTheme.BG);

        styleCombo(modeFilter);
        styleCombo(statusFilter);

        JButton clearBtn = new JButton("Clear");
        JButton refreshBtn = new JButton("Refresh");
        StudentTheme.styleSecondaryButton(clearBtn);
        StudentTheme.styleSecondaryButton(refreshBtn);

        right.add(new JLabel("Mode"));
        right.add(modeFilter);
        right.add(new JLabel("Status"));
        right.add(statusFilter);
        right.add(clearBtn);
        right.add(refreshBtn);

        modeFilter.addActionListener(e -> applyFilters());
        statusFilter.addActionListener(e -> applyFilters());

        clearBtn.addActionListener(e -> {
            modeFilter.setSelectedItem("All");
            statusFilter.setSelectedItem("All");
            applyFilters();
        });

        refreshBtn.addActionListener(e -> refreshInterviews());

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        p.setBackground(StudentTheme.BG);

        JButton openLinkBtn = new JButton("Open Meeting Link");
        JButton closeBtn = new JButton("Close");

        StudentTheme.stylePrimaryButton(openLinkBtn);
        StudentTheme.styleSecondaryButton(closeBtn);

        openLinkBtn.addActionListener(e -> openSelectedMeetingLink());
        closeBtn.addActionListener(e -> StudentNav.goHome(this));

        p.add(openLinkBtn);
        p.add(closeBtn);
        return p;
    }

    private void refreshInterviews() {
        try {
            cache = interviewController.viewMyInterviews(studentId);
            applyFilters();
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void applyFilters() {
        String modeSel = String.valueOf(modeFilter.getSelectedItem());
        String statusSel = String.valueOf(statusFilter.getSelectedItem());

        model.setRowCount(0);

        for (Interview i : cache) {
            if (!"All".equalsIgnoreCase(modeSel)) {
                if (i.getMode() == null || !i.getMode().equalsIgnoreCase(modeSel)) continue;
            }
            if (!"All".equalsIgnoreCase(statusSel)) {
                if (i.getStatus() == null || !i.getStatus().equalsIgnoreCase(statusSel)) continue;
            }

            model.addRow(new Object[]{
                    i.getInterviewId(),
                    i.getApplicationId(),
                    i.getScheduledAt(),
                    i.getMode(),
                    i.getLocation(),
                    i.getMeetingLink(),
                    i.getStatus(),
                    i.getNotes()
            });
        }

        if (model.getRowCount() == 0) card.show(tableOrEmpty, "EMPTY");
        else card.show(tableOrEmpty, "TABLE");
    }

    private String getSelectedMeetingLink() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object val = model.getValueAt(row, 5);
        return val == null ? null : val.toString().trim();
    }

    private void openSelectedMeetingLink() {
        String link = getSelectedMeetingLink();
        if (link == null || link.isEmpty()) {
            UiUtil.error("Select an interview with a meeting link.");
            return;
        }

        try {
            if (!Desktop.isDesktopSupported()) {
                UiUtil.error("Desktop browsing is not supported on this system.");
                return;
            }
            Desktop.getDesktop().browse(new URI(link));
        } catch (Exception e) {
            UiUtil.error("Failed to open link: " + e.getMessage());
        }
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(StudentTheme.fontBold(12));
        combo.setBackground(new Color(243, 244, 246));
        combo.setForeground(new Color(31, 41, 55));
        combo.setBorder(new EmptyBorder(6, 8, 6, 8));
        combo.setFocusable(false);
    }
}
