package com.placement.student.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Lightweight navigation helper for student pages hosted inside StudentDashboardView.
 * Pages can call StudentNav.goHome(this) to return to the dashboard home card.
 */
public final class StudentNav {
    private StudentNav() {}

    public static void goHome(Component from) {
        Runnable r = findGoHome(from);
        if (r != null) {
            r.run();
        }
    }

    private static Runnable findGoHome(Component c) {
        Component cur = c;
        while (cur != null) {
            if (cur instanceof JComponent) {
                Object prop = ((JComponent) cur).getClientProperty("goHome");
                if (prop instanceof Runnable) return (Runnable) prop;
            }
            cur = cur.getParent();
        }
        return null;
    }
}
