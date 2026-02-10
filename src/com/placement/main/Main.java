package com.placement.main;

import javax.swing.SwingUtilities;
import com.placement.common.ui.LoginScreen;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginScreen().setVisible(true);
        });
    }
}
