package com.placement.common.service;

import com.placement.common.dao.UserDao;
import com.placement.common.model.User;
import com.placement.common.model.UserRole;
import com.placement.common.util.PasswordUtil;

public class AuthService {

    private final UserDao userDao = new UserDao();

    public enum AuthStatus {
        SUCCESS,
        NEEDS_VERIFICATION,
        FAIL
    }

    public static class AuthResult {
        public final AuthStatus status;
        public final String message;
        public final User user;

        private AuthResult(AuthStatus status, String message, User user) {
            this.status = status;
            this.message = message;
            this.user = user;
        }

        public static AuthResult ok(User user) {
            return new AuthResult(AuthStatus.SUCCESS, "OK", user);
        }

        public static AuthResult needsVerification(User user) {
            return new AuthResult(AuthStatus.NEEDS_VERIFICATION, "Account not verified yet.", user);
        }

        public static AuthResult fail(String msg) {
            return new AuthResult(AuthStatus.FAIL, msg, null);
        }
    }

    public AuthResult login(String login, String plainPassword) throws Exception {
        String lg = (login == null) ? "" : login.trim();
        String pw = (plainPassword == null) ? "" : plainPassword;

        if (lg.isEmpty()) return AuthResult.fail("Username/email required.");
        if (pw.isEmpty()) return AuthResult.fail("Password required.");

        User user = userDao.findByUsernameOrEmail(lg);
        if (user == null) return AuthResult.fail("Invalid username/email or password.");

        String hash = userDao.getPasswordHashByUserId(user.getId());
        if (hash == null || hash.isBlank()) return AuthResult.fail("Invalid username/email or password.");

        boolean ok = PasswordUtil.verify(pw, hash);
        if (!ok) return AuthResult.fail("Invalid username/email or password.");

        // ✅ IMPORTANT CHANGE:
        // Don’t block unverified users — return the user so UI can send them to OTP.
        if (!user.isVerified()) {
            return AuthResult.needsVerification(user);
        }

        return AuthResult.ok(user);
    }

    public UserRole getRoleForLogin(String login) throws Exception {
        String lg = (login == null) ? "" : login.trim();
        if (lg.isEmpty()) return null;

        User user = userDao.findByUsernameOrEmail(lg);
        return user == null ? null : user.getRole();
    }
}
