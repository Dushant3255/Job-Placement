package com.placement.common.service;

import com.placement.common.dao.UserDao;
import com.placement.common.model.UserRole;
import com.placement.common.util.PasswordUtil;
import com.placement.company.dao.CompanyDao;
import com.placement.company.model.CompanyProfile;

import java.sql.SQLException;

public class RegistrationService {

    private final UserDao userDao = new UserDao();
    private final CompanyDao companyDao = new CompanyDao();
    private final OtpService otpService = new OtpService();

    public int registerCompanyPendingOtp(String username, String email, String plainPassword, CompanyProfile profile)
            throws Exception {

        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required");
        if (plainPassword == null || plainPassword.isBlank()) throw new IllegalArgumentException("Password required");
        if (profile == null || profile.getCompanyName() == null || profile.getCompanyName().isBlank())
            throw new IllegalArgumentException("Company name required");

        String hash = PasswordUtil.hash(plainPassword);

        int userId;
        try {
            userId = userDao.createUser(UserRole.COMPANY, username.trim(), email.trim(), hash, false);
        } catch (SQLException ex) {
            String msg = (ex.getMessage() == null) ? "" : ex.getMessage().toLowerCase();

            if (msg.contains("username")) throw new IllegalArgumentException("Username already exists.");
            if (msg.contains("email")) throw new IllegalArgumentException("Email already exists.");
            throw ex;
        }

        companyDao.createCompanyProfile(userId, profile);

        // ✅ OTP (email sending may throw Exception)
        otpService.issueOtp(email.trim(), OtpService.PURPOSE_COMPANY_SIGNUP);

        return userId;
    }
}
