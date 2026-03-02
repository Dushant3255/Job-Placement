package com.placement.admin.ui;

import com.placement.admin.dao.AdminStudentDao;
import com.placement.admin.dao.AdminStudentDao.StudentRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageStudentsPanel extends JPanel {

    private final AdminStudentDao dao = new AdminStudentDao();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> programFilter = new JComboBox<>();
    private final JComboBox<String> yearFilter = new JComboBox<>();

    public ManageStudentsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(AdminTheme.BG);

        add(createTopBar(), BorderLayout.NORTH);

        String[] cols = {"User ID", "Username", "Email", "Name", "Program", "Year", "GPA", "CGPA"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AdminTheme.styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        AdminTheme.styleScrollPane(sp);
        add(sp, BorderLayout.CENTER);

        initFilters();
        load(null);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(AdminTheme.SURFACE);
        bar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel s1 = new JLabel("Search:");
        AdminTheme.styleLabel(s1);
        bar.add(s1);
        AdminTheme.styleField(searchField);
        bar.add(searchField);

        JLabel s2 = new JLabel("Program:");
        AdminTheme.styleLabel(s2);
        bar.add(s2);
        AdminTheme.styleCombo(programFilter);
        bar.add(programFilter);

        JLabel s3 = new JLabel("Year:");
        AdminTheme.styleLabel(s3);
        bar.add(s3);
        AdminTheme.styleCombo(yearFilter);
        bar.add(yearFilter);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> load(searchField.getText()));
        AdminTheme.styleButton(searchBtn, AdminTheme.ACCENT);
        bar.add(searchBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            load(null);
        });
        AdminTheme.styleButton(refreshBtn, AdminTheme.MUTED_BUTTON);
        bar.add(refreshBtn);

        return bar;
    }

    private void load(String keyword) {
        tableModel.setRowCount(0);

        String program = String.valueOf(programFilter.getSelectedItem());
        Integer year = null;

        String yearSel = String.valueOf(yearFilter.getSelectedItem());
        if (yearSel != null && !"All".equalsIgnoreCase(yearSel)) {
            try { year = Integer.parseInt(yearSel); } catch (Exception ignore) {}
        }

        List<StudentRow> rows = dao.listAll(keyword, program, year);
        for (StudentRow r : rows) {
            tableModel.addRow(new Object[]{
                    r.userId,
                    r.username,
                    r.email,
                    r.fullName(),
                    r.program,
                    r.yearOfStudy,
                    r.gpa,
                    r.cgpa
            });
        }
    }

    private void initFilters() {
        programFilter.removeAllItems();
        programFilter.addItem("All");
        for (String p : dao.listPrograms()) {
            programFilter.addItem(p);
        }

        yearFilter.removeAllItems();
        yearFilter.addItem("All");
        for (Integer y : dao.listYears()) {
            yearFilter.addItem(String.valueOf(y));
        }

        // Re-load when filter changes
        programFilter.addActionListener(e -> load(searchField.getText()));
        yearFilter.addActionListener(e -> load(searchField.getText()));
    }
}
