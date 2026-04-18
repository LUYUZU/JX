package com.example.studentdemo.dao;

import com.example.studentdemo.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StudentDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<Student> studentRowMapper = new RowMapper<Student>() {
        @Override
        public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
            Student student = new Student();
            student.setStudentId(rs.getInt("StudentID"));
            student.setStudentName(rs.getString("StudentName"));
            student.setStudentNumber(rs.getString("StudentNumber"));
            student.setGender(rs.getString("Gender"));
            student.setAge(rs.getInt("Age"));
            student.setMajor(rs.getString("Major"));
            student.setGrade(rs.getString("Grade"));
            student.setEmail(rs.getString("Email"));
            student.setEnrollmentDate(rs.getDate("EnrollmentDate") != null ?
                    rs.getDate("EnrollmentDate").toString() : null);
            student.setIsActive(rs.getBoolean("IsActive"));
            return student;
        }
    };

    // 1. 查询所有学生
    public List<Student> getAllStudents() {
        String sql = "SELECT * FROM Students WHERE IsActive = 1 ORDER BY StudentID";
        return jdbcTemplate.query(sql, studentRowMapper);
    }

    // 2. 带搜索和筛选的查询 - 确保这个方法存在
    public List<Student> searchStudents(String keyword, String major, String grade) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Students WHERE IsActive = 1");
        List<Object> params = new ArrayList<>();

        // 添加搜索条件（姓名或学号）
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (StudentName LIKE ? OR StudentNumber LIKE ?)");
            String likePattern = "%" + keyword.trim() + "%";
            params.add(likePattern);
            params.add(likePattern);
        }

        // 添加专业筛选
        if (major != null && !major.trim().isEmpty() && !"全部".equals(major)) {
            sql.append(" AND Major = ?");
            params.add(major.trim());
        }

        // 添加年级筛选
        if (grade != null && !grade.trim().isEmpty() && !"全部".equals(grade)) {
            sql.append(" AND Grade = ?");
            params.add(grade.trim());
        }

        sql.append(" ORDER BY StudentID");

        return jdbcTemplate.query(sql.toString(), params.toArray(), studentRowMapper);
    }

    // 3. 根据ID查询学生
    public Student getStudentById(Integer id) {
        String sql = "SELECT * FROM Students WHERE StudentID = ?";
        return jdbcTemplate.queryForObject(sql, studentRowMapper, id);
    }

    // 4. 新增学生
    public int addStudent(Student student) {
        String sql = "INSERT INTO Students (StudentName, StudentNumber, Gender, Age, Major, Grade, Email, EnrollmentDate, IsActive) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.update(sql,
                student.getStudentName(),
                student.getStudentNumber(),
                student.getGender(),
                student.getAge(),
                student.getMajor(),
                student.getGrade(),
                student.getEmail(),
                student.getEnrollmentDate(),
                student.getIsActive()
        );
    }

    // 5. 更新学生信息
    public int updateStudent(Student student) {
        String sql = "UPDATE Students SET StudentName=?, StudentNumber=?, Gender=?, Age=?, " +
                "Major=?, Grade=?, Email=?, EnrollmentDate=?, IsActive=? WHERE StudentID=?";

        return jdbcTemplate.update(sql,
                student.getStudentName(),
                student.getStudentNumber(),
                student.getGender(),
                student.getAge(),
                student.getMajor(),
                student.getGrade(),
                student.getEmail(),
                student.getEnrollmentDate(),
                student.getIsActive(),
                student.getStudentId()
        );
    }

    // 6. 删除学生
    public int deleteStudent(Integer id) {
        String sql = "UPDATE Students SET IsActive = 0 WHERE StudentID = ?";
        return jdbcTemplate.update(sql, id);
    }

    // 7. 检查学号是否已存在
    public boolean isStudentNumberExists(String studentNumber, Integer excludeId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Students WHERE StudentNumber = ?");
        List<Object> params = new ArrayList<>();
        params.add(studentNumber);

        if (excludeId != null) {
            sql.append(" AND StudentID != ?");
            params.add(excludeId);
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null && count > 0;
    }

    // 8. 获取所有专业
    public List<String> getAllMajors() {
        String sql = "SELECT DISTINCT Major FROM Students WHERE IsActive = 1 AND Major IS NOT NULL AND Major != '' ORDER BY Major";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    // 9. 获取所有年级
    public List<String> getAllGrades() {
        String sql = "SELECT DISTINCT Grade FROM Students WHERE IsActive = 1 AND Grade IS NOT NULL AND Grade != '' ORDER BY Grade";
        return jdbcTemplate.queryForList(sql, String.class);
    }
}