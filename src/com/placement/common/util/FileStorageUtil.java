package com.placement.common.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Locale;

public final class FileStorageUtil {
    private FileStorageUtil() {}

    /* =========================
       COMPANY
       ========================= */

    public static String saveCompanyLogo(File sourceFile, int userId) throws IOException {
        ensureDir("data/uploads/companies");

        String ext = getExt(sourceFile.getName());
        String fileName = "company_" + userId + "_" + System.currentTimeMillis() + "." + ext;

        Path dest = Paths.get("data/uploads/companies", fileName);
        Files.copy(sourceFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

        return normalize(dest);
    }

    // Used by CompanyLogoService.applyDefaultLogo(email, defaultImageName)
    public static String copyDefaultImageToCompanyUploads(String defaultImageName, int userId) throws IOException {
        ensureDir("data/uploads/companies");

        Path dest = Paths.get("data/uploads/companies", "company_" + userId + "_default.png");

        // 1) classpath: /images/<defaultImageName>
        try (InputStream in = FileStorageUtil.class.getResourceAsStream("/images/" + defaultImageName)) {
            if (in != null) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                return normalize(dest);
            }
        }

        // 2) disk: resources/images/<defaultImageName>
        Path disk = Paths.get("resources/images", defaultImageName);
        if (Files.exists(disk)) {
            Files.copy(disk, dest, StandardCopyOption.REPLACE_EXISTING);
            return normalize(dest);
        }

        throw new IOException("Default image not found: " + defaultImageName);
    }

    // Optional legacy method
    public static String writeDefaultCompanyLogo(int userId) throws IOException {
        return copyDefaultImageToCompanyUploads("default_company_logo.png", userId);
    }

    /* =========================
       STUDENT
       ========================= */
 // ✅ Student: save uploaded profile image to data/uploads/students
    public static String saveStudentProfileImage(File sourceFile, int userId) throws IOException {
        ensureDir("data/uploads/students");

        String ext = getExt(sourceFile.getName());
        String fileName = "student_" + userId + "_" + System.currentTimeMillis() + "." + ext;

        Path dest = Paths.get("data/uploads/students", fileName);
        Files.copy(sourceFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

        return normalize(dest);
    }

    // ✅ Student: copy default image into student uploads and return saved path
    public static String copyDefaultImageToStudentUploads(int userId, String defaultImageName) throws IOException {
        ensureDir("data/uploads/students");

        // stable default filename (overwrites if user clicks default again)
        Path dest = Paths.get("data/uploads/students", "student_" + userId + "_default.png");

        // 1) classpath: /images/<defaultImageName>
        try (InputStream in = FileStorageUtil.class.getResourceAsStream("/images/" + defaultImageName)) {
            if (in != null) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                return normalize(dest);
            }
        }

        // 2) disk: resources/images/<defaultImageName>
        Path disk = Paths.get("resources/images", defaultImageName);
        if (Files.exists(disk)) {
            Files.copy(disk, dest, StandardCopyOption.REPLACE_EXISTING);
            return normalize(dest);
        }

        throw new IOException("Default image not found: " + defaultImageName);
    }

    /* =========================
       HELPERS
       ========================= */

    private static void ensureDir(String dir) throws IOException {
        Files.createDirectories(Paths.get(dir));
    }

    private static String getExt(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        int dot = n.lastIndexOf('.');
        if (dot < 0 || dot == n.length() - 1) return "png";
        String ext = n.substring(dot + 1);
        return switch (ext) {
            case "png", "jpg", "jpeg", "gif", "bmp" -> ext;
            default -> "png";
        };
    }

    private static String normalize(Path path) {
        return path.toString().replace("\\", "/");
    }
}
