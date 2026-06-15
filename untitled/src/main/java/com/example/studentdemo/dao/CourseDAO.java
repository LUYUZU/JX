package com.example.studentdemo.dao;

import com.example.studentdemo.entity.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CourseDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Course> rowMapper = (rs, n) -> {
        Course c = new Course();
        c.setCourseId(rs.getInt("CourseID"));
        c.setCourseName(rs.getString("CourseName"));
        c.setCourseCode(rs.getString("CourseCode"));
        c.setCredits(rs.getDouble("Credits"));
        c.setTeacherId(rs.getInt("TeacherID"));
        c.setTeacherName(rs.getString("TeacherName"));
        c.setSemester(rs.getString("Semester"));
        c.setSchedule(rs.getString("Schedule"));
        c.setMaxStudents(rs.getInt("MaxStudents"));
        c.setEnrolledCount(rs.getInt("EnrolledCount"));
        c.setIsActive(rs.getBoolean("IsActive"));
        return c;
    };

    public List<Course> getAllCourses() {
        return jdbcTemplate.query("SELECT * FROM Courses WHERE IsActive = 1 ORDER BY Semester DESC, CourseID", rowMapper);
    }

    public List<Course> getCoursesByTeacher(Integer teacherId) {
        return jdbcTemplate.query("SELECT * FROM Courses WHERE IsActive = 1 AND TeacherID = ? ORDER BY Semester DESC, CourseID", rowMapper, teacherId);
    }

    public List<Course> getAvailableCourses(Integer studentId) {
        String sql = "SELECT c.* FROM Courses c WHERE c.IsActive = 1 AND c.EnrolledCount < c.MaxStudents " +
                "AND c.CourseID NOT IN (SELECT CourseID FROM Enrollments WHERE StudentID = ?) " +
                "ORDER BY c.Semester DESC, c.CourseID";
        return jdbcTemplate.query(sql, rowMapper, studentId);
    }

    public Course getCourseById(Integer id) {
        return jdbcTemplate.queryForObject("SELECT * FROM Courses WHERE CourseID = ?", rowMapper, id);
    }

    public int addCourse(Course course) {
        return jdbcTemplate.update(
            "INSERT INTO Courses (CourseName, CourseCode, Credits, TeacherID, TeacherName, Semester, Schedule, MaxStudents, IsActive) VALUES (?,?,?,?,?,?,?,?,1)",
            course.getCourseName(), course.getCourseCode(), course.getCredits(),
            course.getTeacherId(), course.getTeacherName(), course.getSemester(),
            course.getSchedule(), course.getMaxStudents()
        );
    }

    public int updateCourse(Course course) {
        return jdbcTemplate.update(
            "UPDATE Courses SET CourseName=?, CourseCode=?, Credits=?, TeacherID=?, TeacherName=?, Semester=?, Schedule=?, MaxStudents=? WHERE CourseID=?",
            course.getCourseName(), course.getCourseCode(), course.getCredits(),
            course.getTeacherId(), course.getTeacherName(), course.getSemester(),
            course.getSchedule(), course.getMaxStudents(), course.getCourseId()
        );
    }

    public int deleteCourse(Integer id) {
        return jdbcTemplate.update("UPDATE Courses SET IsActive = 0 WHERE CourseID = ?", id);
    }

    public boolean isCourseCodeExists(String code, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM Courses WHERE CourseCode = ?";
        if (excludeId != null) sql += " AND CourseID != ?";
        Integer count = excludeId != null
            ? jdbcTemplate.queryForObject(sql, Integer.class, code, excludeId)
            : jdbcTemplate.queryForObject(sql, Integer.class, code);
        return count != null && count > 0;
    }

    public void updateEnrolledCount(Integer courseId) {
        jdbcTemplate.update("UPDATE Courses c SET EnrolledCount = (SELECT COUNT(*) FROM Enrollments e WHERE e.CourseID = c.CourseID) WHERE c.CourseID = ?", courseId);
    }
}
