package com.placement.common.service;

import com.placement.common.dao.UserDao;
import com.placement.common.model.User;
import com.placement.common.model.UserRole;
import com.placement.common.util.PasswordUtil;
import com.placement.company.dao.CompanyDao;
import com.placement.company.model.CompanyProfile;
import com.placement.student.dao.StudentDao;
import com.placement.student.model.StudentProfile;

import java.sql.SQLException;

public class RegistrationService {

    private final UserDao userDao = new UserDao();
    private final CompanyDao companyDao = new CompanyDao();
    private final StudentDao studentDao = new StudentDao();

    public int registerCompanyPendingOtp(String username, String email, String plainPassword, CompanyProfile profile)
            throws SQLException {

        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required");
        if (plainPassword == null || plainPassword.isBlank()) throw new IllegalArgumentException("Password required");
        if (profile == null || profile.getCompanyName() == null || profile.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name required");
        }

        String un = username.trim();
        String em = email.trim();
        String hash = PasswordUtil.hash(plainPassword);

        int userId = createOrReusePendingUser(UserRole.COMPANY, un, em, hash);

        // Ensure company profile row exists (if retrying same unverified email)
        try {
            companyDao.createCompanyProfile(userId, profile);
        } catch (SQLException ex) {
            // If already exists, ignore (pending account retry)
            String msg = (ex.getMessage() == null) ? "" : ex.getMessage().toLowerCase();
            if (!(msg.contains("unique") || msg.contains("constraint") || msg.contains("primary key"))) {
                throw ex;
            }
        }

        // OTP is issued by VerifyOtpScreen when it opens
        return userId;
    }

    public int registerStudentPendingOtp(String username, String email, String plainPassword, StudentProfile profile)
            throws SQLException {

        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required");
        if (plainPassword == null || plainPassword.isBlank()) throw new IllegalArgumentException("Password required");

        if (profile == null
                || profile.getFirstName() == null || profile.getFirstName().isBlank()
                || profile.getLastName() == null || profile.getLastName().isBlank()) {
            throw new IllegalArgumentException("First and last name required");
        }

        String un = username.trim();
        String em = email.trim();
        String hash = PasswordUtil.hash(plainPassword);

        int userId = createOrReusePendingUser(UserRole.STUDENT, un, em, hash);

        // Ensure student profile row exists (if retrying same unverified email)
        try {
            studentDao.createStudentProfile(userId, profile);
        } catch (SQLException ex) {
            // If already exists, ignore (pending account retry)
            String msg = (ex.getMessage() == null) ? "" : ex.getMessage().toLowerCase();
            if (!(msg.contains("unique") || msg.contains("constraint") || msg.contains("primary key"))) {
                throw ex;
            }
        }

        // OTP is issued by VerifyOtpScreen when it opens
        return userId;
    }

    /**
     * Creates a NEW user if possible.
     * If duplicate happens:
     *  - If existing account is UNVERIFIED and matches role -> reuse it (allow resend OTP)
     *  - If VERIFIED -> block
     *  - If role mismatch -> block
     */
    private int createOrReusePendingUser(UserRole role, String username, String email, String passwordHash)
            throws SQLException {

        try {
            return userDao.createUser(role, username, email, passwordHash, false);
        } catch (SQLException ex) {

            String msg = (ex.getMessage() == null) ? "" : ex.getMessage().toLowerCase();

            boolean emailDup = msg.contains("users.email") || msg.contains("unique constraint failed: users.email") || msg.contains("email");
            boolean userDup  = msg.contains("users.username") || msg.contains("unique constraint failed: users.username") || msg.contains("username");

            // 1) EMAIL duplicate -> check if it's an unverified pending account
            if (emailDup) {
                User existing = userDao.findByUsernameOrEmail(email);
                if (existing != null) {
                    if (existing.isVerified()) {
                        throw new IllegalArgumentException("Email already in use.");
                    }
                    if (existing.getRole() != role) {
                        throw new IllegalArgumentException("This email is already registered as " + existing.getRole() + ".");
                    }
                    // reuse pending account
                    return existing.getId();
                }
                throw new IllegalArgumentException("Email already in use.");
            }

            // 2) USERNAME duplicate -> only reuse if same email + unverified
            if (userDup) {
                User existing = userDao.findByUsernameOrEmail(username);
                if (existing != null) {
                    if (existing.isVerified()) {
                        throw new IllegalArgumentException("Username already in use.");
                    }
                    if (existing.getRole() != role) {
                        throw new IllegalArgumentException("Username already in use.");
                    }
                    if (!existing.getEmail().equalsIgnoreCase(email)) {
                        throw new IllegalArgumentException("Username already in use.");
                    }
                    // reuse pending account
                    return existing.getId();
                }
                throw new IllegalArgumentException("Username already in use.");
            }

            throw ex;
        }
    }
}
