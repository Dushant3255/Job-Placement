package com.placement.common.service;

import com.placement.common.dao.OtpDao;
import com.placement.common.util.PasswordUtil;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OtpService {

    public static final String PURPOSE_STUDENT_SIGNUP = "STUDENT_SIGNUP";
    public static final String PURPOSE_COMPANY_SIGNUP = "COMPANY_SIGNUP";
    public static final String PURPOSE_FORGOT_PASSWORD = "FORGOT_PASSWORD";
    public static final String PURPOSE_TWO_FACTOR = "TWO_FACTOR";

    public static final String TEST_OTP = "123456";

    private static final int EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final OtpDao otpDao = new OtpDao();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final SecureRandom random = new SecureRandom();

    // ✅ lazy init so test mode can run without env vars
    private EmailService emailService;

    /** Set OTP_TEST_MODE=true (env var) OR -Dotp.test=true (VM arg) */
    public boolean isTestMode() {
        String env = System.getenv("OTP_TEST_MODE");
        if (env != null && env.equalsIgnoreCase("true")) return true;

        String prop = System.getProperty("otp.test");
        return prop != null && prop.equalsIgnoreCase("true");
    }

    public void issueOtp(String email, String purpose) throws Exception {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required");
        if (purpose == null || purpose.isBlank()) throw new IllegalArgumentException("Purpose required");

        String cleanEmail = email.trim();
        String cleanPurpose = purpose.trim();

        String otp = isTestMode() ? TEST_OTP : generateOtp6();

        String expiresAt = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES).format(fmt);
        String otpHash = PasswordUtil.hash(otp);

        // ✅ Always store in DB (even test mode)
        otpDao.createOtp(cleanEmail, cleanPurpose, otpHash, expiresAt);

        // ✅ Test mode: do NOT send email
        if (isTestMode()) {
            System.out.println("[OTP TEST MODE] OTP for " + cleanEmail + " (" + cleanPurpose + ") = " + TEST_OTP);
            return;
        }

        // ✅ Normal mode: send real email (init only here)
        if (emailService == null) emailService = new EmailService();

        String subject = "Your OTP Code";
        String body =
                "Your OTP code is: " + otp + "\n\n" +
                "It expires in " + EXPIRY_MINUTES + " minutes.\n" +
                "If you did not request this, ignore this email.\n\n" +
                "Purpose: " + cleanPurpose;

        emailService.send(cleanEmail, subject, body);
    }

    public boolean verifyOtp(String email, String purpose, String enteredOtp) throws SQLException {
        if (email == null || email.isBlank()) return false;
        if (purpose == null || purpose.isBlank()) return false;
        if (enteredOtp == null || enteredOtp.isBlank()) return false;

        String cleanEmail = email.trim();
        String cleanPurpose = purpose.trim();

        OtpDao.OtpRow row = otpDao.findLatestActive(cleanEmail, cleanPurpose);
        if (row == null) return false;

        if (row.attempts >= MAX_ATTEMPTS) return false;

        otpDao.incrementAttempts(row.id);

        boolean ok = PasswordUtil.verify(enteredOtp.trim(), row.otpHash);
        if (ok) otpDao.markUsed(row.id);

        return ok;
    }

    private String generateOtp6() {
        int n = random.nextInt(1_000_000);
        return String.format("%06d", n);
    }
}
