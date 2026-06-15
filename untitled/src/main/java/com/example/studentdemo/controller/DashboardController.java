package com.example.studentdemo.controller;

import com.example.studentdemo.dao.StudentDAO;
import com.example.studentdemo.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private StudentDAO studentDAO;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("totalStudents", studentDAO.getTotalStudentCount());
            data.put("byMajor", studentDAO.getStudentCountByMajor());
            data.put("byGrade", studentDAO.getStudentCountByGrade());
            data.put("byGender", studentDAO.getStudentCountByGender());
            data.put("enrollmentTrend", studentDAO.getEnrollmentTrend());
            data.put("recentStudents", studentDAO.getRecentStudents(5));

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计数据失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
