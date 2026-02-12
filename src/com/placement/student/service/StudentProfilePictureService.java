package com.placement.student.service;

import com.placement.common.dao.UserDao;
import com.placement.common.model.User;
import com.placement.common.util.FileStorageUtil;
import com.placement.student.dao.StudentDao;

import java.io.File;

public class StudentProfilePictureService {

    private final UserDao userDao = new UserDao();
    private final StudentDao studentDao = new StudentDao();

    private int getUserIdByEmail(String email) throws Exception {
        User u = userDao.findByUsernameOrEmail(email.trim());
        if (u == null) throw new IllegalArgumentException("User not found for: " + email);
        return u.getId();
    }

    public String saveUploadedProfilePicture(String studentEmail, File selectedImageFile) throws Exception {
        int userId = getUserIdByEmail(studentEmail);
        String path = FileStorageUtil.saveStudentProfileImage(selectedImageFile, userId);
        studentDao.updateProfileImagePath(userId, path);
        return path;
    }

    public String applyDefaultProfilePicture(String studentEmail, String defaultImageName) throws Exception {
        int userId = getUserIdByEmail(studentEmail);
        String path = FileStorageUtil.copyDefaultImageToStudentUploads(userId, defaultImageName);
        studentDao.updateProfileImagePath(userId, path);
        return path;
    }
}
