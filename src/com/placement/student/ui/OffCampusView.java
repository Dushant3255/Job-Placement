package com.placement.student.ui;

import com.placement.student.controller.OffCampusController;
import com.placement.student.dao.OffCampusJobDAO;
import com.placement.student.dao.OffCampusJobDAOImpl;
import com.placement.student.model.OffCampusJob;
import com.placement.student.service.OffCampusService;
import com.placement.student.service.ServiceException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OffCampusView extends JPanel {

    private final long studentId;
    private final OffCampusController offCampusController;

    private final DefaultTableModel model;
    private final JTable table;

    private final CardLayout card = new CardLayout();
    private final JPanel tableOrEmpty = new JPanel(card);

    private final JTextField companyField = new JTextField(14);
    private final JTextField roleField = new JTextField(14);
    private final JTextField dateField = new JTextField(10);
    private final JTextField statusField = new JTextField(10);
    private final JTextField notesField = new JTextField(20);

    public OffCampusView(long studentId) {
        
        setLayout(new BorderLayout());
        setBackground(StudentTheme.BG);
this.studentId = studentId;

        OffCampusJobDAO dao = new OffCampusJobDAOImpl();
        OffCampusService service = new OffCampusService(dao);
        this.offCampusController = new OffCampusController(service);

        model = new DefaultTableModel(new Object[]{"OffCampus ID", "Company", "Role", "Applied Date", "Status", "Notes"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        StudentTheme.styleTable(table);
        table.getColumnModel().getColumn(4).setCellRenderer(StudentTheme.statusChipRenderer());

        add(StudentTheme.header("Off-Campus Jobs", "Track applications outside the portal."), BorderLayout.NORTH);

        JPanel center = StudentTheme.contentPanel();
        center.setLayout(new BorderLayout(12, 12));
        center.add(buildTopBar(), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        tableOrEmpty.add(sp, "TABLE");
        tableOrEmpty.add(StudentTheme.emptyState("No off-campus jobs yet", "Add an off-campus application to track it here."), "EMPTY");
        center.add(tableOrEmpty, BorderLayout.CENTER);
        center.add(buildBottom(), BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedIntoForm();
        });

        refresh();
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(StudentTheme.BG);

        JLabel title = new JLabel("Off-campus jobs for Student ID: " + studentId);
        title.setFont(StudentTheme.fontBold(13));
        p.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        StudentTheme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> refresh());
        p.add(refreshBtn, BorderLayout.EAST);

        return p;
    }

    private JPanel buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        bottom.setBackground(StudentTheme.BG);

        bottom.add(buildForm(), BorderLayout.CENTER);
        bottom.add(buildActions(), BorderLayout.SOUTH);

        return bottom;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridLayout(2, 5, 10, 8));
        form.setBackground(StudentTheme.BG);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Add / Edit"),
                new EmptyBorder(8, 10, 10, 10)
        ));

        StudentTheme.styleField(companyField);
        StudentTheme.styleField(roleField);
        StudentTheme.styleField(dateField);
        StudentTheme.styleField(statusField);
        StudentTheme.styleField(notesField);

        form.add(new JLabel("Company"));
        form.add(new JLabel("Role"));
        form.add(new JLabel("Applied Date"));
        form.add(new JLabel("Status"));
        form.add(new JLabel("Notes"));

        form.add(companyField);
        form.add(roleField);
        form.add(dateField);
        form.add(statusField);
        form.add(notesField);

        return form;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        p.setBackground(StudentTheme.BG);

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton clearBtn = new JButton("Clear");
        JButton closeBtn = new JButton("Close");

        StudentTheme.stylePrimaryButton(addBtn);
        StudentTheme.styleSecondaryButton(updateBtn);
        StudentTheme.styleSecondaryButton(deleteBtn);
        StudentTheme.styleSecondaryButton(clearBtn);
        StudentTheme.styleSecondaryButton(closeBtn);

        addBtn.addActionListener(e -> add());
        updateBtn.addActionListener(e -> update());
        deleteBtn.addActionListener(e -> delete());
        clearBtn.addActionListener(e -> clearForm());
        closeBtn.addActionListener(e -> StudentNav.goHome(this));

        p.add(addBtn);
        p.add(updateBtn);
        p.add(deleteBtn);
        p.add(clearBtn);
        p.add(closeBtn);
        return p;
    }

    private void refresh() {
        try {
            List<OffCampusJob> jobs = offCampusController.viewOffCampusJobs(studentId);
            model.setRowCount(0);
            for (OffCampusJob j : jobs) {
                model.addRow(new Object[]{
                        j.getOffCampusId(),
                        j.getCompanyName(),
                        j.getRoleTitle(),
                        j.getAppliedDate(),
                        j.getStatus(),
                        j.getNotes()
                });
            }
            if (model.getRowCount() == 0) card.show(tableOrEmpty, "EMPTY");
            else card.show(tableOrEmpty, "TABLE");
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private long selectedOffCampusId() {
        int row = table.getSelectedRow();
        if (row < 0) return -1;
        Object val = model.getValueAt(row, 0);
        return (val instanceof Number) ? ((Number) val).longValue() : Long.parseLong(val.toString());
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        companyField.setText(String.valueOf(model.getValueAt(row, 1)));
        roleField.setText(String.valueOf(model.getValueAt(row, 2)));
        dateField.setText(String.valueOf(model.getValueAt(row, 3)));
        statusField.setText(String.valueOf(model.getValueAt(row, 4)));
        notesField.setText(String.valueOf(model.getValueAt(row, 5)));
    }

    private OffCampusJob buildFromForm() {
        OffCampusJob job = new OffCampusJob();
        job.setStudentId(studentId);
        job.setCompanyName(companyField.getText().trim());
        job.setRoleTitle(roleField.getText().trim());
        job.setAppliedDate(dateField.getText().trim());
        job.setStatus(statusField.getText().trim());
        job.setNotes(notesField.getText().trim());
        return job;
    }

    private void add() {
        try {
            OffCampusJob job = buildFromForm();
            long id = offCampusController.addOffCampusJob(job);
            UiUtil.info(id > 0 ? "Added. ID = " + id : "Not added.");
            refresh();
            clearForm();
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void update() {
        long id = selectedOffCampusId();
        if (id <= 0) {
            UiUtil.error("Select a row to update.");
            return;
        }

        try {
            OffCampusJob job = buildFromForm();
            job.setOffCampusId(id);
            boolean ok = offCampusController.updateOffCampusJob(job);
            UiUtil.info(ok ? "Updated." : "Update failed.");
            refresh();
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void delete() {
        long id = selectedOffCampusId();
        if (id <= 0) {
            UiUtil.error("Select a row to delete.");
            return;
        }
        if (!UiUtil.confirm("Delete off-campus job ID " + id + "?")) return;

        try {
            boolean ok = offCampusController.deleteOffCampusJob(studentId, id);
            UiUtil.info(ok ? "Deleted." : "Delete failed.");
            refresh();
            clearForm();
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void clearForm() {
        companyField.setText("");
        roleField.setText("");
        dateField.setText("");
        statusField.setText("");
        notesField.setText("");
        table.clearSelection();
    }
}
