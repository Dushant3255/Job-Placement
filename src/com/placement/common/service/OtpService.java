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


    // ✅ temporary dev OTP
    public static final String TEST_OTP = "123456";

    private static final int EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final OtpDao otpDao = new OtpDao();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final SecureRandom random = new SecureRandom();

    // email sender (your gmail sender)
    private final EmailService emailService = new EmailService();

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

        String otp = isTestMode() ? TEST_OTP : generateOtp6();

        String expiresAt = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES).format(fmt);
        String otpHash = PasswordUtil.hash(otp);

        // ✅ Always store in DB (even test mode)
        otpDao.createOtp(email.trim(), purpose, otpHash, expiresAt);

        // ✅ Test mode: do NOT send email
        if (isTestMode()) {
            System.out.println("[OTP TEST MODE] OTP for " + email + " (" + purpose + ") = " + TEST_OTP);
            return;
        }

        // ✅ Normal mode: send real email
        String subject = "Your OTP Code";
        String body =
                "Your OTP code is: " + otp + "\n\n" +
                "It expires in " + EXPIRY_MINUTES + " minutes.\n" +
                "If you did not request this, ignore this email.\n\n" +
                "Purpose: " + purpose;

        emailService.send(email.trim(), subject, body);
    }

    public boolean verifyOtp(String email, String purpose, String enteredOtp) throws SQLException {
        if (email == null || email.isBlank()) return false;
        if (purpose == null || purpose.isBlank()) return false;
        if (enteredOtp == null || enteredOtp.isBlank()) return false;

        OtpDao.OtpRow row = otpDao.findLatestActive(email.trim(), purpose);
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
