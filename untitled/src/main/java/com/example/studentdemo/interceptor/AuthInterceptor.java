package com.example.studentdemo.interceptor;

import com.example.studentdemo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求（预检请求）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 放行登录请求
        if (request.getRequestURI().contains("/api/auth/login")) {
            return true;
        }

        // 获取令牌
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"未登录或登录已过期\"}");
            return false;
        }

        // 验证令牌
        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtUtil.validateToken(token);

        if (claims == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"令牌无效或已过期\"}");
            return false;
        }

        // 将用户信息存入请求
        request.setAttribute("userId", claims.get("userId"));
        request.setAttribute("username", claims.get("username"));
        request.setAttribute("role", claims.get("role"));
        request.setAttribute("studentNumber", claims.get("studentNumber"));

        return true;
    }
}