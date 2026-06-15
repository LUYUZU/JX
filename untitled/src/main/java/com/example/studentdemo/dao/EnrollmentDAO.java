package com.example.studentdemo.dao;

import com.example.studentdemo.entity.Enrollment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EnrollmentDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Enrollment> rowMapper = (rs, n) -> {
        Enrollment e = new Enrollment();
        e.setEnrollmentId(rs.getInt("EnrollmentID"));
        e.setStudentId(rs.getInt("StudentID"));
        e.setCourseId(rs.getInt("CourseID"));
        e.setUsualScore(rs.getObject("UsualScore") != null ? rs.getDouble("UsualScore") : null);
        e.setFinalScore(rs.getObject("FinalScore") != null ? rs.getDouble("FinalScore") : null);
        e.setTotalScore(rs.getObject("TotalScore") != null ? rs.getDouble("TotalScore") : null);
        e.setGradePoint(rs.getObject("GradePoint") != null ? rs.getDouble("GradePoint") : null);
        e.setGradeLevel(rs.getString("GradeLevel"));
        e.setIsPublished(rs.getBoolean("IsPublished"));
        e.setCreatedAt(rs.getString("CreatedAt"));
        e.setUpdatedAt(rs.getString("UpdatedAt"));
        try { e.setStudentName(rs.getString("StudentName")); } catch (SQLException ignored) {}
        try { e.setStudentNumber(rs.getString("StudentNumber")); } catch (SQLException ignored) {}
        try { e.setCourseName(rs.getString("CourseName")); } catch (SQLException ignored) {}
        try { e.setCourseCode(rs.getString("CourseCode")); } catch (SQLException ignored) {}
        try { e.setCredits(rs.getObject("Credits") != null ? rs.getDouble("Credits") : null); } catch (SQLException ignored) {}
        return e;
    };

    // 获取课程的所有选课学生
    public List<Enrollment> getByCourse(Integer courseId) {
        String sql = "SELECT e.*, s.StudentName, s.StudentNumber FROM Enrollments e " +
                "JOIN Students s ON e.StudentID = s.StudentID WHERE e.CourseID = ? ORDER BY s.StudentNumber";
        return jdbcTemplate.query(sql, rowMapper, courseId);
    }

    // 获取学生所有选课
    public List<Enrollment> getByStudent(Integer studentId) {
        String sql = "SELECT e.*, c.CourseName, c.CourseCode, c.Credits FROM Enrollments e " +
                "JOIN Courses c ON e.CourseID = c.CourseID WHERE e.StudentID = ? ORDER BY c.Semester DESC, c.CourseID";
        return jdbcTemplate.query(sql, rowMapper, studentId);
    }

    // 获取学生已发布成绩的课程（用于成绩单）
    public List<Enrollment> getPublishedByStudent(Integer studentId) {
        String sql = "SELECT e.*, c.CourseName, c.CourseCode, c.Credits FROM Enrollments e " +
                "JOIN Courses c ON e.CourseID = c.CourseID WHERE e.StudentID = ? AND e.IsPublished = 1 ORDER BY c.Semester DESC, c.CourseID";
        return jdbcTemplate.query(sql, rowMapper, studentId);
    }

    // 获取课程待录入成绩的学生
    public List<Enrollment> getUngradedByCourse(Integer courseId) {
        String sql = "SELECT e.*, s.StudentName, s.StudentNumber FROM Enrollments e " +
                "JOIN Students s ON e.StudentID = s.StudentID WHERE e.CourseID = ? AND (e.TotalScore IS NULL OR e.IsPublished = 0) ORDER BY s.StudentNumber";
        return jdbcTemplate.query(sql, rowMapper, courseId);
    }

    // 选课
    public int enroll(Integer studentId, Integer courseId) {
        return jdbcTemplate.update("INSERT INTO Enrollments (StudentID, CourseID) VALUES (?, ?)", studentId, courseId);
    }

    // 退课
    public int unenroll(Integer enrollmentId) {
        return jdbcTemplate.update("DELETE FROM Enrollments WHERE EnrollmentID = ?", enrollmentId);
    }

    // 录入成绩
    public int updateGrade(Integer enrollmentId, Double usualScore, Double finalScore, Double totalScore, Double gradePoint, String gradeLevel, Boolean isPublished) {
        return jdbcTemplate.update(
            "UPDATE Enrollments SET UsualScore=?, FinalScore=?, TotalScore=?, GradePoint=?, GradeLevel=?, IsPublished=? WHERE EnrollmentID=?",
            usualScore, finalScore, totalScore, gradePoint, gradeLevel, isPublished, enrollmentId
        );
    }

    // 发布/取消发布成绩
    public int publish(Integer enrollmentId, Boolean publish) {
        return jdbcTemplate.update("UPDATE Enrollments SET IsPublished = ? WHERE EnrollmentID = ?", publish, enrollmentId);
    }

    // 检查是否已选课
    public boolean isEnrolled(Integer studentId, Integer courseId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Enrollments WHERE StudentID = ? AND CourseID = ?", Integer.class, studentId, courseId);
        return count != null && count > 0;
    }

    // 获取选课总数
    public int getEnrollmentCount(Integer courseId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Enrollments WHERE CourseID = ?", Integer.class, courseId);
        return count != null ? count : 0;
    }
}
