package com.example.studentdemo.controller;

import com.example.studentdemo.dao.UserDAO;
import com.example.studentdemo.entity.User;
import com.example.studentdemo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*", allowCredentials = "true")  // 添加这一行
// 移除 @CrossOrigin 注解，因为已经在 WebConfig 中全局配置
public class AuthController {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private JwtUtil jwtUtil;

    // 登录接口
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> response = new HashMap<>();

        String username = loginData.get("username");
        String password = loginData.get("password");

        System.out.println("登录尝试 - 用户名: " + username);

        // 简单校验
        if (username == null || password == null) {
            response.put("success", false);
            response.put("message", "用户名和密码不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 查询用户
            User user = userDAO.findByUsername(username);

            // 验证密码
            String encryptedPassword = jwtUtil.encryptPassword(password);

            if (user == null || !encryptedPassword.equals(user.getPassword())) {
                response.put("success", false);
                response.put("message", "用户名或密码错误");
                return ResponseEntity.status(401).body(response);
            }

            // 更新最后登录时间
            userDAO.updateLastLoginTime(username);

            // 生成JWT令牌
            String token = jwtUtil.generateToken(user);

            // 返回用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("username", user.getUsername());
            userInfo.put("realName", user.getRealName());
            userInfo.put("role", user.getRole());
            userInfo.put("studentNumber", user.getStudentNumber());

            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("user", userInfo);

            System.out.println("登录成功: " + username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 验证令牌
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();

        if (token == null || !token.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "未提供有效的认证令牌");
            return ResponseEntity.status(401).body(response);
        }

        try {
            String jwtToken = token.substring(7);
            Map<String, Object> claims = jwtUtil.validateToken(jwtToken);

            if (claims == null) {
                response.put("success", false);
                response.put("message", "令牌无效或已过期");
                return ResponseEntity.status(401).body(response);
            }

            response.put("success", true);
            response.put("user", claims);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "验证失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}