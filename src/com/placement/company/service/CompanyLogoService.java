package com.placement.company.service;

import com.placement.common.dao.UserDao;
import com.placement.common.model.User;
import com.placement.company.dao.CompanyDao;
import com.placement.common.util.FileStorageUtil;

import java.io.File;

public class CompanyLogoService {

    private final UserDao userDao = new UserDao();
    private final CompanyDao companyDao = new CompanyDao();

    private int getUserIdByEmail(String email) throws Exception {
        User u = userDao.findByUsernameOrEmail(email.trim());
        if (u == null) throw new IllegalArgumentException("User not found for: " + email);
        return u.getId();
    }

    public String saveUploadedLogo(String companyEmail, File selectedImageFile) throws Exception {
        int userId = getUserIdByEmail(companyEmail);
        String path = FileStorageUtil.saveCompanyLogo(selectedImageFile, userId);
        companyDao.updateLogoPath(userId, path);
        return path;
    }

    // ✅ This matches your ProfilePictureScreen call: applyDefaultLogo(email, defaultImageName)
    public String applyDefaultLogo(String companyEmail, String defaultImageName) throws Exception {
        int userId = getUserIdByEmail(companyEmail);
        String path = FileStorageUtil.copyDefaultImageToCompanyUploads(defaultImageName, userId);
        companyDao.updateLogoPath(userId, path);
        return path;
    }

    // ✅ Optional: keep your old method too (in case you call it elsewhere)
    public String applyDefaultLogo(String companyEmail) throws Exception {
        return applyDefaultLogo(companyEmail, "default_company_logo.png");
    }
}
