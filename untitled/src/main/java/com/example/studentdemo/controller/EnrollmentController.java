package com.example.studentdemo.controller;

import com.example.studentdemo.dao.CourseDAO;
import com.example.studentdemo.dao.EnrollmentDAO;
import com.example.studentdemo.entity.Enrollment;
import com.example.studentdemo.util.GradeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentDAO enrollmentDAO;

    @Autowired
    private CourseDAO courseDAO;

    // 获取课程选课列表
    @GetMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> getByCourse(@PathVariable Integer courseId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            resp.put("success", true);
            resp.put("data", enrollmentDAO.getByCourse(courseId));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    // 获取学生的选课（已发布成绩）
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getByStudent(@PathVariable Integer studentId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            resp.put("success", true);
            resp.put("data", enrollmentDAO.getPublishedByStudent(studentId));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    // 获取学生全部选课（含未发布成绩）
    @GetMapping("/student/{studentId}/all")
    public ResponseEntity<Map<String, Object>> getByStudentAll(@PathVariable Integer studentId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            resp.put("success", true);
            resp.put("data", enrollmentDAO.getByStudent(studentId));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    // 获取课程待录入成绩
    @GetMapping("/course/{courseId}/ungraded")
    public ResponseEntity<Map<String, Object>> getUngraded(@PathVariable Integer courseId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            resp.put("success", true);
            resp.put("data", enrollmentDAO.getUngradedByCourse(courseId));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    // 选课
    @PostMapping
    public ResponseEntity<Map<String, Object>> enroll(@RequestBody Map<String, Integer> body) {
        Map<String, Object> resp = new HashMap<>();
        Integer studentId = body.get("studentId");
        Integer courseId = body.get("courseId");
        try {
            if (enrollmentDAO.isEnrolled(studentId, courseId)) {
                resp.put("success", false);
                resp.put("message", "该学生已选此课程");
                return ResponseEntity.badRequest().body(resp);
            }
            enrollmentDAO.enroll(studentId, courseId);
            courseDAO.updateEnrolledCount(courseId);
            resp.put("success", true);
            resp.put("message", "选课成功");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "选课失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    // 退课
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> unenroll(@PathVariable Integer id,
            @RequestParam Integer courseId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            enrollmentDAO.unenroll(id);
            courseDAO.updateEnrolledCount(courseId);
            resp.put("success", true);
            resp.put("message", "退课成功");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "退课失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    // 录入/更新成绩
    @PutMapping("/{id}/grade")
    public ResponseEntity<Map<String, Object>> updateGrade(@PathVariable Integer id,
            @RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Double usualScore = body.get("usualScore") != null ? ((Number) body.get("usualScore")).doubleValue() : null;
            Double finalScore = body.get("finalScore") != null ? ((Number) body.get("finalScore")).doubleValue() : null;

            Double totalScore = null;
            Double gradePoint = null;
            String gradeLevel = null;

            if (usualScore != null && finalScore != null) {
                totalScore = GradeUtil.calcTotalScore(usualScore, finalScore);
                gradePoint = GradeUtil.calcGradePoint(totalScore);
                gradeLevel = GradeUtil.calcGradeLevel(totalScore);
            }

            boolean published = body.get("isPublished") != null && (Boolean) body.get("isPublished");

            enrollmentDAO.updateGrade(id, usualScore, finalScore, totalScore, gradePoint, gradeLevel, published);
            resp.put("success", true);
            resp.put("message", "成绩录入成功");
            resp.put("data", Map.of("totalScore", totalScore, "gradePoint", gradePoint, "gradeLevel", gradeLevel));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "录入失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    // 发布/取消发布成绩
    @PutMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishGrade(@PathVariable Integer id,
            @RequestBody Map<String, Boolean> body) {
        Map<String, Object> resp = new HashMap<>();
        try {
            boolean publish = body.getOrDefault("publish", true);
            enrollmentDAO.publish(id, publish);
            resp.put("success", true);
            resp.put("message", publish ? "成绩已发布" : "成绩已取消发布");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    // 获取课程已发布成绩列表（用于成绩分析）
    @GetMapping("/course/{courseId}/published")
    public ResponseEntity<Map<String, Object>> getPublishedByCourse(@PathVariable Integer courseId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            List<Enrollment> list = enrollmentDAO.getByCourse(courseId);
            list.removeIf(e -> e.getIsPublished() == null || !e.getIsPublished());
            resp.put("success", true);
            resp.put("data", list);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }
}
