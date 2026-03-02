package com.placement.company.ui;

import com.placement.company.dao.CompanyJobDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditJobDialog extends JDialog {

    private static final Color GRAD_START = new Color(220, 38, 38);
    private static final Color GRAD_END   = new Color(244, 63, 94);

    private static final Color BG = Color.WHITE;
    private static final Color BORDER = new Color(220, 226, 235);
    private static final Color TEXT_DARK = new Color(15, 23, 42);

    private final CompanyJobDao jobDao;
    private final String companyName;
    private final CompanyJobDao.JobRow job;
    private final Runnable onSuccess;

    private JTextField titleField, deptField, minGpaField, minYearField, skillsField, positionsField;
    private JComboBox<String> statusBox;
    private JTextArea descArea;

    private JButton saveBtn;

    public EditJobDialog(JFrame owner, String companyName, CompanyJobDao jobDao, CompanyJobDao.JobRow job, Runnable onSuccess) {
        super(owner, "Edit Job", true);
        this.companyName = companyName;
        this.jobDao = jobDao;
        this.job = job;
        this.onSuccess = onSuccess;

        setSize(720, 600);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setContentPane(buildUI());
        prefill();
    }

    private JComponent buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        return root;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, GRAD_START, getWidth(), getHeight(), GRAD_END));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(720, 110));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel t = new JLabel("Edit Job Listing");
        t.setForeground(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel sub = new JLabel("Job ID: " + job.jobId);
        sub.setForeground(new Color(255, 235, 240));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        header.add(t);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);
        return header;
    }

    private JComponent buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        form.setBorder(new EmptyBorder(18, 18, 10, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        titleField = new JTextField();
        deptField = new JTextField();
        minGpaField = new JTextField();
        minYearField = new JTextField();
        skillsField = new JTextField();
        positionsField = new JTextField();
        statusBox = new JComboBox<>(new String[]{"OPEN", "CLOSED"});

        styleField(titleField);
        styleField(deptField);
        styleField(minGpaField);
        styleField(minYearField);
        styleField(skillsField);
        styleField(positionsField);
        styleCombo(statusBox);

        // Description text area (nice like admin)
        descArea = new JTextArea(8, 30);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        descArea.setBackground(new Color(250, 251, 253));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        descScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        descScroll.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        descScroll.getViewport().setBackground(descArea.getBackground());

        int y = 0;
        addRow(form, gbc, y++, "Title *", titleField);
        addRow(form, gbc, y++, "Department", deptField);
        addRow(form, gbc, y++, "Min GPA", minGpaField);
        addRow(form, gbc, y++, "Min Year", minYearField);
        addRow(form, gbc, y++, "Positions Available *", positionsField);
        addRow(form, gbc, y++, "Skills Required", skillsField);
        addRow(form, gbc, y++, "Status", statusBox);

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        form.add(new JLabel("Description"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        form.add(descScroll, gbc);

        return form;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG);
        footer.setBorder(new EmptyBorder(10, 18, 16, 18));

        JLabel hint = new JLabel("Tip: Keep title short and put details in Description.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(new Color(100, 110, 125));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton cancel = new SoftButton("Cancel", new Color(226, 232, 240), TEXT_DARK);
        cancel.addActionListener(e -> dispose());

        saveBtn = new SolidButton("Save Changes", GRAD_START);
        saveBtn.addActionListener(e -> onSave());

        actions.add(cancel);
        actions.add(saveBtn);

        footer.add(hint, BorderLayout.WEST);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private void prefill() {
        titleField.setText(n(job.title));
        deptField.setText(n(job.department));
        descArea.setText(n(job.description));
        minGpaField.setText(job.minGpa == null ? "" : String.valueOf(job.minGpa));
        minYearField.setText(job.minYear == null ? "" : String.valueOf(job.minYear));
        positionsField.setText(job.positionsAvailable == null ? "0" : String.valueOf(job.positionsAvailable));
        skillsField.setText(n(job.skills));
        statusBox.setSelectedItem(job.status == null ? "OPEN" : job.status.toUpperCase());
    }

    private void onSave() {
        String title = titleField.getText().trim();
        if (title.isBlank()) {
            JOptionPane.showMessageDialog(this, "Title is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dept = deptField.getText().trim();
        String desc = descArea.getText().trim();
        String skills = skillsField.getText().trim();
        String status = String.valueOf(statusBox.getSelectedItem());

        Double minGpa = parseDoubleOrNull(minGpaField.getText().trim());
        Integer minYear = parseIntOrNull(minYearField.getText().trim());

String positionsTxt = positionsField.getText().trim();
int positionsAvailable;
try {
    positionsAvailable = Integer.parseInt(positionsTxt);
    if (positionsAvailable <= 0) throw new NumberFormatException();
} catch (NumberFormatException ex) {
    JOptionPane.showMessageDialog(this, "Positions Available must be a positive integer.", "Validation", JOptionPane.WARNING_MESSAGE);
    return;
}


        saveBtn.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return jobDao.updateJob(
                        job.jobId,              // ✅ FIX: use job.jobId
                        companyName,
                        title,
                        dept,
                        desc,
                        minGpa,
                        minYear,
                        skills,
                        status,
                        positionsAvailable       // ✅ FIX: return boolean
                );
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (!ok) throw new RuntimeException("Update failed.");

                    JOptionPane.showMessageDialog(EditJobDialog.this,
                            "Job updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    if (onSuccess != null) onSuccess.run();
                    dispose();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EditJobDialog.this,
                            "Failed to update job: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    saveBtn.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int y, String label, JComponent field) {
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_DARK);

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        form.add(field, gbc);
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        combo.setBackground(Color.WHITE);
    }

    private static String n(String s) { return s == null ? "" : s; }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    /* ===================== Nice buttons (dashboard style) ===================== */

    private static class SolidButton extends JButton {
        private final int radius = 18;

        SolidButton(String text, Color bg) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setBackground(bg);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class SoftButton extends JButton {
        private final int radius = 18;

        SoftButton(String text, Color bg, Color fg) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(fg);
            setBackground(bg);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
