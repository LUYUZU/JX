package com.example.studentdemo.controller;

import com.example.studentdemo.dao.CourseDAO;
import com.example.studentdemo.dao.UserDAO;
import com.example.studentdemo.entity.Course;
import com.example.studentdemo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseDAO courseDAO;

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/teachers")
    public ResponseEntity<Map<String, Object>> getTeachers() {
        Map<String, Object> resp = new HashMap<>();
        try {
            resp.put("success", true);
            resp.put("data", userDAO.findAllTeachers());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCourses(
            @RequestAttribute(value = "role", required = false) String role,
            @RequestAttribute(value = "userId", required = false) Integer userId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            List<Course> list;
            if ("teacher".equals(role) && userId != null) {
                list = courseDAO.getCoursesByTeacher(userId);
            } else {
                list = courseDAO.getAllCourses();
            }
            resp.put("success", true);
            resp.put("data", list);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    // 获取可选课程（未被当前学生选中的）
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailable(
            @RequestParam Integer studentId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            resp.put("success", true);
            resp.put("data", courseDAO.getAvailableCourses(studentId));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Integer id) {
        Map<String, Object> resp = new HashMap<>();
        try {
            resp.put("success", true);
            resp.put("data", courseDAO.getCourseById(id));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody Course course) {
        Map<String, Object> resp = new HashMap<>();
        if (course.getCourseName() == null || course.getCourseCode() == null || course.getCredits() == null) {
            resp.put("success", false);
            resp.put("message", "课程名称、课程代码和学分不能为空");
            return ResponseEntity.badRequest().body(resp);
        }
        try {
            if (courseDAO.isCourseCodeExists(course.getCourseCode(), null)) {
                resp.put("success", false);
                resp.put("message", "课程代码已存在");
                return ResponseEntity.badRequest().body(resp);
            }
            if (course.getTeacherId() != null) {
                User t = userDAO.findById(course.getTeacherId());
                if (t != null) course.setTeacherName(t.getRealName());
            }
            courseDAO.addCourse(course);
            resp.put("success", true);
            resp.put("message", "添加成功");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "添加失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Integer id, @RequestBody Course course) {
        Map<String, Object> resp = new HashMap<>();
        try {
            if (courseDAO.isCourseCodeExists(course.getCourseCode(), id)) {
                resp.put("success", false);
                resp.put("message", "课程代码已存在");
                return ResponseEntity.badRequest().body(resp);
            }
            if (course.getTeacherId() != null) {
                User t = userDAO.findById(course.getTeacherId());
                if (t != null) course.setTeacherName(t.getRealName());
            }
            course.setCourseId(id);
            courseDAO.updateCourse(course);
            resp.put("success", true);
            resp.put("message", "更新成功");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id) {
        Map<String, Object> resp = new HashMap<>();
        try {
            courseDAO.deleteCourse(id);
            resp.put("success", true);
            resp.put("message", "删除成功");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }
}
