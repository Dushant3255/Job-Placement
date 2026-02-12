package com.placement.common.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private final String fromEmail;
    private final String appPassword;

    // If true => do NOT send OTP emails (you can still verify with 123456 in your OtpService)
    private final boolean otpTestMode;

    // If true => disable ALL emails (optional)
    private final boolean disableAllEmails;

    public EmailService() {
        this.fromEmail = System.getenv("GMAIL_USER");
        this.appPassword = System.getenv("GMAIL_APP_PASSWORD");

        this.otpTestMode = parseBool(System.getenv("OTP_TEST_MODE"));           // <== add this env var
        this.disableAllEmails = parseBool(System.getenv("DISABLE_ALL_EMAILS")); // optional

        // Only require Gmail creds if we will actually send emails
        if (!disableAllEmails) {
            if (fromEmail == null || fromEmail.isBlank() || appPassword == null || appPassword.isBlank()) {
                throw new IllegalStateException(
                        "Missing env vars. Set GMAIL_USER and GMAIL_APP_PASSWORD in your Run Configuration."
                );
            }
        }
    }

    public boolean isOtpTestMode() {
        return otpTestMode;
    }

    public void send(String toEmail, String subject, String body) throws MessagingException {
        if (disableAllEmails) return;

        // Gmail SMTP (STARTTLS)
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // timeouts
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromEmail));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
        msg.setSubject(subject);
        msg.setText(body);

        Transport.send(msg);
    }

    /* ---------------- Convenience methods ---------------- */

    public void sendOtpEmail(String toEmail, String otp, String purpose) throws MessagingException {
        // ✅ In OTP test mode: DO NOT send OTP emails
        if (otpTestMode || disableAllEmails) return;

        String subject = "Your OTP Code";
        String body =
                "Your OTP is: " + otp + "\n\n" +
                "Purpose: " + purpose + "\n" +
                "This OTP expires in a few minutes.\n\n" +
                "If you didn’t request this, ignore this email.";

        send(toEmail, subject, body);
    }

    public void sendAccountCreatedEmail(String toEmail, String username, String role) throws MessagingException {
        if (disableAllEmails) return;

        String subject = "Account Created Successfully";
        String body =
                "Hi " + safe(username) + ",\n\n" +
                "Your " + safe(role) + " account was created successfully and verified.\n\n" +
                "You can now log in to the Student Placement Portal.\n\n" +
                "Regards,\nStudent Placement Portal";

        send(toEmail, subject, body);
    }

    public void sendPasswordResetConfirmation(String toEmail) throws MessagingException {
        if (disableAllEmails) return;

        String subject = "Password Reset Successful";
        String body =
                "Hi,\n\n" +
                "Your password was successfully reset.\n\n" +
                "If you did not do this, please contact support immediately.\n\n" +
                "Regards,\nStudent Placement Portal";

        send(toEmail, subject, body);
    }

    /* ---------------- Helpers ---------------- */

    private static boolean parseBool(String v) {
        if (v == null) return false;
        v = v.trim().toLowerCase();
        return v.equals("1") || v.equals("true") || v.equals("yes") || v.equals("on");
    }

    private static String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}
