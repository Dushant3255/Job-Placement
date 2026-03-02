package com.placement.student.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Themed dialogs (replacement for plain JOptionPane).
 * Keep it simple + consistent with StudentTheme.
 */
public final class StudentDialogs {

    private StudentDialogs() {}

    public static void info(Component parent, String title, String message) {
        showMessage(parent, title == null ? "Info" : title, message, false);
    }

    public static void error(Component parent, String title, String message) {
        showMessage(parent, title == null ? "Error" : title, message, true);
    }

    public static boolean confirm(Component parent, String title, String message) {
        return showConfirm(parent, title == null ? "Confirm" : title, message);
    }

    private static void showMessage(Component parent, String title, String message, boolean isError) {
        JDialog dialog = createDialog(parent, title);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(StudentTheme.BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel headline = new JLabel(title);
        headline.setFont(StudentTheme.fontBold(15));
        headline.setForeground(isError ? new Color(153, 27, 27) : StudentTheme.TEXT);
        root.add(headline, BorderLayout.NORTH);

        JTextArea area = new JTextArea(message == null ? "" : message);
        area.setFont(StudentTheme.fontPlain(13));
        area.setForeground(StudentTheme.TEXT_MUTED);
        area.setBackground(StudentTheme.BG);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setBorder(null);

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createLineBorder(StudentTheme.BORDER));
        sp.setPreferredSize(new Dimension(420, 140));
        root.add(sp, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(StudentTheme.BG);

        JButton ok = new JButton("OK");
        StudentTheme.stylePrimaryButton(ok);
        ok.addActionListener(e -> dialog.dispose());
        actions.add(ok);

        root.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static boolean showConfirm(Component parent, String title, String message) {
        final boolean[] result = {false};

        JDialog dialog = createDialog(parent, title);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(StudentTheme.BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel headline = new JLabel(title);
        headline.setFont(StudentTheme.fontBold(15));
        headline.setForeground(StudentTheme.TEXT);
        root.add(headline, BorderLayout.NORTH);

        JTextArea area = new JTextArea(message == null ? "" : message);
        area.setFont(StudentTheme.fontPlain(13));
        area.setForeground(StudentTheme.TEXT_MUTED);
        area.setBackground(StudentTheme.BG);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setBorder(null);

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createLineBorder(StudentTheme.BORDER));
        sp.setPreferredSize(new Dimension(420, 120));
        root.add(sp, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(StudentTheme.BG);

        JButton no = new JButton("No");
        JButton yes = new JButton("Yes");
        StudentTheme.styleSecondaryButton(no);
        StudentTheme.stylePrimaryButton(yes);

        no.addActionListener(e -> { result[0] = false; dialog.dispose(); });
        yes.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        actions.add(no);
        actions.add(yes);

        root.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    private static JDialog createDialog(Component parent, String title) {
        Window w = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(w, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        return dialog;
    }
}
