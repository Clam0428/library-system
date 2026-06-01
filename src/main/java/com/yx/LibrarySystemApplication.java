package com.yx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 图书管理系统主程序
 */
@SpringBootApplication
@EnableScheduling
public class LibrarySystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibrarySystemApplication.class, args);
        System.out.println("========================================");
        System.out.println("图书管理系统 已启动");
        System.out.println("访问地址: http://localhost:8888");
        System.out.println("API 文档: http://localhost:8888/api/");
        System.out.println("========================================");
    }
}
