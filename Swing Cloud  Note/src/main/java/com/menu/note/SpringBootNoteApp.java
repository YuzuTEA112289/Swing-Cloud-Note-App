package com.menu.note;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//Spring Boot 应用程序的启动类
@SpringBootApplication
public class SpringBootNoteApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootNoteApp.class, args);
        System.out.println("Spring Boot Note App Started! Access: http://localhost:8080");
    }
}