package com.placement.common.service;

import com.placement.common.dao.UserDao;
import com.placement.common.model.User;
import com.placement.common.util.PasswordUtil;

public class PasswordResetService {

    private final UserDao userDao = new UserDao();

    public void resetPassword(String emailOrUsername, String newPlainPassword) throws Exception {
        if (emailOrUsername == null || emailOrUsername.isBlank())
            throw new IllegalArgumentException("Email required.");

        if (newPlainPassword == null || newPlainPassword.isBlank())
            throw new IllegalArgumentException("Password required.");

        if (newPlainPassword.length() < 8)
            throw new IllegalArgumentException("Password must be at least 8 characters.");

        User u = userDao.findByUsernameOrEmail(emailOrUsername.trim());
        if (u == null) throw new IllegalArgumentException("Account not found.");

        String newHash = PasswordUtil.hash(newPlainPassword);

        int updated = userDao.updatePasswordHashByUserId(u.getId(), newHash);
        if (updated != 1) throw new IllegalStateException("Password update failed.");
    }
}
