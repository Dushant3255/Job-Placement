package com.placement.common.db;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.*;

import com.placement.common.util.PasswordUtil;

/**
 * SQLite DB helper.
 * Fixes "multiple DB files" by resolving DB location more robustly.
 * - Enables foreign keys on EVERY connection
 * - Creates tables if missing (including student_documents)
 * - Light migrations for older DB files
 */
public final class DB {

    private static final String DB_DIR_NAME = "data";
    private static final String DB_FILE_NAME = "student_placement.db";

    // Resolved once at startup to a stable location
    private static final File DB_FILE = resolveDbFile();
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE.getAbsolutePath();

    private DB() {}

    public static Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection(JDBC_URL);
        // IMPORTANT: must be set per-connection in SQLite
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        return con;
    }

    public static String getDbAbsolutePath() {
        return DB_FILE.getAbsolutePath();
    }

    /**
     * Call once at app startup.
     */
    public static void init() {
        ensureDbFolder();

        try (Connection con = getConnection();
             Statement st = con.createStatement()) {

            // Users table: shared for students/companies/admin
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    role TEXT NOT NULL,                  -- STUDENT / COMPANY / ADMIN
                    username TEXT NOT NULL UNIQUE,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    is_verified INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL DEFAULT (datetime('now'))
                );
            """);

            // Student profile table
            st.execute("""
                CREATE TABLE IF NOT EXISTS students (
                    user_id INTEGER PRIMARY KEY,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    gender TEXT,
                    profile_image_path TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            // Company profile table
            st.execute("""
                CREATE TABLE IF NOT EXISTS companies (
                    user_id INTEGER PRIMARY KEY,
                    company_name TEXT NOT NULL,
                    phone TEXT,
                    website TEXT,
                    industry TEXT,
                    company_size TEXT,
                    address TEXT,
                    logo_path TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            // OTP table (signup + password reset + 2FA if needed)
            st.execute("""
                CREATE TABLE IF NOT EXISTS otp_codes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email TEXT NOT NULL,
                    purpose TEXT NOT NULL,              -- e.g. COMPANY_SIGNUP / STUDENT_SIGNUP / PASSWORD_RESET
                    otp_hash TEXT NOT NULL,
                    expires_at TEXT NOT NULL,
                    used_at TEXT,
                    attempts INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL DEFAULT (datetime('now'))
                );
            """);

            st.execute("""
                CREATE INDEX IF NOT EXISTS idx_otp_email_purpose
                ON otp_codes(email, purpose);
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS academic_details (
                  student_id INTEGER PRIMARY KEY,
                  program TEXT,
                  year_of_study INTEGER,
                  gpa REAL,
                  cgpa REAL,
                  backlogs INTEGER,
                  graduation_year INTEGER,
                  eligibility_status TEXT,
                  FOREIGN KEY(student_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS job_listings (
                  job_id INTEGER PRIMARY KEY AUTOINCREMENT,
                  company_name TEXT NOT NULL,
                  title TEXT NOT NULL,
                  department TEXT,
                  description TEXT,
                  min_gpa REAL,
                  min_year INTEGER,
                  skills TEXT,
                  positions_available INTEGER NOT NULL DEFAULT 0,
                  hired_count INTEGER NOT NULL DEFAULT 0,
                  status TEXT NOT NULL DEFAULT 'OPEN',
                  posted_at TEXT NOT NULL DEFAULT (datetime('now'))
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS applications (
                  application_id INTEGER PRIMARY KEY AUTOINCREMENT,
                  student_id INTEGER NOT NULL,
                  job_id INTEGER NOT NULL,
                  status TEXT NOT NULL DEFAULT 'APPLIED',
                  resume_path TEXT,
                  applied_at TEXT NOT NULL DEFAULT (datetime('now')),
                  admin_confirmed INTEGER NOT NULL DEFAULT 0,
                  admin_confirmed_at TEXT,
                  FOREIGN KEY(student_id) REFERENCES users(id) ON DELETE CASCADE,
                  FOREIGN KEY(job_id) REFERENCES job_listings(job_id) ON DELETE CASCADE
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS interviews (
                    interview_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    application_id INTEGER NOT NULL,
                    scheduled_at TEXT NOT NULL,
                    mode TEXT,
                    location TEXT,
                    meeting_link TEXT,
                    status TEXT NOT NULL DEFAULT 'SCHEDULED',
                    notes TEXT,
                    created_at TEXT NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (application_id) REFERENCES applications(application_id) ON DELETE CASCADE
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS offers (
                  offer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                  application_id INTEGER NOT NULL,
                  package_lpa REAL,
                  joining_date TEXT,
                  status TEXT NOT NULL DEFAULT 'PENDING',
                  issued_at TEXT NOT NULL DEFAULT (datetime('now')),
                  letter_path TEXT,
                  FOREIGN KEY(application_id) REFERENCES applications(application_id) ON DELETE CASCADE
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS off_campus_jobs (
                  offcampus_id INTEGER PRIMARY KEY AUTOINCREMENT,
                  student_id INTEGER NOT NULL,
                  company_name TEXT NOT NULL,
                  role_title TEXT NOT NULL,
                  applied_date TEXT,
                  status TEXT,
                  notes TEXT,
                  FOREIGN KEY(student_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            // ✅ CV upload table (fixes "no such table: student_documents")
            st.execute("""
                CREATE TABLE IF NOT EXISTS student_documents (
                    student_id INTEGER PRIMARY KEY,
                    cv_path TEXT,
                    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY(student_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            // ---- Integrity: prevent duplicates ----
            try { st.execute("DELETE FROM applications WHERE application_id NOT IN (SELECT MIN(application_id) FROM applications GROUP BY student_id, job_id)"); } catch (SQLException ignore) {}
            try { st.execute("DELETE FROM interviews WHERE interview_id NOT IN (SELECT MIN(interview_id) FROM interviews GROUP BY application_id)"); } catch (SQLException ignore) {}
            try { st.execute("DELETE FROM offers WHERE offer_id NOT IN (SELECT MIN(offer_id) FROM offers GROUP BY application_id)"); } catch (SQLException ignore) {}

            try { st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_app_student_job ON applications(student_id, job_id)"); } catch (SQLException ignore) {}
            try { st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_interview_application ON interviews(application_id)"); } catch (SQLException ignore) {}
            try { st.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_offer_application ON offers(application_id)"); } catch (SQLException ignore) {}

            // ---- Lightweight migrations for older DB files ----
            try { st.execute("ALTER TABLE offers ADD COLUMN letter_path TEXT"); } catch (SQLException ignore) {}

            try { st.execute("ALTER TABLE job_listings ADD COLUMN positions_available INTEGER NOT NULL DEFAULT 0"); } catch (SQLException ignore) {}
            try { st.execute("ALTER TABLE job_listings ADD COLUMN hired_count INTEGER NOT NULL DEFAULT 0"); } catch (SQLException ignore) {}
            try { st.execute("ALTER TABLE job_listings ADD COLUMN skills TEXT"); } catch (SQLException ignore) {}

            // student_documents: add updated_at for older DBs (if they existed with only student_id/cv_path)
            try { st.execute("ALTER TABLE student_documents ADD COLUMN updated_at TEXT NOT NULL DEFAULT (datetime('now'))"); } catch (SQLException ignore) {}

            // Clean empty strings in interview fields
            try { st.execute("UPDATE interviews SET location=NULL WHERE location IS NOT NULL AND TRIM(location)=''"); } catch (SQLException ignore) {}
            try { st.execute("UPDATE interviews SET meeting_link=NULL WHERE meeting_link IS NOT NULL AND TRIM(meeting_link)=''"); } catch (SQLException ignore) {}

            // Seed a default admin user if none exists
            seedDefaultAdmin(con);

            System.out.println("✅ DB initialized at: " + DB_FILE.getAbsolutePath());

        } catch (SQLException e) {
            throw new RuntimeException("DB init failed: " + e.getMessage(), e);
        }
    }

    private static void ensureDbFolder() {
        File dir = DB_FILE.getParentFile();
        if (dir != null && !dir.exists()) {
            boolean ok = dir.mkdirs();
            if (!ok) {
                throw new RuntimeException("Could not create DB folder: " + dir.getAbsolutePath());
            }
        }
    }

    private static void seedDefaultAdmin(Connection con) throws SQLException {
        String checkSql = "SELECT 1 FROM users WHERE role='ADMIN' LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return;
        }

        String insertSql = """
            INSERT INTO users (role, username, email, password_hash, is_verified)
            VALUES ('ADMIN', ?, ?, ?, 1)
        """;

        try (PreparedStatement ps = con.prepareStatement(insertSql)) {
            ps.setString(1, "admin");
            ps.setString(2, "admin@admin.com");
            ps.setString(3, PasswordUtil.hash("admin123"));
            ps.executeUpdate();
        }
    }

    /**
     * Picks a stable DB file location to avoid accidentally creating multiple DBs.
     *
     * Priority:
     *  1) If system property placement.db.path is provided, use it.
     *  2) If a DB exists in user.dir/data and looks initialized (has users table), use it.
     *  3) If a DB exists near the running classes/jar in ../data, use it.
     *  4) Otherwise default to user.dir/data/student_placement.db
     */
    private static File resolveDbFile() {
        // 1) explicit override (recommended for labs)
        String override = System.getProperty("placement.db.path");
        if (override != null && !override.trim().isEmpty()) {
            return new File(override.trim());
        }

        File candidate1 = new File(System.getProperty("user.dir"), DB_DIR_NAME + File.separator + DB_FILE_NAME);

        File candidate2 = null;
        try {
            File codeSource = new File(DB.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // If running from /bin or /target/classes, go up one level and look for /data
            File base = codeSource.isFile() ? codeSource.getParentFile() : codeSource;
            if (base != null) {
                candidate2 = new File(base.getParentFile(), DB_DIR_NAME + File.separator + DB_FILE_NAME);
            }
        } catch (URISyntaxException ignore) {}

        // Prefer an existing initialized DB (has users table)
        if (looksInitialized(candidate1)) return candidate1;
        if (candidate2 != null && looksInitialized(candidate2)) return candidate2;

        // If one exists but not initialized, still prefer it (so it becomes the single DB)
        if (candidate1.exists()) return candidate1;
        if (candidate2 != null && candidate2.exists()) return candidate2;

        // Default
        return candidate1;
    }

    private static boolean looksInitialized(File dbFile) {
        if (dbFile == null || !dbFile.exists() || dbFile.length() == 0) return false;
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        try (Connection con = DriverManager.getConnection(url);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT name FROM sqlite_master WHERE type='table' AND name='users' LIMIT 1"
             )) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
}