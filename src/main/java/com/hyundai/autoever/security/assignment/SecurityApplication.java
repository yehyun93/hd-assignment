package com.hyundai.autoever.security.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.hyundai.autoever.security.assignment.repository")
@EntityScan(basePackages = "com.hyundai.autoever.security.assignment.domain.entity")
public class SecurityApplication {
  public static void main(String[] args) {
    SpringApplication.run(SecurityApplication.class, args);
  }
}