package com.placement.company.ui;

import com.placement.company.dao.CompanyJobDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PostJobDialog extends JDialog {

    private static final Color GRAD_START = new Color(220, 38, 38);
    private static final Color GRAD_END   = new Color(244, 63, 94);

    private final String companyName;
    private final CompanyJobDao jobDao;
    private final Runnable onSuccess;

    private JTextField titleField, deptField, minGpaField, minYearField, skillsField, positionsField;
    private JTextArea descArea;

    private JButton postBtn;

    public PostJobDialog(JFrame owner, String companyName, CompanyJobDao jobDao, Runnable onSuccess) {
        super(owner, "Post New Job", true);
        this.companyName = companyName;
        this.jobDao = jobDao;
        this.onSuccess = onSuccess;

        setSize(720, 620);
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
        header.setPreferredSize(new Dimension(720, 120));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel t = new JLabel("Post New Job");
        t.setForeground(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel sub = new JLabel("Create a job listing for " + (companyName == null ? "Company" : companyName));
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
        form.setBorder(new EmptyBorder(18, 18, 10, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        titleField = new JTextField();
        deptField  = new JTextField();
        minGpaField = new JTextField();
        minYearField = new JTextField();
        skillsField = new JTextField();
        positionsField = new JTextField();

        styleField(titleField);
        styleField(deptField);
        styleField(minGpaField);
        styleField(minYearField);
        styleField(skillsField);
        styleField(positionsField);

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
        addRow(form, gbc, y++, "Positions Available *", positionsField);
        addRow(form, gbc, y++, "Skills Required", skillsField);

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        form.add(new JLabel("Description"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        form.add(descScroll, gbc);

        return form;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(0, 12, 12, 12));

        JButton cancel = new SoftButton("Cancel", new Color(226, 232, 240), new Color(15, 23, 42));
        cancel.addActionListener(e -> dispose());

        postBtn = new GradientButton("Post Job", GRAD_START, GRAD_END);
        postBtn.addActionListener(e -> onPost());

        footer.add(cancel);
        footer.add(postBtn);
        return footer;
    }

    private void onPost() {
        String title = titleField.getText().trim();
        if (title.isBlank()) {
            JOptionPane.showMessageDialog(this, "Title is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dept = deptField.getText().trim();
        String desc = descArea.getText().trim();
        String skills = skillsField.getText().trim();

String positionsTxt = positionsField.getText().trim();
int positionsAvailable;
try {
    positionsAvailable = Integer.parseInt(positionsTxt);
    if (positionsAvailable <= 0) throw new NumberFormatException();
} catch (NumberFormatException ex) {
    JOptionPane.showMessageDialog(this, "Positions Available must be a positive integer.", "Validation", JOptionPane.WARNING_MESSAGE);
    return;
}


        Double minGpa = parseDoubleOrNull(minGpaField.getText().trim());
        Integer minYear = parseIntOrNull(minYearField.getText().trim());

        postBtn.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() {
                return jobDao.insertJob(
                        companyName,
                        title,
                        dept,
                        desc,
                        minGpa,
                        minYear,
                        skills,
                        positionsAvailable
                );
            }

            @Override
            protected void done() {
                try {
                    long id = get();
                    if (id <= 0) throw new RuntimeException("Insert failed.");

                    JOptionPane.showMessageDialog(PostJobDialog.this,
                            "Job posted successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    if (onSuccess != null) onSuccess.run();
                    dispose();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PostJobDialog.this,
                            "Failed to post job: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    postBtn.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
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

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }
    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    /* ================= Buttons (same style family as dashboard) ================= */

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

    private static class GradientButton extends JButton {
        private final int radius = 18;
        private final Color start;
        private final Color end;
        private boolean hover = false;

        GradientButton(String text, Color start, Color end) {
            super(text);
            this.start = start;
            this.end = end;

            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color s = hover ? start.brighter() : start;
            Color e = hover ? end.brighter() : end;

            g2.setPaint(new GradientPaint(0, 0, s, getWidth(), getHeight(), e));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
