package com.placement.student.ui;

public class UiUtil {

    public static void info(String msg) {
        StudentDialogs.info(null, "Info", msg);
    }

    public static void error(String msg) {
        StudentDialogs.error(null, "Error", msg);
    }

    public static boolean confirm(String msg) {
        return StudentDialogs.confirm(null, "Confirm", msg);
    }
}
