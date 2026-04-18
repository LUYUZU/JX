// UserDAO.java
package com.example.studentdemo.dao;

import com.example.studentdemo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setUserId(rs.getInt("UserID"));
            user.setUsername(rs.getString("Username"));
            user.setPassword(rs.getString("Password"));
            user.setRealName(rs.getString("RealName"));
            user.setRole(rs.getString("Role"));
            user.setStudentNumber(rs.getString("StudentNumber"));
            user.setIsActive(rs.getBoolean("IsActive"));
            user.setCreatedAt(rs.getTimestamp("CreatedAt") != null ?
                    rs.getTimestamp("CreatedAt").toString() : null);
            user.setLastLoginAt(rs.getTimestamp("LastLoginAt") != null ?
                    rs.getTimestamp("LastLoginAt").toString() : null);
            return user;
        }
    };

    // 根据用户名查询用户
    public User findByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE Username = ? AND IsActive = 1";
        try {
            System.out.println("执行SQL: " + sql + ", 参数: " + username);
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            System.out.println("查询结果: " + (user != null ? user.getUsername() : "null"));
            return user;
        } catch (Exception e) {
            System.out.println("查询用户失败: " + e.getMessage());
            return null;
        }
    }

    // 根据学号查询用户
    public User findByStudentNumber(String studentNumber) {
        String sql = "SELECT * FROM Users WHERE StudentNumber = ? AND IsActive = 1";
        try {
            return jdbcTemplate.queryForObject(sql, userRowMapper, studentNumber);
        } catch (Exception e) {
            return null;
        }
    }

    // 更新最后登录时间
    public void updateLastLoginTime(String username) {
        String sql = "UPDATE Users SET LastLoginAt = GETDATE() WHERE Username = ?";
        jdbcTemplate.update(sql, username);
    }
    // 添加用户账号
    public int addUser(User user) {
        String sql = "INSERT INTO Users (Username, Password, RealName, Role, StudentNumber, IsActive) VALUES (?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                user.getRole(),
                user.getStudentNumber(),
                user.getIsActive()
        );
    }

    // 根据学号禁用用户账号
    public int disableUserByStudentNumber(String studentNumber) {
        String sql = "UPDATE Users SET IsActive = 0 WHERE StudentNumber = ?";
        return jdbcTemplate.update(sql, studentNumber);
    }

    // 检查用户名是否存在
    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM Users WHERE Username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    // 根据学号更新用户信息
    public int updateUserByStudentNumber(User user) {
        String sql = "UPDATE Users SET RealName = ?, IsActive = ? WHERE StudentNumber = ?";
        return jdbcTemplate.update(sql,
                user.getRealName(),
                user.getIsActive(),
                user.getStudentNumber()
        );
    }
}