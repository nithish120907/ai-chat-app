package com.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.chatapp")
public class ChatappApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatappApplication.class, args);
    }
}