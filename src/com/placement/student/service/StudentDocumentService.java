package com.placement.student.service;

import com.placement.common.util.FileStorageUtil;
import com.placement.student.dao.StudentDocumentDAO;

import java.io.File;

public class StudentDocumentService {

    private final StudentDocumentDAO dao;

    public StudentDocumentService(StudentDocumentDAO dao) {
        this.dao = dao;
    }

    public String getCvPath(long studentId) {
        return dao.getCvPath(studentId);
    }

    public String uploadCv(long studentId, File cvFile) throws Exception {
        if (cvFile == null) throw new IllegalArgumentException("CV file required");
        String name = cvFile.getName().toLowerCase();
        if (!(name.endsWith(".pdf") || name.endsWith(".doc") || name.endsWith(".docx"))) {
            throw new IllegalArgumentException("CV must be PDF/DOC/DOCX");
        }

        String saved = FileStorageUtil.saveStudentCv(cvFile, (int) studentId);
        dao.saveOrUpdateCvPath(studentId, saved);
        return saved;
    }
}
