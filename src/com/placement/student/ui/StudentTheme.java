package com.placement.student.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;

/**
 * Shared styling for Student UI so it matches the overall app look (Login/Admin/Company).
 */
public final class StudentTheme {

    private StudentTheme() {}

    // Match common/ui gradients
    public static final Color GRADIENT_A = new Color(99, 102, 241);
    public static final Color GRADIENT_B = new Color(124, 58, 237);

    public static final Color BG = Color.WHITE;
    public static final Color BORDER = new Color(229, 231, 235);
    public static final Color TEXT = new Color(17, 24, 39);
    public static final Color TEXT_MUTED = new Color(107, 114, 128);

    public static final Color SIDEBAR_BG = new Color(249, 250, 251);
    public static final Color SIDEBAR_HOVER = new Color(243, 244, 246);

    public static Font fontRegular(int size) { return fontPlain(size); }

    public static Font fontPlain(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    public static Font fontBold(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }

    // ---------------------------
    // Layout blocks
    // ---------------------------

    public static JPanel header(String title, String subtitle) {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, GRADIENT_A, getWidth(), getHeight(), GRADIENT_B));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(900, 90));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel t = new JLabel(title);
        t.setForeground(Color.WHITE);
        t.setFont(fontBold(18));
        header.add(t);

        if (subtitle != null && !subtitle.isBlank()) {
            header.add(Box.createVerticalStrut(4));
            JLabel s = new JLabel(subtitle);
            s.setForeground(new Color(230, 230, 255));
            s.setFont(fontPlain(12));
            header.add(s);
        }

        return header;
    }

    /** Dashboard header with avatar + name on the right side. */
    public static JPanel dashboardHeader(String title, String subtitle, JLabel avatar, String displayName) {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, GRADIENT_A, getWidth(), getHeight(), GRADIENT_B));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(900, 96));
        header.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setForeground(Color.WHITE);
        t.setFont(fontBold(18));
        left.add(t);

        if (subtitle != null && !subtitle.isBlank()) {
            left.add(Box.createVerticalStrut(4));
            JLabel s = new JLabel(subtitle);
            s.setForeground(new Color(230, 230, 255));
            s.setFont(fontPlain(12));
            left.add(s);
        }

        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JLabel name = new JLabel(displayName == null ? "" : displayName);
        name.setForeground(Color.WHITE);
        name.setFont(fontBold(13));

        right.add(name);
        if (avatar != null) right.add(avatar);

        header.add(right, BorderLayout.EAST);

        return header;
    }

    public static JPanel contentPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(16, 18, 18, 18));
        return p;
    }

    public static JPanel sidebar() {
        JPanel p = new JPanel();
        p.setBackground(SIDEBAR_BG);
        p.setBorder(new EmptyBorder(14, 12, 14, 12));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    public static JPanel emptyState(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(40, 24, 40, 24));

        JLabel t = new JLabel(title);
        t.setFont(fontBold(16));
        t.setForeground(TEXT);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel s = new JLabel(subtitle == null ? "" : subtitle);
        s.setFont(fontPlain(13));
        s.setForeground(TEXT_MUTED);
        s.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(Box.createVerticalGlue());
        p.add(t);
        if (subtitle != null && !subtitle.isBlank()) {
            p.add(Box.createVerticalStrut(8));
            p.add(s);
        }
        p.add(Box.createVerticalGlue());
        return p;
    }

    // ---------------------------
    // Controls
    // ---------------------------

    public static void styleField(JTextField field) {
        field.setFont(fontPlain(13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    public static void stylePrimaryButton(JButton btn) {
        btn.setFont(fontBold(13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(GRADIENT_A);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
    }

    public static void styleSecondaryButton(JButton btn) {
        btn.setFont(fontBold(13));
        btn.setForeground(new Color(31, 41, 55));
        btn.setBackground(new Color(243, 244, 246));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
    }

    /** Sidebar nav button (left aligned + hover). */
    public static JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(fontBold(13));
        b.setForeground(new Color(31, 41, 55));
        b.setBackground(SIDEBAR_BG);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 12, 10, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(SIDEBAR_HOVER); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(SIDEBAR_BG); }
        });

        return b;
    }

    public static JButton navButtonDanger(String text) {
        JButton b = navButton(text);
        b.setForeground(new Color(185, 28, 28));
        return b;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(fontPlain(13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(BORDER);

        JTableHeader h = table.getTableHeader();
        h.setFont(fontBold(13));
        h.setReorderingAllowed(false);
    }

    // ---------------------------
    // Avatar + images
    // ---------------------------

    public static JLabel avatarLabel(String profileImagePath, String gender, int size) {
        ImageIcon icon = loadProfileImage(profileImagePath, gender, size);
        JLabel label = new JLabel(icon);
        label.setPreferredSize(new Dimension(size, size));
        return label;
    }

    public static ImageIcon loadProfileImage(String profileImagePath, String gender, int size) {
        try {
            Image img = null;

            if (profileImagePath != null && !profileImagePath.isBlank()) {
                File f = new File(profileImagePath);
                if (f.exists()) {
                    img = ImageIO.read(f);
                }
            }

            if (img == null) {
                String g = gender == null ? "" : gender.trim().toLowerCase();
                String res = "/images/default_other.png";
                if (g.startsWith("m")) res = "/images/default_male.png";
                else if (g.startsWith("f")) res = "/images/default_female.png";

                URL url = StudentTheme.class.getResource(res);
                if (url != null) {
                    img = ImageIO.read(url);
                } else {
                    // Fallback for IDEs that don't put /resources on the classpath
                    String rel = res.startsWith("/images/") ? res.substring("/images/".length()) : res;
                    File rf = new File("resources" + File.separator + "images" + File.separator + rel);
                    if (rf.exists()) img = ImageIO.read(rf);
                }
            }

            if (img == null) {
                img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            }

            Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception ignored) {
            return new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));
        }
    }

    // ---------------------------
    // Status chips (table renderer)
    // ---------------------------

    public static TableCellRenderer statusChipRenderer() {
        return new StatusChipRenderer();
    }

    private static class StatusChipRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {

            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(fontBold(12));
            lbl.setOpaque(true);

            String v = value == null ? "" : String.valueOf(value).trim().toUpperCase();

            // Defaults
            Color bg = new Color(243, 244, 246);
            Color fg = new Color(55, 65, 81);
            Color br = BORDER;

            if (v.contains("OPEN")) { bg = new Color(220, 252, 231); fg = new Color(22, 101, 52); br = new Color(187, 247, 208); }
            else if (v.contains("CLOSED")) { bg = new Color(254, 226, 226); fg = new Color(153, 27, 27); br = new Color(254, 202, 202); }
            else if (v.contains("OFFERED") || v.contains("SENT")) { bg = new Color(219, 234, 254); fg = new Color(30, 64, 175); br = new Color(191, 219, 254); }
            else if (v.contains("ACCEPT")) { bg = new Color(220, 252, 231); fg = new Color(22, 101, 52); br = new Color(187, 247, 208); }
            else if (v.contains("REJECT")) { bg = new Color(254, 226, 226); fg = new Color(153, 27, 27); br = new Color(254, 202, 202); }
            else if (v.contains("PENDING") || v.contains("APPLIED") || v.contains("IN REVIEW")) { bg = new Color(254, 249, 195); fg = new Color(133, 77, 14); br = new Color(253, 230, 138); }
            else if (v.contains("SCHEDULED")) { bg = new Color(219, 234, 254); fg = new Color(30, 64, 175); br = new Color(191, 219, 254); }
            else if (v.contains("DONE") || v.contains("COMPLETED")) { bg = new Color(220, 252, 231); fg = new Color(22, 101, 52); br = new Color(187, 247, 208); }
            else if (v.contains("ONLINE")) { bg = new Color(224, 231, 255); fg = new Color(55, 48, 163); br = new Color(199, 210, 254); }
            else if (v.contains("OFFLINE")) { bg = new Color(243, 244, 246); fg = new Color(55, 65, 81); br = BORDER; }

            lbl.setForeground(isSelected ? Color.WHITE : fg);
            lbl.setBackground(isSelected ? GRADIENT_A : bg);
            lbl.setBorder(new CompoundBorder(new LineBorder(isSelected ? GRADIENT_A : br, 1, true),
                    new EmptyBorder(4, 8, 4, 8)));

            // show compact text
            lbl.setText(v.isEmpty() ? "-" : v);
            return lbl;
        }
    }
}
