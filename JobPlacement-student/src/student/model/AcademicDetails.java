package student.model;

public class AcademicDetails {
    private long studentId;
    private String program;
    private Integer yearOfStudy;
    private Double gpa;
    private Double cgpa;
    private Integer backlogs;
    private Integer graduationYear;
    private String eligibilityStatus; // ELIGIBLE / NOT_ELIGIBLE / UNKNOWN

    public AcademicDetails() {}

    // Getters/Setters
    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public Integer getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }

    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }

    public Double getCgpa() { return cgpa; }
    public void setCgpa(Double cgpa) { this.cgpa = cgpa; }

    public Integer getBacklogs() { return backlogs; }
    public void setBacklogs(Integer backlogs) { this.backlogs = backlogs; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public String getEligibilityStatus() { return eligibilityStatus; }
    public void setEligibilityStatus(String eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }
}