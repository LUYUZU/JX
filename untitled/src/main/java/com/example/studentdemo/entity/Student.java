package com.example.studentdemo.entity;

public class Student {
    private Integer studentId;
    private String studentName;
    private String studentNumber;
    private String gender;
    private Integer age;
    private String major;
    private String grade;
    private String email;
    private String enrollmentDate;
    private Boolean isActive;

    // 构造函数
    public Student() {}

    public Student(Integer studentId, String studentName, String studentNumber,
                   String gender, Integer age, String major, String grade,
                   String email, String enrollmentDate, Boolean isActive) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentNumber = studentNumber;
        this.gender = gender;
        this.age = age;
        this.major = major;
        this.grade = grade;
        this.email = email;
        this.enrollmentDate = enrollmentDate;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(String enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Student(String studentName, String studentNumber, String gender,
                   Integer age, String major, String grade, String email,
                   String enrollmentDate, Boolean isActive) {
        this.studentName = studentName;
        this.studentNumber = studentNumber;
        this.gender = gender;
        this.age = age;
        this.major = major;
        this.grade = grade;
        this.email = email;
        this.enrollmentDate = enrollmentDate;
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", studentNumber='" + studentNumber + '\'' +
                '}';
    }
}