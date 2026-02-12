package com.placement.main;

import com.placement.common.db.DB;
import com.placement.common.ui.LoginScreen;

public class Main_test {
    public static void main(String[] args) {
        DB.init();
        new LoginScreen().setVisible(true);
    }
}
