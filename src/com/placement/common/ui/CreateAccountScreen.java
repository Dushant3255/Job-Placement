package com.placement.common.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.placement.common.ui.HeaderBackButton;
import com.placement.student.ui.StudentCreateAccountScreen;
import com.placement.company.ui.CompanyCreateAccountScreen;



public class CreateAccountScreen extends JFrame {

    private JComboBox<String> typeCombo;

    public CreateAccountScreen() {
        setTitle("Create Account");
        setSize(420, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        /* ================= HEADER ================= */
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(
                        0, 0, new Color(79, 70, 229),
                        getWidth(), getHeight(), new Color(124, 58, 237)
                ));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        header.setPreferredSize(new Dimension(420, 120));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(18, 26, 15, 20));

        /* ---------- BACK BUTTON ---------- */
        JButton backBtn = new HeaderBackButton("← Back");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        backBtn.addActionListener(e -> {
            new LoginScreen().setVisible(true);
            dispose();
        });


        JLabel title = new JLabel("Create Account");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Select account type");
        subtitle.setForeground(new Color(220, 220, 255));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(backBtn);
        header.add(Box.createVerticalStrut(10));
        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        /* ================= CARD ================= */
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 30, 40, 30));
        card.setBackground(Color.WHITE);

        JLabel typeLabel = new JLabel("Account Type");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        typeCombo = new JComboBox<>(new String[]{"Student", "Company"});
        typeCombo.setMaximumSize(new Dimension(260, 40));
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeCombo.setAlignmentX(Component.CENTER_ALIGNMENT);

        InteractiveButton continueBtn =
                new InteractiveButton("Continue", new Color(109, 40, 217));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(continueBtn);

        continueBtn.addActionListener(e -> {
            String selectedType = (String) typeCombo.getSelectedItem();

            if ("Student".equals(selectedType)) {
                new StudentCreateAccountScreen().setVisible(true);
                dispose();
            } else {
                new CompanyCreateAccountScreen().setVisible(true);
                dispose();
            }
        });


        card.add(typeLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(typeCombo);
        card.add(Box.createVerticalStrut(30));
        card.add(buttonPanel);

        root.add(header, BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        add(root);
    }

    /* ================= BUTTON ================= */
    static class InteractiveButton extends JButton {

        private final Color normal;
        private final Color hover;
        private final Color pressed;

        public InteractiveButton(String text, Color normal) {
            super(text);
            this.normal = normal;
            this.hover = normal.darker();
            this.pressed = normal.darker().darker();

            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { repaint(); }
                @Override public void mouseExited(MouseEvent e) { repaint(); }
                @Override public void mousePressed(MouseEvent e) { repaint(); }
                @Override public void mouseReleased(MouseEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) {
                g2.setColor(pressed);
            } else if (getModel().isRollover()) {
                g2.setColor(hover);
            } else {
                g2.setColor(normal);
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
