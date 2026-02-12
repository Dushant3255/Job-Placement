package com.placement.common.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite DB helper.
 * - Uses an absolute DB path (prevents "multiple DB files" depending on run directory)
 * - Creates tables if missing
 * - Enables foreign keys
 */
public final class DB {

    // Folder + file name (relative to project root), but we convert to absolute for JDBC
    private static final String DB_DIR_NAME = "data";
    private static final String DB_FILE_NAME = "student_placement.db";

    private static final File DB_DIR = new File(DB_DIR_NAME);
    private static final File DB_FILE = new File(DB_DIR, DB_FILE_NAME);

    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE.getAbsolutePath();

    private DB() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
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

            // IMPORTANT: SQLite requires this per connection for FK enforcement
            st.execute("PRAGMA foreign_keys = ON;");

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
            		  eligibility_rule TEXT,
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
            		  status TEXT NOT NULL DEFAULT 'SCHEDULED',
            		  notes TEXT,
            		  FOREIGN KEY(application_id) REFERENCES applications(application_id) ON DELETE CASCADE
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


            System.out.println("✅ DB initialized at: " + DB_FILE.getAbsolutePath());

        } catch (SQLException e) {
            throw new RuntimeException("DB init failed: " + e.getMessage(), e);
        }
    }

    private static void ensureDbFolder() {
        if (!DB_DIR.exists()) {
            boolean ok = DB_DIR.mkdirs();
            if (!ok) {
                throw new RuntimeException("Could not create DB folder: " + DB_DIR.getAbsolutePath());
            }
        }
    }
}
