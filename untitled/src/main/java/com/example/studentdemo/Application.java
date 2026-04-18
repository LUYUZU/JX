package com.example.studentdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("学生信息管理系统启动成功！");
        System.out.println("访问地址: http://localhost:9000");
    }
}