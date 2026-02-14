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

    public ManageStudentsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        add(createTopBar(), BorderLayout.NORTH);

        String[] cols = {"User ID", "Username", "Email", "Name", "Program", "Year", "GPA", "CGPA"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);

        load(null);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(new Color(200, 200, 200));
        bar.setBorder(new EmptyBorder(10, 10, 10, 10));

        bar.add(new JLabel("Search:"));
        bar.add(searchField);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> load(searchField.getText()));
        bar.add(searchBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            load(null);
        });
        bar.add(refreshBtn);

        return bar;
    }

    private void load(String keyword) {
        tableModel.setRowCount(0);
        List<StudentRow> rows = dao.listAll(keyword);
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
}
