package com.placement.company.ui;

import com.placement.company.dao.CompanyJobDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PostJobDialog extends JDialog {

    // Match your dashboard theme (red -> pink)
    private static final Color GRAD_START = new Color(220, 38, 38);
    private static final Color GRAD_END   = new Color(244, 63, 94);

    private final CompanyJobDao jobDao;
    private final String companyName;
    private final Runnable onSuccess;

    private JTextField titleField;
    private JTextField deptField;
    private JTextField minGpaField;
    private JTextField minYearField;
    private JTextField ruleField;

    // ✅ Description as TEXT AREA like admin
    private JTextArea descArea;

    private JButton postBtn;

    public PostJobDialog(JFrame owner, String companyName, CompanyJobDao jobDao, Runnable onSuccess) {
        super(owner, "Post New Job", true);
        this.companyName = companyName;
        this.jobDao = jobDao;
        this.onSuccess = onSuccess;

        setSize(650, 560);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setContentPane(buildUI());
    }

    private JComponent buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

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
        header.setPreferredSize(new Dimension(650, 95));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(18, 18, 18, 18));
        header.setOpaque(false);

        JLabel t = new JLabel("Post New Job");
        t.setForeground(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel sub = new JLabel("Company: " + companyName);
        sub.setForeground(new Color(255, 235, 240));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        header.add(t);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);
        return header;
    }

    private JComponent buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(18, 18, 12, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        titleField = new JTextField();
        deptField = new JTextField();
        minGpaField = new JTextField();
        minYearField = new JTextField();
        ruleField = new JTextField("GENERAL");

        styleField(titleField);
        styleField(deptField);
        styleField(minGpaField);
        styleField(minYearField);
        styleField(ruleField);

        // ✅ Description area (admin-like)
        descArea = new JTextArea(8, 30);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        descScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 226, 235), 1, true));

        int y = 0;

        addRow(form, gbc, y++, "Title *", titleField);
        addRow(form, gbc, y++, "Department", deptField);
        addRow(form, gbc, y++, "Min GPA", minGpaField);
        addRow(form, gbc, y++, "Min Year", minYearField);
        addRow(form, gbc, y++, "Eligibility Rule", ruleField);

        // Description row (taller)
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; gbc.weighty = 0;
        form.add(new JLabel("Description"), gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        form.add(descScroll, gbc);

        return form;
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int y, String label, JComponent field) {
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        form.add(new JLabel(label), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        form.add(field, gbc);
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(0, 12, 8, 12));

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());

        postBtn = new JButton("Post Job");
        postBtn.setBackground(GRAD_START);
        postBtn.setForeground(Color.WHITE);
        postBtn.setFocusPainted(false);
        postBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        postBtn.addActionListener(e -> onPost());

        footer.add(cancel);
        footer.add(postBtn);
        return footer;
    }

    private void onPost() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dept = deptField.getText().trim();
        String desc = descArea.getText().trim();
        String rule = ruleField.getText().trim();

        Double minGpa = parseDoubleOrNull(minGpaField.getText().trim());
        Integer minYear = parseIntOrNull(minYearField.getText().trim());

        postBtn.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() {
                return jobDao.insertJob(companyName, title, dept, desc, minGpa, minYear, rule);
            }

            @Override
            protected void done() {
                try {
                    long id = get();
                    if (id <= 0) {
                        JOptionPane.showMessageDialog(PostJobDialog.this, "Failed to create job.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    JOptionPane.showMessageDialog(PostJobDialog.this, "Job posted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    if (onSuccess != null) onSuccess.run();
                    dispose();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PostJobDialog.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    postBtn.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }
}
