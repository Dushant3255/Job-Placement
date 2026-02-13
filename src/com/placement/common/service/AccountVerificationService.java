package com.placement.common.service;

import com.placement.common.dao.UserDao;
import jakarta.mail.MessagingException;

import java.sql.SQLException;

public class AccountVerificationService {

    private final UserDao userDao = new UserDao();
    private final EmailService emailService = new EmailService();

    public void verifySignupEmail(String email) throws SQLException {
        userDao.setVerifiedByEmail(email, true);

        try {
            emailService.send(
                    email,
                    "Account Verified",
                    "Your account has been verified successfully. You can now sign in."
            );
        } catch (MessagingException ex) {
        	ex.printStackTrace();
        }
    }
}
