package com.placement.admin.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Small, lightweight theme helper for the Admin module.
 * Keeps changes minimal while giving the dashboard a darker, consistent look.
 */
public final class AdminTheme {

    private AdminTheme() {}

    public static final Color BG = new Color(24, 24, 27);          // near-black
    public static final Color SURFACE = new Color(39, 39, 42);     // dark gray
    public static final Color SURFACE_2 = new Color(63, 63, 70);   // mid gray
    public static final Color BORDER = new Color(82, 82, 91);

    public static final Color TEXT = new Color(245, 245, 245);
    public static final Color TEXT_MUTED = new Color(180, 180, 190);

    public static final Color ACCENT = new Color(99, 102, 241);    // indigo
    public static final Color MUTED_BUTTON = new Color(82, 82, 91);

    public static Font fontPlain(int size) { return new Font("Segoe UI", Font.PLAIN, size); }
    public static Font fontBold(int size)  { return new Font("Segoe UI", Font.BOLD, size); }

    public static void styleLabel(JLabel l) {
        l.setForeground(TEXT);
        l.setFont(fontPlain(13));
    }

    public static void styleField(JTextField f) {
        f.setFont(fontPlain(13));
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBackground(SURFACE_2);
        f.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(6, 10, 6, 10)));
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setFont(fontPlain(13));
        c.setForeground(TEXT);
        c.setBackground(SURFACE_2);
    }

    public static void styleButton(JButton b, Color bg) {
        b.setFont(fontBold(12));
        b.setForeground(TEXT);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 12, 8, 12));
    }

    public static void styleTable(JTable t) {
        t.setFont(fontPlain(12));
        t.setForeground(TEXT);
        t.setBackground(SURFACE);
        t.setGridColor(BORDER);
        t.setSelectionBackground(new Color(59, 59, 70));
        t.setSelectionForeground(TEXT);

        JTableHeader h = t.getTableHeader();
        h.setFont(fontBold(12));
        h.setForeground(TEXT);
        h.setBackground(SURFACE_2);
        h.setReorderingAllowed(false);

        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setOpaque(true);
        r.setForeground(TEXT);
        r.setBackground(SURFACE);
        t.setDefaultRenderer(Object.class, r);
    }

    public static void styleScrollPane(JScrollPane sp) {
        sp.getViewport().setBackground(SURFACE);
        sp.setBorder(new LineBorder(BORDER));
    }
}
