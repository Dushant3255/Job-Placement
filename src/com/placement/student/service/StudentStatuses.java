package com.placement.student.service;

public final class StudentStatuses {

    private StudentStatuses() {}

    public static final class OfferStatus {
        public static final String PENDING = "PENDING";
        public static final String ACCEPTED = "ACCEPTED";
        public static final String REJECTED = "REJECTED";
    }

    public static final class ApplicationStatus {
        public static final String SUBMITTED = "SUBMITTED";
        public static final String SHORTLISTED = "SHORTLISTED";
        public static final String REJECTED = "REJECTED";
        public static final String OFFERED = "OFFERED";
        public static final String WITHDRAWN = "WITHDRAWN";
    }

    public static final class JobStatus {
        public static final String OPEN = "OPEN";
        public static final String CLOSED = "CLOSED";
    }
}
