package com.example.studentdemo.controller;

import com.example.studentdemo.dao.StudentDAO;
import com.example.studentdemo.dao.UserDAO;
import com.example.studentdemo.entity.Student;
import com.example.studentdemo.entity.User;
import com.example.studentdemo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private UserDAO userDAO;  // 添加UserDAO注入

    @Autowired
    private JwtUtil jwtUtil;   // 添加JwtUtil注入

    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // 1. 获取所有学生（支持搜索和筛选）
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String grade,
            @RequestAttribute(value = "role", required = false) String role,
            @RequestAttribute(value = "studentNumber", required = false) String studentNumber) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<Student> students;

            // 如果是学生角色，强制按自己的学号查询
            if ("student".equals(role) && studentNumber != null) {
                students = studentDAO.searchStudents(studentNumber, null, null);
            }
            // 如果有搜索条件，调用搜索方法
            else if (keyword != null || major != null || grade != null) {
                students = studentDAO.searchStudents(keyword, major, grade);
            } else {
                students = studentDAO.getAllStudents();
            }

            response.put("success", true);
            response.put("data", students);
            response.put("message", "查询成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 2. 根据ID获取学生
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStudentById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Student student = studentDAO.getStudentById(id);
            response.put("success", true);
            response.put("data", student);
            response.put("message", "查询成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 3. 获取所有专业
    @GetMapping("/majors")
    public ResponseEntity<Map<String, Object>> getAllMajors() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> majors = studentDAO.getAllMajors();
            response.put("success", true);
            response.put("data", majors);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取专业列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 4. 获取所有年级
    @GetMapping("/grades")
    public ResponseEntity<Map<String, Object>> getAllGrades() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> grades = studentDAO.getAllGrades();
            response.put("success", true);
            response.put("data", grades);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取年级列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 5. 新增学生（同时创建用户账号）
    @PostMapping
    public ResponseEntity<Map<String, Object>> addStudent(@RequestBody Student student) {
        Map<String, Object> response = new HashMap<>();

        // 后端数据校验
        String validationError = validateStudent(student, null);
        if (validationError != null) {
            response.put("success", false);
            response.put("message", validationError);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 检查学号是否已存在
            if (studentDAO.isStudentNumberExists(student.getStudentNumber(), null)) {
                response.put("success", false);
                response.put("message", "学号已存在，请使用其他学号");
                return ResponseEntity.badRequest().body(response);
            }

            // 添加学生信息到Students表
            int result = studentDAO.addStudent(student);

            if (result > 0) {
                // 自动在Users表中创建学生登录账号
                try {
                    User newUser = new User();
                    newUser.setUsername(student.getStudentNumber()); // 使用学号作为用户名
                    newUser.setPassword(jwtUtil.encryptPassword("123456")); // 默认密码123456
                    newUser.setRealName(student.getStudentName());
                    newUser.setRole("student");
                    newUser.setStudentNumber(student.getStudentNumber());
                    newUser.setIsActive(true);

                    // 调用UserDAO中的方法保存用户
                    userDAO.addUser(newUser);

                    response.put("success", true);
                    response.put("message", "添加成功，学生可使用学号登录，初始密码为123456");
                } catch (Exception e) {
                    // 如果创建用户账号失败，记录日志但不影响学生信息的添加
                    System.err.println("创建用户账号失败: " + e.getMessage());
                    response.put("success", true);
                    response.put("message", "学生信息添加成功，但创建登录账号失败，请手动创建");
                }
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "添加失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 6. 更新学生信息
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateStudent(
            @PathVariable Integer id,
            @RequestBody Student student) {

        Map<String, Object> response = new HashMap<>();

        // 后端数据校验
        String validationError = validateStudent(student, id);
        if (validationError != null) {
            response.put("success", false);
            response.put("message", validationError);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 检查学号是否已存在（排除当前学生）
            if (studentDAO.isStudentNumberExists(student.getStudentNumber(), id)) {
                response.put("success", false);
                response.put("message", "学号已存在，请使用其他学号");
                return ResponseEntity.badRequest().body(response);
            }

            student.setStudentId(id);
            int result = studentDAO.updateStudent(student);
            if (result > 0) {
                response.put("success", true);
                response.put("message", "更新成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "更新失败，学生不存在");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 7. 删除学生（可选：同时禁用用户账号）
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteStudent(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 先获取学生信息，以便知道学号
            Student student = studentDAO.getStudentById(id);

            int result = studentDAO.deleteStudent(id);
            if (result > 0) {
                // 可选：同时禁用对应的用户账号
                if (student != null && student.getStudentNumber() != null) {
                    try {
                        userDAO.disableUserByStudentNumber(student.getStudentNumber());
                    } catch (Exception e) {
                        System.err.println("禁用用户账号失败: " + e.getMessage());
                    }
                }
                response.put("success", true);
                response.put("message", "删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "删除失败，学生不存在");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 数据校验方法
    private String validateStudent(Student student, Integer id) {
        // 必填字段检查
        if (student.getStudentName() == null || student.getStudentName().trim().isEmpty()) {
            return "姓名不能为空";
        }
        if (student.getStudentNumber() == null || student.getStudentNumber().trim().isEmpty()) {
            return "学号不能为空";
        }

        // 年龄校验（15-50岁）
        if (student.getAge() != null) {
            if (student.getAge() < 15 || student.getAge() > 50) {
                return "年龄必须在15到50岁之间";
            }
        }

        // 邮箱格式校验
        if (student.getEmail() != null && !student.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(student.getEmail()).matches()) {
                return "邮箱格式不正确（必须包含@）";
            }
        }

        // 学号长度校验
        if (student.getStudentNumber().length() > 20) {
            return "学号长度不能超过20个字符";
        }

        // 姓名长度校验
        if (student.getStudentName().length() > 50) {
            return "姓名长度不能超过50个字符";
        }

        return null; // 校验通过
    }
}