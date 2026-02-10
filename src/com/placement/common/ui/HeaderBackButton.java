package com.placement.common.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HeaderBackButton extends JButton {

    private final Color bg = new Color(255, 255, 255, 45);
    private final Color bgHover = new Color(255, 255, 255, 70);
    private final Color border = new Color(255, 255, 255, 160);

    public HeaderBackButton(String text) {
        super(text);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Same padding/shape as your ProfilePictureScreen version
        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { repaint(); }
            @Override public void mouseExited(MouseEvent e) { repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getModel().isRollover() ? bgHover : bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

        g2.setColor(border);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

        super.paintComponent(g2);
        g2.dispose();
    }
}
