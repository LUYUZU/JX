package com.example.studentdemo.controller;

import com.example.studentdemo.dao.CourseDAO;
import com.example.studentdemo.dao.EnrollmentDAO;
import com.example.studentdemo.dao.StudentDAO;
import com.example.studentdemo.entity.Enrollment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private EnrollmentDAO enrollmentDAO;

    @Autowired
    private CourseDAO courseDAO;

    @Autowired
    private StudentDAO studentDAO;

    // 学生成绩单
    @GetMapping("/transcript/{studentId}")
    public ResponseEntity<Map<String, Object>> getTranscript(@PathVariable Integer studentId,
            @RequestAttribute(value = "role", required = false) String role,
            @RequestAttribute(value = "userId", required = false) Integer userId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            List<Enrollment> list = enrollmentDAO.getPublishedByStudent(studentId);
            double totalCredits = 0;
            double weightedPoints = 0;
            for (Enrollment e : list) {
                if (e.getCredits() != null && e.getGradePoint() != null) {
                    totalCredits += e.getCredits();
                    weightedPoints += e.getCredits() * e.getGradePoint();
                }
            }
            double gpa = totalCredits > 0 ? Math.round(weightedPoints / totalCredits * 100.0) / 100.0 : 0;

            resp.put("success", true);
            resp.put("data", Map.of(
                "enrollments", list,
                "totalCourses", list.size(),
                "totalCredits", totalCredits,
                "gpa", gpa
            ));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "生成成绩单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    // 课程成绩分析
    @GetMapping("/course-analysis/{courseId}")
    public ResponseEntity<Map<String, Object>> getCourseAnalysis(@PathVariable Integer courseId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            List<Enrollment> list = enrollmentDAO.getByCourse(courseId);
            list.removeIf(e -> e.getTotalScore() == null || e.getIsPublished() == null || !e.getIsPublished());

            int excellent = 0, good = 0, medium = 0, pass = 0, fail = 0;
            double sum = 0, max = 0, min = 100;
            for (Enrollment e : list) {
                double s = e.getTotalScore();
                sum += s;
                max = Math.max(max, s);
                min = Math.min(min, s);
                if (s >= 90) excellent++;
                else if (s >= 80) good++;
                else if (s >= 70) medium++;
                else if (s >= 60) pass++;
                else fail++;
            }
            int n = list.size();
            double avg = n > 0 ? Math.round(sum / n * 10.0) / 10.0 : 0;
            double passRate = n > 0 ? Math.round((n - fail) * 1000.0 / n) / 10.0 : 0;

            Map<String, Object> data = new HashMap<>();
            data.put("studentCount", n);
            data.put("avgScore", avg);
            data.put("maxScore", max);
            data.put("minScore", min);
            data.put("passRate", passRate);
            data.put("excellent", excellent);
            data.put("good", good);
            data.put("medium", medium);
            data.put("pass", pass);
            data.put("fail", fail);

            List<Map<String, Object>> dist = new ArrayList<>();
            dist.add(Map.of("range", "90-100", "count", excellent));
            dist.add(Map.of("range", "80-89", "count", good));
            dist.add(Map.of("range", "70-79", "count", medium));
            dist.add(Map.of("range", "60-69", "count", pass));
            dist.add(Map.of("range", "<60", "count", fail));
            data.put("distribution", dist);
            resp.put("data", data);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "分析失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    // 所有课程统计概览
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        Map<String, Object> resp = new HashMap<>();
        try {
            double[] allScores = {0};
            int[] counts = {0};

            resp.put("success", true);
            resp.put("data", Map.of(
                "totalStudents", studentDAO.getTotalStudentCount(),
                "totalCourses", courseDAO.getAllCourses().size()
            ));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }
}
