package io.orkes.demo.service;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@AllArgsConstructor
@SpringBootApplication
public class RateLimitApp {
    public static void main(String[] args) {
        SpringApplication.run(RateLimitApp.class, args);
    }
}
