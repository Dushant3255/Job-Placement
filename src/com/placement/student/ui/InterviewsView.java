package com.placement.student.ui;

import com.placement.student.controller.InterviewController;
import com.placement.student.dao.InterviewDAO;
import com.placement.student.dao.InterviewDAOImpl;
import com.placement.student.model.Interview;
import com.placement.student.service.InterviewService;
import com.placement.student.service.ServiceException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Desktop;
import java.net.URI;
import java.util.List;

public class InterviewsView extends BaseFrame {

    private final long studentId;
    private final InterviewController interviewController;

    private final DefaultTableModel model;
    private final JTable table;

    public InterviewsView(long studentId) {
        super("My Interviews");
        this.studentId = studentId;

        // light wiring
        InterviewDAO interviewDAO = new InterviewDAOImpl();
        InterviewService interviewService = new InterviewService(interviewDAO);
        this.interviewController = new InterviewController(interviewService);

        model = new DefaultTableModel(new Object[]{"Interview ID", "Application ID", "Scheduled At", "Mode", "Status", "Meeting Link"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);

        add(buildTopBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        refreshInterviews();

        setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Interviews for Student ID: " + studentId);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        p.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshInterviews());
        p.add(refreshBtn, BorderLayout.EAST);

        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return p;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton openLinkBtn = new JButton("Open Meeting Link");
        JButton closeBtn = new JButton("Close");

        openLinkBtn.addActionListener(e -> openSelectedMeetingLink());
        closeBtn.addActionListener(e -> dispose());

        p.add(openLinkBtn);
        p.add(closeBtn);
        return p;
    }

    private void refreshInterviews() {
        try {
            List<Interview> interviews = interviewController.viewMyInterviews(studentId);
            model.setRowCount(0);
            for (Interview i : interviews) {
                model.addRow(new Object[]{
                        i.getInterviewId(),
                        i.getApplicationId(),
                        i.getScheduledAt(),
                        i.getMode(),
                        i.getStatus(),
                        i.getMeetingLink()
                });
            }
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
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
}
