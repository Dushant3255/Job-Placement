package com.placement.company.service;

import com.placement.common.dao.UserDao;
import com.placement.common.model.User;
import com.placement.company.dao.CompanyDao;
import com.placement.company.model.CompanyProfile;

public class CompanyService {

    private final UserDao userDao = new UserDao();
    private final CompanyDao companyDao = new CompanyDao();

    public CompanyProfile getProfileByEmail(String email) throws Exception {
        User u = userDao.findByUsernameOrEmail(email.trim());
        if (u == null) throw new IllegalArgumentException("User not found for: " + email);
        return companyDao.findByUserId(u.getId());
    }

    public void updateProfileByEmail(String email, CompanyProfile profile) throws Exception {
        User u = userDao.findByUsernameOrEmail(email.trim());
        if (u == null) throw new IllegalArgumentException("User not found for: " + email);
        companyDao.updateCompanyProfile(u.getId(), profile);
    }

    public String getLogoPathByEmail(String email) throws Exception {
        User u = userDao.findByUsernameOrEmail(email.trim());
        if (u == null) throw new IllegalArgumentException("User not found for: " + email);
        return companyDao.getLogoPath(u.getId());
    }
}
