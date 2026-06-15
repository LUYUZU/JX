package com.example.studentdemo.entity;

public class Enrollment {
    private Integer enrollmentId;
    private Integer studentId;
    private Integer courseId;
    private Double usualScore;
    private Double finalScore;
    private Double totalScore;
    private Double gradePoint;
    private String gradeLevel;
    private Boolean isPublished;
    private String createdAt;
    private String updatedAt;

    private String studentName;
    private String studentNumber;
    private String courseName;
    private String courseCode;
    private Double credits;

    public Enrollment() {}

    public Integer getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(Integer enrollmentId) { this.enrollmentId = enrollmentId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public Double getUsualScore() { return usualScore; }
    public void setUsualScore(Double usualScore) { this.usualScore = usualScore; }
    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }
    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }
    public Double getGradePoint() { return gradePoint; }
    public void setGradePoint(Double gradePoint) { this.gradePoint = gradePoint; }
    public String getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public Double getCredits() { return credits; }
    public void setCredits(Double credits) { this.credits = credits; }
}
